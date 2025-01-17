package com.ch.mhy.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ch.mhy.R;
import com.ch.mhy.activity.book.UpdateMsg;
import com.ch.mhy.application.MhyApplication;
import com.ch.mhy.download.FileDownloader;
import com.ch.mhy.download.ImageDownProgress;
import com.ch.mhy.entity.Down;
import com.ch.mhy.fragment.ReadedFragment;
import com.ch.mhy.interf.UpdateCollectInfo;
import com.ch.mhy.interf.UpdateEditFlag;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;


/**
 * 帮助类
 *
 * @author DaxstUz
 * @创建时间 2015年3月2日 上午10:29:35
 * 博客  http://blog.csdn.net/u010931818
 */
public class Utils { 
	
	public static String split="@@";

    public static final int PageSize = 10;
    
    public static List<Activity> acts = new ArrayList<Activity>();
    
    public static  FileDownloader loader;
    
    public static  boolean isdowning=false;
    
    public static  boolean isdowned=false;
    
    /**
     * 更新我的界面上的阅读、收藏信息
     */
    public static  UpdateCollectInfo updateCollectInfo;
    
    public static  UpdateEditFlag updateEditFlag;
    
    public static  ReadedFragment ur;
    
    public static  UpdateMsg um;
    
    //用来传看的漫画信息到Reading页面
    public static Integer rindex=1;
    
    //用来记录看漫画时当前的漫画是否加载完毕
    public static Map<String, Boolean> map=null;

    public static ProgressBarThread pbThr = null;
    
    
    /**
	 * 装下需要下载的漫画 键表示一本漫画
	 */
	public static Map<Integer, List<Down>> downsmap = new HashMap<Integer, List<Down>>();

	/**
	 * 用来装下载器
	 */
	public static Map<Integer, ImageDownProgress> downloader = new HashMap<Integer, ImageDownProgress>();

	/**
	 * 用来判断下载器是否在下载
	 */
	public static Map<Integer, Boolean> downloaded = new HashMap<Integer, Boolean>();
	/**
	 * 用来判断下载器是否下载下一个章节
	 */
	public static Map<Integer, Boolean> isdownnext = new HashMap<Integer, Boolean>();
    
    /**
     * 进度条的最大值
     */
    public static int MAX_PROGRESS = 400;
    /**
     * 进度条的进度值
     */
    public static int progress = 0;
    public static   String[] urls;
    
    public static String downPort;
    
    
    /**
     * 记录当前的界面
     * author DaxstUz 2015年3月2日 上午10:44:57
     *
     * @param acti
     */
    public static void addActivity(Activity acti) {
        acts.add(acti);
    }

    /**
     * 把不是非本界面的都关掉
     * author DaxstUz 2015年3月2日 上午10:45:22
     *
     * @param acti
     */
    public static void removeActivity(Activity acti) {
        for (Activity activity : acts) {
            if (activity != acti) {
                activity.finish();
            }
        }
    }

    //开始请求网络
    public static void startnet(ProgressBar mypb) {
    	pbThr = new ProgressBarThread(mypb);
		Thread th = new Thread(pbThr);
		th.start();
    }

    //结束
    public static void endnet() {
        try {
        	Utils.isdowned = true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * 提示框
     * author DaxstUz 2015年2月26日 下午1:41:01
     *
     * @param content
     * @param msg
     */
    public static void showMsg(Context context, String msg) {
    	Toast t = Toast.makeText(MhyApplication.getApplication(),msg,Toast.LENGTH_SHORT);
//    	t.setGravity(Gravity.CENTER, Gravity.CENTER,  400);
//    	t.setMargin(0, 20);
    	t.show();
    }
   
    public static void showErrorMsg(Context content){
    	 AlertDialog.Builder builder = new Builder(content);
         builder.setTitle("提示");
         builder.setPositiveButton("确定", null);
         builder.setIcon(android.R.drawable.ic_dialog_info);
         builder.setMessage("加载数据失败！");
         builder.show();
    }

    
    
    /** 
     * 得到自定义的progressDialog 
     * @param context 
     * @return 
     */  
    public static Dialog createLoadingDialog(Context context) {  
        LayoutInflater inflater = LayoutInflater.from(context);  
        View v = inflater.inflate(R.layout.layout_loading_dialog, null);        // 得到加载view  
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);  // 加载布局  
        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);  
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(context,R.anim.loading_animation); // 加载动画  
        spaceshipImage.startAnimation(hyperspaceJumpAnimation);             // 使用ImageView显示动画  
        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog); // 创建自定义样式dialog  
          
        //loadingDialog.setCancelable(false);// 不可以用"返回键"取消  
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,  
                LinearLayout.LayoutParams.MATCH_PARENT));  
        loadingDialog.setCancelable(false); //
        loadingDialog.setCanceledOnTouchOutside(false);
        
        
        WindowManager windowManager =(WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = loadingDialog.getWindow().getAttributes();
        lp.width = (int)(display.getWidth()); //设置宽度
        lp.height=(int)(display.getHeight()); //设置高度
        loadingDialog.getWindow().setAttributes(lp); 
        
        return loadingDialog;  
    } 
    
    /**
     * 获取分类名字
     * @param n
     * @return
     */
    public static String getCate(int n) {
        String cate = null;
        switch (n) {
            case 1:
                cate = "科幻";
                break;
            case 2:
                cate = "魔幻";
                break;
            case 3:
                cate = "热血";
                break;
            case 4:
                cate = "搞笑";
                break;
            case 5:
                cate = "恋爱";
                break;
            case 6:
                cate = "动作";
                break;
            case 7:
                cate = "战争";
                break;
            case 8:
                cate = "治愈";
                break;
            case 9:
                cate = "恐怖";
                break;
            case 10:
                cate = "推理";
                break;
            case 11:
                cate = "体育";
                break;
            case 12:
                cate = "耽美";
                break;
            case 13:
                cate = "百合";
                break;
            case 14:
                cate = "美食";
                break;
            case 15:
                cate = "校园";
                break;
            case 17:
                cate = "生肉";
                break;
            case 18:
                cate = "冒险";
                break;
            case 19:
                cate = "四格";
                break;    
            case 20:
                cate = "伪娘";
                break;     
            case 30:
                cate = "彩漫";
                break;
            default:
            	cate = "其他";
                break;
        }

        return cate;
    }
    

