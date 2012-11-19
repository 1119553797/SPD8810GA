package com.az.SmsGetLocation;


import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;

import org.apache.http.message.BasicNameValuePair;

import com.az.Location.CellInfoManager;
import com.az.Location.CellLocationManager;
import com.az.Location.WifiInfoManager;
import com.az.Location.WifiPowerManager;
import com.az.Main.R;

import android.app.Service;
//import android.app.IntentService; 

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import java.lang.Thread;
import android.util.Log;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import com.az.Location.*;
public class SMSService extends Service{

	private String GpsAddress;
	private String Longitude;//����
	private String Latitude;//γ��
	private String phoneAddress;
	//public static boolean SmsEvenFish = false;
	public SQLiteDatabase db;
	private boolean threadDisable=false; 
	//cathon xiong add
	private Location location = null;
	private LocationManager locationManager = null;
	//private Context context = null;
	ArrayList<ACellInfo> cellIds = null;
	private AGps gps=null;
	private final static String TAG= "cathon gpslog";
	private Double longti;
	private Double langti;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		phoneAddress = intent.getStringExtra("phoneAddress");
		//if(CheckPhoneNumvAlidity()){
		//GetLocation();	//this is cellid+ wifi position cathon xiong
		GetgpsbymulcellIds();
		//}
		super.onStart(intent, startId);
	}
	
	/*
	public SMSService() {  
	       super("SMSService");  
	}  
	*/
	
	/*
	protected void onHandleIntent(Intent intent) {  

		phoneAddress = intent.getStringExtra("phoneAddress");
		if(CheckPhoneNumvAlidity()){
			GetLocation();
			SmsEvenFish=false;
			while(SmsEvenFish==false){
				 try {   
					Thread.sleep(5 * 1000);   
				} catch (InterruptedException e) {   
					 e.printStackTrace();   
				}   
			}
		}
	}  
	*/
	
	/*
	private void MoveSMSInfo()
	{
		//���������ĺ��뽫ʱ���������ݴ�����ʾ������ط�  --������13528750390 ʵ���÷��������ṩ
		SharedPreferences prs= PreferenceManager.getDefaultSharedPreferences(this);
		String textphone=prs.getString("text_input_phone_pr", "");
		if(msg.getDisplayOriginatingAddress().equals(textphone)||msg.getDisplayOriginatingAddress().equals("+86"+textphone)){
			SQLiteDatabase db=this.openOrCreateDatabase("urgency_contact.db", MODE_PRIVATE, null);

			db.execSQL("create table if not exists contact(" +
								"_id integer primary key autoincrement,"+
								"phone text not null,"+
									"body text not null"+")");

			ContentValues values=new ContentValues();
			values.put("phone", ""+msg.getDisplayOriginatingAddress());
			values.put("body", ""+msg.getDisplayMessageBody());
			db.insert("contact", null, values);
			db.close();
		}
	}
	*/
	
	/*
	private boolean CheckPhoneNumvAlidity()
	{
		SQLiteDatabase sqldb=this.openOrCreateDatabase("paddy_database", MODE_PRIVATE, null);
		Cursor cur = sqldb.query("database01",new String[]{"nameId","name"},"nameId='phoneId'",null,null,null,null);	 
		ArrayList<String> ar=new ArrayList<String>();
		//�Ƿ��ǽ�����ϵ�����ݿⱣ��ĵ绰
		 boolean availability = false;
		 //lianxiren
		 //SharedPreferences pr=PreferenceManager.getDefaultSharedPreferences(this);
		 //String contactphone=pr.getString("emergencyContact_key", "");
		 //if(contactphone.contains(phoneAddress)||("+86"+contactphone).contains(phoneAddress)){
			 //availability=true;
		 //}
		 //���ݿ��е�
		while(cur.moveToNext()){
			ar.add(cur.getString(cur.getColumnIndex("name")));
			//Log.i("life",cur.getString(cur.getColumnIndex("name")));
			String s=cur.getString(cur.getColumnIndex("name"));
			if(s.contains(phoneAddress)||("+86"+s).contains(phoneAddress)){
				availability=true;
			}
		}
		cur.close();
		return availability;
	}
	*/
	
	private void SendSmsToCall() {
		//final String phoneAddress=msg.getDisplayOriginatingAddress();
		Log.i(TAG, " Send agps Sms ToCall ");
		android.telephony.SmsManager manager=android.telephony.SmsManager.getDefault();
		GpsAddress = (GpsAddress == null || GpsAddress == "null") ? GpsAddress
				+ this.getString(R.string.gpsmmserrorindc):GpsAddress;
		ArrayList<String> parts = manager.divideMessage("GPS:"+Longitude+","+Latitude+"."+GpsAddress);
		manager.sendMultipartTextMessage(phoneAddress, null,parts , null, null);
	}
	
	private void SendInfoToNet() {
		  String LoginURIString = getString(R.string.PersonLocation);//"http://61.143.124.173:8080/io/PersonLocation.aspx";
		  TelephonyManager telmgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		  String imei = "IMEI:" + telmgr.getDeviceId();	               
		  List <NameValuePair> InfoParamss = new ArrayList <NameValuePair>(); //Post�������ͱ���������NameValuePair[]���鴢��
		  InfoParamss.add(new BasicNameValuePair("longitude", Longitude)); 
		  InfoParamss.add(new BasicNameValuePair("latitude", Latitude));
		  InfoParamss.add(new BasicNameValuePair("imei_key", imei));
		  InfoParamss.add(new BasicNameValuePair("Address", GpsAddress));
		  new NetInterface().SendInfoToNet(LoginURIString,InfoParamss);
	}
	
	private void GetLocation() {
		CellInfoManager cellManager = new CellInfoManager(this);
		WifiInfoManager wifiManager = new WifiInfoManager(this);
		CellLocationManager locationManager = new CellLocationManager(this, cellManager, wifiManager) {
			@Override
			public void onLocationChanged() {
			  Longitude=String.valueOf(this.longitude());//����
			  Latitude=String.valueOf(this.latitude());//γ��
			  GpsAddress=this.address();
			  this.stop();

			  //SendSmsToCall();
			  //SendInfoToNet();
			  //SmsEvenFish = true;
			  //stopSelf();
			}
		};
		locationManager.start();
	}
	
	private void GetgpsbymulcellIds()
	{
		
		//locationManager=(LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		gps=new AGps(SMSService.this);
		cellIds=AUtilTool.init(SMSService.this);
		
		Log.i(TAG, "GetgpsbymulcellIds begin");

		new Thread(new Runnable(){
			@Override
			public void run() {

				
		Location location=gps.getLocation();
		if(location==null){
			//2.���ݻ�վ��Ϣ��ȡ��γ��
			try {
				
				location = AUtilTool.callGear(SMSService.this, cellIds);	
				if(location==null)
				{
					Log.i(TAG, "gpsmms address null ");
				}
				else
				{
					longti = location.getLongitude();
					langti = location.getLatitude();
					Longitude = longti.toString();
					Latitude =  langti.toString();
					GpsAddress =AUtilTool.getaddfromgoogle ;
					SendSmsToCall();				
					SendInfoToNet();
					Log.i(TAG, "agps location "+ Longitude + " "+ Latitude + " " +GpsAddress);
				}

			} catch (Exception e) {
				location=null;
				e.printStackTrace();
			}
			if(location==null){
				Log.i(TAG, "cell location null"); 
			}
		}		
		

		
	}
	}).start();


	}
	
}
