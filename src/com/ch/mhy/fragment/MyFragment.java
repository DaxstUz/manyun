package com.ch.mhy.fragment;

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
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ch.mhy.R;
import com.ch.mhy.activity.my.CollectActivity;
import com.ch.mhy.activity.my.GeneralsettingActivity;
import com.ch.mhy.activity.my.LoginActivity;
import com.ch.mhy.activity.my.MyMsgActivity;
import com.ch.mhy.activity.my.PersonReturn;
import com.ch.mhy.activity.my.UnLoginActivity;
import com.ch.mhy.activity.my.WonRecActivity;
import com.ch.mhy.db.DBManager;
import com.ch.mhy.db.DBUtil;
import com.ch.mhy.entity.ComicsDetail;
import com.ch.mhy.interf.UpdateCollectInfo;
import com.ch.mhy.net.NetReceiver;
import com.ch.mhy.net.NetReceiver.NetState;
import com.ch.mhy.util.DateUtil;
import com.ch.mhy.util.NetUtil;
import com.ch.mhy.util.UrlConstant;
import com.ch.mhy.util.UserUtil;
import com.ch.mhy.util.Utils;
import com.ch.mhy.widget.CircleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.sso.UMQQSsoHandler;

/**
 * 设置碎片
 *
 * @author DaxstUz 2416738717 2015年5月4日
 */
