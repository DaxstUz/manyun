package com.ch.mhy.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ch.mhy.R;
import com.ch.mhy.activity.book.ShowDetailActivity;
import com.ch.mhy.adapter.BookCollectAdapter;
import com.ch.mhy.db.DBManager;
import com.ch.mhy.db.DBUtil;
import com.ch.mhy.entity.Comics;
import com.ch.mhy.entity.ComicsDetail;
import com.ch.mhy.util.NetUtil;
import com.ch.mhy.util.UrlConstant;
import com.ch.mhy.util.Utils;

/**
 * 我的收藏
 *
 * @author DaxstUz 2416738717 2015年5月6日
 */
public class CollectFragment extends Fragment implements OnClickListener{

	private DBManager manager;

	private ListView lv_book_collect;

	private List<ComicsDetail> list = new ArrayList<ComicsDetail>();

	private BookCollectAdapter adapter;

	
	private String openid;

	/*
	 * 全选操作部分
	 */
	private LinearLayout ll_collect_operate;
	private Button btn_selectall;
	private Button btn_delete;
	private boolean isselect=false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_collect, null);
		initView(view);
		getData();
		adapter = new BookCollectAdapter(CollectFragment.this.getActivity(),
				list);
		lv_book_collect.setAdapter(adapter);

		/**
		 * 获取openid
		 */
		SharedPreferences sp = this.getActivity().getSharedPreferences(
				"userinfo", Context.MODE_PRIVATE);
		 openid=sp.getString("openid", "");
		 
		return view;
	}
	
	  /**
     * 初始化组件
     */
	private void initView(View view) {
		ll_collect_operate = (LinearLayout) view.findViewById(R.id.ll_collect_operate);
		lv_book_collect = (ListView) view.findViewById(R.id.lv_book_collect);
        btn_selectall = (Button) view.findViewById(R.id.btn_selectall);
        btn_delete = (Button) view.findViewById(R.id.btn_delete);
        btn_selectall.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
	}
	

	/**
	 * 获取数据
	 */
	private void getData() {
		manager = new DBManager(CollectFragment.this.getActivity(),
				DBUtil.CollectName, null, DBUtil.Code);
		list.clear();
		list.addAll(manager.query());

		for (ComicsDetail comicsDetail : list) {
			// 设置flag 是否显示删除按钮i
			comicsDetail.setFlag(true);
		}

		lv_book_collect.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ComicsDetail cd = list.get(position);
				if (cd.isFlag()) {
					Comics comic = new Comics();
					comic.setmPic(cd.getmPic());
					comic.setmQid(Long.valueOf(cd.getmId().toString()));
					comic.setmFenAll(cd.getmFenAll());
					comic.setmType1(cd.getmType1());
					comic.setmContent(cd.getmContent());
					comic.setmLianzai(cd.getmLianzai());
					comic.setUpdateMessage(cd.getUpdateMessage());
					comic.setmDirector(cd.getmDirector());
					comic.setmTitle(cd.getmTitle());
					Intent intent = new Intent(CollectFragment.this
							.getActivity(), ShowDetailActivity.class);
					intent.putExtra("manhua", comic);
					intent.putExtra("eventId", "my_book_click");
					CollectFragment.this.getActivity().startActivity(intent);

				} else {
					list.get(position).setIsselect(!list.get(position).isIsselect());
					adapter.notifyDataSetChanged();
				}
			}
		});
	}

	/**
	 * 将阅读的漫画数据进行处理
	 */
	private void delToServer(String mid) {
		/**
		 * 需要插入的记录
		 */
		Map<String, String> obj = new HashMap<String, String>();
		obj.put("comicId", mid);
		obj.put("userId", openid);
		// obj.put("chapterId", mh.getmQid() + "");
		// obj.put("pageNum", ch_read_view.mImageIndex + "");
		JSONObject jobObject = new JSONObject();
		try {
			jobObject.put("object", new JSONObject(obj));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JsonObjectRequest jsonrRequest = new JsonObjectRequest(Method.POST,
				UrlConstant.delCollectToServer, jobObject,
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

	public void updateStatus(boolean flag) {

		if(ll_collect_operate!=null){
			if(flag){
				ll_collect_operate.setVisibility(View.GONE);
			}else{
				ll_collect_operate.setVisibility(View.VISIBLE);
			}
		}
		
		for (ComicsDetail comicsDetail : list) {
			comicsDetail.setFlag(flag);
		}
		
		if(adapter!=null){
			adapter.notifyDataSetChanged();
		}

	}

	private long opreatetime=0;//记录点击时间
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_selectall://点击全选
			isselect=!isselect;
			for (ComicsDetail comicsDetail : list) {
				comicsDetail.setIsselect(isselect);
			}
			adapter.notifyDataSetChanged();
			break;
		case R.id.btn_delete://点击删除
			final List<ComicsDetail> detellist=new ArrayList<ComicsDetail>();
			
			for (ComicsDetail comicsDetail : list) {
				if(comicsDetail.isIsselect()){
					detellist.add(comicsDetail);
				}
			}
			/*
			 * 把要删除的记录，在本地数据库里进行删除，然后在把数据里的数据读取出来，刷新界面
			 */
			if(detellist.size()>0){
	            
        		/**
        		 * 提示是否删除
        		 */
        		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        		builder.setTitle("确认删除吗？");
        		builder.setNegativeButton("取消", null);
        		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if(openid.length()>0){
							for (ComicsDetail comicsDetail : detellist) {
								delToServer(comicsDetail.getmId()+"");
							}
		        		}
						
						manager = new DBManager(CollectFragment.this.getActivity(), DBUtil.CollectName, null, DBUtil.Code);
						manager.delete(detellist);
						list.clear();
						list.addAll(manager.query());
						manager.closeDB();
						adapter.notifyDataSetChanged();
						if(Utils.updateCollectInfo!=null){
							Utils.updateCollectInfo.updateCollects();
						}
					}
				});
        		builder.create().show();
			}else{
				if(System.currentTimeMillis()-opreatetime>2000){
					Utils.showMsg(getActivity(), "没有选择删除项！");
					opreatetime=System.currentTimeMillis();
				}
			}
			
			break;

		default:
			break;
		}
	}
}
