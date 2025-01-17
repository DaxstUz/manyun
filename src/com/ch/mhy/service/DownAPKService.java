package com.ch.mhy.service;

import java.io.File;  
import java.text.DecimalFormat;  
  




import android.app.NotificationManager;  
import android.app.PendingIntent;  
import android.app.Service;  
import android.content.Context;  
import android.content.Intent;  
import android.content.pm.ApplicationInfo;  
import android.content.pm.PackageManager;  
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;  
import android.os.Environment;  
import android.os.IBinder;  
import android.os.Vibrator;  
import android.support.v4.app.NotificationCompat;  
import android.widget.Toast;  
  




import com.ch.comm.saevent.MobSaAgent;
import com.ch.mhy.R;
import com.lidroid.xutils.HttpUtils;  
import com.lidroid.xutils.exception.HttpException;  
import com.lidroid.xutils.http.HttpHandler;  
import com.lidroid.xutils.http.ResponseInfo;  
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.umeng.analytics.MobclickAgent;

public class DownAPKService extends Service {  
	  
    private final int NotificationID = 0x10000;  
    private NotificationManager mNotificationManager = null;  
    private NotificationCompat.Builder builder;  
  
    private HttpHandler<File> mDownLoadHelper;  
  
    // 文件下载路径  
    private String APK_url = "";  
    // 文件保存路径(如果有SD卡就保存SD卡,如果没有SD卡就保存到手机包名下的路径)  
    private String APK_dir = "";  
  
    /** 
     * Title: onBind 
     *  
     * @Description: 
     * @param intent 
     * @return 
     * @see android.app.Service#onBind(android.content.Intent) 
     */  
    @Override  
    public IBinder onBind(Intent intent) {  
        return null;  
    }  
  
    /** 
     * Title: onCreate 
     *  
     * @Description: 
     * @see android.app.Service#onCreate() 
     */  
    @Override  
    public void onCreate() {  
        super.onCreate();  
        initAPKDir();// 创建保存路径  
    }  
  
