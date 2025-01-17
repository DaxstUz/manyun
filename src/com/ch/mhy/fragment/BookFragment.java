package com.ch.mhy.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.InflateException;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ch.mhy.R;
import com.ch.mhy.activity.book.ShowDetailActivity;
import com.ch.mhy.activity.cate.ShowBooksActivity;
import com.ch.mhy.activity.my.CollectActivity;
import com.ch.mhy.entity.AuthorType;
import com.ch.mhy.entity.Comics;
import com.ch.mhy.net.NetReceiver;
import com.ch.mhy.service.DownAPKService;
import com.ch.mhy.util.NetUtil;
import com.ch.mhy.util.UrlConstant;
import com.ch.mhy.util.Utils;
import com.ch.mhy.viewpager.CycleViewPager;
import com.ch.mhy.viewpager.CycleViewPager.ImageCycleViewListener;
import com.ch.mhy.viewpager.ViewFactory;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * 书城碎片
 *
 * @author DaxstUz 2416738717 2015年5月4日
 */
public class BookFragment extends Fragment implements OnClickListener,
		ImageLoadingListener {
	/* 热门漫画每一本书的布局 */
	private LinearLayout ll_hotred_6;
	private LinearLayout ll_hotred_5;
	private LinearLayout ll_hotred_4;
	private LinearLayout ll_hotred_3;
	private LinearLayout ll_hotred_2;
	private LinearLayout ll_hotred_1;

	/* 人气新漫每一本书的布局 */
	private LinearLayout ll_rqred_1;
	private LinearLayout ll_rqred_2;
	private LinearLayout ll_rqred_3;
	private LinearLayout ll_rqred_4;
	private LinearLayout ll_rqred_5;
	private LinearLayout ll_rqred_6;

	/* 完结漫画每一本书的布局 */
	private LinearLayout ll_downred_1;
	private LinearLayout ll_downred_2;
	private LinearLayout ll_downred_3;
	private LinearLayout ll_downred_4;
	private LinearLayout ll_downred_5;
	private LinearLayout ll_downred_6;

	/* 点击查看更多的布局 */
	private LinearLayout ll_hotbook_showmore;
	private LinearLayout ll_renqibook_showmore;
	private LinearLayout ll_downbook_showmore;

	//
	private LinearLayout ll_book_reading;
	// 刷新区域
	private LinearLayout ll_getnet;

	private List<ImageView> views = new ArrayList<ImageView>();
	private CycleViewPager cycleViewPager;

	private AuthorType hotbook = new AuthorType();// 热门漫画
	private AuthorType rqbook = new AuthorType();// 人气新漫
	private AuthorType downbook = new AuthorType();// 完结幽漫

	/* 热门漫画书本控件 */
	private ImageView iv_hot_show1;
	private ImageView iv_hot_show2;
	private ImageView iv_hot_show3;
	private ImageView iv_hot_show4;
	private ImageView iv_hot_show5;
	private ImageView iv_hot_show6;
	private TextView tv_hot_name1;
	private TextView tv_hot_name2;
	private TextView tv_hot_name3;
	private TextView tv_hot_name4;
	private TextView tv_hot_name5;
	private TextView tv_hot_name6;

	/* 人气新漫书本控件 */
	private ImageView iv_rq_show1;
	private ImageView iv_rq_show2;
	private ImageView iv_rq_show3;
	private ImageView iv_rq_show4;
	private ImageView iv_rq_show5;
	private ImageView iv_rq_show6;
	private TextView tv_rq_name1;
	private TextView tv_rq_name2;
	private TextView tv_rq_name3;
	private TextView tv_rq_name4;
	private TextView tv_rq_name5;
	private TextView tv_rq_name6;

	/* 完结优漫书本控件 */
	private ImageView iv_down_show1;
	private ImageView iv_down_show2;
	private ImageView iv_down_show3;
	private ImageView iv_down_show4;
	private ImageView iv_down_show5;
	private ImageView iv_down_show6;
	private TextView tv_down_name1;
	private TextView tv_down_name2;
	private TextView tv_down_name3;
	private TextView tv_down_name4;
	private TextView tv_down_name5;
	private TextView tv_down_name6;

	/* 完结漫画显示的书名总话数 */
	private TextView tv_downtotal_6;
	private TextView tv_downtotal_5;
	private TextView tv_downtotal_4;
	private TextView tv_downtotal_3;
	private TextView tv_downtotal_2;
	private TextView tv_downtotal_1;

	/* 人气新漫显示的书名总话数 */
	private TextView tv_rqtotal_6;
	private TextView tv_rqtotal_5;
	private TextView tv_rqtotal_4;
	private TextView tv_rqtotal_3;
	private TextView tv_rqtotal_2;
	private TextView tv_rqtotal_1;

	/* 热门漫画显示的书名总话数 */
	private TextView tv_hottotal_1;
	private TextView tv_hottotal_2;
	private TextView tv_hottotal_3;
	private TextView tv_hottotal_4;
	private TextView tv_hottotal_5;
	private TextView tv_hottotal_6;

	/* 不同题材的数据源 */
	private List<Comics> at1 = new ArrayList<Comics>();
	private List<Comics> at2 = new ArrayList<Comics>();
	private List<Comics> at3 = new ArrayList<Comics>();

	/* 轮播图数据源 */
	private List<Comics> lbs = new ArrayList<Comics>();

	private ImageView btn_getnet;
	private View view;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (view != null) {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (parent != null)
				parent.removeView(view);
			return view;
		}
		try {
			view = inflater.inflate(R.layout.fragment_book, container, false);
		} catch (InflateException e) {

		}

		init(view);

		/**
		 * 先判断是否有网络，有网络就请求数据，否则显示刷新获取数据界面
		 */
		if (NetReceiver.isConnected(BookFragment.this.getActivity()) != NetReceiver.NetState.NET_NO) {
			initData();
			getLb();
		} else {
			ll_getnet.setVisibility(View.VISIBLE);
		}

		return view;
	}

	/**
	 * 拿到轮播图数据
	 */
	private void getLb() {
		// TODO Auto-generated method stub
		JSONObject params = new JSONObject();
		try {
			params.put("pageSize", 10);
			params.put("currentPage", "1");
			params.put("orderBy", "");
			params.put("object", "1");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
				UrlConstant.UrlFirstTurn, params,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							if (lbs != null) {
								lbs.clear();
							}
							JSONArray array = response.getJSONArray("object");
							// 热门漫画
							for (int i = 0; i < array.length(); i++) {
								JSONObject hotObject = array.getJSONObject(i);
								Comics c = new Comics();
								if(!hotObject.isNull("mDirector")){
									c.setmDirector(hotObject.getString("mDirector"));
								}
								if(!hotObject.isNull("mPic")){
									c.setmPic(hotObject.getString("mPic"));
								}
								if(!hotObject.isNull("mContent")){
									c.setmContent(hotObject.getString("mContent"));
								}
								if(!hotObject.isNull("mTitle")){
									c.setmTitle(hotObject.getString("mTitle"));
								}
								if(!hotObject.isNull("mHits")){
									c.setmHits(hotObject.getInt("mHits"));
								}
								if(!hotObject.isNull("mId")){
									c.setmQid(hotObject.getLong("mId"));
								}
								if(!hotObject.isNull("mType1")){
									c.setmType1(hotObject.getInt("mType1"));
								}
								if(!hotObject.isNull("mLianzai")){
									c.setmLianzai(hotObject.getString("mLianzai"));
								}
								if(!hotObject.isNull("updateMessage")){
									c.setUpdateMessage(hotObject.getString("updateMessage"));
								}
								if(!hotObject.isNull("picPath")){
									c.setPicPath(hotObject.getString("picPath"));
								}
								
								if(!hotObject.isNull("firstTurnType")){
									c.setFirstTurnType(hotObject.getString("firstTurnType"));
								}
								
								if(!hotObject.isNull("firstTurnPath")){
									c.setFirstTurnPath(hotObject.getString("firstTurnPath"));
									c.setmPic(hotObject.getString("firstTurnPath"));
								}
									if(!hotObject.isNull("firstTurnTitle")){
									c.setFirstTurnTitle(hotObject.getString("firstTurnTitle"));
									c.setmTitle(hotObject.getString("firstTurnTitle"));
								}
								
									if(c.getmPic()!=null){
										lbs.add(c);
									}
							}
							initialize();
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
					}
				}) {
			/**
			 * Passing some request headers
			 * */
			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				HashMap<String, String> headers = new HashMap<String, String>();
				headers.put("Content-Type", "application/json; charset=utf-8");
				return headers;
			}
		};
		jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(60000,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		NetUtil.rqueue.add(jsonObjReq);
	}

	/**
	 * 初始化数据
	 */
	private void initData() {
		JSONObject params = new JSONObject();
		try {
			params.put("pageSize", 6);
			params.put("currentPage", "1");
			params.put("orderBy", "");
			params.put("object", "");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
				UrlConstant.UrlBookType, params,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						// Utils.endnet();
						ll_getnet.setVisibility(View.GONE);
						try {
							JSONArray array = response.getJSONObject("object")
									.getJSONArray("data");
							// 热门漫画
							JSONObject joObject = array.getJSONObject(0);
							hotbook.setTypeName(joObject.getString("typeName"));
							hotbook.setTypeId(joObject.getInt("id"));
							JSONArray hots = joObject
									.getJSONArray("listBookVo");
							at1.clear();
							for (int i = 0; i < hots.length(); i++) {
								JSONObject hotObject = hots.getJSONObject(i);
								Comics c = new Comics();
								c.setmDirector(hotObject.getString("mDirector"));
								c.setmPic(hotObject.getString("mPic"));
								c.setmContent(hotObject.getString("mContent"));
								c.setUpdateMessage(hotObject
										.getString("updateMessage"));
								c.setmTitle(hotObject.getString("mTitle"));
								c.setmHits(hotObject.getInt("mHits"));
								c.setmQid(hotObject.getLong("mId"));
								c.setmType1(hotObject.getInt("mType1"));
								c.setmLianzai(hotObject.getString("mLianzai"));
								c.setUpdateMessage(hotObject.getString("updateMessage"));
								c.setmTotal(hotObject.getInt("mTotal"));
								at1.add(c);
							}

							if (at1.size() > 0) {
								updateHot();
							}

							// 人气新漫
							JSONObject joObject1 = array.getJSONObject(1);
							rqbook.setTypeName(joObject1.getString("typeName"));
							rqbook.setTypeId(joObject1.getInt("id"));
							JSONArray renqi = joObject1
									.getJSONArray("listBookVo");
							at2.clear();
							for (int i = 0; i < renqi.length(); i++) {
								JSONObject rqobject = renqi.getJSONObject(i);
								Comics c = new Comics();
								c.setmDirector(rqobject.getString("mDirector"));
								c.setmPic(rqobject.getString("mPic"));
								c.setmContent(rqobject.getString("mContent"));
								c.setUpdateMessage(rqobject
										.getString("updateMessage"));
								c.setmTitle(rqobject.getString("mTitle"));
								c.setmHits(rqobject.getInt("mHits"));
								c.setmQid(rqobject.getLong("mId"));
								c.setmType1(rqobject.getInt("mType1"));
								c.setmLianzai(rqobject.getString("mLianzai"));
								c.setUpdateMessage(rqobject.getString("updateMessage"));
								c.setmTotal(rqobject.getInt("mTotal"));
								at2.add(c);
							}

							if (at2.size() > 0) {
								updateRenqi();
							}

							// 完结漫画
							JSONObject joObject2 = array.getJSONObject(2);
							downbook.setTypeName(joObject2
									.getString("typeName"));
							downbook.setTypeId(joObject2.getInt("id"));
							JSONArray wanjie = joObject2
									.getJSONArray("listBookVo");

							at3.clear();
							for (int i = 0; i < wanjie.length(); i++) {
								JSONObject rqobject = wanjie.getJSONObject(i);
								Comics c = new Comics();
								c.setmDirector(rqobject.getString("mDirector"));
								c.setmPic(rqobject.getString("mPic"));
								c.setmContent(rqobject.getString("mContent"));
								c.setmTitle(rqobject.getString("mTitle"));
								c.setmHits(rqobject.getInt("mHits"));
								c.setmQid(rqobject.getLong("mId"));
								c.setmType1(rqobject.getInt("mType1"));
								c.setmLianzai(rqobject.getString("mLianzai"));
								c.setUpdateMessage(rqobject.getString("updateMessage"));
								c.setmTotal(rqobject.getInt("mTotal"));
								c.setUpdateMessage(rqobject
										.getString("updateMessage"));
								at3.add(c);
							}
							if (at3.size() > 0) {
								updateDown();
							}

						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
					}
				}) {

			/**
			 * Passing some request headers
			 * */
			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				HashMap<String, String> headers = new HashMap<String, String>();
				headers.put("Content-Type", "application/json; charset=utf-8");
				return headers;
			}
		};
		jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(60000,
				DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		NetUtil.rqueue.add(jsonObjReq);
	}

	/* 设置完结优漫的值 */
	private void updateDown() {
		iv_down_show1.setBackgroundResource(0);
		iv_down_show2.setBackgroundResource(0);
		iv_down_show3.setBackgroundResource(0);
		iv_down_show4.setBackgroundResource(0);
		iv_down_show5.setBackgroundResource(0);
		iv_down_show6.setBackgroundResource(0);
		ImageLoader.getInstance().displayImage(at3.get(0).getmPic(),
				iv_down_show1, Utils.options, this);
		ImageLoader.getInstance().displayImage(at3.get(1).getmPic(),
				iv_down_show2, Utils.options, this);
		ImageLoader.getInstance().displayImage(at3.get(2).getmPic(),
				iv_down_show3, Utils.options, this);
		ImageLoader.getInstance().displayImage(at3.get(3).getmPic(),
				iv_down_show4, Utils.options, this);
		ImageLoader.getInstance().displayImage(at3.get(4).getmPic(),
				iv_down_show5, Utils.options, this);
		ImageLoader.getInstance().displayImage(at3.get(5).getmPic(),
				iv_down_show6, Utils.options, this);

		/* 设置显示的漫画名字，并对名字的长度做处理 */
		tv_down_name6.setText(at3.get(5).getmTitle().length() > 4 ? at3.get(5)
				.getmTitle().subSequence(0, 4)
				+ "..." : at3.get(5).getmTitle());
		tv_down_name5.setText(at3.get(4).getmTitle().length() > 4 ? at3.get(4)
				.getmTitle().subSequence(0, 4)
				+ "..." : at3.get(4).getmTitle());
		tv_down_name4.setText(at3.get(3).getmTitle().length() > 4 ? at3.get(3)
				.getmTitle().subSequence(0, 4)
				+ "..." : at3.get(3).getmTitle());
		tv_down_name3.setText(at3.get(2).getmTitle().length() > 4 ? at3.get(2)
				.getmTitle().subSequence(0, 4)
				+ "..." : at3.get(2).getmTitle());
		tv_down_name2.setText(at3.get(1).getmTitle().length() > 4 ? at3.get(1)
				.getmTitle().subSequence(0, 4)
				+ "..." : at3.get(1).getmTitle());
		tv_down_name1.setText(at3.get(0).getmTitle().length() > 4 ? at3.get(0)
				.getmTitle().subSequence(0, 4)
				+ "..." : at3.get(0).getmTitle());

		/* 设置书的总话数 */
		tv_downtotal_6.setText(at3.get(5).getUpdateMessage().length() > 4 ? at3
				.get(5).getUpdateMessage().substring(0, 4) : at3.get(5)
				.getUpdateMessage());
		tv_downtotal_5.setText(at3.get(4).getUpdateMessage().length() > 4 ? at3
				.get(4).getUpdateMessage().substring(0, 4) : at3.get(4)
				.getUpdateMessage());
		tv_downtotal_4.setText(at3.get(3).getUpdateMessage().length() > 4 ? at3
				.get(3).getUpdateMessage().substring(0, 4) : at3.get(3)
				.getUpdateMessage());
		tv_downtotal_3.setText(at3.get(2).getUpdateMessage().length() > 4 ? at3
				.get(2).getUpdateMessage().substring(0, 4) : at3.get(2)
				.getUpdateMessage());
		tv_downtotal_2.setText(at3.get(1).getUpdateMessage().length() > 4 ? at3
				.get(1).getUpdateMessage().substring(0, 4) : at3.get(1)
				.getUpdateMessage());
		tv_downtotal_1.setText(at3.get(0).getUpdateMessage().length() > 4 ? at3
				.get(0).getUpdateMessage().substring(0, 4) : at3.get(0)
				.getUpdateMessage());
	}

	/* 初始化人气新漫数据 */
	private void updateRenqi() {
		/* 图片显示之前先去掉他们的背景图片，节省内存 */
		iv_rq_show1.setBackgroundResource(0);
		iv_rq_show2.setBackgroundResource(0);
		iv_rq_show3.setBackgroundResource(0);
		iv_rq_show4.setBackgroundResource(0);
		iv_rq_show5.setBackgroundResource(0);
		iv_rq_show6.setBackgroundResource(0);
		ImageLoader.getInstance().displayImage(at2.get(0).getmPic(),
				iv_rq_show1, Utils.options, this);
		ImageLoader.getInstance().displayImage(at2.get(1).getmPic(),
				iv_rq_show2, Utils.options, this);
		ImageLoader.getInstance().displayImage(at2.get(2).getmPic(),
				iv_rq_show3, Utils.options, this);
		ImageLoader.getInstance().displayImage(at2.get(3).getmPic(),
				iv_rq_show4, Utils.options, this);
		ImageLoader.getInstance().displayImage(at2.get(4).getmPic(),
				iv_rq_show5, Utils.options, this);
		ImageLoader.getInstance().displayImage(at2.get(5).getmPic(),
				iv_rq_show6, Utils.options, this);
		tv_rq_name1.setText(at2.get(0).getmTitle().length() > 4 ? at2.get(0)
				.getmTitle().subSequence(0, 4)
				+ "..." : at2.get(0).getmTitle());
		tv_rq_name2.setText(at2.get(1).getmTitle().length() > 4 ? at2.get(1)
				.getmTitle().subSequence(0, 4)
				+ "..." : at2.get(1).getmTitle());
		tv_rq_name3.setText(at2.get(2).getmTitle().length() > 4 ? at2.get(2)
				.getmTitle().subSequence(0, 4)
				+ "..." : at2.get(2).getmTitle());
		tv_rq_name4.setText(at2.get(3).getmTitle().length() > 4 ? at2.get(3)
				.getmTitle().subSequence(0, 4)
				+ "..." : at2.get(3).getmTitle());
		tv_rq_name5.setText(at2.get(4).getmTitle().length() > 4 ? at2.get(4)
				.getmTitle().subSequence(0, 4)
				+ "..." : at2.get(4).getmTitle());
		tv_rq_name6.setText(at2.get(5).getmTitle().length() > 4 ? at2.get(5)
				.getmTitle().subSequence(0, 4)
				+ "..." : at2.get(5).getmTitle());

		tv_rqtotal_6.setText(at2.get(5).getUpdateMessage().length() > 4 ? at2
				.get(5).getUpdateMessage().substring(0, 4) : at2.get(5)
				.getUpdateMessage());
		tv_rqtotal_5.setText(at2.get(4).getUpdateMessage().length() > 4 ? at2
				.get(4).getUpdateMessage().substring(0, 4) : at2.get(4)
				.getUpdateMessage());
		tv_rqtotal_4.setText(at2.get(3).getUpdateMessage().length() > 4 ? at2
				.get(3).getUpdateMessage().substring(0, 4) : at2.get(3)
				.getUpdateMessage());
		tv_rqtotal_3.setText(at2.get(2).getUpdateMessage().length() > 4 ? at2
				.get(2).getUpdateMessage().substring(0, 4) : at2.get(2)
				.getUpdateMessage());
		tv_rqtotal_2.setText(at2.get(1).getUpdateMessage().length() > 4 ? at2
				.get(1).getUpdateMessage().substring(0, 4) : at2.get(1)
				.getUpdateMessage());
		tv_rqtotal_1.setText(at2.get(0).getUpdateMessage().length() > 4 ? at2
				.get(0).getUpdateMessage().substring(0, 4) : at2.get(0)
				.getUpdateMessage());
	}

	/**
	 * 更新热门漫画信息
	 */
	private void updateHot() {
		iv_hot_show1.setBackgroundResource(0);
		iv_hot_show2.setBackgroundResource(0);
		iv_hot_show3.setBackgroundResource(0);
		iv_hot_show4.setBackgroundResource(0);
		iv_hot_show5.setBackgroundResource(0);
		iv_hot_show6.setBackgroundResource(0);
		ImageLoader.getInstance().displayImage(at1.get(0).getmPic(),
				iv_hot_show1, Utils.options, this);
		ImageLoader.getInstance().displayImage(at1.get(1).getmPic(),
				iv_hot_show2, Utils.options, this);
		ImageLoader.getInstance().displayImage(at1.get(2).getmPic(),
				iv_hot_show3, Utils.options, this);
		ImageLoader.getInstance().displayImage(at1.get(3).getmPic(),
				iv_hot_show4, Utils.options, this);
		ImageLoader.getInstance().displayImage(at1.get(4).getmPic(),
				iv_hot_show5, Utils.options, this);
		ImageLoader.getInstance().displayImage(at1.get(5).getmPic(),
				iv_hot_show6, Utils.options, this);

		tv_hot_name1.setText(at1.get(0).getmTitle().length() > 4 ? at1.get(0)
				.getmTitle().subSequence(0, 4)
				+ "..." : at1.get(0).getmTitle());
		tv_hot_name2.setText(at1.get(1).getmTitle().length() > 4 ? at1.get(1)
				.getmTitle().subSequence(0, 4)
				+ "..." : at1.get(1).getmTitle());
		tv_hot_name3.setText(at1.get(2).getmTitle().length() > 4 ? at1.get(2)
				.getmTitle().subSequence(0, 4)
				+ "..." : at1.get(2).getmTitle());
		tv_hot_name4.setText(at1.get(3).getmTitle().length() > 4 ? at1.get(3)
				.getmTitle().subSequence(0, 4)
				+ "..." : at1.get(3).getmTitle());
		tv_hot_name5.setText(at1.get(4).getmTitle().length() > 4 ? at1.get(4)
				.getmTitle().subSequence(0, 4)
				+ "..." : at1.get(4).getmTitle());
		tv_hot_name6.setText(at1.get(5).getmTitle().length() > 4 ? at1.get(5)
				.getmTitle().subSequence(0, 4)
				+ "..." : at1.get(5).getmTitle());

		tv_hottotal_1.setText(at1.get(0).getUpdateMessage().length() > 4 ? at1
				.get(0).getUpdateMessage().substring(0, 4) : at1.get(0)
				.getUpdateMessage());
		tv_hottotal_2.setText(at1.get(1).getUpdateMessage().length() > 4 ? at1
				.get(1).getUpdateMessage().substring(0, 4) : at1.get(1)
				.getUpdateMessage());
		tv_hottotal_3.setText(at1.get(2).getUpdateMessage().length() > 4 ? at1
				.get(2).getUpdateMessage().substring(0, 4) : at1.get(2)
				.getUpdateMessage());
		tv_hottotal_4.setText(at1.get(3).getUpdateMessage().length() > 4 ? at1
				.get(3).getUpdateMessage().substring(0, 4) : at1.get(3)
				.getUpdateMessage());
		tv_hottotal_5.setText(at1.get(4).getUpdateMessage().length() > 4 ? at1
				.get(4).getUpdateMessage().substring(0, 4) : at1.get(4)
				.getUpdateMessage());
		tv_hottotal_6.setText(at1.get(5).getUpdateMessage().length() > 4 ? at1
				.get(5).getUpdateMessage().substring(0, 4) : at1.get(5)
				.getUpdateMessage());
	}

	/**
	 * 初始化组件
	 *
	 * @param view
	 */
	private void init(View view) {

		tv_downtotal_6 = (TextView) view.findViewById(R.id.tv_downtotal_6);
		tv_downtotal_5 = (TextView) view.findViewById(R.id.tv_downtotal_5);
		tv_downtotal_4 = (TextView) view.findViewById(R.id.tv_downtotal_4);
		tv_downtotal_3 = (TextView) view.findViewById(R.id.tv_downtotal_3);
		tv_downtotal_2 = (TextView) view.findViewById(R.id.tv_downtotal_2);
		tv_downtotal_1 = (TextView) view.findViewById(R.id.tv_downtotal_1);

		tv_rqtotal_6 = (TextView) view.findViewById(R.id.tv_rqtotal_6);
		tv_rqtotal_5 = (TextView) view.findViewById(R.id.tv_rqtotal_5);
		tv_rqtotal_4 = (TextView) view.findViewById(R.id.tv_rqtotal_4);
		tv_rqtotal_3 = (TextView) view.findViewById(R.id.tv_rqtotal_3);
		tv_rqtotal_2 = (TextView) view.findViewById(R.id.tv_rqtotal_2);
		tv_rqtotal_1 = (TextView) view.findViewById(R.id.tv_rqtotal_1);

		tv_hottotal_1 = (TextView) view.findViewById(R.id.tv_hottotal_1);
		tv_hottotal_2 = (TextView) view.findViewById(R.id.tv_hottotal_2);
		tv_hottotal_3 = (TextView) view.findViewById(R.id.tv_hottotal_3);
		tv_hottotal_4 = (TextView) view.findViewById(R.id.tv_hottotal_4);
		tv_hottotal_5 = (TextView) view.findViewById(R.id.tv_hottotal_5);
		tv_hottotal_6 = (TextView) view.findViewById(R.id.tv_hottotal_6);

		iv_hot_show1 = (ImageView) view.findViewById(R.id.iv_hot_show1);
		iv_hot_show2 = (ImageView) view.findViewById(R.id.iv_hot_show2);
		iv_hot_show3 = (ImageView) view.findViewById(R.id.iv_hot_show3);
		iv_hot_show4 = (ImageView) view.findViewById(R.id.iv_hot_show4);
		iv_hot_show5 = (ImageView) view.findViewById(R.id.iv_hot_show5);
		iv_hot_show6 = (ImageView) view.findViewById(R.id.iv_hot_show6);
		tv_hot_name1 = (TextView) view.findViewById(R.id.tv_hot_name1);
		tv_hot_name2 = (TextView) view.findViewById(R.id.tv_hot_name2);
		tv_hot_name3 = (TextView) view.findViewById(R.id.tv_hot_name3);
		tv_hot_name4 = (TextView) view.findViewById(R.id.tv_hot_name4);
		tv_hot_name5 = (TextView) view.findViewById(R.id.tv_hot_name5);
		tv_hot_name6 = (TextView) view.findViewById(R.id.tv_hot_name6);

		iv_rq_show1 = (ImageView) view.findViewById(R.id.iv_rq_show1);
		iv_rq_show2 = (ImageView) view.findViewById(R.id.iv_rq_show2);
		iv_rq_show3 = (ImageView) view.findViewById(R.id.iv_rq_show3);
		iv_rq_show4 = (ImageView) view.findViewById(R.id.iv_rq_show4);
		iv_rq_show5 = (ImageView) view.findViewById(R.id.iv_rq_show5);
		iv_rq_show6 = (ImageView) view.findViewById(R.id.iv_rq_show6);
		tv_rq_name1 = (TextView) view.findViewById(R.id.tv_rq_name1);
		tv_rq_name2 = (TextView) view.findViewById(R.id.tv_rq_name2);
		tv_rq_name3 = (TextView) view.findViewById(R.id.tv_rq_name3);
		tv_rq_name4 = (TextView) view.findViewById(R.id.tv_rq_name4);
		tv_rq_name5 = (TextView) view.findViewById(R.id.tv_rq_name5);
		tv_rq_name6 = (TextView) view.findViewById(R.id.tv_rq_name6);

		iv_down_show1 = (ImageView) view.findViewById(R.id.iv_down_show1);
		iv_down_show2 = (ImageView) view.findViewById(R.id.iv_down_show2);
		iv_down_show3 = (ImageView) view.findViewById(R.id.iv_down_show3);
		iv_down_show4 = (ImageView) view.findViewById(R.id.iv_down_show4);
		iv_down_show5 = (ImageView) view.findViewById(R.id.iv_down_show5);
		iv_down_show6 = (ImageView) view.findViewById(R.id.iv_down_show6);
		tv_down_name1 = (TextView) view.findViewById(R.id.tv_down_name1);
		tv_down_name2 = (TextView) view.findViewById(R.id.tv_down_name2);
		tv_down_name3 = (TextView) view.findViewById(R.id.tv_down_name3);
		tv_down_name4 = (TextView) view.findViewById(R.id.tv_down_name4);
		tv_down_name5 = (TextView) view.findViewById(R.id.tv_down_name5);
		tv_down_name6 = (TextView) view.findViewById(R.id.tv_down_name6);

		ll_hotred_6 = (LinearLayout) view.findViewById(R.id.ll_hotred_6);
		ll_hotred_6.setOnClickListener(this);
		ll_hotred_5 = (LinearLayout) view.findViewById(R.id.ll_hotred_5);
		ll_hotred_5.setOnClickListener(this);
		ll_hotred_4 = (LinearLayout) view.findViewById(R.id.ll_hotred_4);
		ll_hotred_4.setOnClickListener(this);
		ll_hotred_3 = (LinearLayout) view.findViewById(R.id.ll_hotred_3);
		ll_hotred_3.setOnClickListener(this);
		ll_hotred_2 = (LinearLayout) view.findViewById(R.id.ll_hotred_2);
		ll_hotred_2.setOnClickListener(this);
		ll_hotred_1 = (LinearLayout) view.findViewById(R.id.ll_hotred_1);
		ll_hotred_1.setOnClickListener(this);

		ll_rqred_1 = (LinearLayout) view.findViewById(R.id.ll_rqred_1);
		ll_rqred_1.setOnClickListener(this);
		ll_rqred_2 = (LinearLayout) view.findViewById(R.id.ll_rqred_2);
		ll_rqred_2.setOnClickListener(this);
		ll_rqred_3 = (LinearLayout) view.findViewById(R.id.ll_rqred_3);
		ll_rqred_3.setOnClickListener(this);
		ll_rqred_4 = (LinearLayout) view.findViewById(R.id.ll_rqred_4);
		ll_rqred_4.setOnClickListener(this);
		ll_rqred_5 = (LinearLayout) view.findViewById(R.id.ll_rqred_5);
		ll_rqred_5.setOnClickListener(this);
		ll_rqred_6 = (LinearLayout) view.findViewById(R.id.ll_rqred_6);
		ll_rqred_6.setOnClickListener(this);

		ll_downred_1 = (LinearLayout) view.findViewById(R.id.ll_downred_1);
		ll_downred_1.setOnClickListener(this);
		ll_downred_2 = (LinearLayout) view.findViewById(R.id.ll_downred_2);
		ll_downred_2.setOnClickListener(this);
		ll_downred_3 = (LinearLayout) view.findViewById(R.id.ll_downred_3);
		ll_downred_3.setOnClickListener(this);
		ll_downred_4 = (LinearLayout) view.findViewById(R.id.ll_downred_4);
		ll_downred_4.setOnClickListener(this);
		ll_downred_5 = (LinearLayout) view.findViewById(R.id.ll_downred_5);
		ll_downred_5.setOnClickListener(this);
		ll_downred_6 = (LinearLayout) view.findViewById(R.id.ll_downred_6);
		ll_downred_6.setOnClickListener(this);
		ll_getnet = (LinearLayout) view.findViewById(R.id.ll_getnet);

		btn_getnet = (ImageView) view.findViewById(R.id.btn_getnet);
		btn_getnet.setOnClickListener(this);

		ll_hotbook_showmore = (LinearLayout) view
				.findViewById(R.id.ll_hotbook_showmore);
		ll_hotbook_showmore.setOnClickListener(this);

		ll_renqibook_showmore = (LinearLayout) view
				.findViewById(R.id.ll_renqibook_showmore);
		ll_renqibook_showmore.setOnClickListener(this);

		ll_downbook_showmore = (LinearLayout) view
				.findViewById(R.id.ll_downbook_showmore);
		ll_downbook_showmore.setOnClickListener(this);

		ll_book_reading = (LinearLayout) view
				.findViewById(R.id.ll_book_reading);
		ll_book_reading.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.btn_getnet:// 点击刷新按钮
			if (NetReceiver.isConnected(BookFragment.this.getActivity()) != NetReceiver.NetState.NET_NO) {
				ll_getnet.setVisibility(View.GONE);
				initData();
				getLb();
			} else {
				Toast.makeText(BookFragment.this.getActivity(), "请链接网络！",
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.ll_book_reading:// 点击左上角最近阅读按钮
			intent = new Intent(BookFragment.this.getActivity(),
					CollectActivity.class);
			intent.putExtra("operate", "1");
			break;
		case R.id.ll_hotbook_showmore:// 点击热门漫画查看更多按钮
			intent = new Intent(BookFragment.this.getActivity(),
					ShowBooksActivity.class);
			intent.putExtra("cate", 0);
			intent.putExtra("author", hotbook);
			break;
		case R.id.ll_renqibook_showmore:// 点击人气查看更多
			intent = new Intent(BookFragment.this.getActivity(),
					ShowBooksActivity.class);
			intent.putExtra("cate", 0);
			intent.putExtra("author", rqbook);
			break;
		case R.id.ll_downbook_showmore:// 点击完结漫画家查看更多
			intent = new Intent(BookFragment.this.getActivity(),
					ShowBooksActivity.class);
			intent.putExtra("cate", 0);
			intent.putExtra("author", downbook);
			break;

		case R.id.ll_hotred_6:// 热门第6本书
			if (at1.size() > 0) {
				intent = new Intent(BookFragment.this.getActivity(),
						ShowDetailActivity.class);
				intent.putExtra("manhua", at1.get(5));
				intent.putExtra("eventId", "first_book");
			}
			break;
		case R.id.ll_hotred_5:// 热门第5本书
			if (at1.size() > 0) {

				intent = new Intent(BookFragment.this.getActivity(),
						ShowDetailActivity.class);
				intent.putExtra("manhua", at1.get(4));
				intent.putExtra("eventId", "first_book");
			}
			break;
		case R.id.ll_hotred_4:// 热门第4本书
			if (at1.size() > 0) {
				intent = new Intent(BookFragment.this.getActivity(),
						ShowDetailActivity.class);
				intent.putExtra("manhua", at1.get(3));
				intent.putExtra("eventId", "first_book");
			}
			break;
		case R.id.ll_hotred_3:// 热门第3本书
			if (at1.size() > 0) {
				intent = new Intent(BookFragment.this.getActivity(),
						ShowDetailActivity.class);
				intent.putExtra("manhua", at1.get(2));
				intent.putExtra("eventId", "first_book");
			}
			break;
		case R.id.ll_hotred_2:// 热门第2本书
			if (at1.size() > 0) {
				intent = new Intent(BookFragment.this.getActivity(),
						ShowDetailActivity.class);
				intent.putExtra("manhua", at1.get(1));
				intent.putExtra("eventId", "first_book");
			}
			break;
		case R.id.ll_hotred_1:// 热门第1本书
			if (at1.size() > 0) {
				intent = new Intent(BookFragment.this.getActivity(),
						ShowDetailActivity.class);
				intent.putExtra("manhua", at1.get(0));
				intent.putExtra("eventId", "first_book");
			}
			break;
		case R.id.ll_rqred_6:// 人气第6本书
			if (at2.size() > 0) {

				intent = new Intent(BookFragment.this.getActivity(),
						ShowDetailActivity.class);
				intent.putExtra("manhua", at2.get(5));
				intent.putExtra("eventId", "first_book");
			}
			break;
		case R.id.ll_rqred_5:// 人气第5本书
			if (at2.size() > 0) {

				intent = new Intent(BookFragment.this.getActivity(),
						ShowDetailActivity.class);
				intent.putExtra("manhua", at2.get(4));
				intent.putExtra("eventId", "first_book");
			}
			break;
		case R.id.ll_rqred_4:// 人气第4本书
			if (at2.size() > 0) {

				intent = new Intent(BookFragment.this.getActivity(),
						ShowDetailActivity.class);
				intent.putExtra("manhua", at2.get(3));
				intent.putExtra("eventId", "first_book");
			}
			break;
		case R.id.ll_rqred_3:// 人气第3本书
			if (at2.size() > 0) {
				intent = new Intent(BookFragment.this.getActivity(),
						ShowDetailActivity.class);
				intent.putExtra("manhua", at2.get(2));
				intent.putExtra("eventId", "first_book");
			}
			break;
		case R.id.ll_rqred_2:// 人气第2本书
			if (at2.size() > 0) {
				intent = new Intent(BookFragment.this.getActivity(),
						ShowDetailActivity.class);
				intent.putExtra("manhua", at2.get(1));
				intent.putExtra("eventId", "first_book");
			}
			break;
		case R.id.ll_rqred_1:// 人气第1本书
			if (at2.size() > 0) {
				intent = new Intent(BookFragment.this.getActivity(),
						ShowDetailActivity.class);
				intent.putExtra("manhua", at2.get(0));
				intent.putExtra("eventId", "first_book");
			}
			break;
		case R.id.ll_downred_1:// 完结第1本书
			if (at3.size() > 0) {
				intent = new Intent(BookFragment.this.getActivity(),
						ShowDetailActivity.class);
				intent.putExtra("manhua", at3.get(0));
				intent.putExtra("eventId", "first_book");
			}
			break;
		case R.id.ll_downred_2:// 完结第2本书
			if (at3.size() > 0) {
				intent = new Intent(BookFragment.this.getActivity(),
						ShowDetailActivity.class);
				intent.putExtra("manhua", at3.get(1));
				intent.putExtra("eventId", "first_book");
			}
			break;
		case R.id.ll_downred_3:// 完结第3本书
			if (at3.size() > 0) {
				intent = new Intent(BookFragment.this.getActivity(),
						ShowDetailActivity.class);
				intent.putExtra("manhua", at3.get(2));
				intent.putExtra("eventId", "first_book");
			}
			break;
		case R.id.ll_downred_4:// 完结第4本书
			if (at3.size() > 0) {
				intent = new Intent(BookFragment.this.getActivity(),
						ShowDetailActivity.class);
				intent.putExtra("manhua", at3.get(3));
				intent.putExtra("eventId", "first_book");
			}
			break;
		case R.id.ll_downred_5:// 完结第5本书
			if (at3.size() > 0) {
				intent = new Intent(BookFragment.this.getActivity(),
						ShowDetailActivity.class);
				intent.putExtra("manhua", at3.get(4));
				intent.putExtra("eventId", "first_book");
			}
			break;
		case R.id.ll_downred_6:// 完结第6本书
			if (at3.size() > 0) {

				intent = new Intent(BookFragment.this.getActivity(),
						ShowDetailActivity.class);
				intent.putExtra("manhua", at3.get(5));
				intent.putExtra("eventId", "first_book");
			}
			break;
		default:
			intent = new Intent(BookFragment.this.getActivity(),
					ShowDetailActivity.class);
			intent.putExtra("eventId", "first_book");
			break;
		}
		if (intent != null) {
			BookFragment.this.getActivity().startActivity(intent);
		}
	}

	/**
	 * 轮播图，viewpager监听事件
	 */
	private ImageCycleViewListener mAdCycleViewListener = new ImageCycleViewListener() {

		@Override
		public void onImageClick(Comics info, int position, View imageView) {
			if (cycleViewPager.isCycle()) {
				position = position - 1;
				if (lbs.size() > 0) {
					if("1".equals(lbs.get(position).getFirstTurnType())){
						/* 点击轮播图开始看漫画 */
						Intent intent = new Intent(BookFragment.this.getActivity(),
								ShowDetailActivity.class);
						intent.putExtra("manhua", lbs.get(position));
						intent.putExtra("eventId", "first_banner");
						BookFragment.this.getActivity().startActivity(intent);
					}else{
						/*
						 * apk下载
						 */
						Intent intent = new Intent(BookFragment.this.getActivity(), DownAPKService.class);  
			            intent.putExtra("apk_url", lbs.get(position).getFirstTurnPath());  
			            BookFragment.this.getActivity().startService(intent);  
			            Toast.makeText(BookFragment.this.getActivity(), "正在后台进行下载，稍后会自动安装", Toast.LENGTH_SHORT).show();
					}
					
				}
			}
		}
	};

	/**
	 * 初始化轮播图信息
	 */
	private void initialize() {
		cycleViewPager = (CycleViewPager) this.getFragmentManager()
				.findFragmentById(R.id.fragment_cycle_viewpager_content);

		if (views != null) {
			views.clear();
		}
		// 将最后一个ImageView添加进来
		views.add(ViewFactory.getImageView(BookFragment.this.getActivity(),
				UrlConstant.Ip1 + UrlConstant.Port1
						+ lbs.get(lbs.size() - 1).getPicPath()));
		for (int i = 0; i < lbs.size(); i++) {
			views.add(ViewFactory.getImageView(BookFragment.this.getActivity(),
					UrlConstant.Ip1 + UrlConstant.Port1
							+ lbs.get(i).getPicPath()));
		}
		// 将第一个ImageView添加进来
		views.add(ViewFactory.getImageView(BookFragment.this.getActivity(),
				UrlConstant.Ip1 + UrlConstant.Port1 + lbs.get(0).getPicPath()));

		// 设置循环，在调用setData方法前调用
		cycleViewPager.setCycle(true);

		// 在加载数据前设置是否循环
		cycleViewPager.setData(views, lbs, mAdCycleViewListener);
		// 设置轮播
		cycleViewPager.setWheel(true);

		// 设置轮播时间，默认5000ms
		cycleViewPager.setTime(2000);
		// 设置圆点指示图标组居中显示，默认靠右
		cycleViewPager.setIndicatorCenter();
	}

	@Override
	public void onLoadingStarted(String imageUri, View view) {
		view.setBackgroundResource(R.drawable.ic_loading_small);
	}

	@Override
	public void onLoadingFailed(String imageUri, View view,
			FailReason failReason) {
	}

	@Override
	public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
	}

	@Override
	public void onLoadingCancelled(String imageUri, View view) {
	}
}
