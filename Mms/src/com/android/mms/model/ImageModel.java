/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.model;

import com.android.mms.ContentRestrictionException;
import com.android.mms.ExceedMessageSizeException;
import com.android.mms.LogTag;
import com.android.mms.MmsConfig;
import com.android.mms.dom.smil.SmilMediaElementImpl;
import android.drm.mobile1.DrmException;
import com.android.mms.drm.DrmWrapper;
import com.android.mms.ui.UriImage;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.PduPersister;

import org.w3c.dom.events.Event;
import org.w3c.dom.smil.ElementTime;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Config;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;


public class ImageModel extends RegionMediaModel {
    private static final String TAG = "Mms/image";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;

    private static final int THUMBNAIL_BOUNDS_LIMIT = 480;

    private int mWidth;
    private int mHeight;
    private int mOrientation = 0;

    private SoftReference<Bitmap> mBitmapCache = new SoftReference<Bitmap>(null);

    public ImageModel(Context context, Uri uri, RegionModel region)
            throws MmsException {
        super(context, SmilHelper.ELEMENT_TAG_IMAGE, uri, region);
        initModelFromUri(uri);
        checkContentRestriction();
    }

    public ImageModel(Context context, String contentType, String src,
            Uri uri, RegionModel region) throws DrmException, MmsException {
        super(context, SmilHelper.ELEMENT_TAG_IMAGE,
                contentType, src, uri, region);
        decodeImageBounds();
    }

    public ImageModel(Context context, String contentType, String src,
            DrmWrapper wrapper, RegionModel regionModel) throws IOException {
        super(context, SmilHelper.ELEMENT_TAG_IMAGE, contentType, src,
                wrapper, regionModel);
    }

    private void initModelFromUri(Uri uri) throws MmsException {
        UriImage uriImage = new UriImage(mContext, uri);

        mContentType = uriImage.getContentType();
        if (TextUtils.isEmpty(mContentType)) {
            throw new MmsException("Type of media is unknown.");
        }
        mSrc = uriImage.getSrc();
        mWidth = uriImage.getWidth();
        mHeight = uriImage.getHeight();
        mOrientation = uriImage.getOrientation();
        if (LOCAL_LOGV) {
            Log.v(TAG, "New ImageModel created:"
                    + " mSrc=" + mSrc
                    + " mContentType=" + mContentType
                    + " mUri=" + uri);
            Log.v(TAG, "In ImageModel: width=" + mWidth + ";height=" + mHeight + ";Orientation="
                    + mOrientation);
        }
    }

    private void decodeImageBounds() throws DrmException {
        UriImage uriImage = new UriImage(mContext, getUriWithDrmCheck());
        mWidth = uriImage.getWidth();
        mHeight = uriImage.getHeight();
        mOrientation = uriImage.getOrientation();

        if (LOCAL_LOGV) {
            Log.v(TAG, "Image bounds: " + mWidth + "x" + mHeight);
            Log.d(TAG, "Image orientation:"+mOrientation);
        }
    }

    // EventListener Interface
    public void handleEvent(Event evt) {
        if (evt.getType().equals(SmilMediaElementImpl.SMIL_MEDIA_START_EVENT)) {
            mVisible = true;
        } else if (mFill != ElementTime.FILL_FREEZE) {
            mVisible = false;
        }

        notifyModelChanged(false);
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    protected void checkContentRestriction() throws ContentRestrictionException {
        ContentRestriction cr = ContentRestrictionFactory.getContentRestriction();
        cr.checkImageContentType(mContentType);
    }

    public Bitmap getBitmap() {
        return internalGetBitmap(getUri());
    }

    public Bitmap getBitmapWithDrmCheck() throws DrmException {
        return internalGetBitmap(getUriWithDrmCheck());
    }

    private Bitmap internalGetBitmap(Uri uri) {
        Bitmap bm = mBitmapCache.get();
        if (bm == null) {
            try {
                bm = createThumbnailBitmap(THUMBNAIL_BOUNDS_LIMIT, uri);
                if (bm != null) {
                    mBitmapCache = new SoftReference<Bitmap>(bm);
                }
            } catch (OutOfMemoryError ex) {
                // fall through and return a null bitmap. The callers can handle a null
                // result and show R.drawable.ic_missing_thumbnail_picture
            }
        }
        return bm;
    }
    
    /* fixed CR<NEWMS00138991> by luning at 2011.11.10*/
    public Bitmap getBitmapCache(){
    	return mBitmapCache.get();
    }
    

    private Bitmap createThumbnailBitmap(int thumbnailBoundsLimit, Uri uri) {
        int outWidth = mWidth;
        int outHeight = mHeight;

        int s = 1;
        while ((outWidth / s > thumbnailBoundsLimit)
                || (outHeight / s > thumbnailBoundsLimit)) {
            s *= 2;
        }
        if (Log.isLoggable(LogTag.APP, Log.VERBOSE)) {
            Log.v(TAG, "createThumbnailBitmap: scale=" + s + ", w=" + outWidth / s
                    + ", h=" + outHeight / s);
        }

        InputStream input = null;
        // ===== fixed CR<NEWMS00121388> by luning at 11-09-15 begin=====
        Bitmap bitmap = null;
        // ===== fixed CR<NEWMS00121388> by luning at 11-09-15 end=====
        try {
            input = mContext.getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            bitmap = BitmapFactory.decodeStream(input, null, options);

            options.inJustDecodeBounds = false;
            options.inSampleSize = computeSampleSize(options, -1, thumbnailBoundsLimit * thumbnailBoundsLimit);
            input = mContext.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(input, null, options);

            if (bitmap != null && mOrientation != 0) {
                bitmap = rotate(bitmap, mOrientation);
                Log.d(TAG, "In createThumbnailBitmap rotate bmp :" + mOrientation);
            }

            return bitmap;
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        } catch (OutOfMemoryError ex) {
        	// ===== fixed CR<NEWMS00121388> by luning at 11-09-15 begin=====
        	if(null != bitmap){
        		bitmap.recycle();
        		bitmap = null;
        	}
        	// ===== fixed CR<NEWMS00121388> by luning at 11-09-15 end=====
//            MessageUtils.writeHprofDataToFile();
            throw ex;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    }

    public static int computeSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength,
                maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options,
            int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 :
                (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 :
                (int) Math.min(Math.floor(w / minSideLength),
                Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) &&
                (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    @Override
    public boolean getMediaResizable() {
        return true;
    }

    @Override
    protected void resizeMedia(int byteLimit, long messageId) throws MmsException {
        UriImage image = new UriImage(mContext, getUri());
        if (image == null) {
            throw new ExceedMessageSizeException("No room to resize picture: " + getUri());
        }
        PduPart part = image.getResizedImageAsPart(
                MmsConfig.getMaxImageWidth(),
                MmsConfig.getMaxImageHeight(),
                byteLimit);

        if (part == null) {
            throw new ExceedMessageSizeException("Not enough memory to turn image into part: " +
                    getUri());
        }

        String src = getSrc();
        byte[] srcBytes = src.getBytes();
        part.setContentLocation(srcBytes);
        int period = src.lastIndexOf(".");
        byte[] contentId = period != -1 ? src.substring(0, period).getBytes() : srcBytes;
        part.setContentId(contentId);

        PduPersister persister = PduPersister.getPduPersister(mContext);
        this.mSize = part.getData().length;
        Uri newUri = persister.persistPart(part, messageId);
        setUri(newUri);
    }

    private Bitmap rotate(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                // We have no memory to rotate. Return the original bitmap.
            }
        }
        return b;
    }
}
