package com.ch.mhy.activity.book;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
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
import com.ch.mhy.adapter.BookDownAdapter;
import com.ch.mhy.db.DBManager;
import com.ch.mhy.db.DBUtil;
import com.ch.mhy.entity.Comics;
import com.ch.mhy.entity.ComicsDetail;
import com.ch.mhy.entity.Down;
import com.ch.mhy.interf.UpdateBg;
import com.ch.mhy.net.NetReceiver;
import com.ch.mhy.net.NetReceiver.NetState;
import com.ch.mhy.service.DownComicService;
import com.ch.mhy.util.DateUtil;
import com.ch.mhy.util.FileUtil;
import com.ch.mhy.util.ListUtil;
import com.ch.mhy.util.NetUtil;
import com.ch.mhy.util.SDUtil;
import com.ch.mhy.util.UrlConstant;
import com.ch.mhy.util.Utils;
import com.umeng.analytics.MobclickAgent;

/**
 * 下载界面
 *
 * @author DaxstUz 2416738717 2015年5月15日
 */
public class DownSelectActivity extends Activity implements UpdateBg,
		ServiceConnection {

	private Comics comics;

	private List<ComicsDetail> cds = new ArrayList<ComicsDetail>();// 从服务器获取的数据

	private List<Down> localcds = new ArrayList<Down>();// 本地已下载的数据

	private GridView gv_book_select;

	private BookDownAdapter adapter;

	private int currentpage = 1;
	private int totalPage;

	private String order = "desc";

	private Button btn_lingchang;

	private TextView tv_down_title;
	private TextView tv_showselects;
	private TextView tv_all_select;

	private DBManager manager;

	private boolean isSelect = true;// 是否是全选

	private LinearLayout ll_down_operate;
	private String mId;

	/**
	 * 下载的队列
	 */
	List<Down> addDown = new ArrayList<Down>();

	/* 通过Binder，实现Activity与Service通信 */
	private DownComicService.MsgBinder mBinderService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_downmore);

		gv_book_select = (GridView) findViewById(R.id.gv_book_select);
		btn_lingchang = (Button) findViewById(R.id.btn_lingchang);
		tv_down_title = (TextView) findViewById(R.id.tv_down_title);
		tv_showselects = (TextView) findViewById(R.id.tv_showselects);
		tv_all_select = (TextView) findViewById(R.id.tv_all_select);
		ll_down_operate = (LinearLayout) findViewById(R.id.ll_down_operate);
		ll_down_operate.setVisibility(View.GONE);

		adapter = new BookDownAdapter(this, cds, this);

		gv_book_select.setAdapter(adapter);

		comics = (Comics) this.getIntent().getSerializableExtra("mh");
		mId = String.valueOf(comics.getmQid());
		tv_down_title.setText(comics.getmTitle().length() > 6 ? comics
				.getmTitle().substring(0, 5) : comics.getmTitle());

		if (NetReceiver.isConnected(this) != NetState.NET_NO) {
			getLocalData();
			getData();
		} else {
			Utils.showMsg(this, "没联网...");
		}

		// 绑定service， 用于通知后台开启下载任务
		Intent service = new Intent(DownSelectActivity.this,
				DownComicService.class);
		DownSelectActivity.this.bindService(service, DownSelectActivity.this,
				Context.BIND_AUTO_CREATE);

	}

	/**
	 * 获取本地下载的数据
	 */
	private void getLocalData() {
		manager = new DBManager(this, DBUtil.ReadName, null, DBUtil.Code);
		localcds = manager.queryDown("select * from down  where mId =?",
				new String[] { comics.getmQid() + "" });
		manager.closeDB();
	}

	JSONObject param = new JSONObject();

	/**
	 * 找出下载项
	 * 
	 * @param cds
	 * @param localcds
	 */
	private void setIsDown(List<ComicsDetail> cds, List<Down> localcds) {
		for (ComicsDetail cd : cds) {
			for (int i = 0; i < localcds.size(); i++) {
				ComicsDetail cdd = localcds.get(i).getCd();
				if (cd.getmQid().equals(cdd.getmQid())) {
					cd.setIsdown(true);
				}
			}
		}
	}

	/**
	 * 获取数据
	 */
	private void getData() {
		initParam();

		JsonObjectRequest joObjectRequest = new JsonObjectRequest(Method.POST,
				UrlConstant.UrlComicsDetailPage, param,
				new Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject response) {
						try {

							totalPage = response.getJSONObject("object")
									.getJSONObject("page")
									.getJSONObject("pageInfo")
									.getInt("totalPageCount");

							cds.clear();
							JSONArray array = response.getJSONObject("object")
									.getJSONObject("page").getJSONArray("data");
							
							String dirpath=SDUtil.getSecondExterPath()
									+ "/manyun/" + comics.getmTitle();
							FileUtil.writeAdatpterToFile(dirpath, array.toString());
							
							for (int i = 0; i < array.length(); i++) {
								JSONObject joObject = array.getJSONObject(i);
								ComicsDetail cd = new ComicsDetail();
								cd.setmId(joObject.getInt("mId"));
								cd.setmQid(joObject.getInt("mQid"));
								cd.setmUrl(joObject.getString("mUrl"));
								cd.setmName(joObject.getString("mName"));
								cd.setmPic(comics.getmPic());
								cd.setmTitle(comics.getmTitle());
								cd.setmNo(joObject.getInt("mNo"));
								cd.setmDirector(comics.getmDirector());
								cd.setmLianzai(comics.getmLianzai());
								cd.setUpdateMessage(comics.getUpdateMessage());
								cd.setPartSize(joObject.getString("partsize"));
								cd.setLocalUrl(joObject.getString("localUrl"));
								cd.setPort(joObject.getString("port"));
								cd.setTotalPage(joObject.getInt("totalpage"));
								cd.setCreateTime(DateUtil.Date2String(new Date(
										Long.parseLong(joObject
												.getString("mDate")))));
								cd.setmContent(comics.getmContent());
								cd.setmType1(comics.getmType1());
								cd.setmFenAll(comics.getmFenAll());
								cd.setSysDate(joObject.getString("sysDate"));
								cd.setMinNo(joObject.getInt("minNo"));
								cd.setMaxNo(joObject.getInt("maxNo"));
								if(!joObject.isNull("prevNo")){
									cd.setPmNo(joObject.getInt("prevNo"));
								}
								if(!joObject.isNull("nextNo")){
									cd.setNmNo(joObject.getInt("nextNo"));
								}
								cds.add(cd);
							}

							setIsDown(cds, localcds);
							adapter.notifyDataSetChanged();
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}, new ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError error) {
						Utils.showMsg(DownSelectActivity.this, "服务器异常！");
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
			param.put("pageSize", 1000);
			param.put("currentPage", currentpage++);
			param.put("orderBy", "");
			param.put("object", order + "@@" + comics.getmQid());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	boolean flag = true;// 设置默认是可以下载的

	public void onclick(View view) {
		switch (view.getId()) {
		case R.id.btn_show_back:// 返回按钮
			DownSelectActivity.this.finish();
			break;

		case R.id.tv_sure:// 确定
			SharedPreferences sp = this.getSharedPreferences("userConfig", 0);
			// 当设置了wifi下载，而当前的网络不是wifi连接就变为不可下载
			if (sp.getBoolean("wifidown", false)) {
				if (NetReceiver.isConnected(this) != NetReceiver.NetState.NET_WIFI) {
					flag = false;
				}
			}
			if (n > 0 && flag) {
				Utils.showMsg(this, "已开始下载,请到我的下载中查看！");
				// 保存到数据库并下载
					manager = new DBManager(this, DBUtil.ReadName, null, DBUtil.Code);
					manager.addDown(addDown);
					manager.closeDB();
				
				if(Utils.updateCollectInfo!=null){
					Utils.updateCollectInfo.updateDowns();
				}
				
				/* 开始下载 */
				DownTasd dt = new DownTasd();
				dt.execute();
			} else {
				if(!flag){
					Utils.showMsg(this, "您设置了仅在wifi下下载！");
				}
			}
			

			// 友盟统计
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("type", "my_book_down");
			map.put("typeName", comics.getmTitle());
			MobclickAgent.onEventValue(this, "my_book_down", map, 1);

			DownSelectActivity.this.finish();
			break;

		case R.id.tv_all_select: // 选择所有
			float f = 0;
			addDown.clear();
			int selectize = 0;
			for (ComicsDetail cd : cds) {
				if (!cd.isIsdown()) {
					cd.setFlag(isSelect);
					f += Float.parseFloat(cd.getPartSize());
					selectize++;
					n++;
				}
				if (cd.isFlag()) {
					Down down = new Down();
					down.setCd(cd);
					down.setIsdonw(3);
					addDown.add(down);
				}
			}
			adapter.notifyDataSetChanged();

			if (isSelect) {
				tv_showselects.setText("共选择" + selectize + "项  共"
						+ (int) (f + 0.5) + "MB");
				tv_all_select.setText("取消全选");
				ll_down_operate.setVisibility(View.VISIBLE);
			} else {
				tv_showselects.setText("共选择" + 0 + "项   共" + 0 + "MB");
				tv_all_select.setText("全部下载");
				ll_down_operate.setVisibility(View.GONE);
			}
			isSelect = !isSelect;
			break;

		case R.id.tv_down_canser:// 取消
			for (ComicsDetail cd : cds) {
				cd.setFlag(false);
			}
			tv_showselects.setText("共选择" + 0 + "项   共" + 0 + "MB");
			adapter.notifyDataSetChanged();
			isSelect = true;
			tv_all_select.setText("全部下载");
			ll_down_operate.setVisibility(View.GONE);
			break;
		case R.id.btn_lingchang:// 排序
			currentpage = 1;
			if ("desc".equals(order)) {
				order = "asc";
				btn_lingchang.setBackgroundResource(R.drawable.ch_books_list);
				ListUtil.doAscSort(cds);
			} else {
				order = "desc";
				btn_lingchang.setBackgroundResource(R.drawable.ch_books_listf);
				ListUtil.doDescSort(cds);
			}
			adapter.notifyDataSetChanged();
			break;

		default:
			break;
		}
	}

	/**
	 * 开始下载
	 * 
	 * @param flag
	 */
	private void startDown(boolean flag, List<Down> cdss) {
		if (flag) {
			try {
				mBinderService.addDownload(ListUtil.doAscSortDown(cdss));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			flag = false;
		} else {
			Utils.showMsg(this, "您设置了仅在wifi下下载！");
		}
	}


	int n;// 统计选择了几个

	@Override
	public void update(int p) {
		cds.get(p).setFlag(!cds.get(p).isFlag());
		adapter.notifyDataSetChanged();
		n = 0;
		float f = 0;
		addDown.clear();
		for (ComicsDetail cd : cds) {
			if (cd.isFlag()) {
				n++;
				f += Float.parseFloat(cd.getPartSize());
				Down down = new Down();
				down.setCd(cd);
				down.setIsdonw(3);
				addDown.add(down);
			}
		}

		if (n == 0) {
			isSelect = true;
			tv_all_select.setText("全部下载");
			ll_down_operate.setVisibility(View.GONE);
		} else {
			ll_down_operate.setVisibility(View.VISIBLE);
		}

		if (n == cds.size()) {
			isSelect = false;
			tv_all_select.setText("取消全选");
		}

		tv_showselects.setText("共选择" + n + "项   共" + (int) (f + 0.5) + "MB");
	}

	/**
	 * 下载
	 * 
	 * @author DaxstUz 2416738717 2015年8月6日
	 *
	 */
	class DownTasd extends AsyncTask<Void, Integer, Integer> {
		@Override
		protected Integer doInBackground(Void... params) {
			if (addDown.size() > 0) {
				startDown(flag, addDown);
			}
			return null;
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
		mBinderService = (DownComicService.MsgBinder) service;
		Log.d("tag", "onServiceConnected");
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		Log.d("tag", "onServiceDisconnected");
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unbindService(DownSelectActivity.this);
	}

}