public class MyFragment extends Fragment implements OnClickListener,
		UpdateCollectInfo {

	private LinearLayout ll_my_msg;// 我的消息
	private LinearLayout ll_my_collect;// 我的收藏
	private LinearLayout ll_my_login;// 点击登录
	private LinearLayout ll_my_readed;// 最近阅读
	private LinearLayout ll_my_down; // 我的下载
	private LinearLayout ll_my_set;// 点击设置进入通用设置页面......
	private LinearLayout ll_my_return;// 点击意见反馈
	private LinearLayout ll_wonrec; // 点击精彩推荐

	private TextView tv_rsl;
	private TextView tv_ssl;
	private TextView tv_downs;

	private TextView tv_nickname;// 昵称
	private TextView tv_msg;//

	private DBManager manager;

	private long collects;
	private long readeds;
	private long downs;

	/**
	 * 头像
	 */
	private CircleImageView iv_myhead;

	/**
	 * umeng 第三方登录
	 */
	UMSocialService mController = UMServiceFactory
			.getUMSocialService("com.umeng.login");

	/**
	 * 用来存用户本地信息
	 */
	private SharedPreferences sp;
	private String openid;
	private ComicsDetail maxcd;
	private ComicsDetail maxcdc;
	private static final int login = 001;
	private static final int unlogin = 002;

	private View view;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		sp = this.getActivity().getSharedPreferences("userinfo",
				Context.MODE_PRIVATE);
		openid = sp.getString("openid", "");

		if (view != null) {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (parent != null)
				parent.removeView(view);
			//从其他地方跳转登录后，进入该界面需要重新填充用户信息
			if(UserUtil.isLogin(this.getActivity())){
				this.showHead();
			}
			return view;
		}
		try {
			view = inflater.inflate(R.layout.fragment_my, null);
		} catch (InflateException e) {

		}
		initView(view);
		Utils.updateCollectInfo = this;
		return view;
	}

	/**
	 * 初始化view
	 * 
	 * @param view
	 */
	private void initView(View view) {
		ll_my_msg = (LinearLayout) view.findViewById(R.id.ll_my_msg);
		ll_wonrec = (LinearLayout) view.findViewById(R.id.ll_wonrec);
		ll_my_collect = (LinearLayout) view.findViewById(R.id.ll_my_collect);
		ll_my_login = (LinearLayout) view.findViewById(R.id.ll_my_login);
		ll_my_readed = (LinearLayout) view.findViewById(R.id.ll_my_readed);
		ll_my_down = (LinearLayout) view.findViewById(R.id.ll_my_down);
		ll_my_set = (LinearLayout) view.findViewById(R.id.ll_my_set);// 获得设置按钮对象......
		ll_my_return = (LinearLayout) view.findViewById(R.id.ll_my_return);
		ll_my_set.setOnClickListener(MyFragment.this);// 为设置按钮添加监听器......
		ll_my_return.setOnClickListener(MyFragment.this);
		ll_my_down.setOnClickListener(MyFragment.this);
		ll_my_collect.setOnClickListener(MyFragment.this);
		ll_my_login.setOnClickListener(MyFragment.this);
		ll_my_readed.setOnClickListener(MyFragment.this);
		ll_wonrec.setOnClickListener(MyFragment.this);
		ll_my_msg.setOnClickListener(MyFragment.this);

		tv_rsl = (TextView) view.findViewById(R.id.tv_rsl);
		tv_ssl = (TextView) view.findViewById(R.id.tv_ssl);
		tv_downs = (TextView) view.findViewById(R.id.tv_downs);

		tv_msg = (TextView) view.findViewById(R.id.tv_msg);

		tv_nickname = (TextView) view.findViewById(R.id.tv_nickname);

		iv_myhead = (CircleImageView) view.findViewById(R.id.iv_myhead);

		showHead();
		getInfo();
	}

	/**
	 * 显示用户头像
	 */
	public void showHead() {
		if(!UserUtil.isLogin(this.getActivity())){
			return;
		}
		openid = sp.getString("openid", "");
		/**
		 * 如果有登录信息，就加载头像信息，否则不加载
		 */
		if (openid.length() > 0) {
			String screen_name = sp.getString("screen_name", "");
			if (screen_name.length() > 0) {
				tv_nickname.setText(screen_name);
				tv_msg.setText("点击注销");
			}

			/**
			 * 展示图片
			 */
			String headurl = sp.getString("headurl", "");

			if (headurl.length() > 0) {
				ImageLoader.getInstance().displayImage(headurl, iv_myhead,
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

	}

	private void unDoAuthor() {
		Intent intent = new Intent(getActivity(), UnLoginActivity.class);
		startActivityForResult(intent, unlogin);
		
//		mController.deleteOauth(MyFragment.this.getActivity(), SHARE_MEDIA.QQ,
//				new SocializeClientListener() {
//					@Override
//					public void onStart() {
//					}
//
//					@Override
//					public void onComplete(int status, SocializeEntity entity) {
//					}
//				});
//		updateInfo();
	}

	/**
	 * 更新用户展示信息
	 */
	public void updateInfo() {
		tv_msg.setText(R.string.my_msg);
		openid = "";
		Editor editer = sp.edit();
		editer.putString("openid", openid);
		String headurl = "";
		iv_myhead.setImageResource(R.drawable.ch_my_head);
		editer.putString("headurl", headurl);

		String screen_name = "";
		tv_nickname.setText(R.string.my_operate);
		editer.putString("screen_name", screen_name);
		editer.commit();

	}

	/**
	 * 授权登录平台
	 */
	private void doAuthor() {
		Intent intent = new Intent(getActivity(), LoginActivity.class);
		startActivityForResult(intent, login);
//		 UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(getActivity(),
//		 "1104686309", "LsxbkRrXqzao9zVP");
//		 qqSsoHandler.addToSocialSDK();
		
//		 mController.doOauthVerify(MyFragment.this.getActivity(),
//		 SHARE_MEDIA.QQ, new UMAuthListener() {
//		 @Override
//		 public void onStart(SHARE_MEDIA platform) {
//		 // Toast.makeText(MyFragment.this.getActivity(), "授权开始",
//		 // Toast.LENGTH_SHORT).show();
//		 }
//		
//		 @Override
//		 public void onError(SocializeException e,
//		 SHARE_MEDIA platform) {
//		 // Toast.makeText(MyFragment.this.getActivity(), "授权错误",
//		 // Toast.LENGTH_SHORT).show();
//		 }
//		
//		 @Override
//		 public void onComplete(Bundle value, SHARE_MEDIA platform) {
//		 // Toast.makeText(MyFragment.this.getActivity(), "授权完成",
//		 // Toast.LENGTH_SHORT).show();
//		
//		 if (value != null
//		 && !TextUtils.isEmpty(value.getString("uid"))) {
//		 openid = value.getString("openid");
//		
//		 }
//		
//		 // 获取相关授权信息
//		 mController.getPlatformInfo(
//		 MyFragment.this.getActivity(), SHARE_MEDIA.QQ,
//		 new UMDataListener() {
//		 @Override
//		 public void onStart() {
//		 // Toast.makeText(
//		 // MyFragment.this.getActivity(),
//		 // "获取平台数据开始...",
//		 // Toast.LENGTH_SHORT).show();
//		 }
//		
//		 @Override
//		 public void onComplete(int status,
//		 Map<String, Object> info) {
//		 if (status == 200 && info != null) {
//		 Editor editer = sp.edit();
//		 editer.putString("openid", openid);
//		 String headurl = (String) info
//		 .get("profile_image_url");
//		 ImageLoader.getInstance()
//		 .displayImage(headurl,
//		 iv_myhead);
//		 editer.putString("headurl", headurl);
//		
//		 String screen_name = (String) info
//		 .get("screen_name");
//		 tv_nickname.setText(screen_name);
//		 tv_msg.setText("点击注销");
//		 editer.putString("screen_name",
//		 screen_name);
//		
//		 editer.commit();
//		
//		
//		
//		 /**
//		 * 提示是否删除
//		 */
//		 AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
//		 builder.setTitle("是否需要把数据同步到您的账号？");
//		 builder.setNegativeButton("取消", null);
//		 builder.setPositiveButton("确认", new DialogInterface.OnClickListener()
//		 {
//		
//		 @Override
//		 public void onClick(DialogInterface dialog, int which) {
//		 // TODO Auto-generated method stub
//		 getMaxReadTime();
//		 }
//		 });
//		 builder.create().show();
//		
//		
//		 } else {
//		 Log.d("TestData", "发生错误：" + status);
//		 }
//		 }
//		 });
//		 }
//		
//		 @Override
//		 public void onCancel(SHARE_MEDIA platform) {
//		 // Toast.makeText(MyFragment.this.getActivity(), "授权取消",
//		 // Toast.LENGTH_SHORT).show();
//		 }
//		 });
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case unlogin:
			if (data!=null&&data.getBooleanExtra("isexit", false)) {
				updateInfo();
			}
			break;
		case login:
			if (data!=null&&data.getBooleanExtra("islogin", false)&&UserUtil.isLogin(getActivity())) {
				showHead();
				/**
				 * 提示是否删除
				 */
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getActivity());
				builder.setTitle("是否需要把数据同步到您的账号？");
				builder.setNegativeButton("取消", null);
				builder.setPositiveButton("确认",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								getMaxReadTime();
							}
						});
				builder.create().show();
			}

			break;

		default:
			break;
		}
	}

	/**
	 * 从本地获取用户的下载、最近阅读、收藏等信息
	 */
	private void getInfo() {
		// TODO Auto-generated method stub
		manager = new DBManager(MyFragment.this.getActivity(), DBUtil.ReadName,
				null, DBUtil.Code);
		List<ComicsDetail> list = manager.query();

		List<ComicsDetail> tempList = new ArrayList<ComicsDetail>();

		for (int i = 0; i < list.size(); i++) {
			ComicsDetail comicsDetail = list.get(i);
			if (comicsDetail.getmId() != null && comicsDetail.getmUrl() != null
					&& comicsDetail.getmPic() != null) {
				comicsDetail.setFlag(true);
				tempList.add(comicsDetail);
			}
		}
		readeds = tempList.size();

		downs = manager.queryDownsCount();
		manager.closeDB();

		manager = new DBManager(MyFragment.this.getActivity(),
				DBUtil.CollectName, null, DBUtil.Code);
		collects = manager.queryCollectCount();
		manager.closeDB();

		tv_rsl.setText("(" + readeds + ")");
		tv_ssl.setText("(" + collects + ")");
		tv_downs.setText("(" + downs + ")");
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;

		switch (v.getId()) {
		case R.id.ll_my_return:// 点击意见反馈
			intent = new Intent(MyFragment.this.getActivity(),
					PersonReturn.class);
			MyFragment.this.getActivity().startActivity(intent);
			break;
		case R.id.ll_wonrec:// 点击精彩
			intent = new Intent(MyFragment.this.getActivity(),
					WonRecActivity.class);
			MyFragment.this.getActivity().startActivity(intent);
			break;
		case R.id.ll_my_set:// 点击设置
			intent = new Intent(MyFragment.this.getActivity(),
					GeneralsettingActivity.class);
			MyFragment.this.getActivity().startActivity(intent);
			break;
		case R.id.ll_my_login:// 点击登录
			if (UserUtil.isLogin(getActivity())) {
				unDoAuthor();
			} else {
				doAuthor();
			}
			break;
		case R.id.ll_my_down:// 点击我的下载
			intent = new Intent(MyFragment.this.getActivity(),
					CollectActivity.class);
			intent.putExtra("operate", "2");
			MyFragment.this.getActivity().startActivity(intent);
			break;
		case R.id.ll_my_readed:// 点击最近阅读
			intent = new Intent(MyFragment.this.getActivity(),
					CollectActivity.class);
			intent.putExtra("operate", "1");
			MyFragment.this.getActivity().startActivity(intent);
			break;
		case R.id.ll_my_collect:// 点击我的收藏
			intent = new Intent(MyFragment.this.getActivity(),
					CollectActivity.class);
			intent.putExtra("operate", "3");
			MyFragment.this.getActivity().startActivity(intent);
			break;
		case R.id.ll_my_msg:// 点击我的收藏
			intent = new Intent(MyFragment.this.getActivity(),
					MyMsgActivity.class);
			intent.putExtra("operate", "3");
			MyFragment.this.getActivity().startActivity(intent);
			break;

		default:
			break;
		}
	}

	@Override
	public void updateDowns() {
		manager = new DBManager(MyFragment.this.getActivity(), DBUtil.ReadName,
				null, DBUtil.Code);
		downs = manager.queryDownsCount();
		manager.closeDB();
		tv_downs.setText("(" + downs + ")");
	}

	@Override
	public void updateReads() {
		manager = new DBManager(MyFragment.this.getActivity(), DBUtil.ReadName,
				null, DBUtil.Code);
		List<ComicsDetail> list = manager.query();

		List<ComicsDetail> tempList = new ArrayList<ComicsDetail>();

		for (int i = 0; i < list.size(); i++) {
			ComicsDetail comicsDetail = list.get(i);
			if (comicsDetail.getmId() != null && comicsDetail.getmUrl() != null
					&& comicsDetail.getmPic() != null) {
				comicsDetail.setFlag(true);
				tempList.add(comicsDetail);
			}
		}
		readeds = tempList.size();
		tv_rsl.setText("(" + readeds + ")");
		manager.closeDB();
	}

	@Override
	public void updateCollects() {
		manager = new DBManager(MyFragment.this.getActivity(),
				DBUtil.CollectName, null, DBUtil.Code);
		collects = manager.queryCollectCount();
		tv_ssl.setText("(" + collects + ")");
		manager.closeDB();
	}

	/**
	 * 获取最近阅读的漫画
	 */
	private void getMaxReadTime() {
		DBManager manager = new DBManager(MyFragment.this.getActivity(),
				DBUtil.ReadName, null, DBUtil.Code);
		this.maxcd = manager.queryMaxtReadTime();
		manager.closeDB();
		manager = new DBManager(MyFragment.this.getActivity(),
				DBUtil.CollectName, null, DBUtil.Code);
		this.maxcdc = manager.queryMaxtReadTime();
		manager.closeDB();
		updateUserInfo();
	}

	/**
	 * 同步用户的最近阅读、收藏信息到本地
	 */
	private void updateUserInfo() {
		NetState state = NetReceiver.isConnected(MyFragment.this.getActivity());
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
											MyFragment.this.getActivity(),
											DBUtil.ReadName, null, DBUtil.Code);
									manager.addComicsDetail(cds);
									manager.closeDB();

									updateReads();
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
											MyFragment.this.getActivity(),
											DBUtil.CollectName, null,
											DBUtil.Code);
									manager.addComicsDetail(cds);
									manager.closeDB();

									updateCollects();
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
