package com.ch.mhy.activity.book;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.ViewPager.SavedState;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ch.mhy.R;
import com.ch.mhy.activity.book.loadcomic.ThreadPoolManager;
import com.ch.mhy.application.MhyApplication;
import com.ch.mhy.db.DBManager;
import com.ch.mhy.db.DBUtil;
import com.ch.mhy.dialog.SelectDialog;
import com.ch.mhy.entity.ComicsDetail;
import com.ch.mhy.entity.Down;
import com.ch.mhy.listener.Add;
import com.ch.mhy.listener.UtilDialogListener;
import com.ch.mhy.net.NetReceiver;
import com.ch.mhy.util.DateUtil;
import com.ch.mhy.util.FileUtil;
import com.ch.mhy.util.NetUtil;
import com.ch.mhy.util.ProgressBarThread;
import com.ch.mhy.util.SDUtil;
import com.ch.mhy.util.ScreenBrightnessTool;
import com.ch.mhy.util.UrlConstant;
import com.ch.mhy.util.Utils;
import com.ch.mhy.widget.ImageCycleView2;
import com.ch.mhy.widget.ImageCycleView2.ImageCycleViewListener;
import com.ch.mhy.widget.PhotoView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;

//import com.ch.mhy.widget.TouchImageView;

/**
 * 阅读中
 * 
 * @author DaxstUz 2416738717 2015年5月4日
 */
