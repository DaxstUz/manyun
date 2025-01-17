package com.ch.mhy.activity.my;

import com.ch.mhy.R;
import com.ch.mhy.fragment.MyFragment;
import com.ch.mhy.widget.CircleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SocializeClientListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class UnLoginActivity  extends Activity{
	
	/**
	 * 用来存用户本地信息
	 */
	private SharedPreferences sp;
	private String openid;
	
	
	/**
	 * umeng 第三方登录
	 */
	UMSocialService mController = UMServiceFactory
			.getUMSocialService("com.umeng.login");
	
	private TextView tv_nickname_canser;
	private TextView tv_platfrom_canser;
	
	private CircleImageView iv_show_head;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_unlogin);
		sp = this.getSharedPreferences("userinfo",
				Context.MODE_PRIVATE);
		openid = sp.getString("openid", "");
		
		iv_show_head=(CircleImageView) findViewById(R.id.iv_show_head);
		tv_nickname_canser=(TextView) findViewById(R.id.tv_nickname_canser);
		tv_platfrom_canser=(TextView) findViewById(R.id.tv_platfrom_canser);
		String screen_name = sp.getString("screen_name", "");
		tv_nickname_canser.setText(screen_name);
		
		String pname=sp.getString("SHARE_MEDIA", "");
		if("qq".equals(pname)){
			pname="腾讯QQ";
			sm=SHARE_MEDIA.QQ;
		}
		if("sina".equals(pname)){
			pname="新浪微博";
			sm=SHARE_MEDIA.SINA;
		}
		if("weixin".equals(pname)){
			pname="微信";
			sm=SHARE_MEDIA.WEIXIN;
		}
		
		tv_platfrom_canser.setText(pname);
		
		/**
		 * 展示图片
		 */
		String headurl = sp.getString("headurl", "");

		if (headurl.length() > 0) {
			ImageLoader.getInstance().displayImage(headurl, iv_show_head,
					new ImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri,
								View view) {
							// TODO Auto-generated method stub
							view.setBackgroundResource(R.drawable.ch_my_head);
						}

						@Override
						public void onLoadingFailed(String imageUri,
								View view, FailReason failReason) {
							// TODO Auto-generated method stub

							view.setBackgroundResource(0);
						}

						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							// TODO Auto-generated method stub
							view.setBackgroundResource(0);
						}

						@Override
						public void onLoadingCancelled(String imageUri,
								View view) {
							// TODO Auto-generated method stub
							view.setBackgroundResource(0);
						}
					});
		}
	}

	private SHARE_MEDIA sm;
	
	public void onclick(View view){
		switch (view.getId()) {
		case R.id.btn_return_back:
			Intent intent=new Intent();
			intent.putExtra("isexit", false);
			setResult(002, intent);
			this.finish();
			break;
		case R.id.ll_unlogin:
			mController.deleteOauth(UnLoginActivity.this, sm,
					new SocializeClientListener() {
						@Override
						public void onStart() {
						}

						@Override
						public void onComplete(int status, SocializeEntity entity) {
						}
					});
			
			Intent intent2=new Intent();
			intent2.putExtra("isexit", true);
			setResult(002, intent2);
			this.finish();
			break;

		default:
			break;
		}
		
	}
}
