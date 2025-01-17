package com.ch.mhy.activity.my;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ch.mhy.R;
import com.ch.mhy.util.Loading;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.UMAuthListener;
import com.umeng.socialize.controller.listener.SocializeListeners.UMDataListener;
import com.umeng.socialize.exception.SocializeException;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;

/**
 * 登陆界面
 * 
 * @author DaxstUz 2416738717 2015年9月30日
 *
 */
public class LoginActivity extends Activity {

	/* 登陆变量声明 */
	UMSocialService mController = UMServiceFactory
			.getUMSocialService("com.umeng.login");
	
	private ImageView iv_loginqq;
	private ImageView iv_loginweixin;
	private ImageView iv_loginsina;
	private TextView tv_loginqq;
	private TextView tv_loginweixin;
	private TextView tv_loginsina;
	
	private SharedPreferences sp;
	private String openid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		sp = this.getSharedPreferences("userinfo",
				Context.MODE_PRIVATE);
		openid = sp.getString("openid", "");
		
		iv_loginqq=(ImageView) this.findViewById(R.id.iv_loginqq);
		iv_loginweixin=(ImageView) this.findViewById(R.id.iv_loginweixin);
		iv_loginsina=(ImageView) this.findViewById(R.id.iv_loginsina);
		tv_loginqq=(TextView) this.findViewById(R.id.tv_loginqq);
		tv_loginsina=(TextView) this.findViewById(R.id.tv_loginsina);
		tv_loginweixin=(TextView) this.findViewById(R.id.tv_loginweixin);
	}

	private SHARE_MEDIA platm;

	/**
	 * 点击事件
	 * */
	public void onclick(View view) {
		switch (view.getId()) {
		case R.id.ll_login_qq:
			// 参数1为当前Activity， 参数2为开发者在QQ互联申请的APP ID，参数3为开发者在QQ互联申请的APP kEY.
			UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(this,
					"1104686309", "LsxbkRrXqzao9zVP");
			qqSsoHandler.addToSocialSDK();

			platm = SHARE_MEDIA.QQ;
			
//			iv_loginqq.setImageResource(R.drawable.login_qq);
//			tv_loginqq.setTextColor(getResources().getColor(R.color.white));
			
			doAuthor();
			break;
		case R.id.ll_login_weixin:
//			iv_loginweixin.setImageResource(R.drawable.login_weixin);
//			tv_loginweixin.setTextColor(getResources().getColor(R.color.white));
			// 添加微信平台
			UMWXHandler wxHandler = new UMWXHandler(this, "wx51997f48a10bdff8",
					"5d7d2282149b115c8965f91bb71d7350");
			wxHandler.addToSocialSDK();

			platm = SHARE_MEDIA.WEIXIN;
			doAuthor();
			break;
		case R.id.ll_login_sina:
//			iv_loginsina.setImageResource(R.drawable.login_sina);
//			tv_loginsina.setTextColor(getResources().getColor(R.color.white));

			// 设置新浪SSO handler
			mController.getConfig().setSsoHandler(new SinaSsoHandler());

			platm = SHARE_MEDIA.SINA;
			doAuthor();
			break;
		default:

			break;
		}
	}

	/**
	 * 开始授权
	 */
	private void doAuthor() {
		mController.doOauthVerify(this, platm, new UMAuthListener() {
			@Override
			public void onError(SocializeException e, SHARE_MEDIA platform) {
				Log.d("tag", "doAuthor  onError ");
			}

			@Override
			public void onComplete(Bundle value, SHARE_MEDIA platform) {
				if (value != null) {
					if(platm.equals(SHARE_MEDIA.SINA)){
						openid = value.getString("uid");
					}else{
						openid = value.getString("openid");
					}

					Loading.startnet(LoginActivity.this);
					getPlatFormInfo(platform);
				} else {
//					Toast.makeText(LoginActivity.this, "授权失败",
//							Toast.LENGTH_SHORT).show();
				}


			}

			@Override
			public void onCancel(SHARE_MEDIA platform) {
			}

			@Override
			public void onStart(SHARE_MEDIA platform) {
			}
		});
	}

	/**
	 * 获取平台返回用户信息
	 * @param platform
	 */
	public void getPlatFormInfo(final SHARE_MEDIA platform) {
		// 获取相关授权信息
		mController.getPlatformInfo(LoginActivity.this, platform,
				new UMDataListener() {
					@Override
					public void onStart() {
					}

					@Override
					public void onComplete(int status, Map<String, Object> info) {
						if (status == 200 && info != null) {
							 Editor editer = sp.edit();
							 editer.putString("openid", openid);
							 String headurl;
							
							 String screen_name;
							 if(platform.equals(SHARE_MEDIA.WEIXIN)){
								 screen_name = (String) info
										 .get("nickname");
								 headurl = (String) info
										 .get("headimgurl");
							 }else{
								 screen_name = (String) info
										 .get("screen_name");
								 headurl = (String) info
										 .get("profile_image_url");
							 }
							 editer.putString("headurl", headurl);
							 editer.putString("screen_name",
							 screen_name);
							 editer.putString("SHARE_MEDIA", platform.toString());
							 editer.commit();
							 Intent intent=new Intent();
							 intent.putExtra("islogin", true);
							 setResult(003, intent);
							 LoginActivity.this.finish();
							 Loading.endnet();
						} else {
//							Log.d("TestData", "发生错误：" + status);
						}
					}
				});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/** 使用SSO授权必须添加如下代码 */
		UMSsoHandler ssoHandler = mController.getConfig().getSsoHandler(
				requestCode);
		if (ssoHandler != null) {
			ssoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
	}
}
