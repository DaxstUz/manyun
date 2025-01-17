package com.ch.mhy.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ch.mhy.R;
import com.ch.mhy.activity.cate.ShowBooksActivity;
import com.ch.mhy.adapter.AuthorListAdapter;
import com.ch.mhy.entity.AuthorType;
import com.ch.mhy.net.NetReceiver;
import com.ch.mhy.net.NetReceiver.NetState;
import com.ch.mhy.pulltorefresh.SingleLayoutListView;
import com.ch.mhy.pulltorefresh.SingleLayoutListView.OnLoadMoreListener;
import com.ch.mhy.pulltorefresh.SingleLayoutListView.OnRefreshListener;
import com.ch.mhy.util.NetUtil;
import com.ch.mhy.util.UrlConstant;
import com.ch.mhy.util.Utils;

/**
 * 作者碎片
 *
 * @author DaxstUz 2416738717
 *         2015年5月6日
 */
public class AuthorFragment extends Fragment implements OnClickListener{

    private SingleLayoutListView lv_cate_authorlist;

    private List<AuthorType> ats;
    
    private AuthorListAdapter adapter;

    private int currentPage = 1;
    
    private static final int LOAD_DATA_FINISH = 10;
    private static final int REFRESH_DATA_FINISH = 11;
    private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case REFRESH_DATA_FINISH:
				lv_cate_authorlist.onRefreshComplete(); // 加载更多完成
				break;
			case LOAD_DATA_FINISH:
				lv_cate_authorlist.onLoadMoreComplete(); // 加载更多完成
				break;
			}
		};
	};

	private  View view ;
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
	       view = inflater.inflate(R.layout.fragment_auth, null);
	    } catch (InflateException e) {
	        
	    }
        initView(view);

        if (NetReceiver.isConnected(AuthorFragment.this.getActivity()) != NetState.NET_NO) {
            initData();
        } else {
            Utils.showMsg(AuthorFragment.this.getActivity(), "没联网...");
        }
        return view;
    }


    /**
     * 初始化view
     * @param view
     */
    private void initView(View view) {
        lv_cate_authorlist = (SingleLayoutListView) view.findViewById(R.id.mListView);

        ats = new ArrayList<AuthorType>();

        adapter = new AuthorListAdapter(AuthorFragment.this.getActivity(), ats);
        lv_cate_authorlist.setAdapter(adapter);
        lv_cate_authorlist.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(id<0) {// 点击的是headerView或者footerView 
					return; 
				} 
				int realPosition=(int)id; 
                Intent intent = new Intent(AuthorFragment.this.getActivity(), ShowBooksActivity.class);
                intent.putExtra("cate", 3);
                intent.putExtra("author", ats.get(realPosition));
                intent.putExtra("eventId", "sort_author");
                AuthorFragment.this.getActivity().startActivity(intent);
            }
        });
        lv_cate_authorlist.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				updateData(lv_cate_authorlist, 0);
			}
		});
        
        lv_cate_authorlist.setOnLoadListener(new OnLoadMoreListener() {
			
			@Override
			public void onLoadMore() {
			     updateData(lv_cate_authorlist, 1);
			}
		});
        lv_cate_authorlist.setCanLoadMore(true);
        lv_cate_authorlist.setCanRefresh(true);
        lv_cate_authorlist.setAutoLoadMore(true);
    }


    /**
     * 初始化作者列表信息
     */
    private void initData() {
        JSONObject params = new JSONObject();
        try {
            params.put("pageSize", Utils.PageSize);
            params.put("currentPage", currentPage);
            params.put("orderBy", "");
            params.put("object", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
                UrlConstant.UrlAuthorType, params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                             JSONArray array = response.getJSONObject("object").getJSONArray("data");
                             noMoreData(array);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject joObject = array.getJSONObject(i);
                                AuthorType at = new AuthorType();
                                at.setTypeId(joObject.getInt("id"));
                                at.setTypeName(joObject.getString("typeName"));
                                at.setTypePic(joObject.getString("typePic"));
                                at.setTypeNum(joObject.getInt("typeNum"));
                                ats.add(at);
                            }


                            adapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

					
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
//                Utils.endnet();
                Utils.showMsg(AuthorFragment.this.getActivity(), "没联网...");

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_cate_authname:
                AuthorFragment.this.getActivity().startActivity(new Intent(AuthorFragment.this.getActivity(), ShowBooksActivity.class));
                break;

            default:
                break;
        }
    }

    private void noMoreData(JSONArray array) {
		if(array.length()==0){
			 lv_cate_authorlist.setmIsMore(false);
		 	//Toast.makeText(lv_cate_authorlist.getContext(), "没有更多了",Toast.LENGTH_SHORT ).show();
		 }else{
			 lv_cate_authorlist.setmIsMore(true);
		 }
	}
    private void updateData(SingleLayoutListView pullToRefreshLayout, final int n) {

        if (n == 0) {
            currentPage = 1;
        } else {
            ++currentPage;
        }
        // 加载操作
        JSONObject params = new JSONObject();

        try {
            params.put("pageSize", Utils.PageSize);
            params.put("currentPage", currentPage);
            params.put("orderBy", "");
            params.put("object", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
                UrlConstant.UrlAuthorType, params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        if (n != 1) {
                            if (ats != null) {
                                ats.clear();
                            }
                        }

                        try {
                            JSONArray array = response.getJSONObject("object").getJSONArray("data");
                            noMoreData(array);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject joObject = array.getJSONObject(i);
                                AuthorType at = new AuthorType();
                                at.setTypeId(joObject.getInt("id"));
                                at.setTypeName(joObject.getString("typeName"));
                                at.setTypePic(joObject.getString("typePic"));
                                at.setTypeNum(joObject.getInt("typeNum"));
                                ats.add(at);
                            }

                            if (n == 0) {
                            	adapter.notifyDataSetChanged();
                            	Message _Msg = mHandler.obtainMessage(REFRESH_DATA_FINISH,ats);
            					mHandler.sendMessage(_Msg);
                            } else if (n == 1) {
                            	adapter.notifyDataSetChanged();
                            	Message _Msg = mHandler.obtainMessage(LOAD_DATA_FINISH,ats);
            					mHandler.sendMessage(_Msg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                if (n == 1) {
                    //pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.FAIL);
                } else {
                   // pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
                }
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

}
