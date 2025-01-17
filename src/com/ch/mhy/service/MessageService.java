package com.ch.mhy.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ch.mhy.MainActivity;
import com.ch.mhy.R;
import com.ch.mhy.activity.msg.UpdateComicActivity;
import com.ch.mhy.application.MhyApplication;
import com.ch.mhy.db.DBManager;
import com.ch.mhy.db.DBUtil;
import com.ch.mhy.entity.ComicsDetail;
import com.ch.mhy.entity.Down;
import com.ch.mhy.net.NetReceiver;
import com.ch.mhy.net.NetReceiver.NetState;
import com.ch.mhy.util.NetUtil;
import com.ch.mhy.util.TelPhoneInfo;
import com.ch.mhy.util.UrlConstant;

/**
 * 消息推送服务及广播监听
 * @author xc.li
 * @date 2015年7月28日
 */
public class MessageService extends Service{

	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

	/**
	 * 当服务创建时，注册广播
	 */
	@Override
	public void onCreate() {
		super.onCreate();
	}

	/**
	 * 当服务销毁时，同时取消广播接收者,销毁接收对象
	 */
	@Override
	public void onDestroy() {
		Intent localIntent = new Intent();
		localIntent.setClass(this, MessageService.class); //销毁时重新启动Service
		this.startService(localIntent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//启动一个定时任务，检测更新服务
		TimerTask task = new TimerTask(){
			@Override
			public void run() {
				getUpdateCominData();
			}
        };
        Timer timer = new Timer();
        long timeMi = 6*60*60*1000; //6小时执行一次
        timer.schedule(task, 0, timeMi);
        flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 通知栏信息
	 */
	private void notification(String title, String msg, String type, String cnt, long param){
		Notification note = new Notification(R.drawable.nlog, title, System.currentTimeMillis());
		note.flags = Notification.FLAG_AUTO_CANCEL;//点击通知栏后，通知自动关闭
		note.defaults = Notification.DEFAULT_SOUND;
		Class<?> clz = getTopActivity(this, type);
		Intent noteIntent = new Intent(this, clz);//点击后跳转到当前打开界面界面
		noteIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		noteIntent.putExtra("fromNotification", type);//传参
		noteIntent.putExtra("updateCnt", cnt);//传参
		noteIntent.putExtra("lastUpdateTime", param);//传参
		
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, noteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		note.setLatestEventInfo(this, title, msg, pendingIntent);
		
		NotificationManager nm=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.notify(1000,note);
	}

	/**
	 * 判断漫云APP是否运行在后台
	 * @return boolean
	 */
	public Class<?> getTopActivity(Context context, String type){
		Class<?> clz = MainActivity.class;
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = activityManager.getRunningTasks(1);
		if(tasks!=null && tasks.size()>0){
			RunningTaskInfo task = tasks.get(0);
			ComponentName cname = task.topActivity;
			String pkgName = cname.getPackageName();
			String clzName = cname.getClassName();
			Log.e("clzName", "clzName="+clzName);
			if(!TextUtils.isEmpty(pkgName) && "com.ch.mhy".equals(pkgName) && !"MainActivity".equals(clzName)){
				if("1".equals(type)){
					clz = UpdateComicActivity.class; //cname.getClass();
				}else{
					clz = cname.getClass();
				}
			}
		}
		return clz;
	}
	
	private void getUseData(){
		SharedPreferences spf = MessageService.this.getSharedPreferences("appUseDate", Context.MODE_PRIVATE);
		long currDate = System.currentTimeMillis();
		long appUseDate = spf.getLong("appUseDate", currDate);
		long milliseconds = currDate - appUseDate;
		long days = milliseconds / 86400000L;
		if(currDate == appUseDate || days>1){//离上次提醒大于1天时，则提醒
			NetState state = NetReceiver.isConnected(this);
			if(!NetState.NET_NO.equals(state)){//有网情况下
				JSONObject param = new JSONObject();
				JSONObject obj = new JSONObject();
				try {
					obj.put("mechineId", TelPhoneInfo.getDeviceId());
					obj.put("mechineType", TelPhoneInfo.getPhoneModel());
					param.put("pageSize", 1);
					param.put("currentPage", 1);
					param.put("orderBy", "");
					param.put("object", obj);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				String url = UrlConstant.UrlpushUserUseMessageNotLogin;
				JsonObjectRequest joObjectRequest = new JsonObjectRequest(Method.POST, url, param,
					new Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							try {
								JSONObject joObject = response.getJSONObject("object").getJSONObject("object");
								if(joObject != null){
									int state = joObject.getInt("state");
									if(state==1){
										String msg = joObject.getString("message");
										notification("漫云系统提醒", msg, "0", "", 0);
										//记录本次通知提醒时间
										SharedPreferences spf = MessageService.this.getSharedPreferences("appUseDate", Context.MODE_PRIVATE);
										Editor editor = spf.edit();//获取编辑器  
										editor.putLong("appUseDate", System.currentTimeMillis());  
										editor.commit();//提交修改
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							} 
						}
					}, new ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError error) {
						}
					}) {
					@Override
					public Map<String, String> getHeaders() throws AuthFailureError {
						Map<String, String> map = new HashMap<String, String>();
						map.put("Content-Type", "application/json; charset=utf-8");
						return map;
					}
				};
	
				joObjectRequest.setRetryPolicy(new DefaultRetryPolicy(3000,
					DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
	
				NetUtil.rqueue.add(joObjectRequest);
			}
		}
	}
	
	private String getMids(){
		List<Integer> ids = new ArrayList<Integer>();
		DBManager manager = new DBManager(MhyApplication.getApplication(), DBUtil.CollectName, null, DBUtil.Code);
		List<ComicsDetail> list=new ArrayList<ComicsDetail>();
		list = manager.query();
		for(int i=0; i<list.size(); i++){
			ComicsDetail item = list.get(i);
			ids.add(item.mId);
		}
		manager.closeDB();
		
		/*manager = new DBManager(this, DBUtil.ReadName, null, DBUtil.Code);
		list=manager.query();
		for(int i=0; i<list.size(); i++){
			ComicsDetail item = list.get(i);
			ids.add(item.mId);
		}
		manager.closeDB();*/
		
		manager = new DBManager(this, DBUtil.ReadName, null, DBUtil.Code);
		List<Down> dlist=manager.queryDown("select * from down GROUP BY mId ",null);
		for(int i=0; i<dlist.size(); i++){
			Down item = dlist.get(i);
			ids.add(item.getCd().mId);
		}
		manager.closeDB();
		String idstr = Arrays.deepToString(ids.toArray());
		return idstr.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", "");
	}
	
	
	
	/**
	 * 获取有更新漫画列表
	 */
	private void getUpdateCominData(){
		NetState state = NetReceiver.isConnected(this);
		String mids = getMids();
		
		/*SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		try {
			final Date strtodate = formatter.parse("2015-07-29");*/
		
		
		if(mids!=null && !"".equals(mids) && !NetState.NET_NO.equals(state)){//有网情况下
			JSONObject param = new JSONObject();
			JSONObject obj = new JSONObject();
			SharedPreferences spf = MessageService.this.getSharedPreferences("comicUpdateDate", Context.MODE_PRIVATE);
			long lastUpdateTime = spf.getLong("lastUpdateDate", System.currentTimeMillis());
			try {
				obj.put("mids", mids);
				obj.put("lastUpdateTime", lastUpdateTime);
				param.put("pageSize", 10);
				param.put("currentPage", 1);
				param.put("orderBy", "");
				param.put("object", obj);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			String url = UrlConstant.UrlpushUserNewComicChapterMessage;
			JsonObjectRequest joObjectRequest = new JsonObjectRequest(Method.POST, url, param,
				new Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							JSONObject joObject = response.getJSONObject("object").getJSONObject("object");
							if(joObject != null){
								int state = joObject.getInt("state");
								Log.e("state", "查询漫画更新返回state="+state);
								if(state==1){//根据返回结果，有漫画更新则提示漫画更新
									SharedPreferences spf = MessageService.this.getSharedPreferences("comicUpdateDate", Context.MODE_PRIVATE);
									String count = joObject.getString("count");
									String subName = joObject.getString("subName");
									JSONObject serverTime = joObject.getJSONObject("serverTime");
									String msg = "亲，您有 "+subName+" 等"+count+"本漫画有更新哦，快点我去看看吧";
									long lastUpdateDate = spf.getLong("lastUpdateDate", spf.getLong("lastUpdateDate", System.currentTimeMillis()));
									notification("漫画更新提醒", msg, "1", count, lastUpdateDate);
									
									//记录本次通知提醒时间
									Editor editor = spf.edit();//获取编辑器  
									editor.putLong("lastUpdateDate", serverTime.getLong("time"));  //最新消息时间
									editor.commit();//提交修改
								}else{//没有漫画更新则检测长时间未登录
									getUseData();
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						} 
					}
				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
					}
				}) {
				@Override
				public Map<String, String> getHeaders() throws AuthFailureError {
					Map<String, String> map = new HashMap<String, String>();
					map.put("Content-Type", "application/json; charset=utf-8");
					return map;
				}
			};

			joObjectRequest.setRetryPolicy(new DefaultRetryPolicy(3000,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

			NetUtil.rqueue.add(joObjectRequest);
		}else if(!NetState.NET_NO.equals(state)){
			getUseData();
		}
		
		/*} catch (ParseException e1) {
			e1.printStackTrace();
		}*/
	}	
}
