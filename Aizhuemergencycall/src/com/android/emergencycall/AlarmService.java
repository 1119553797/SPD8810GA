package com.android.emergencycall;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.az.Location.ACellInfo;
import com.az.Location.AGps;
import com.az.Location.AUtilTool;
import com.az.SmsGetLocation.NetInterface;

public class AlarmService extends Service {

	// static boolean doLocationFlish = true;
	//cathon xiong add
	private Location location = null;
	private LocationManager locationManager = null;
	//private Context context = null;
	ArrayList<ACellInfo> cellIds = null;
	private AGps gps=null;
	private final static String TAG= "cathon gpslog";
	private Double longti;
	private Double langti;
	private String GpsAddress;
	private String Longitude;//����
	private String Latitude;//γ��
	private String phoneAddress;
	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		//onLocation();
		Log.i(TAG, "emgenci call begin");
		
		GetgpsbymulcellIds();
		return START_STICKY;
	}

	// @Override
	// public void onStart(Intent intent, int startId) {
	// onLocation();
	// super.onStart(intent, startId);
	// }

	private void GetgpsbymulcellIds()
	{
		
		//locationManager=(LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		gps=new AGps(this);
		cellIds=AUtilTool.init(this);
		
		Log.i(TAG, "GetgpsbymulcellIds begin");
		
		Location location=gps.getLocation();
		if(location==null){
			//2.���ݻ�վ��Ϣ��ȡ��γ��
			try {
				location = AUtilTool.callGear(this, cellIds);
				
				longti = location.getLongitude();	//location.getLatitude().toString();
				langti = location.getLatitude();
				
				String Longitude = longti.toString();
				String Latitude =  langti.toString();
				
				GpsAddress =AUtilTool.getaddfromgoogle ;

				String LoginURIString = "http://210.51.7.193/io/PersonLocation.aspx";
				  TelephonyManager telmgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				  String imei = "IMEI:" + telmgr.getDeviceId();

				  List <NameValuePair> InfoParamss = new ArrayList <NameValuePair>(); //Post�������ͱ���������NameValuePair[]���鴢��
				  InfoParamss.add(new BasicNameValuePair("longitude", Longitude));
				  InfoParamss.add(new BasicNameValuePair("latitude", Latitude));
				  InfoParamss.add(new BasicNameValuePair("imei_key", imei));
				  InfoParamss.add(new BasicNameValuePair("Address", GpsAddress));
				  InfoParamss.add(new BasicNameValuePair("IsEmergency", "1"));
				  
				  new NetInterface().SendInfoToNet(LoginURIString,InfoParamss);
				  				
				//Log.i(TAG, "agps location "+ Longitude + " "+ Latitude + " " +GpsAddress);
			} catch (Exception e) {
				location=null;
				e.printStackTrace();
			}
			if(location==null){
				Log.i(TAG, "cell location null"); 
			}
		}		
	}
	
	
	
}
