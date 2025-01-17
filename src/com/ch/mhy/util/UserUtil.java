package com.ch.mhy.util;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * 用户信息 帮助类
 * @author DaxstUz 2416738717
 * 2015年10月14日
 *
 */
public class UserUtil {

	private static SharedPreferences sp;
	private static String openid;
	private static String screen_name;
	private static String headurl;
	private static String platForm;
	
	/**
	 * 判断是否登陆
	 * @param context
	 * @return
	 */
	public static boolean isLogin(Context context){
		sp = context.getSharedPreferences("userinfo",
				Context.MODE_PRIVATE);
		openid = sp.getString("openid", "");
		if("qquser".equals(getScreen_name(context).toLowerCase())){
			return false;
		}
		if(openid.length()>0){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 获取openid
	 * @param context
	 * @return
	 */
	public static String getOpenId(Context context){
		sp = context.getSharedPreferences("userinfo",
				Context.MODE_PRIVATE);
		openid = sp.getString("openid", "");
		return openid;
	}
	
	/**
	 * 获取screen_name（昵称）
	 * @param context
	 * @return
	 */
	public static String getScreen_name(Context context){
		sp = context.getSharedPreferences("userinfo",
				Context.MODE_PRIVATE);
		screen_name = sp.getString("screen_name", "");
		return screen_name;
	}
	
	/**
	 * 获取headurl
	 * @param context
	 * @return
	 */
	public static String getHeadurl(Context context){
		sp = context.getSharedPreferences("userinfo",
				Context.MODE_PRIVATE);
		headurl = sp.getString("headurl", "");
		return headurl;
	}
	
	/**
	 * 获取登陆平台信息
	 * @param context
	 * @return
	 */
	public static String getPlatForm(Context context){
		sp = context.getSharedPreferences("userinfo",
				Context.MODE_PRIVATE);
		platForm = sp.getString("SHARE_MEDIA", "");
		return platForm;
	}
	
	
}
