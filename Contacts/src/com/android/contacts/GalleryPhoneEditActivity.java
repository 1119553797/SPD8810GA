package com.android.contacts;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class GalleryPhoneEditActivity extends Activity {

	private Button btnComfirm;
	private Button btnNew;
	private Button btnCancel;
	private String TAG = "GalleryPhoneEditActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_name_phone_numbler);
		setupView();
	}

	public void setupView() {
		Intent intent = getIntent();

		final Boolean isAddMode = intent.getBooleanExtra ("mode", false);
		final String contactName = intent.getStringExtra("name");
		final String contactPhone = intent.getStringExtra("phone");
		final EditText nameTextView = (EditText) findViewById(R.id.phone_name_contact_edit);
		nameTextView.setText(contactName);
		final EditText phoneTextView = (EditText) findViewById(R.id.phone_number_contact_edit);
		phoneTextView.setText(contactPhone);

		btnCancel = (Button) findViewById(R.id.phone_contact_edit_btn_canel);
		btnNew = (Button) findViewById(R.id.phone_contact_edit_btn_new);
		btnComfirm = (Button) findViewById(R.id.phone_contact_edit_btn_ok);

		if(isAddMode){
			btnNew.setVisibility(View.VISIBLE);
			btnComfirm.setVisibility(View.GONE);
		}else{
			btnNew.setVisibility(View.GONE);
			btnComfirm.setVisibility(View.VISIBLE);
		}

		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		btnNew.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				String name = nameTextView.getText().toString().trim();
				String phone = phoneTextView.getText().toString().trim();
				Toast toast = null;
				TextView textView = null;
				if("" == "" + name){
					toast = new Toast(GalleryPhoneEditActivity.this);
					textView = new TextView(GalleryPhoneEditActivity.this);
					textView.setText(R.string.alert_name);
					textView.setBackgroundResource(R.drawable.toast_warnning);
					textView.setTextSize(25);
					textView.setTextColor(Color.BLACK);

					toast.setView(textView);
					toast.setDuration(Toast.LENGTH_LONG);
					toast.setGravity(Gravity.TOP, -60, 105);
					toast.show();
					return;
				} else if ("" == "" + phone) {
					toast = new Toast(GalleryPhoneEditActivity.this);
					textView = new TextView(GalleryPhoneEditActivity.this);
					textView.setText(R.string.alert_num);
					textView.setBackgroundResource(R.drawable.toast_warnning);
					textView.setTextSize(25);
					textView.setTextColor(Color.BLACK);

					toast.setView(textView);
					toast.setDuration(Toast.LENGTH_LONG);
					toast.setGravity(Gravity.TOP, -60, 200);
					toast.show();
					return;
				}

				ContentValues values = new ContentValues();
				//������RawContacts.CONTENT_URIִ��һ����ֵ���룬Ŀ���ǻ�ȡϵͳ���ص�rawContactId
				Uri rawContactUri = getContentResolver().insert(RawContacts.CONTENT_URI, values);
				long rawContactId = ContentUris.parseId(rawContactUri);
				
				//��data������������
				values.clear();
				values.put(Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
				values.put(StructuredName.GIVEN_NAME, name);
				getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);
				
				//��data����绰����
				values.clear();
				values.put(android.provider.ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactId);
				values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
				values.put(Phone.NUMBER, phone);
				values.put(Phone.TYPE, Phone.TYPE_MOBILE);
				getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);
				Intent i=new Intent(GalleryPhoneEditActivity.this, GalleryContactPhoneActivity.class);
				startActivity(i);
				finish();
			}
		});

		btnComfirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				String name = nameTextView.getText().toString().trim();
				String phone = phoneTextView.getText().toString().trim();
				Toast toast = null;
				TextView textView = null;
				if("" == "" + name){
					toast = new Toast(GalleryPhoneEditActivity.this);
					textView = new TextView(GalleryPhoneEditActivity.this);
					textView.setText(R.string.alert_name);
					textView.setBackgroundResource(R.drawable.toast_warnning);
					textView.setTextSize(25);
					textView.setTextColor(Color.BLACK);

					toast.setView(textView);
					toast.setDuration(Toast.LENGTH_LONG);
					toast.setGravity(Gravity.TOP, -60, 105);
					toast.show();
					return;
				} else if ("" == "" + phone) {
					toast = new Toast(GalleryPhoneEditActivity.this);
					textView = new TextView(GalleryPhoneEditActivity.this);
					textView.setText(R.string.alert_num);
					textView.setBackgroundResource(R.drawable.toast_warnning);
					textView.setTextSize(25);
					textView.setTextColor(Color.BLACK);

					toast.setView(textView);
					toast.setDuration(Toast.LENGTH_LONG);
					toast.setGravity(Gravity.TOP, -60, 200);
					toast.show();
					return;
				}

				ContentResolver contentResolver = getContentResolver();
				Cursor cursorId = null;

				/*
				 * 1.��ȡ��ϵ�ˡ�������������������������������������������������������������������
				 * 1.1.�ֻ���ȡ����������������������������������������������
				 * 1.2.����SIM���е���ϵ�ˡ���������������������������������������������
				 * 2.������ϵ�ˡ���������������������������������������������
				 */

				String _id = "";

				// 1.1.�ֻ���ȡ_id
				Uri uriRawContacts = Uri
						.parse("content://com.android.contacts/raw_contacts");
				String[] projection = new String[] { "_id" };
				String selection = " display_name = ? ";
				String[] selectionArgs = new String[] { contactName };

				/*
				 * uri:Ҫ��ѯ�������ṩ��(content provider)��URI ����������
				 * projection:Ҫ���ص�columns�б� ������������������������������
				 * selection:SQL����where�Ӿ�
				 * selectionArgs:selection�Ĳ������������?��?�Ž��ᱻ�������滻
				 * sortOrder:SQL��ORDER BY�����Ӿ�
				 */
				cursorId = contentResolver.query(uriRawContacts, projection,
						selection, selectionArgs, null);

				while (cursorId.moveToNext()) {
					// �����ֲ�ѯ��ѯID ��id�����Ƕ��
					String raw_contact_id = cursorId.getString(cursorId
							.getColumnIndex("_id"));
					Uri uriData = Uri
							.parse("content://com.android.contacts/data");
					projection = new String[] { "data1" };
					selection = " raw_contact_id = ? and mimetype = ? ";
					selectionArgs = new String[] { raw_contact_id,
							"vnd.android.cursor.item/phone_v2" };

					Cursor cursorData1 = contentResolver.query(uriData,
							projection, selection, selectionArgs, null);

					while (cursorData1.moveToNext()) {
						String data1 = cursorData1.getString(cursorData1
								.getColumnIndex("data1"));
						if (data1.equals(contactPhone)) {
							_id = raw_contact_id;
							break;
						}
					}
					cursorData1.close();
				}

				//1.2.����SIM���е���ϵ�ˣ�����ֻ��ϲ���������ϵ�ˣ�������һ���жϣ�
				int rows = -1;
				// if (_id == null || _id == "") {  //����������ִ�в���
				Uri uriSim = Uri.parse("content://icc/adn");
				ContentValues values = new ContentValues();
				//�����ϵ�˵�SIM��
				values.put("tag", contactName);
				values.put("number", contactPhone);
				values.put("newTag", nameTextView.getText().toString());
				values.put("newNumber", phoneTextView.getText().toString());
				
				//����SIM����ϵ��
				rows = getContentResolver().update(uriSim, values, null, null);
				// }
				Log.i(TAG, "update sim row count:" + rows);

				// 2.������ϵ��
				if (!(_id == null || _id == "")) {

					Uri uriData = Uri
							.parse("content://com.android.contacts/data");
					// ��������
					values = new ContentValues();
					// Ϊ�˷�ֹ���ϡ��м��������ֱ༭���������
					values.put("data3", "");// ���� Family Name
					values.put("data5", "");// �м��� Middle Name
					values.put("data2", "");// ���� First Name
					values.put("data1", name);
					// values.put("mimetype", "vnd.android.cursor.item/name");
					String where = " raw_contact_id = ? and mimetype = ? ";
					selectionArgs = new String[] { _id,
							"vnd.android.cursor.item/name" };
					rows = getContentResolver().update(uriData, values, where,
							selectionArgs);
					Log.i(TAG, "update name row count:" + rows);
					// ���º���
					values = new ContentValues();
					values.put("data1", phone);
					// values.put("mimetype",
					// "vnd.android.cursor.item/phone_v2");
					where = " raw_contact_id = ? and mimetype = ? ";
					selectionArgs = new String[] { _id,
							"vnd.android.cursor.item/phone_v2" };
					rows = getContentResolver().update(uriData, values, where,
							selectionArgs);
					Log.i(TAG, "update phone row count:" + rows);
				}

				cursorId.close();
				finish();
			}
		});

	}
}
