package com.az.TimingUpGps;


import java.util.Calendar;

import com.az.Main.MainActivity;
import com.az.PersonInfo.SettingActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;


public class SetAlarmTimeService extends Service{
	private static final String TAG = "Aizhuservice-SettingActivity";
	public Context con;
	public SQLiteDatabase db;
	public AlarmManager an;
	
	private TimeZone tz;
	private DataChange dc;
	private TimeMinChange tm;

	@Override
	public IBinder onBind(Intent intent) {
		
		return null;
	}
	/*
	1. settings ��stop service
		onDestroy�����У�����startService����Service��������
	2.settings��force stop Ӧ��
		��׽ϵͳ���й㲥��actionΪandroid.intent.action.PACKAGE_RESTARTED��
	3. ����������Ӧ��kill��running task
		����service�����ȼ�
	 */
    public int onStartCommand(Intent intent, int flags, int startId) {
            // TODO Auto-generated method stub
            //Log.v("TrafficService","startCommand");
            
            flags =  START_STICKY;//START_STICKY��service��kill�����Զ���д����
            return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {   
    	unregisterReceiver(tz);  
    	unregisterReceiver(dc);  
    	unregisterReceiver(tm);  
    	Log.i("life", "------------------SetAlarmTimeService kill ---------------");
        Intent localIntent = new Intent();
        localIntent.setClass(this, SetAlarmTimeService.class);  //����ʱ��������Service
        this.startService(localIntent);
    }

	@Override
	public void onStart(Intent intent, int startId) {
		//Log.i("life", "Startservice������");

		//first boot start SettingActivity
		new Thread(){
			public void run(){
				try{
					SharedPreferences perferences = getSharedPreferences("com.az.PersonInfo_preferences",Context.MODE_WORLD_READABLE);

					String setFlag = perferences.getString("setinfo_flag_key", "");					
					Log.i(TAG, "SettingService::onStart setFlag = " + setFlag);
					if(setFlag == null || setFlag.equals("")|| !setFlag.equals(SettingActivity.SETINFO_SUCC)){
						Intent activity=new Intent(SetAlarmTimeService.this,SettingActivity.class);
						activity.putExtra("setting_action", SettingActivity.FIRST_BOOT_ACTION_SETTING);
						activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(activity);
					}		
				}catch (Exception e){
					e.printStackTrace();
				}finally{
					Log.d(TAG, " oncreate sendEmptyMessage finish");
					}
				}
			}.start();
		
		setForeground(true);
		/*receiver=new SMSBroadCastReceiver();
		
		IntentFilter filter =new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		filter.setPriority(2147483647);
		
		registerReceiver(receiver, filter);*/
		
		Log.i("life", "------------------SetAlarmTimeService start ---------------");

		if(tz == null){
			tz=new TimeZone();
			IntentFilter tzfilter=new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
			registerReceiver(tz, tzfilter);
	    	Log.i("life", "------------------tz new ---------------");
	    	
		}
		
		if(dc == null){
			dc=new DataChange();
			IntentFilter dcfilter=new IntentFilter(Intent.ACTION_DATE_CHANGED);
			registerReceiver(dc, dcfilter);
			Log.i("life", "------------------dc new ---------------");
		}
		
		if(tm ==null){
			tm=new TimeMinChange();
			IntentFilter tmfilter=new IntentFilter(Intent.ACTION_DATE_CHANGED);
			tmfilter.addAction(Intent.ACTION_TIME_CHANGED);
			registerReceiver(tm, tmfilter);
			Log.i("life", "------------------tm new ---------------");
		}
		
		
		//����һ����ʱ��
		if(an == null){
			Calendar c=Calendar.getInstance();
			Calendar calendar=Calendar.getInstance();
			c.set(1500, calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),  calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)+2, calendar.get(Calendar.SECOND));
			Log.i("life", " "+calendar.get(Calendar.YEAR)+calendar.get(Calendar.MONTH)+calendar.get(Calendar.DAY_OF_MONTH)+calendar.get(Calendar.HOUR_OF_DAY)+calendar.get(Calendar.MINUTE)+2+calendar.get(Calendar.SECOND));
			Intent alertIntent=new Intent(this,AlarmBroadCast.class);
			PendingIntent pi=PendingIntent.getBroadcast(this, 0, alertIntent, 0);
			
			an=(AlarmManager) getSystemService(ALARM_SERVICE);
			an.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 120000,pi);//600000
			
			Log.i("life", "------------------an new ---------------");
		}	
			super.onStart(intent, startId);
	}
	//ʱ�����Ĺ㲥
	
	public class TimeZone extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())){
				//����һ����ʱ��
				Calendar c=Calendar.getInstance();
				Calendar calendar=Calendar.getInstance();
				c.set(1500, calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),  calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)+2, calendar.get(Calendar.SECOND));
				Log.i("life", " "+calendar.get(Calendar.YEAR)+calendar.get(Calendar.MONTH)+calendar.get(Calendar.DAY_OF_MONTH)+calendar.get(Calendar.HOUR_OF_DAY)+calendar.get(Calendar.MINUTE)+2+calendar.get(Calendar.SECOND));
				Intent alertIntent=new Intent(SetAlarmTimeService.this,AlarmBroadCast.class);
				PendingIntent pi=PendingIntent.getBroadcast(SetAlarmTimeService.this, 0, alertIntent, 0);
				
					an=(AlarmManager) getSystemService(ALARM_SERVICE);
					an.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 120000,pi);
				
			}
			
		}
		
	}
	//���ڸ���
	public class DataChange extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(Intent.ACTION_DATE_CHANGED.equals(intent.getAction())){
				//����һ����ʱ��
				Calendar c=Calendar.getInstance();
				Calendar calendar=Calendar.getInstance();
				c.set(1500, calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),  calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)+2, calendar.get(Calendar.SECOND));
				Log.i("life", " "+calendar.get(Calendar.YEAR)+calendar.get(Calendar.MONTH)+calendar.get(Calendar.DAY_OF_MONTH)+calendar.get(Calendar.HOUR_OF_DAY)+calendar.get(Calendar.MINUTE)+2+calendar.get(Calendar.SECOND));
				Intent alertIntent=new Intent(SetAlarmTimeService.this,AlarmBroadCast.class);
				PendingIntent pi=PendingIntent.getBroadcast(SetAlarmTimeService.this, 0, alertIntent, 0);
				
					an=(AlarmManager) getSystemService(ALARM_SERVICE);
					an.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 120000,pi);
			}
		}
		
	}
	//ʱ�����
	public class TimeMinChange extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			Calendar c=Calendar.getInstance();
			Calendar calendar=Calendar.getInstance();
			c.set(1500, calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),  calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)+2, calendar.get(Calendar.SECOND));
			Log.i("life", " "+calendar.get(Calendar.YEAR)+calendar.get(Calendar.MONTH)+calendar.get(Calendar.DAY_OF_MONTH)+calendar.get(Calendar.HOUR_OF_DAY)+calendar.get(Calendar.MINUTE)+2+calendar.get(Calendar.SECOND));
			Intent alertIntent=new Intent(SetAlarmTimeService.this,AlarmBroadCast.class);
			PendingIntent pi=PendingIntent.getBroadcast(SetAlarmTimeService.this, 0, alertIntent, 0);
			
				an=(AlarmManager) getSystemService(ALARM_SERVICE);
				an.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), 120000,pi);
			
		}
		
	}
	
}