//    public static DisplayImageOptions options=new DisplayImageOptions.Builder().imageScaleType(ImageScaleType.EXACTLY_STRETCHED).displayer(new RoundedBitmapDisplayer(5)).build();
    public static DisplayImageOptions options=new DisplayImageOptions.Builder()
//    		.showImageOnLoading(R.drawable.ic_loading_small) //设置图片在下载期间显示的图片         
    		.showImageForEmptyUri(R.drawable.ic_empty) // 设置图片Uri为空或是错误的时候显示的图片
            .showImageOnFail(R.drawable.ic_error) // 设置图片加载或解码过程中发生错误显示的图片
            .cacheInMemory(true) // 设置下载的图片是否缓存在内存中
            .cacheOnDisc(true) // 设置下载的图片是否缓存在SD卡中
            .bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED).displayer(new RoundedBitmapDisplayer(5)).build();
   
    public static DisplayImageOptions options3=new DisplayImageOptions.Builder()
    .showImageOnLoading(R.drawable.ic_loading_small) //设置图片在下载期间显示的图片         
    .showImageForEmptyUri(R.drawable.ic_empty) // 设置图片Uri为空或是错误的时候显示的图片
    .showImageOnFail(R.drawable.ic_error) // 设置图片加载或解码过程中发生错误显示的图片
//    .cacheInMemory(false) // 设置下载的图片是否缓存在内存中
//    .cacheOnDisc(false) // 设置下载的图片是否缓存在SD卡中
    .bitmapConfig(Bitmap.Config.RGB_565).build();
   
    public static DisplayImageOptions options2=new DisplayImageOptions.Builder()
//    		.showImageOnLoading(R.drawable.ic_stub) //设置图片在下载期间显示的图片         
    		.showImageForEmptyUri(R.drawable.ic_empty) // 设置图片Uri为空或是错误的时候显示的图片
    		.showImageOnFail(R.drawable.load_error) // 设置图片加载或解码过程中发生错误显示的图片
//    		.cacheInMemory(true) // 设置下载的图片是否缓存在内存中
    		.cacheOnDisk(true) // 设置下载的图片是否缓存在SD卡中
    		.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
    		.bitmapConfig(Bitmap.Config.RGB_565).build();
    
    public static DisplayImageOptions options5=new DisplayImageOptions.Builder()
//    		.showImageOnLoading(R.drawable.ic_stub) //设置图片在下载期间显示的图片         
//    .showImageForEmptyUri(R.drawable.ic_empty) // 设置图片Uri为空或是错误的时候显示的图片
//    .showImageOnFail(R.drawable.load_error) // 设置图片加载或解码过程中发生错误显示的图片
//    		.cacheInMemory(true) // 设置下载的图片是否缓存在内存中
    .cacheOnDisk(true) // 设置下载的图片是否缓存在SD卡中
    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
    .bitmapConfig(Bitmap.Config.RGB_565).build();
    
    public static DisplayImageOptions optionsh=new DisplayImageOptions.Builder()
		//	.showImageOnLoading(R.drawable.ic_stub) //设置图片在下载期间显示的图片         
		.showImageForEmptyUri(R.drawable.ic_empty) // 设置图片Uri为空或是错误的时候显示的图片
		.showImageOnFail(R.drawable.load_error_h) // 设置图片加载或解码过程中发生错误显示的图片
		.cacheInMemory(true) // 设置下载的图片是否缓存在内存中
		.cacheOnDisk(true) // 设置下载的图片是否缓存在SD卡中
		.imageScaleType(ImageScaleType.EXACTLY)
		.bitmapConfig(Bitmap.Config.RGB_565).build();
    
    public static DisplayImageOptions adapterOpt =new DisplayImageOptions.Builder()
		.showImageOnLoading(R.drawable.ic_loading_small) //设置图片在下载期间显示的图片         
		.showImageForEmptyUri(R.drawable.ic_empty) // 设置图片Uri为空或是错误的时候显示的图片
		.showImageOnFail(R.drawable.ic_error) // 设置图片加载或解码过程中发生错误显示的图片
		//.cacheInMemory(false) // 设置下载的图片是否缓存在内存中
		.cacheOnDisk(true) // 设置下载的图片是否缓存在SD卡中
		.imageScaleType(ImageScaleType.EXACTLY)
		.bitmapConfig(Bitmap.Config.RGB_565).build();

    /**
     * 返回屏幕的宽、高
     * @return
     */
    public static int[] getScreenDispaly() {
		WindowManager windowManager = (WindowManager) MhyApplication.getApplication().getSystemService(Context.WINDOW_SERVICE);
		int width = windowManager.getDefaultDisplay().getWidth();// 手机屏幕的宽度
		int height = windowManager.getDefaultDisplay().getHeight();// 手机屏幕的高度
		int result[] = { width, height };
		return result;
	}
    
    public static String getCachePath() {
		Context context = MhyApplication.getApplication().getApplicationContext();
		final String cachePath = (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED || !Environment
				.isExternalStorageRemovable()) ? context.getExternalCacheDir()
				.getPath() : context.getCacheDir().getPath();
		return cachePath;
	}
    
}