public class ReadingActivity extends Activity implements OnGestureListener,
		OnSeekBarChangeListener, NetReceiver.NetEventHandle,
		UtilDialogListener, Add {
	// 分享成员变量
	final UMSocialService mController = UMServiceFactory
			.getUMSocialService("com.umeng.share");

	// 图片展示
	private ImageCycleView2 ch_read_view;

	private LinearLayout saveView;// 用于截图

	private ArrayList<String> mImageUrl = null;
	private ArrayList<String> localUrlArr = null;

	private GestureDetector gestureDetector;
	private int verticalMinDistance = 180;
	private int minVelocity = 0;

	// 操作按钮界面
	private LayoutInflater inflater;
	private FrameLayout layout;
	private FrameLayout fl_bookmain;

	// 调节屏幕亮度
	private SeekBar sb_light;
	private ScreenBrightnessTool sBrightnessTool;

	private ComicsDetail mh;

	private TextView tv_operate_title;

	private String comefrom; // 0表示读取本地资源， 1表示读取网络资源

	private String order = "asc";
	TextView tv_book_operateindex;
	SeekBar sb_book_adjustbook;
	// 涨水加载
	private ProgressBar mypb;
	private LinearLayout view;

	private LinearLayout ll_showinfo;

	private boolean issp = true;// 判断当前是否是横屏

	SharedPreferences sp;

	/* 漫画信息展示 */
	private TextView tv_showindex;
	private TextView tv_title;
	private TextView tv_time;

	private ProgressBarThread loadPgTh;

	/**
	 * 获取openid
	 */
	private String openid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences sp1 = this.getSharedPreferences("userinfo",
				Context.MODE_PRIVATE);
		openid = sp1.getString("openid", "");
		
		setRing(true);

		mh = (ComicsDetail) getIntent().getSerializableExtra("mh");
		registerReceiver(batteryReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));

		/* set it to be no title */
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		/* set it to be full screen */
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_reading);

		if (Utils.ur != null) {
			Utils.ur.update();
		}

		sp = this.getSharedPreferences("userConfig", 0);

		// 当设置了wifi下载，而当前的网络不是wifi连接就变为不可下载
		if ("1".equals(comefrom) && sp.getBoolean("wifiread", true)) {
			if (NetReceiver.isConnected(this) != NetReceiver.NetState.NET_NO) {
				if (NetReceiver.isConnected(this) != NetReceiver.NetState.NET_WIFI) {
					Utils.showMsg(this, "当前不是wifi连接，请注意流量哦！");
				}
			} else {
				Utils.showMsg(this, "没有网络！");
			}

		}

		initShare();

		initView();
	}

	/**
	 * 初始化控件
	 */
	private void initView() {
		tv_showindex = (TextView) findViewById(R.id.tv_showindex);
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_time = (TextView) findViewById(R.id.tv_time);

		layout = (FrameLayout) findViewById(R.id.layout_read);
		fl_bookmain = (FrameLayout) findViewById(R.id.fl_bookmain);

		ll_showinfo = (LinearLayout) findViewById(R.id.ll_showinfo);

		ch_read_view = (ImageCycleView2) findViewById(R.id.ch_read_view);
		mImageUrl = new ArrayList<String>();
		localUrlArr = new ArrayList<String>();

		Utils.rindex = mh.getrIndex();
		comefrom = (String) getIntent().getSerializableExtra("comefrom");

		// 张水加载视图
		view = (LinearLayout) this.findViewById(R.id.pbView);
		mypb = (ProgressBar) this.findViewById(R.id.mypb);
		loadPgTh = new ProgressBarThread(mypb);
		loadPgTh.start();

		initShowView();

		if (MhyApplication.brightness != -20) {
			sBrightnessTool.setBrightness(ReadingActivity.this,
					MhyApplication.brightness);
		}

		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layout = (FrameLayout) findViewById(R.id.layout_read);
		// layout.setOnTouchListener(this);
		gestureDetector = new GestureDetector(this);
		sBrightnessTool = ScreenBrightnessTool.Builder(ReadingActivity.this);
		getXy();

		hander.sendMessage(Message.obtain());

		updateHits();
	}

	/**
	 * 初始化展示视图
	 */
	public void initShowView() {
		String[] urls = mh.getmUrl() == null ? new String[] {} : mh.getmUrl()
				.split(Utils.split);
		String[] localUrls = mh.getLocalUrl() == null ? new String[] {} : mh
				.getLocalUrl().split(Utils.split);

		for (int i = 0; i < urls.length; i++) {
			mImageUrl.add(urls[i]);
			if (i < localUrls.length) {
				localUrlArr.add(localUrls[i]);
			} else {
				localUrlArr.add("/firstTurn/ic_empty.png");
			}
		}

		if (mImageUrl.size() > 0) {
			mImageUrl.add(UrlConstant.Ip1 + UrlConstant.Port1
					+ "/firstTurn/manhua_nomore.jpg");
			mImageUrl.add(0, UrlConstant.Ip1 + UrlConstant.Port1
					+ "/firstTurn/manhua_nomore.jpg");
			localUrlArr.add("/firstTurn/manhua_nomore.jpg");
			localUrlArr.add(0, "/firstTurn/manhua_nomore.jpg");
		}

		if ("0".equals(comefrom)) {
			ch_read_view.setImageResources(localUrlArr, mAdCycleViewListener,
					this);
		} else {
			ch_read_view.setImageResources(mImageUrl, mAdCycleViewListener,
					this);
		}
	}

	/**
	 * 初始化分享信息
	 */
	private void initShare() {

		String appID = "wx51997f48a10bdff8";
		String appSecret = "5d7d2282149b115c8965f91bb71d7350";
		// 添加微信平台
		UMWXHandler wxHandler = new UMWXHandler(this, appID, appSecret);
		wxHandler.addToSocialSDK();
		// 添加微信朋友圈
		UMWXHandler wxCircleHandler = new UMWXHandler(this, appID, appSecret);
		wxCircleHandler.setToCircle(true);
		wxCircleHandler.addToSocialSDK();
	}

	/**
	 * 分享的信息
	 */
	private void shareInfo() {
		// 设置分享图片，参数2为本地图片的资源引用
		UMImage umi = new UMImage(this, R.drawable.my);
		umi.setTargetUrl("http://my.67az.com");
		umi.setTitle("漫云分享");
		mController.setShareMedia(umi);

		// 设置分享内容
		mController
				.setShareContent("我正在用漫云看<<"
						+ mh.getmTitle()
						+ "  "
						+ mh.getmName()
						+ ">>,你也快下载来一起看吧~强烈推荐哦,所有最新最热的漫画这款软件里面都有哟~进入官网或者进入各大应用平台都可以直接下载");

		mController.getConfig().removePlatform(SHARE_MEDIA.SINA,
				SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE, SHARE_MEDIA.TENCENT);

		// 是否只有已登录用户才能打开分享选择页
		mController.openShare(this, false);

		mController.registerListener(mSnsPostListener);
	}

	int width = 0;
	int height = 0;

	/**
	 * 获取屏幕宽高
	 */
	private void getXy() {
		WindowManager wm = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels;
	}

	long endTime = 0;

	private boolean isadjust=false;//是否显示亮度调节框
	
	public void onclick(View view) {

		switch (view.getId()) {
		case R.id.btn_book_setshow:
			if (ReadingActivity.this.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
				ReadingActivity.this
						.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				issp = false;
			} else {
				ReadingActivity.this
						.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				issp = true;
			}

			strList.clear();
			stop();

			break;
		case R.id.iv_book_share:
			shareInfo();
			break;

		case R.id.ll_book_showdown:
			Utils.showMsg(ReadingActivity.this, "截图成功！");

			SaveImageTask st = new SaveImageTask();
			st.execute();
			saveView = (LinearLayout) ch_read_view.mAdvAdapter.getPrimaryItem();
			break;
		case R.id.btn_operate_back:
			loadPgTh.setRunning(false);// 关闭涨水加载线程
			ReadingActivity.this.finish();
			break;
		case R.id.btn_operate_select:
			if (cds.size() > 0) {
				SelectDialog sdDialog = new SelectDialog(ReadingActivity.this,
						R.style.select_dialog, cds, this, mh.getmNo());
				sdDialog.show();
			} else {
				if ((System.currentTimeMillis() - endTime) > 2000) {

					Utils.showMsg(ReadingActivity.this, "获取数据未完毕！");
					endTime = System.currentTimeMillis();
				}
			}

			break;
		case R.id.btn_adjust_light:
			layout.removeViewAt(1);

			View vvv = inflater.inflate(R.layout.dialog_adjust_light, null);
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			lp.gravity = Gravity.CENTER;
			sb_light = (SeekBar) vvv.findViewById(R.id.sb_light);
			if (MhyApplication.brightness == -20) {
				MhyApplication.brightness = sBrightnessTool
						.getSystemBrightness();
			}
			sb_light.setProgress(MhyApplication.brightness);
			sb_light.setOnSeekBarChangeListener(ReadingActivity.this);

			layout.addView(vvv, 1, lp);

			isadjust=true;
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	boolean flag = true;

	private long excuteTime = 0;// 记得点击事件，快速点击时，1秒时间内只能一次有效

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if ((System.currentTimeMillis() - excuteTime) > 100) {
			excuteTime = System.currentTimeMillis();

			if (getIsRight(e)) {
				if (!iscycle) {
					ch_read_view.next();
				}
			} else if (getIsCenter(e)) {
				if (layout.getChildCount() > 1) {
					layout.removeViewAt(1);
					flag = false;
				} else {
					flag = true;
				}

				if (flag) {
					View vv = inflater.inflate(R.layout.activity_operate, null);
					FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
							LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
					lp.gravity = Gravity.BOTTOM;
					vv.getBackground().setAlpha(0);

					tv_book_operateindex = (TextView) vv
							.findViewById(R.id.tv_book_operateindex);

					String show = ch_read_view.mImageIndex + "/"
							+ (ch_read_view.mImageViews.length - 2);
					tv_book_operateindex.setText(show);
					sb_book_adjustbook = (SeekBar) vv
							.findViewById(R.id.sb_book_adjustbook);
					sb_book_adjustbook
							.setMax(ch_read_view.mImageViews.length - 2);
					sb_book_adjustbook.setProgress(ch_read_view.mImageIndex);
					sb_book_adjustbook
							.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

								@Override
								public void onStopTrackingTouch(SeekBar seekBar) {

									/* 看之前清理下缓存 */
									ImageLoader.getInstance()
											.clearMemoryCache();
									ImageLoader.getInstance().clearDiscCache();
									System.gc();
									ch_read_view.mImageIndex = ch_read_view.mImageIndex + 1;
									ch_read_view.pre();
								}

								@Override
								public void onStartTrackingTouch(SeekBar seekBar) {
								}

								@Override
								public void onProgressChanged(SeekBar seekBar,
										int progress, boolean fromUser) {
									if (progress == 0) {
										progress = 1;
									}
									ch_read_view.mImageIndex = progress;
									String show = ch_read_view.mImageIndex
											+ "/"
											+ (ch_read_view.mImageViews.length - 2);
									tv_book_operateindex.setText(show);
								}
							});

					tv_operate_title = (TextView) vv
							.findViewById(R.id.tv_operate_title);
					if (mh.getmTitle() != null) {
						String mht = mh.getmTitle().length() > 6 ? mh
								.getmTitle().substring(0, 6) : mh.getmTitle();
						mht = mht + " " + mh.getmName();
						mht = mht.length() > 10 ? mht.substring(0, 10) : mht;
						tv_operate_title.setText(mht);
					}
					layout.addView(vv, 1, lp);
					flag = false;
				}
			} else if (getIsLeft(e)) {
				if (!iscycle) {
					ch_read_view.pre();
				}
			}
		}
		return false;
	}

	/**
	 * 判断是否点击左边
	 * 
	 * @param e
	 * @return
	 */
	public boolean getIsLeft(MotionEvent e) {
		if (issp) {
			if (e.getX() < width / 3 && e.getY() > height / 11
					&& e.getY() < height * 10 / 11 || e.getY() < height / 3) {
				return true;
			} else {
				return false;
			}
		} else {
			if (e.getX() < height / 3 && e.getY() > width / 11
					&& e.getY() < width * 10 / 11 || e.getY() < width / 3) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * 判断是否点击右边
	 * 
	 * @param e
	 * @return
	 */
	public boolean getIsRight(MotionEvent e) {
		if (issp) {
			if (e.getX() > width * 2 / 3 && e.getY() > height / 11
					&& e.getY() < height * 10 / 11 || e.getX() > width / 3
					&& e.getY() > height * 2 / 3) {
				return true;
			} else {
				return false;
			}
		} else {
			if (e.getX() > height * 2 / 3 && e.getY() > width / 11
					&& e.getY() < width * 10 / 11 || e.getX() > height / 3
					&& e.getY() > width * 2 / 3) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * 判断是否是点击中间区域
	 * 
	 * @param e
	 * @return
	 */
	public boolean getIsCenter(MotionEvent e) {
		if (issp) {
			if ((width / 2 - width / 6) < e.getX()
					&& e.getX() < (width / 2 + width / 6)
					&& e.getY() > height / 3 && e.getY() < 2 * height / 3) {
				return true;
			} else {
				return false;
			}
		} else {
			if ((height / 2 - height / 6) < e.getX()
					&& e.getX() < (height / 2 + height / 6)
					&& e.getY() > width / 3 && e.getY() < 2 * width / 3) {
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return true;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (progress <=1) {
			progress=2;
			seekBar.setProgress(1);
		}
		MhyApplication.brightness = progress;
		sBrightnessTool.setBrightness(ReadingActivity.this, progress);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	private ImageCycleViewListener mAdCycleViewListener = new ImageCycleViewListener() {
		@Override
		public void onImageClick(final int position, View imageView) {
		}

		@Override
		public void displayImage(final String imageURL,
				final PhotoView imageView, final Map<String, Boolean> map) {

			boolean load = true;
			if (imageURL.indexOf("d17a64a81f1f4e388196b80c34dc5feb") > -1) {// 图片缺失
				imageView.setBackgroundResource(R.drawable.lose);
				load = false;
			}

			/**
			 * 横竖屏 配置
			 */
			DisplayImageOptions oio;
			if (ReadingActivity.this.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
				oio = Utils.options2;
			} else {
				oio = Utils.optionsh;
			}

			if ("1".equals(comefrom) && load) {

				showBit(imageURL, imageView, map, oio);

//				new Thread(new Runnable() {
//					@Override
//					public void run() {
//						try {
//							HttpURLConnection conn = (HttpURLConnection) new URL(
//									imageURL).openConnection();
//							int code = conn.getResponseCode();
//							Message msg = new Message();
//
//							LoadInfo li = new LoadInfo();
//							li.setImageView(imageView);
//							li.setMap(map);
//							li.setImageURL(imageURL);
//							li.setLocalimageURL("");
//							String contentType = conn.getContentType();
//							if (contentType == null) {
//								contentType = "";
//							}
//							if (code != 200
//									|| (contentType.indexOf("image/jpeg") == -1
//											&& contentType.indexOf("image/png") == -1
//											&& contentType
//													.indexOf("image/x-icon") == -1 && contentType
//											.indexOf("application/x-jpg") == -1)) {// 请求本地服务器
//								String localUrl = null;
//
//								int i = 0;
//								if (mImageUrl != null) {
//									for (String st : mImageUrl) {
//										if (st.equals(imageURL)) {
//											break;
//										}
//										i++;
//									}
//								}
//
//								if (localUrlArr != null
//										&& localUrlArr.size() > 0) {
//									if (i < localUrlArr.size()) {
//										localUrl = localUrlArr.get(i);
//										localUrl = UrlConstant.Ip1 + ":"
//												+ mh.getPort() + "/" + localUrl;
//										li.setLocalimageURL(localUrl);
//									}
//								}
//							} else {
//							}
//							msg.obj = li;
//
//							handler1.sendMessage(msg);// 发送服务请求消息，加载图片
//
//						} catch (MalformedURLException e) {
//							e.printStackTrace();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
//				}).start();
			}

			if ("0".equals(comefrom) && load) {
				String sdparth = imageURL;
				if (!imageURL.contains("manhua_nomore.jpg")) {// 图片缺失
					sdparth = "file://" + SDUtil.getSecondExterPath()
							+ "/manyun/";
					String[] strs = imageURL.split("\\/");
					sdparth = sdparth + mh.getmTitle() + "/" + mh.getmName()
							+ "/" + strs[strs.length - 1];
				} else {
					sdparth = "assets://manhua_nomore.jpg";
				}

				ImageLoader.getInstance().displayImage(sdparth, imageView, oio,
						new ImageLoadingListener() {
							@Override
							public void onLoadingStarted(String imageUri,
									View view) {
								map.put(imageURL, false);
								imageView
										.setBackgroundResource(R.drawable.ic_stub);
							}

							@Override
							public void onLoadingFailed(String imageUri,
									View view, FailReason failReason) {
								imageView.setBackgroundResource(0);
								map.put(imageURL, true);

								if (strList.contains(imageURL)) {
									strList.remove(imageURL);
								}
								if (strList.size() == 0) {
									stop();
								}
							}

							@Override
							public void onLoadingComplete(String imageUri,
									View view, Bitmap loadedImage) {
								imageView.setBackgroundResource(0);

								map.put(imageURL, true);
								if (strList.contains(imageURL)) {
									strList.remove(imageURL);
								}
								if (strList.size() == 0) {
									stop();
								}
							}

							@Override
							public void onLoadingCancelled(String imageUri,
									View view) {
								// TODO Auto-generated method stub
								map.put(imageURL, true);
								if (strList.contains(imageURL)) {
									strList.remove(imageURL);
								}
								if (strList.size() == 0) {
									stop();
								}
								map.put(imageURL, true);
								imageView.setBackgroundResource(0);
							}
						});
			}

		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		NetReceiver.ehList.remove(this);
		MobclickAgent.onPause(this);
	}

	@Override
	public void netState(NetReceiver.NetState netCode) {
		switch (netCode) {
		case NET_WIFI:
			break;
		default:
		}
	}

	@Override
	public void dialogToActivity(ComicsDetail cd) {

		List<Down> cddd = null;
		DBManager manager = new DBManager(this, DBUtil.ReadName, null,
				DBUtil.Code);
		cddd = manager.queryDown(
				"SELECT * FROM down where mId=? and mNo=? and isdown=0 ",
				new String[] { cd.getmId().toString(), cd.getmNo() + "" });

		if (cddd.size() > 0 && cddd.get(0).getIsdonw().equals(0)) {
			cd.setPmNo(cddd.get(0).getCd().getPmNo());
			cd.setNmNo(cddd.get(0).getCd().getNmNo());
			comefrom = "0";
		} else {
			comefrom = "1";
		}

		mh = cd;
		manager.addOrUpdateComicDetail(cd);
		manager.closeDB();

		String mht = mh.getmTitle() + " " + mh.getmName();
		mht = mht.length() > 10 ? mht.substring(0, 10) : mht;

		tv_operate_title.setText(mht);
		mImageUrl.clear();
		localUrlArr.clear();

		String[] urls = mh.getmUrl().split(Utils.split);

		String[] localUrls = mh.getLocalUrl() == null ? new String[] {} : mh
				.getLocalUrl().split(Utils.split);

		for (int i = 0; i < urls.length; i++) {
			mImageUrl.add(urls[i]);
			if (i < localUrls.length) {
				localUrlArr.add(localUrls[i]);
			} else {
				localUrlArr.add("/firstTurn/ic_empty.png");
			}
		}

		if (mImageUrl.size() > 1) {
			mImageUrl.add(UrlConstant.Ip1 + UrlConstant.Port1
					+ "/firstTurn/manhua_nomore.jpg");
			mImageUrl.add(0, UrlConstant.Ip1 + UrlConstant.Port1
					+ "/firstTurn/manhua_nomore.jpg");
			localUrlArr.add("/firstTurn/manhua_nomore.jpg");
			localUrlArr.add(0, "/firstTurn/manhua_nomore.jpg");
		}

		if (tv_book_operateindex != null) {

			String show = 1 + "/" + (mImageUrl.size() - 2);
			tv_book_operateindex.setText(show);

		}

		if (sb_book_adjustbook != null) {
			sb_book_adjustbook.setMax(mImageUrl.size() - 2);
			sb_book_adjustbook.setProgress(1);
		}

		Utils.rindex = 0;

		if ("0".equals(comefrom)) {
			// new RecycleBitmapInLayout(true).recycle(ch_read_view);
			ch_read_view.setImageResources(localUrlArr, mAdCycleViewListener,
					this);
		} else {
			strList.clear();
			stop();
			if (NetReceiver.isConnected(this) != NetReceiver.NetState.NET_NO) {
				ch_read_view.setImageResources(mImageUrl, mAdCycleViewListener,
						this);
			} else {
				Utils.showMsg(this, "没有网络！");
			}
		}
	}

	public void onResume() {
		super.onResume();
//		setRing(true);
		MobclickAgent.onResume(this);
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

	SnsPostListener mSnsPostListener = new SnsPostListener() {

		@Override
		public void onStart() {

		}

		@Override
		public void onComplete(SHARE_MEDIA platform, int stCode,
				SocializeEntity entity) {
			if (stCode == 200) {
				insertIntoServer(platform);
			} else {
			}
		}
	};

	private JSONObject param = new JSONObject();

	private void insertIntoServer(SHARE_MEDIA platform) {
		initParam(platform);

		JsonObjectRequest jsonRequest = new JsonObjectRequest(Method.POST,
				UrlConstant.UrlinsertBookShare, param,
				new Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {

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

		jsonRequest.setRetryPolicy(new DefaultRetryPolicy(6000,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		NetUtil.rqueue.add(jsonRequest);
	}

	private void initParam(SHARE_MEDIA platform) {
		try {
			param.put("pageSize", Utils.PageSize);
			param.put("currentPage", "1");
			param.put("orderBy", "");
			JSONObject js = new JSONObject();
			js.put("mId", mh.getmId());
			js.put("shareType", platform);
			js.put("shareTo", "");
			js.put("shareFrom", "");
			param.put("object", new JSONArray().put(js));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 是否在加载
	 */
	boolean iscycle = false;
	/**
	 * 装载正在加载的图片地址
	 */
	List<String> strList = new ArrayList<String>();

	private long stoptime;

	@Override
	// 如果不是读取本地就加载网络
	public void start(String url) {
		strList.add(url);
		if (!iscycle) {
			ll_showinfo.setVisibility(View.GONE);
			exitTime = System.currentTimeMillis();
			if (sb_book_adjustbook != null) {
				sb_book_adjustbook.setEnabled(false);
			}
			iscycle = true;
			view.setVisibility(View.VISIBLE);
			mypb.setProgress(0);
			loadPgTh.setWaiting(false);
			ch_read_view.mAdvPager.setVisibility(View.INVISIBLE);
			stoptime = System.currentTimeMillis();
		}
	}

	@Override
	public void stop() {
		if (iscycle) {
			if (sb_book_adjustbook != null) {
				sb_book_adjustbook.setEnabled(true);
			}
			iscycle = false;
			loadPgTh.setWaiting(true);
			view.setVisibility(View.GONE);
			ch_read_view.mAdvPager.setVisibility(View.VISIBLE);
			ll_showinfo.setVisibility(View.VISIBLE);

		}

	}

	private long exitTime = 0;

	@Override
	public void getNewData(String order1) {

		order = order1;
		if (mh.getmNo().equals(mh.getMaxNo()) && "asc".equals(order)
				|| mh.getmNo().equals(mh.getMinNo()) && "desc".equals(order)) {
			if ((System.currentTimeMillis() - exitTime) > 3000) {
				Utils.showMsg(ReadingActivity.this, "没有更多了！");
				exitTime = System.currentTimeMillis();
			}
			strList.clear();
			stop();
			updateIndex();
		} else {

			ComicsDetail cd = null;
			synchronized (ReadingActivity.this) {
				// 设置看到第几页
				DBManager manager = new DBManager(this, DBUtil.ReadName, null,
						DBUtil.Code);
				if ("desc".equals(order)) {
					cd = manager
							.queryByMno(
									"SELECT * FROM down where mId=? and mNo=?  and isdown=0  ",
									new String[] { mh.getmId().toString(),
											(mh.getNmNo().intValue()) + "" });
				} else {
					cd = manager
							.queryByMno(
									"SELECT * FROM down where mId=? and mNo=?  and isdown=0  ",
									new String[] { mh.getmId().toString(),
											(mh.getPmNo().intValue()) + "" });
				}
				manager.closeDB();

			}

			if (cd != null) {
				mh = cd;

				comefrom = "0";

				if (iscycle) {
					strList.clear();
					stop();
				}

				mImageUrl.clear();

				String[] urls = cd.getLocalUrl().split(Utils.split);
				for (int i = 0; i < urls.length; i++) {
					mImageUrl.add(urls[i]);
				}
				mImageUrl.add(UrlConstant.Ip1 + UrlConstant.Port1
						+ "/firstTurn/manhua_nomore.jpg");
				mImageUrl.add(0, UrlConstant.Ip1 + UrlConstant.Port1
						+ "/firstTurn/manhua_nomore.jpg");

				if (tv_operate_title != null) {

					String str = mh.getmTitle() + " " + mh.getmName();
					str = str.length() > 10 ? str.substring(0, 10) : str;
					tv_operate_title.setText(str);

				}
				if (tv_book_operateindex != null) {

					String show = 1 + "/" + (mImageUrl.size() - 2);
					tv_book_operateindex.setText(show);

				}

				if (sb_book_adjustbook != null) {
					if (mImageUrl.size() > 2) {
						sb_book_adjustbook.setMax(mImageUrl.size() - 2);
						sb_book_adjustbook.setProgress(1);
					}
				}

				if ("desc".equals(order)) {
					Utils.rindex = mImageUrl.size() - 2;
				} else {
					Utils.rindex = 0;
				}

				Utils.map.clear();
				poolManager.stop();
				ch_read_view.setImageResources(mImageUrl, mAdCycleViewListener,
						this);

			} else {
				if (NetUtil.checkNetWorkStatus(this)) {
					comefrom = "1";
					Utils.map.clear();
					Utils.showMsg(this, "请求网络数据。。。");
					getData();
				} else {
					Utils.showMsg(this, "请检查网络！");
					strList.clear();
					stop();
				}
			}

		}
	}

	JSONObject param2 = new JSONObject();

	/**
	 * 获取数据
	 */
	private void getData() {
		initParam();
		JsonObjectRequest joObjectRequest = new JsonObjectRequest(Method.POST,
				UrlConstant.UrlComicsDetailPage, param2,
				new Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {

						try {
							if (response.getString("object").equals("")) {
								// Log.d("tag", "response 236 ");
								Toast.makeText(ReadingActivity.this, "没有更多了！",
										Toast.LENGTH_SHORT).show();
								strList.clear();
								stop();
							} else {
								if (response.getJSONObject("object")
										.getString("object").trim().length() != 4) {
									JSONObject object = response.getJSONObject(
											"object").getJSONObject("object");
									ComicsDetail cdd = new ComicsDetail();

									if (mh != null && mh.getmPic() != null) {
										cdd.setmPic(mh.getmPic());
										cdd.setmDirector(mh.getmDirector());
										cdd.setmLianzai(mh.getmLianzai());
									}
									cdd.setmQid(object.getInt("mQid"));
									cdd.setmId(object.getInt("mId"));
									cdd.setmName(object.getString("mName"));
									cdd.setmUrl(object.getString("mUrl"));
									cdd.setmNo(object.getInt("mNo"));
									cdd.setmTitle(object.getString("mTitle"));
									cdd.setGradescore(object
											.getDouble("gradeScore"));
									cdd.setPartSize(object
											.getString("partsize"));
									cdd.setTotalPage(object.getInt("totalpage"));
									cdd.setLocalUrl(object
											.getString("localUrl"));
									cdd.setMaxNo(object.getInt("maxNo"));
									cdd.setMinNo(mh.getMinNo());
									cdd.setPort(object.getString("port"));

									if (!object.isNull("prevNo")) {
										cdd.setPmNo(object.getInt("prevNo"));
									}
									if (!object.isNull("nextNo")) {
										cdd.setNmNo(object.getInt("nextNo"));
									}

									mh = cdd;

									if (mImageUrl != null) {
										mImageUrl.clear();
									}
									if (localUrlArr != null) {
										localUrlArr.clear();
									}

									String[] urls = mh.getmUrl().split(
											Utils.split);

									String[] localUrls = mh.getLocalUrl() == null ? new String[] {}
											: mh.getLocalUrl().split(
													Utils.split);

									if (!(mImageUrl != null)) {
										mImageUrl = new ArrayList<String>();
									}

									for (int i = 0; i < urls.length; i++) {
										mImageUrl.add(urls[i]);
										if (i < localUrls.length) {
											localUrlArr.add(localUrls[i]);
										} else {
											localUrlArr
													.add("/firstTurn/ic_empty.png");
										}
									}

									if (mImageUrl.size() > 0) {
										mImageUrl
												.add(UrlConstant.Ip1
														+ UrlConstant.Port1
														+ "/firstTurn/manhua_nomore.jpg");
										mImageUrl
												.add(0,
														UrlConstant.Ip1
																+ UrlConstant.Port1
																+ "/firstTurn/manhua_nomore.jpg");
										localUrlArr
												.add("/firstTurn/manhua_nomore.jpg");
										localUrlArr.add(0,
												"/firstTurn/manhua_nomore.jpg");
									}

									if (tv_operate_title != null) {

										String str = mh.getmTitle() + " "
												+ mh.getmName();
										str = str.length() > 10 ? str
												.substring(0, 10) : str;
										tv_operate_title.setText(str);

									}
									if (tv_book_operateindex != null) {

										String show = 1 + "/"
												+ (mImageUrl.size() - 2);
										tv_book_operateindex.setText(show);

									}

									if (sb_book_adjustbook != null) {
										if (mImageUrl.size() > 2) {
											sb_book_adjustbook.setMax(mImageUrl
													.size() - 2);
											sb_book_adjustbook.setProgress(1);
										}
									}

									strList.clear();

									if ("desc".equals(order)) {
										Utils.rindex = mImageUrl.size() - 2;
									} else {
										Utils.rindex = cdd.getrIndex();
									}

									poolManager.stop();
									new RecycleBitmapInLayout(true)
											.recycle(ch_read_view);
									ch_read_view.setImageResources(mImageUrl,
											mAdCycleViewListener,
											ReadingActivity.this);

								}
							}

						} catch (JSONException e) {
							e.printStackTrace();
							Toast.makeText(ReadingActivity.this, "没有更多了！",
									Toast.LENGTH_SHORT).show();
							strList.clear();
							stop();
						} finally {
							Utils.endnet();
						}
					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Utils.endnet();
					}
				}) {
			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				Map<String, String> map = new HashMap<String, String>();
				map.put("Content-Type", "application/json; charset=utf-8");
				return map;
			}
		};

		joObjectRequest.setRetryPolicy(new DefaultRetryPolicy(6000,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		NetUtil.rqueue.add(joObjectRequest);
	}

	/**
	 * 初始化请求参数
	 */
	private void initParam() {
		try {
			param2.put("pageSize", "100");
			param2.put("currentPage", "1");
			param2.put("orderBy", "");
			param2.put("object",
					order + "@@" + mh.getmId() + "@@" + mh.getmNo());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	JSONObject param3 = new JSONObject();

	/**
	 * 更新阅读的人气
	 */
	private void updateHits() {
		try {
			param3.put("pageSize", "100");
			param3.put("currentPage", "1");
			param3.put("orderBy", "");
			param3.put("object", mh.getmId());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		JsonObjectRequest joObjectRequest = new JsonObjectRequest(Method.POST,
				UrlConstant.UrlupdateHits, param3, new Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						// Log.d("tag", "response  "+ response);
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

		joObjectRequest.setRetryPolicy(new DefaultRetryPolicy(6000,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		NetUtil.rqueue.add(joObjectRequest);

	}

	ThreadPoolManager poolManager = new ThreadPoolManager(
			ThreadPoolManager.TYPE_FIFO, 2);
	int loadsize = 1;

	@Override
	public void updateIndex() {
		if(isadjust&&layout.getChildCount()>1){
			layout.removeViewAt(1);
			isadjust=false;
		}
		
		/**
		 * 设置文字进度信息
		 */
		String show = null;
		if (ch_read_view.mImageIndex <= ch_read_view.mImageViews.length - 2) {
			show = ch_read_view.mImageIndex + "/"
					+ (ch_read_view.mImageViews.length - 2);
		}
		tv_showindex.setText(show);

		if (tv_book_operateindex != null) {
			tv_book_operateindex.setText(show);
		}

		/**
		 * 设置进度条
		 */
		if (sb_book_adjustbook != null) {
			sb_book_adjustbook.setMax(ch_read_view.mImageViews.length - 2);
			sb_book_adjustbook.setProgress(ch_read_view.mImageIndex);
		}

		if (Utils.updateCollectInfo != null) {
			Utils.updateCollectInfo.updateReads();
		}

	}

	/**
	 * 获取所有的章节信息
	 */
	private Handler hander = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 5) {
				strList.clear();
				ReadingActivity.this.stop();
			} else {
				getAdapterData();
			}
		}
	};

	JSONObject paramadapter = new JSONObject();

	private List<ComicsDetail> cds = new ArrayList<ComicsDetail>();

	/**
	 * 获取数据
	 */
	private void getAdapterData() {
		if ("0".equals(comefrom)) {
			/**
			 * 本地路径
			 */
			String dirpath = SDUtil.getSecondExterPath() + "/manyun/"
					+ mh.getmTitle() + "/temp.txt";
			String content = null;
			try {
				content = FileUtil.getStringFormFile(dirpath);
			} catch (Exception e1) {
				comefrom = "1";
				getAdapterData();
			}
			try {
				/*
				 * 判断本地数据有没有，没有就继续取网络数据
				 */
				if (content != null) {
					JSONArray ja = new JSONArray(content);
					initAdapterInfo(ja);
				} else {
					comefrom = "1";
					getAdapterData();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			initParam2();
			JsonObjectRequest joObjectRequest = new JsonObjectRequest(
					Method.POST, UrlConstant.UrlComicsDetailPage, param,
					new Listener<JSONObject>() {

						@Override
						public void onResponse(JSONObject response) {
							try {

								JSONArray array = response
										.getJSONObject("object")
										.getJSONObject("page")
										.getJSONArray("data");

								initAdapterInfo(array);

							} catch (JSONException e) {
								e.printStackTrace();
							}
						}

					}, new ErrorListener() {

						@Override
						public void onErrorResponse(VolleyError error) {
							Utils.endnet();
						}
					}) {
				@Override
				public Map<String, String> getHeaders() throws AuthFailureError {
					Map<String, String> map = new HashMap<String, String>();
					map.put("Content-Type", "application/json; charset=utf-8");
					return map;
				}
			};

			joObjectRequest.setRetryPolicy(new DefaultRetryPolicy(6000,
					DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

			NetUtil.rqueue.add(joObjectRequest);
		}
	}

	/**
	 * 获取章节数数组
	 * 
	 * @param array
	 * @throws JSONException
	 */
	private void initAdapterInfo(JSONArray array) throws JSONException {
		cds.clear();
		ComicsDetail temp = null;
		for (int i = 0; i < array.length(); i++) {
			JSONObject joObject = array.getJSONObject(i);
			ComicsDetail cd = new ComicsDetail();
			if (mh != null && mh.getmPic() != null) {
				cd.setmPic(mh.getmPic());
				cd.setmDirector(mh.getmDirector());
				cd.setmLianzai(mh.getmLianzai());
			}
			cd.setmId(joObject.getInt("mId"));
			cd.setmQid(joObject.getInt("mQid"));
			cd.setmUrl(joObject.getString("mUrl"));
			cd.setmName(joObject.getString("mName"));
			cd.setmNo(joObject.getInt("mNo"));
			cd.setGradescore(joObject.getDouble("gradeScore"));
			cd.setmTitle(joObject.getString("mTitle"));
			cd.setPartSize(joObject.getString("partsize"));
			cd.setTotalPage(joObject.getInt("totalpage"));
			cd.setLocalUrl(joObject.getString("localUrl"));
			cd.setPort(joObject.getString("port"));
			cd.setCreateTime(DateUtil.Date2String(new Date(Long
					.parseLong(joObject.getString("mDate")))));
			if (i == 0) {
				cd.setMinNo(joObject.getInt("minNo"));
				cd.setMaxNo(joObject.getInt("maxNo"));
				temp = cd;
			}
			cd.setMinNo(temp.getMinNo());
			cd.setMaxNo(temp.getMaxNo());
			cds.add(cd);
		}
	}

	/**
	 * 初始化请求参数
	 */
	private void initParam2() {
		try {
			param.put("pageSize", "1000");
			param.put("currentPage", currentpage++);
			param.put("orderBy", "");
			param.put("object", order + "@@" + mh.getmId());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private int currentpage = 1;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Utils.map.clear();
		Utils.rindex = ch_read_view.mImageIndex;

		if (mImageUrl != null) {
			mImageUrl.clear();
		}
		if (localUrlArr != null) {
			localUrlArr.clear();
		}

		String[] urls = mh.getmUrl().split(Utils.split);

		String[] localUrls = mh.getLocalUrl() == null ? new String[] {} : mh
				.getLocalUrl().split(Utils.split);

		for (int i = 0; i < urls.length; i++) {
			mImageUrl.add(urls[i]);
			if (i < localUrls.length) {
				localUrlArr.add(localUrls[i]);
			} else {
				localUrlArr.add("/firstTurn/ic_empty.png");
			}
		}

		if (mImageUrl.size() > 0) {
			mImageUrl.add(UrlConstant.Ip1 + UrlConstant.Port1
					+ "/firstTurn/manhua_nomore.jpg");
			mImageUrl.add(0, UrlConstant.Ip1 + UrlConstant.Port1
					+ "/firstTurn/manhua_nomore.jpg");
			localUrlArr.add("/firstTurn/manhua_nomore.jpg");
			localUrlArr.add(0, "/firstTurn/manhua_nomore.jpg");
		}

		new RecycleBitmapInLayout(true).recycle(ch_read_view);

		if ("0".equals(comefrom)) {
			ch_read_view.setImageResources(localUrlArr, mAdCycleViewListener,
					this);
		} else {
			ch_read_view.setImageResources(mImageUrl, mAdCycleViewListener,
					this);
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// 保存横屏数据
		Parcelable savedState = ch_read_view.mAdvPager.onSaveInstanceState();
		outState.putParcelable("adview", savedState);

		outState.putSerializable("saveMh", mh);
		outState.putSerializable("comefrom", comefrom);

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mh = (ComicsDetail) savedInstanceState.getSerializable("saveMh");
		comefrom = (String) savedInstanceState.getSerializable("comefrom");
		Log.d("tag", "onRestoreInstanceState ");
		// 横屏数据
		SavedState savedState = savedInstanceState.getParcelable("adview");
		ch_read_view.mAdvPager.onRestoreInstanceState(savedState);

	}

	private Handler handler1 = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			final LoadInfo li = (LoadInfo) msg.obj;

			DisplayImageOptions oio;
			if (ReadingActivity.this.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
				oio = Utils.options2;
			} else {
				oio = Utils.optionsh;
			}

			final String url;

			if (li.getLocalimageURL() != null
					&& li.getLocalimageURL().length() > 0) {
				url = li.getLocalimageURL();
			} else {
				url = li.getImageURL();
			}

			ImageAware imageAware = new ImageViewAware(li.imageView, false);
			ImageLoader.getInstance().displayImage(url, imageAware, oio,
					new ImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							li.map.put(li.imageURL, false);
						}

						@Override
						public void onLoadingFailed(String imageUri, View view,
								FailReason failReason) {
							li.map.put(li.imageURL, true);
							if (strList.contains(li.imageURL)) {
								strList.remove(li.imageURL);
							}
							if (strList.size() == 0) {
								stop();
							}
							refresh(li.imageView, imageUri, view, li.map);
						}

						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							li.map.put(li.imageURL, true);
							if (strList.contains(li.imageURL)) {
								strList.remove(li.imageURL);
							}
							if (strList.size() == 0) {
								stop();
							}
						}

						@Override
						public void onLoadingCancelled(String imageUri,
								View view) {
							li.map.put(li.imageURL, true);
							if (strList.contains(li.imageURL)) {
								strList.remove(li.imageURL);
							}
							if (strList.size() == 0) {
								stop();
							}
							view.destroyDrawingCache();
						}
					}, new ImageLoadingProgressListener() {

						@Override
						public void onProgressUpdate(String imageUri,
								View view, int current, int total) {
							NumberFormat numberFormat = NumberFormat
									.getInstance();// 设置精确到小数点后2位
							numberFormat.setMaximumFractionDigits(0);
							String result = numberFormat.format((float) current
									/ (float) total * 100);
						}
					});// 此处本人使用了ImageLoader对图片进行加装！

			li.localimageURL = "";

		}
	};

	/**
	 * 重新加载 加载失败的图片
	 * 
	 * @param imageView
	 * @param imageUri
	 * @param view
	 */
	private void refresh(final ImageView imageView, String imageUri, View view,
			final Map<String, Boolean> map) {
		Log.d("tag", "imageUri  " + imageUri);
		final LinearLayout vg = (LinearLayout) view.getParent();
		final PhotoView cv = (PhotoView) vg.getChildAt(0);
		cv.setVisibility(View.GONE);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.CENTER;
		final LinearLayout rl = new LinearLayout(this);
		rl.setLayoutParams(params);
		rl.setGravity(Gravity.CENTER);
		rl.setBackgroundColor(Color.WHITE);
		rl.setOrientation(LinearLayout.VERTICAL);

		if (ReadingActivity.this.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
			rl.setBackgroundResource(R.drawable.load_error);
		} else {
			rl.setBackgroundResource(R.drawable.load_errorh);
		}

		ImageView iv = new ImageView(this);
		iv.setBackgroundResource(R.drawable.refresh);
		LinearLayout.LayoutParams imgparams = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;
		imgparams.topMargin = 500;
		iv.setLayoutParams(imgparams);
		rl.addView(iv);

		TextView v = new TextView(ReadingActivity.this);
		v.setText("点击重新加载！");
		v.setGravity(Gravity.CENTER);
		rl.addView(v);

		vg.addView(rl);

		final String url = imageUri;
		iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v1) {
				DisplayImageOptions opt;
				if (ReadingActivity.this.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
					opt = Utils.options2;
				} else {
					opt = Utils.optionsh;
				}
				cv.setVisibility(View.VISIBLE);
				start(url);
				ImageLoader.getInstance().displayImage(url, imageView, opt,
						new ImageLoadingListener() {

							@Override
							public void onLoadingStarted(String imageUri,
									View view) {
								map.put(url, false);
							}

							@Override
							public void onLoadingFailed(String imageUri,
									View view, FailReason failReason) {
								refresh(imageView, url, view, map);
								map.put(url, true);
								if (strList.contains(url)) {
									strList.remove(url);
								}
								if (strList.size() == 0) {
									stop();
								}
							}

							@Override
							public void onLoadingComplete(String imageUri,
									View view, Bitmap loadedImage) {
								map.put(url, true);
								if (strList.contains(url)) {
									strList.remove(url);
								}
								if (strList.size() == 0) {
									stop();
								}
							}

							@Override
							public void onLoadingCancelled(String imageUri,
									View view) {
								map.put(url, true);
								if (strList.contains(url)) {
									strList.remove(url);
								}
								if (strList.size() == 0) {
									stop();
								}
							}

						});
				vg.removeView(rl);
			}
		});
	}

	/**
	 * 图片加载帮助类
	 * 
	 * @author DaxstUz 2416738717 2015年8月19日
	 *
	 */
	class LoadInfo {
		public String imageURL;
		public String localimageURL;
		public ImageView imageView;
		public Map<String, Boolean> map;

		public String getImageURL() {
			return imageURL;
		}

		public void setImageURL(String imageURL) {
			this.imageURL = imageURL;
		}

		public String getLocalimageURL() {
			return localimageURL;
		}

		public void setLocalimageURL(String localimageURL) {
			this.localimageURL = localimageURL;
		}

		public ImageView getImageView() {
			return imageView;
		}

		public void setImageView(ImageView imageView) {
			this.imageView = imageView;
		}

		public Map<String, Boolean> getMap() {
			return map;
		}

		public void setMap(Map<String, Boolean> map) {
			this.map = map;
		}

	}

	@Override
	protected void onDestroy() {
		if (Utils.um != null) {
			Utils.um.update(mh.getmName());
		}
		unregisterReceiver(batteryReceiver);
		onTrimMemory(80);
		// 回收图片
		loadPgTh.setRunning(false);// 关闭涨水加载线程
		loadPgTh.setWaiting(false);
		poolManager.stop();
		System.gc();

		/* 退出前，保存当前看的下标到本地 */
		synchronized (ACCESSIBILITY_SERVICE) {
			DBManager manager = new DBManager(ReadingActivity.this,
					DBUtil.ReadName, null, DBUtil.Code);
			mh.setrIndex(ch_read_view.mImageIndex);
			manager.addOrUpdateComicDetail(mh);
			manager.closeDB();

			if (Utils.updateCollectInfo != null) {
				Utils.updateCollectInfo.updateReads();
			}
		}

		if (openid.length() > 0) {
			addToServer();
		}
		Utils.map.clear();
		super.onDestroy();

	}

	/**
	 * 将阅读的漫画数据进行处理
	 */
	private void addToServer() {
		/**
		 * 需要插入的记录
		 */
		Map<String, String> obj = new HashMap<String, String>();
		obj.put("mId", mh.getmId() + "");
		obj.put("userId", openid);
		obj.put("chapterId", mh.getmNo() + "");
		obj.put("pageNum", ch_read_view.mImageIndex + "");
		JSONObject jobObject = new JSONObject();
		try {
			jobObject.put("object", new JSONObject(obj));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JsonObjectRequest jsonrRequest = new JsonObjectRequest(Method.POST,
				UrlConstant.addRedToServer, jobObject,
				new Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						// TODO Auto-generated method stub
					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						// TODO Auto-generated method stub

					}
				}) {

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				Map<String, String> map1 = new HashMap<String, String>();
				map1.put("Content-Type", "application/json; charset=utf-8");
				return map1;
			}
		};

		jsonrRequest.setRetryPolicy(new DefaultRetryPolicy(6000,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		NetUtil.rqueue.add(jsonrRequest);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		/**
		 * 通过音量键翻页
		 */
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			ch_read_view.pre();
			return true;
		}

		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			ch_read_view.next();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}

	}

	public FrameLayout getLayout() {
		return layout;
	}

	public void takeScreenShot(View view) {
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		Bitmap bitmap = view.getDrawingCache();
		if (bitmap != null) {
			saveImageToGallery(this, bitmap);
		}
	}

	/**
	 * 保存图片到系统图库
	 * 
	 * @param context
	 * @param bmp
	 */
	public void saveImageToGallery(Context context, Bitmap bmp) {
		// 首先保存图片
		String fileName = System.currentTimeMillis() + ".jpg";
		String realPath = Environment.getExternalStorageDirectory()
				+ "/DCIM/Camera/" + fileName;
		FileOutputStream fos = null;
		try {

			File storeFile = new File(Environment.getExternalStorageDirectory()
					+ "/DCIM/Camera");
			if (!storeFile.exists()) {
				// 若不存在，创建目录，可以在应用启动的时候创建
				storeFile.mkdirs();
			}
			fos = new FileOutputStream(realPath);
			if (null != fos) {
				bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

		Uri uri = Uri.parse(realPath);
		// 最后通知图库更新
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
				uri));
		// 由于上面的广播在有些机型会失效，所以加上下面的
		MediaScannerConnection.scanFile(context, new String[] { realPath },
				null, new MediaScannerConnection.OnScanCompletedListener() {
					public void onScanCompleted(String path, Uri uri) {
					}
				});
	}

	/**
	 * 工具类 释放布局中所有Imageview组件占用的图片，可设置是否释放背景图 用于退出时释放资源，调用完成后，请不要刷新界面
	 * 
	 * @author liubing
	 *
	 */
	public class RecycleBitmapInLayout {
		private static final String TAG = "RecycleBitmapInLayout";
		/* 是否释放背景图 true:释放;false:不释放 */
		private boolean flagWithBackgroud = false;

		/**
		 * 
		 * @param flagWithBackgroud
		 *            是否同时释放背景图
		 */
		public RecycleBitmapInLayout(boolean flagWithBackgroud) {
			this.flagWithBackgroud = flagWithBackgroud;
		}

		/**
		 * 释放Imageview占用的图片资源 用于退出时释放资源，调用完成后，请不要刷新界面
		 * 
		 * @param layout
		 *            需要释放图片的布局 *
		 */
		public void recycle(ViewGroup layout) {

			for (int i = 0; i < layout.getChildCount(); i++) {
				// 获得该布局的所有子布局
				View subView = layout.getChildAt(i);
				// 判断子布局属性，如果还是ViewGroup类型，递归回收
				if (subView instanceof ViewGroup) {
					// 递归回收
					recycle((ViewGroup) subView);
				} else {
					// 是Imageview的子例
					if (subView instanceof ImageView) {
						// 回收占用的Bitmap
						recycleImageViewBitMap((ImageView) subView);
						// 如果flagWithBackgroud为true,则同时回收背景图
						if (flagWithBackgroud) {
							recycleBackgroundBitMap((ImageView) subView);
						}
					}
				}
			}
		}

		private void recycleBackgroundBitMap(ImageView view) {
			if (view != null) {
				BitmapDrawable bd = (BitmapDrawable) view.getBackground();
				rceycleBitmapDrawable(bd);
			}
		}

		private void recycleImageViewBitMap(ImageView imageView) {
			if (imageView != null) {
				BitmapDrawable bd = (BitmapDrawable) imageView.getDrawable();
				rceycleBitmapDrawable(bd);
			}
		}

		private void rceycleBitmapDrawable(BitmapDrawable bd) {
			if (bd != null) {
				Bitmap bitmap = bd.getBitmap();
				rceycleBitmap(bitmap);
			}
			bd = null;
		}

		private void rceycleBitmap(Bitmap bitmap) {
			if (bitmap != null && !bitmap.isRecycled()) {
//				Log.e(TAG, "rceycleBitmap");
				bitmap.recycle();
				bitmap = null;
			}
		}

	}

	/**
	 * 下载
	 * 
	 * @author DaxstUz 2416738717 2015年8月6日
	 *
	 */
	class SaveImageTask extends AsyncTask<Void, Integer, Integer> {
		@Override
		protected Integer doInBackground(Void... params) {
			if (saveView != null) {
				takeScreenShot(saveView);
			}
			return null;
		}
	}

	/**
	 * 获取系统电量
	 */
	private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int level = intent.getIntExtra("level", 0);
			// level加%就是当前电量了
			tv_title.setText("电量:" + level + "%");
			tv_time.setText(DateUtil.getTime());

			if (NetReceiver.isConnected(ReadingActivity.this) == NetReceiver.NetState.NET_NO) {
				strList.clear();
				stop();
			}
		}
	};

	/**
	 * 弹出操作界面
	 * 
	 * @param e
	 */
	public void readOperate(MotionEvent e) {
		if ((System.currentTimeMillis() - excuteTime) > 500) {
			excuteTime = System.currentTimeMillis();
			if (getIsRight(e)) {
				if (!iscycle) {
					if(ch_read_view!=null){
						ch_read_view.next();
					}
				}
			} else if (getIsCenter(e)) {
				if (layout.getChildCount() > 1) {
					layout.removeViewAt(1);
					flag = false;
				} else {
					flag = true;
				}

				if (flag) {
					View vv = inflater.inflate(R.layout.activity_operate, null);
					FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
							LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
					lp.gravity = Gravity.BOTTOM;
					vv.getBackground().setAlpha(0);

					tv_book_operateindex = (TextView) vv
							.findViewById(R.id.tv_book_operateindex);

					String show = ch_read_view.mImageIndex + "/"
							+ (ch_read_view.mImageViews.length - 2);
					tv_book_operateindex.setText(show);
					sb_book_adjustbook = (SeekBar) vv
							.findViewById(R.id.sb_book_adjustbook);
					sb_book_adjustbook
							.setMax(ch_read_view.mImageViews.length - 2);
					sb_book_adjustbook.setProgress(ch_read_view.mImageIndex);
					sb_book_adjustbook
							.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

								@Override
								public void onStopTrackingTouch(SeekBar seekBar) {

									/* 看之前清理下缓存 */
									ImageLoader.getInstance()
											.clearMemoryCache();
									ImageLoader.getInstance().clearDiscCache();
									System.gc();

									ch_read_view.mImageIndex = ch_read_view.mImageIndex + 1;
									ch_read_view.pre();
								}

								@Override
								public void onStartTrackingTouch(SeekBar seekBar) {
								}

								@Override
								public void onProgressChanged(SeekBar seekBar,
										int progress, boolean fromUser) {
									if (progress == 0) {
										progress = 1;
									}
									ch_read_view.mImageIndex = progress;
									String show = ch_read_view.mImageIndex
											+ "/"
											+ (ch_read_view.mImageViews.length - 2);
									tv_book_operateindex.setText(show);
								}
							});

					tv_operate_title = (TextView) vv
							.findViewById(R.id.tv_operate_title);
					if (mh.getmTitle() != null) {
						String mht = mh.getmTitle().length() > 6 ? mh
								.getmTitle().substring(0, 6) : mh.getmTitle();
						mht = mht + " " + mh.getmName();
						mht = mht.length() > 10 ? mht.substring(0, 10) : mht;
						tv_operate_title.setText(mht);
					}
					layout.addView(vv, 1, lp);
					flag = false;
				}
			} else if (getIsLeft(e)) {
				if (!iscycle) {
					ch_read_view.pre();
				}
			}
		}
	}

	// 音量变量
	/**
	 * 设置铃声
	 * 
	 * @param flag
	 */
	private void setRing(boolean flag) {
		AudioManager am = (AudioManager) this
				.getSystemService(Context.AUDIO_SERVICE);
		if (flag) {
//			volume = am.getStreamVolume(AudioManager.STREAM_RING);
//			am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			setVolumeControlStream(AudioManager.STREAM_NOTIFICATION);
			setVolumeControlStream(AudioManager.STREAM_ALARM);
			setVolumeControlStream(AudioManager.STREAM_RING);
			setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
		} else {
//			am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
//			am.setRouting(AudioManager.RINGER_MODE_NORMAL, volume, 0);
		}

	}

	/**
	 * 展示图片
	 * 
	 * @param imageURL
	 * @param imageView
	 * @param map
	 * @param oio
	 */
	private void showBit(final String imageURL, final PhotoView imageView,
			final Map<String, Boolean> map, final DisplayImageOptions oio) {

		ImageLoader.getInstance().displayImage(imageURL, imageView, oio,
				new ImageLoadingListener() {
					@Override
					public void onLoadingStarted(String imageUri, View view) {
						map.put(imageURL, false);
					}

					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
						 map.put(imageURL, true);
						if (strList.contains(imageURL)) {
							strList.remove(imageURL);
						}
						String localUrl = null;
						int i = 0;
						if (mImageUrl != null) {
							for (String st : mImageUrl) {
								if (st.equals(imageURL)) {
									break;
								}
								i++;
							}
						}

						if (localUrlArr != null && localUrlArr.size() > 0) {
							if (i < localUrlArr.size()) {
								localUrl = localUrlArr.get(i);
								localUrl = UrlConstant.Ip1 + ":" + mh.getPort()
										+ "/" + localUrl;
							}
						}

						strList.add(localUrl);
						showBit1(localUrl, imageView, map, oio);
					}

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						map.put(imageURL, true);
						if (strList.contains(imageURL)) {
							strList.remove(imageURL);
						}
						if (strList.size() == 0) {
							stop();
						}
					}

					@Override
					public void onLoadingCancelled(String imageUri, View view) {
						map.put(imageURL, true);
						if (strList.contains(imageURL)) {
							strList.remove(imageURL);
						}
						if (strList.size() == 0) {
							stop();
						}
						view.destroyDrawingCache();
					}
				});
	}

	/**
	 * 展示图片
	 * 
	 * @param imageURL
	 * @param imageView
	 * @param map
	 * @param oio
	 */
	private void showBit1(final String imageURL, final PhotoView imageView,
			final Map<String, Boolean> map, final DisplayImageOptions oio) {

		ImageLoader.getInstance().displayImage(imageURL, imageView, oio,
				new ImageLoadingListener() {
					@Override
					public void onLoadingStarted(String imageUri, View view) {
						map.put(imageURL, false);
					}

					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
						map.put(imageURL, true);
						if (strList.contains(imageURL)) {
							strList.remove(imageURL);
						}
						if (strList.size() == 0) {
							stop();
						}
						refresh(imageView, imageURL, view, map);
					}

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						map.put(imageURL, true);
						if (strList.contains(imageURL)) {
							strList.remove(imageURL);
						}
						if (strList.size() == 0) {
							stop();
						}
					}

					@Override
					public void onLoadingCancelled(String imageUri, View view) {
						map.put(imageURL, true);
						if (strList.contains(imageURL)) {
							strList.remove(imageURL);
						}
						if (strList.size() == 0) {
							stop();
						}
						view.destroyDrawingCache();
					}
				});
	}

}
