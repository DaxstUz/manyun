package com.ch.mhy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ch.mhy.activity.msg.UpdateComicActivity;
import com.ch.mhy.db.DBManager;
import com.ch.mhy.db.DBUtil;
import com.ch.mhy.entity.ComicsDetail;
import com.ch.mhy.fragment.BookFragment;
import com.ch.mhy.fragment.CatgFragment;
import com.ch.mhy.fragment.MyFragment;
import com.ch.mhy.fragment.SearchFragment;
import com.ch.mhy.net.NetReceiver;
import com.ch.mhy.net.NetReceiver.NetState;
import com.ch.mhy.service.MessageService;
import com.ch.mhy.util.DateUtil;
import com.ch.mhy.util.NetUtil;
import com.ch.mhy.util.TelPhoneInfo;
import com.ch.mhy.util.UrlConstant;
//import com.qihoo.gamead.QihooAdAgent;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

/**
 * 主界面
 *
 * @author DaxstUz 2416738717 2015年5月11日
 */
public class MainActivity extends FragmentActivity implements
		NetReceiver.NetEventHandle {

	public static MainActivity instance;

	private FragmentTabHost mTabHost;

	private LayoutInflater mLayoutInflater;

	// private Context ctx = null;

	/**
	 * 用来存用户本地信息
	 */
	private SharedPreferences sp;
	private String openid;
	private ComicsDetail maxcd;
	private ComicsDetail maxcdc;

	private Class mFragmentArray[] = { BookFragment.class, CatgFragment.class,
			SearchFragment.class, MyFragment.class };
	/**
	 * 标签卡图标
	 */
	private int mImageArray[] = { R.drawable.tab_book_btn,
			R.drawable.tab_cate_btn, R.drawable.tab_search_btn,
			R.drawable.tab_my_btn };

	/**
	 * 标签名字
	 */
	private String mTextArray[] = { "书城", "分类", "搜索", "我的" };

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sp = this.getSharedPreferences("userinfo", Context.MODE_PRIVATE);
		openid = sp.getString("openid", "");
		instance = this;

		NetReceiver.ehList.add(this);

		final View view = View.inflate(this, R.layout.activity_main, null);
		setContentView(view);

		final FrameLayout fl = (FrameLayout) findViewById(R.id.fl_main);

		initView();

		final Intent intent = this.getIntent();

		// 动画
		AlphaAnimation aa = new AlphaAnimation(2.0f, 0.1f);
		aa.setDuration(3000);
		view.startAnimation(aa);
		aa.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {
				fl.removeViewAt(1);

				String flag = intent.getStringExtra("fromNotification");
				String updateCnt = intent.getStringExtra("updateCnt");
				if (flag != null) {// 从通知栏进入时
					if ("0".equals(flag)) {

					} else if ("1".equals(flag)) {
						showNewComic();
						TextView newUpdate = (TextView) MainActivity.this
								.findViewById(R.id.newUpdate);
						newUpdate.setText(updateCnt);
					}
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}
		});

		updateLoginTime();

		// 版本检测
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				UmengUpdateAgent.update(MainActivity.this);
			}
		}, 6000);

		if (openid.length() > 0) {
			getMaxReadTime();
			updateUserInfo();
		}
		
		Intent msgIntent = new Intent(this, MessageService.class);
        this.startService(msgIntent);
	}

	/**
	 * 获取最近阅读的漫画
	 */
	private void getMaxReadTime() {
		DBManager manager = new DBManager(this, DBUtil.ReadName, null,
				DBUtil.Code);
		this.maxcd = manager.queryMaxtReadTime();
		manager.closeDB();
		manager = new DBManager(this, DBUtil.CollectName, null, DBUtil.Code);
		this.maxcdc = manager.queryMaxtReadTime();
		manager.closeDB();

	}

	/**
	 * 初始化
	 */
	private void initView() {
		mLayoutInflater = LayoutInflater.from(this);

		// �ҵ�TabHost
		mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		// �õ�fragment�ĸ���
		int count = mFragmentArray.length;
		for (int i = 0; i < count; i++) {
			TabSpec tabSpec = mTabHost.newTabSpec(mTextArray[i]).setIndicator(
					getTabItemView(i));
			mTabHost.addTab(tabSpec, mFragmentArray[i], null);
			mTabHost.getTabWidget().getChildAt(i)
					.setBackgroundResource(R.drawable.selector_tab_background);
		}

		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				if (tabId.equals(mTextArray[3])) {// 点我的的时候，清除更新提示值
					TextView newUpdate = (TextView) MainActivity.this
							.findViewById(R.id.newUpdate);
					newUpdate.setText("");
				}
			}

		});
	}

	private View getTabItemView(int index) {
		View view = mLayoutInflater.inflate(R.layout.tab_item_view, null);
		ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
		imageView.setImageResource(mImageArray[index]);
		TextView textView = (TextView) view.findViewById(R.id.textview);
		textView.setText(mTextArray[index]);

		return view;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			ExitApp();
		}
		return super.onKeyDown(keyCode, event);
	}

	private long exitTime = 0;

	/**
	 * 双击退出
	 */
	public void ExitApp() {
		if ((System.currentTimeMillis() - exitTime) > 2000) {
			Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
			exitTime = System.currentTimeMillis();
		} else {
			MainActivity.this.finish();
		}
	}

	/**
	 * 监听对话框里面的button点击事件
	 */
	DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
				finish();
				break;
			case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		NetReceiver.ehList.remove(this);
		MobclickAgent.onPageEnd("MainActivity"); // 统计页面
		MobclickAgent.onPause(this);
	}

	@Override
	public void netState(NetReceiver.NetState netCode) {
		switch (netCode) {
		case NET_NO:
			Toast.makeText(MainActivity.this, "没有网络连接", Toast.LENGTH_SHORT)
					.show();
			break;
		case NET_2G:
			Toast.makeText(MainActivity.this, "当前是2g网络", Toast.LENGTH_SHORT)
					.show();
			break;
		case NET_3G:
			Toast.makeText(MainActivity.this, "当前是3g网络", Toast.LENGTH_SHORT)
					.show();
			break;
		case NET_4G:
			Toast.makeText(MainActivity.this, "当前是4g网络", Toast.LENGTH_SHORT)
					.show();
			break;
		case NET_WIFI:
			Toast.makeText(MainActivity.this, "当前是WIFI网络", Toast.LENGTH_SHORT)
					.show();
			break;
		case NET_UNKNOWN:
			Toast.makeText(MainActivity.this, "未知网络", Toast.LENGTH_SHORT)
					.show();
			break;
		default:
			Toast.makeText(MainActivity.this, "不知道什么情况~>_<~",
					Toast.LENGTH_SHORT).show();
		}
	}

	public void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("MainActivity"); // 统计页面
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Intent msgIntent = new Intent(this, MessageService.class);
        this.stopService(msgIntent);
		System.exit(0);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		String flag = intent.getStringExtra("fromNotification");
		String updateCnt = intent.getStringExtra("updateCnt");
		if (flag != null) {// 从通知栏进入时
			if ("0".equals(flag)) {

			} else if ("1".equals(flag)) {
				showNewComic();
				TextView newUpdate = (TextView) MainActivity.this
						.findViewById(R.id.newUpdate);
				newUpdate.setText(updateCnt);
			}
			updateLoginTime();
		}
	}

	/**
	 * 公用方法，设置漫画更新数
	 * 
	 * @param intent
	 */
	public void setNewUpdate(Intent intent) {
		String flag = intent.getStringExtra("fromNotification");
		String updateCnt = intent.getStringExtra("updateCnt");
		if (flag != null) {// 从通知栏进入时
			if ("0".equals(flag)) {

			} else if ("1".equals(flag)) {
				showNewComic();
				TextView newUpdate = (TextView) MainActivity.this
						.findViewById(R.id.newUpdate);
				newUpdate.setText(updateCnt);
				updateLoginTime();
			}
		}
	}

	/**
	 * 更新用户开启APP时间
	 */
	private void updateLoginTime() {
		NetState state = NetReceiver.isConnected(this);
		if (!NetState.NET_NO.equals(state)) {// 有网情况下
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
			JsonObjectRequest joObjectRequest = new JsonObjectRequest(
					Method.POST, UrlConstant.UrlpushUserUseMessage, param,
					new Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							/*
							 * try { if(response!=null){ JSONObject joObject =
							 * response.getJSONObject("object"); if(joObject !=
							 * null){ boolean result =
							 * joObject.getBoolean("result"); if(!result){
							 * //Toast.makeText(MainActivity.this, "",
							 * Toast.LENGTH_SHORT); } } } } catch (JSONException
							 * e) { e.printStackTrace(); }
							 */
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

	/**
	 * 显示新更新的漫画列表
	 */
	private void showNewComic() {
		Intent intent = new Intent();
		intent.setClass(this, UpdateComicActivity.class);
		this.startActivity(intent);
	}

	/**
	 * 同步用户的最近阅读、收藏信息到本地
	 */
	private void updateUserInfo() {
		NetState state = NetReceiver.isConnected(this);
		if (!NetState.NET_NO.equals(state)) {// 有网情况下
			JSONObject param = new JSONObject();
			try {
				param.put("pageSize", 1);
				param.put("currentPage", 1);
				param.put("orderBy", "");
				Map<String, String> objpar = new HashMap<String, String>();
				objpar.put("date", maxcd.getReadTime());
				objpar.put("userId", openid);
				param.put("object", new JSONObject(objpar));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			JSONObject param2 = new JSONObject();
			try {
				param2.put("pageSize", 1);
				param2.put("currentPage", 1);
				param2.put("orderBy", "");
				Map<String, String> objpar = new HashMap<String, String>();
				objpar.put("date", maxcdc.getReadTime());
				objpar.put("userId", openid);
				param2.put("object", new JSONObject(objpar));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			JsonObjectRequest joObjectRequest = new JsonObjectRequest(
					Method.POST, UrlConstant.getRedLogs, param,
					new Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							// Log.d("tag", "response  "+response);
							try {
								if (response.getJSONArray("object") != null) {
									JSONArray array = response
											.getJSONArray("object");
									List<ComicsDetail> cds = new ArrayList<ComicsDetail>();
									for (int i = 0; i < array.length(); i++) {
										JSONObject joObject = array
												.getJSONObject(i);
										ComicsDetail cd = new ComicsDetail();
										cd.setmId(joObject.getInt("mId"));
										cd.setmQid(joObject.getInt("mQid"));
										cd.setmUrl(joObject.getString("mUrl"));
										cd.setmName(joObject.getString("mName"));
										cd.setmContent(joObject
												.getString("mContent"));
										cd.setMaxNo(joObject.getInt("maxNo"));
										cd.setMinNo(joObject.getInt("minNo"));
										cd.setmTitle(joObject
												.getString("mTitle"));
										cd.setmDirector(joObject
												.getString("mDirector"));
										cd.setmPic(joObject.getString("mPic"));
										cd.setmType1(joObject.getInt("mType1"));
										cd.setmNo(joObject.getInt("mNo"));
										cd.setmLianzai(joObject
												.getString("mLianzai"));
										cd.setCreateTime(DateUtil.Date2String(new Date(
												Long.parseLong(joObject
														.getString("mDate")))));
										cd.setLocalUrl(joObject
												.getString("localUrl"));
										cd.setPort(joObject.getString("port"));
										cd.setReadTime(joObject
												.getString("readDate"));
										cds.add(cd);
									}

									DBManager manager = new DBManager(
											MainActivity.this, DBUtil.ReadName,
											null, DBUtil.Code);
									manager.addComicsDetail(cds);
									manager.closeDB();
								}
							} catch (JSONException e) {
								// TODO Auto-generated catch block
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

			JsonObjectRequest joObjectRequest2 = new JsonObjectRequest(
					Method.POST, UrlConstant.getCollectLogs, param2,
					new Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject response) {
							// Log.d("tag", "response collect  "+response);
							try {
								if (response.getJSONArray("object") != null) {
									JSONArray array = response
											.getJSONArray("object");
									List<ComicsDetail> cds = new ArrayList<ComicsDetail>();
									for (int i = 0; i < array.length(); i++) {
										JSONObject joObject = array
												.getJSONObject(i);
										ComicsDetail cd = new ComicsDetail();
										cd.setmId(joObject.getInt("mId"));
										cd.setmQid(joObject.getInt("id"));
										cd.setmContent(joObject
												.getString("mContent"));
										cd.setmTitle(joObject
												.getString("mTitle"));
										cd.setmDirector(joObject
												.getString("mDirector"));
										cd.setmPic(joObject.getString("mPic"));
										cd.setmType1(joObject.getInt("mType1"));
										cd.setmLianzai(joObject
												.getString("mLianzai"));
										cd.setReadTime(joObject
												.getString("collectDateTime"));
										cds.add(cd);
									}

									DBManager manager = new DBManager(
											MainActivity.this,
											DBUtil.CollectName, null,
											DBUtil.Code);
									manager.addComicsDetail(cds);
									manager.closeDB();
								}
							} catch (JSONException e) {
								// TODO Auto-generated catch block
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

			joObjectRequest2.setRetryPolicy(new DefaultRetryPolicy(3000,
					DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
					DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

			NetUtil.rqueue.add(joObjectRequest);
			NetUtil.rqueue.add(joObjectRequest2);
		}
	}
}
