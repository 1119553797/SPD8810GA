/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (c) 2011-2012 Code Aurora Forum. All rights reserved.
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

package com.android.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;

import com.android.internal.telephony.Call;

import java.util.List;


/**
 * "Call card" UI element: the in-call screen contains a tiled layout of call
 * cards, each representing the state of a current "call" (ie. an active call,
 * a call on hold, or an incoming call.)
 */
public class MsmsCallCard extends CallCard {
    private static final String LOG_TAG = "MsmsCallCard";
    private static final boolean DBG = (PhoneApp.DBG_LEVEL >= 2);

    //Display subscription info for incoming call.
    private TextView mSubInfo;
    public MsmsCallCard(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DBG) log("MsmsCallCard constructor...");
        if (DBG) log("- this = " + this);
        if (DBG) log("- context " + context + ", attrs " + attrs);
    }

    protected void inflate(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(
//                R.layout.call_card_multi_sim,  // resource
                R.layout.call_card,  // resource
                this,                // root
                true);
    }

   @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (DBG) log("CallCard onFinishInflate(this = " + this + ")...");
//        mSubInfo = (TextView) findViewById(R.id.subInfo);
    }

    protected void cancelTimer(Call call) {
        Call.State state = call.getState();
        mCallTime.cancelTimer();
        if (state == Call.State.DIALING || state == Call.State.ALERTING) {
            //Display subscription info only for incoming calls.
            if (mSubInfo != null) {
                mSubInfo.setVisibility(View.GONE);
            }
        } else if (state == Call.State.INCOMING || state == Call.State.WAITING) {
            if (mSubInfo != null) {
                //Get the subscription from current call object.
                int subscription = call.getPhone().getPhoneId();
                subscription++;
                String subInfo = "SUB" + subscription;
                if (DBG) Log.v(LOG_TAG, "Setting subinfo: " + subInfo);
                mSubInfo.setText(subInfo);
                mSubInfo.setVisibility(View.VISIBLE);
            }
        } else {
            if (DBG) log(" - call.state: " + call.getState());
        }
    }

    // Accessibility event support.
    // Since none of the CallCard elements are focusable, we need to manually
    // fill in the AccessibilityEvent here (so that the name / number / etc will
    // get pronounced by a screen reader, for example.)
    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        super.dispatchPopulateAccessibilityEvent(event);
        if (mSubInfo != null) {
            dispatchPopulateAccessibilityEvent(event, mSubInfo);
        }
        return true;
    }

    private void dispatchPopulateAccessibilityEvent(AccessibilityEvent event, View view) {
        List<CharSequence> eventText = event.getText();
        int size = eventText.size();
        view.dispatchPopulateAccessibilityEvent(event);
        // if no text added write null to keep relative position
        if (size == eventText.size()) {
            eventText.add(null);
        }
    }

   // Debugging / testing code
    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