    /** 
     * Title: onStartCommand 
     *  
     * @Description: 
     * @param intent 
     * @param flags 
     * @param startId 
     * @return 
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int) 
     */  
    @Override  
    public int onStartCommand(Intent intent, int flags, int startId) {  
        System.out.println("onStartCommand");  
        // 接收Intent传来的参数:  
        if(intent!=null){
        	
       
        APK_url = intent.getStringExtra("apk_url");  
  
        MobclickAgent.onEvent(this,"down_start");
        try {
			MobSaAgent.onEvent(this,"down_start");
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        if(APK_url!=null){
        	DownFile(APK_url, APK_dir + File.separator + System.currentTimeMillis()+".apk");  
        }
        
        }
  
        return super.onStartCommand(intent, flags, startId);  
    }  
  
    private void initAPKDir() {  
        /** 
         * 创建路径的时候一定要用[/],不能使用[\],但是创建文件夹加文件的时候可以使用[\]. 
         * [/]符号是Linux系统路径分隔符,而[\]是windows系统路径分隔符 Android内核是Linux. 
         */  
        if (isHasSdcard())// 判断是否插入SD卡  
            APK_dir = getApplicationContext().getFilesDir().getAbsolutePath() + "/apk/download/";// 保存到app的包名路径下  
        else  
            APK_dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/apk/download/";// 保存到SD卡路径下  
        File destDir = new File(APK_dir);  
        if (!destDir.exists()) {// 判断文件夹是否存在  
            destDir.mkdirs();  
        }  
    }  
  
    /** 
     *  
     * @Description:判断是否插入SD卡 
     */  
    private boolean isHasSdcard() {  
        String status = Environment.getExternalStorageDirectory().getAbsolutePath();  
        if (status.equals(Environment.MEDIA_MOUNTED)) {  
            return true;  
        } else {  
            return false;  
        }  
    }  
  
    private void DownFile(String file_url, String target_name) {  
        mDownLoadHelper = new HttpUtils().download(file_url, target_name, true, true, new RequestCallBack<File>() {  
  
            @Override  
            public void onStart() {  
                super.onStart();  
                  
                mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);  
                builder = new NotificationCompat.Builder(getApplicationContext());  
                builder.setSmallIcon(R.drawable.ic_launcher);  
                builder.setTicker("正在下载apk");  
                builder.setContentTitle(getApplicationName());  
                builder.setContentText("正在下载,请稍后...");  
                builder.setNumber(0);  
                builder.setAutoCancel(true);  
                mNotificationManager.notify(NotificationID, builder.build());  
  
            }  
  
            @Override  
            public void onLoading(long total, long current, boolean isUploading) {  
                super.onLoading(total, current, isUploading);  
                  
                int x = Long.valueOf(current).intValue();  
                int totalS = Long.valueOf(total).intValue();  
                builder.setProgress(totalS, x, false);  
                builder.setContentInfo(getPercent(x, totalS));  
                mNotificationManager.notify(NotificationID, builder.build());  
            }  
  
            @Override  
            public void onSuccess(ResponseInfo<File> responseInfo) {  
                  
                Intent installIntent = new Intent(Intent.ACTION_VIEW);  
//                System.out.println(responseInfo.result.getPath());  
                Uri uri = Uri.fromFile(new File(responseInfo.result.getPath()));  
                installIntent.setDataAndType(uri, "application/vnd.android.package-archive");  
                installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
                PendingIntent mPendingIntent = PendingIntent.getActivity(DownAPKService.this, 0, installIntent, 0);  
                builder.setContentText("下载完成,请点击安装");  
                builder.setContentIntent(mPendingIntent);  
                mNotificationManager.notify(NotificationID, builder.build());  
                // 震动提示  
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);  
                vibrator.vibrate(1000L);// 参数是震动时间(long类型)  
                stopSelf();  
                startActivity(installIntent);// 下载完成之后自动弹出安装界面 
                MobclickAgent.onEvent(DownAPKService.this,"down_over");
                try {
					MobSaAgent.onEvent(DownAPKService.this,"down_over");
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                mNotificationManager.cancel(NotificationID);  
            }  
  
            @Override  
            public void onFailure(HttpException error, String msg) {  
                  
                mNotificationManager.cancel(NotificationID);  
                Toast.makeText(getApplicationContext(), "下载失败，请检查网络！", Toast.LENGTH_SHORT).show();  
            }  
  
            @Override  
            public void onCancelled() {  
                super.onCancelled();  
                  
                mDownLoadHelper.cancel();  
            }  
  
        });  
    }  
  
    /** 
     *  
     * @param x 
     *            当前值 
     * @param total 
     *            总值 
     * @return 当前百分比 
     * @Description:返回百分之值 
     */  
    private String getPercent(int x, int total) {  
        String result = "";// 接受百分比的值  
        double x_double = x * 1.0;  
        double tempresult = x_double / total;  
        // 百分比格式，后面不足2位的用0补齐 ##.00%  
        DecimalFormat df1 = new DecimalFormat("0.00%");  
        result = df1.format(tempresult);  
        return result;  
    }  
  
    /** 
     * @return 
     * @Description:获取当前应用的名称 
     */  
    private String getApplicationName() {  
        PackageManager packageManager = null;  
        ApplicationInfo applicationInfo = null;  
        try {  
            packageManager = getApplicationContext().getPackageManager();  
            applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);  
        } catch (PackageManager.NameNotFoundException e) {  
            applicationInfo = null;  
        }  
        String applicationName = (String) packageManager.getApplicationLabel(applicationInfo);  
        return applicationName;  
    }  
  
    /** 
     * Title: onDestroy 
     *  
     * @Description: 
     * @see android.app.Service#onDestroy() 
     */  
    @Override  
    public void onDestroy() {  
        super.onDestroy();  
        stopSelf();  
    }  
  
}  