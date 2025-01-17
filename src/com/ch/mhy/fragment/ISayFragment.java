package com.ch.mhy.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.ch.comm.resquest.AbsResponseData;
import com.ch.mhy.R;
import com.ch.mhy.activity.comment.CommentDataUtil;
import com.ch.mhy.activity.comment.CommentDetailActivity;
import com.ch.mhy.activity.comment.ICommentInfoUpdate;
import com.ch.mhy.adapter.MsgAdapter;
import com.ch.mhy.entity.CommentInfo;
import com.ch.mhy.pulltorefresh.SingleLayoutListView;
import com.ch.mhy.pulltorefresh.SingleLayoutListView.OnLoadMoreListener;
import com.ch.mhy.pulltorefresh.SingleLayoutListView.OnRefreshListener;
import com.ch.mhy.util.DataUtil;
import com.ch.mhy.util.UrlConstant;
import com.ch.mhy.util.UserUtil;
import com.ch.mhy.util.Utils;

/**
 * 我发起的
 * @author DaxstUz 2416738717
 * 2015年10月17日
 *
 */
public class ISayFragment  extends Fragment implements ICommentInfoUpdate{
	public static ISayFragment activity;
	/*列表*/
	private SingleLayoutListView lv_isay;
	private List<JSONObject> list=new ArrayList<JSONObject>();
	private MsgAdapter madapter;
	
	
	private static final int LOAD_DATA_FINISH = 10;
    private static final int REFRESH_DATA_FINISH = 11;
    private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case REFRESH_DATA_FINISH:
				lv_isay.onRefreshComplete(); // 加载更多完成
				break;
			case LOAD_DATA_FINISH:
				lv_isay.onLoadMoreComplete(); // 加载更多完成
				break;
			}
		};
	};
	
	
	
	private View view;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreateView(inflater, container, savedInstanceState);
		activity = this;
		if(view!=null){
			ViewGroup parent=(ViewGroup) view.getParent();
			if(parent!=null){
				parent.removeView(view);
			}
			return view;
		}else{
			view=inflater.inflate(R.layout.fragment_isay, null);
		}
		
		init();
		getData(null,0);
		return view;
	}

	
	private int currentPage=1;
	
	/**
	 * 获取用户数据
	 */
	private void getData(
			final SingleLayoutListView pullToRefreshLayout, final int n) {

        if (n == 0) {
            currentPage = 1;
            list.clear();
        } else {
            ++currentPage;
        }
		
        JSONObject param=new JSONObject();
		
		HashMap<String, String> map=new HashMap<String, String>();
		
		
		try {
			map.put("userId", UserUtil.getOpenId(getActivity())+"");
			map.put("userType", UserUtil.getPlatForm(getActivity())+"");
			map.put("nickname", UserUtil.getScreen_name(getActivity()));
			map.put("imgUrl", UserUtil.getHeadurl(getActivity()));
			
			param.put("pageSize",Utils.PageSize);
			param.put("currentPage",currentPage);
			param.put("object", new JSONObject(map));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		CommentDataUtil.getTopicList(getActivity(), UrlConstant.UrlCommentsByUser, param, new  AbsResponseData(null) {
			@Override
			public void dataBusi(Object data) {
				list.addAll((List<JSONObject>)data);
				madapter.notifyDataSetChanged();
				
				if(pullToRefreshLayout!=null){
					if (n == 0) {
						Message _Msg = mHandler.obtainMessage(REFRESH_DATA_FINISH,0);
						mHandler.sendMessage(_Msg);
					} else if (n == 1) {
						Message _Msg = mHandler.obtainMessage(LOAD_DATA_FINISH,0);
						mHandler.sendMessage(_Msg);
					}
				}
			}
		});
	}

	/**
	 * 初始化控件
	 */
	private void init() {
		madapter=new MsgAdapter(getActivity(), list);
		lv_isay=(SingleLayoutListView) view.findViewById(R.id.lv_isay);
		lv_isay.setAdapter(madapter);
		
		lv_isay.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				JSONObject json = list.get((int) arg3);
				CommentInfo bean = DataUtil.getInfo(new CommentInfo(), json);
				Intent intent = new Intent();
				intent.setClass(ISayFragment.this.getActivity(), CommentDetailActivity.class);
				intent.putExtra("fromFlag", "ISayFragment");
				intent.putExtra("comment", bean);
				intent.putExtra("position", Integer.valueOf(arg3+""));
				ISayFragment.this.startActivity(intent);
			}
		});
		
		
		lv_isay.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				getData(lv_isay, 0);
			}
		});
        
		lv_isay.setOnLoadListener(new OnLoadMoreListener() {
			
			@Override
			public void onLoadMore() {
				getData(lv_isay, 1);
			}
		});
		lv_isay.setCanLoadMore(true);
		lv_isay.setCanRefresh(true);
		lv_isay.setAutoLoadMore(true);
	}

	@Override
	public void updateView(int position, int okCnt, int discussCnt) {
		JSONObject json = list.get(position);
		try {
			json.put("approveNum", json.getInt("approveNum")+okCnt);
			if(okCnt>0){
				json.put("isApprove", 1);
			}else if(okCnt<0){
				json.put("isApprove", 0);
			}
			json.put("discussNum", json.getInt("discussNum")+discussCnt);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		madapter.notifyDataSetChanged();
	}
}
