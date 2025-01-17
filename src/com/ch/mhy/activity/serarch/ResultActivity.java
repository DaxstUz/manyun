package com.ch.mhy.activity.serarch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ch.comm.saevent.MobSaAgent;
import com.ch.mhy.R;
import com.ch.mhy.activity.book.ShowDetailActivity;
import com.ch.mhy.activity.my.PersonReturn;
import com.ch.mhy.adapter.SearchResultAdapter;
import com.ch.mhy.entity.Comics;
import com.ch.mhy.net.NetReceiver;
import com.ch.mhy.net.NetReceiver.NetState;
import com.ch.mhy.pulltorefresh.SingleLayoutListView;
//import com.ch.mhy.pulltorefresh.PullToRefreshLayout.OnRefreshListener;
import com.ch.mhy.pulltorefresh.SingleLayoutListView.OnLoadMoreListener;
import com.ch.mhy.pulltorefresh.SingleLayoutListView.OnRefreshListener;
import com.ch.mhy.util.NetUtil;
import com.ch.mhy.util.UrlConstant;
import com.ch.mhy.util.Utils;
import com.umeng.analytics.MobclickAgent;

/**
 * 显示搜索结果
 *
 * @author DaxstUz 2416738717 2015年5月20日
 */
public class ResultActivity extends Activity {
	private static final int LOAD_DATA_FINISH = 10;
	private static final int REFRESH_DATA_FINISH = 11;
	
    private TextView tv_searchresutl_title; //显示的页面标题

    private String key;//搜索关键字

    //搜索结果展示
    private SingleLayoutListView listView;
    
    private List<Comics> list = new ArrayList<Comics>();
    private SearchResultAdapter adapter;

    private int currentPage = 1;//查询第几页
    
    private int totalPageCount = 0;//总共多少页

    private FrameLayout fl_return;
    private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case REFRESH_DATA_FINISH:
				listView.onRefreshComplete(); // 加载更多完成
				break;
			case LOAD_DATA_FINISH:
				listView.onLoadMoreComplete(); // 加载更多完成
				break;
			}
		};
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchresult);
        
        fl_return = (FrameLayout) this.findViewById(R.id.fl_return);
        listView = (SingleLayoutListView) this.findViewById(R.id.mListView);
        tv_searchresutl_title = (TextView) findViewById(R.id.tv_searchresutl_title);
        
        //获取要搜索的关键字
        key = (String) getIntent().getSerializableExtra("key");
        //设置显示的标题，如果长度过程就裁剪
        tv_searchresutl_title.setText(key.length()>6?key.substring(0, 6):key);
        
        adapter = new SearchResultAdapter(ResultActivity.this, list);
        listView.setAdapter(adapter);
        
        //添加行点击事件
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
            	if(id<0) {// 点击的是headerView或者footerView 
					return; 
				} 
				int realPosition=(int)id; 
                Intent intent = new Intent(ResultActivity.this,
                        ShowDetailActivity.class);
                intent.putExtra("manhua", list.get(realPosition));
                intent.putExtra("eventId", "my_book_click");
                ResultActivity.this.startActivity(intent);
            }
        });

        listView.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				updateData(listView, 0);
			}
		});
        
        listView.setOnLoadListener(new OnLoadMoreListener() {
			@Override
			public void onLoadMore() {
			     updateData(listView, 1);
			}
		});
        listView.setCanLoadMore(true);
        listView.setCanRefresh(true);
        listView.setAutoLoadMore(true);
        

        //判断是否有网，并进行根据关键字查询
        if (NetReceiver.isConnected(this) != NetState.NET_NO) {
            requestData();
        } else {
            Utils.showMsg(this, "没联网...");
        }

    }

    /**
     * 请求网络
     */
    private void requestData() {
        list.clear();
        JSONObject params = new JSONObject();

        try {
        	//初始化搜索条件
            params.put("pageSize", Utils.PageSize);
            params.put("currentPage", currentPage);
            params.put("orderBy", "");
            params.put("object", key);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
                UrlConstant.UrlSearch, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            totalPageCount = response.getJSONObject("object").getJSONObject("pageInfo").getInt("totalPageCount");
                            JSONArray array = response.getJSONObject("object")
                                    .getJSONArray("data");

                            noMoreData(array);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jObject = array.getJSONObject(i);
                                Comics c = new Comics();
                                c.setmDirector(jObject.getString("mDirector"));
                                c.setmPic(jObject.getString("mPic"));
                                c.setmContent(jObject.getString("mContent"));
                                c.setmLianzai(jObject.getString("mLianzai"));
                                c.setUpdateMessage(jObject.getString("updateMessage"));
                                c.setmTitle(jObject.getString("mTitle"));
                                c.setmHits(jObject.getInt("bigbookview"));
                                c.setmQid(jObject.getLong("mId"));
                                c.setmType1(jObject.getInt("mType1"));
                                list.add(c);
                            }
                            if(list.size()>0){
                            	adapter.notifyDataSetChanged();
                            }else{
                            	fl_return.setVisibility(View.VISIBLE);
//                            	Utils.showMsg(ResultActivity.this, "亲~没有找到这部漫画哟~~赶紧去意见反馈通知我们添加吧!");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Utils.showMsg(ResultActivity.this, "服务器异常！");
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
    private void noMoreData(JSONArray array) {
		if(array.length()==0){
			listView.setmIsMore(false);
		}else{
			listView.setmIsMore(true);
		}
	}
    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.btn_show_back:
                ResultActivity.this.finish();
                break;
            case R.id.tv_return:
            	Intent intent=new Intent(this, PersonReturn.class);
            	this.startActivity(intent);
            	break;
            default:
                break;
        }
    }

    private void updateData(final SingleLayoutListView pullToRefreshLayout, final int n) {

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
            params.put("object", key);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
                UrlConstant.UrlSearch, params,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        if (n != 1) {
                            if (list != null) {
                                list.clear();
                            }
                        }
                        try {
                            JSONArray array = response.getJSONObject("object")
                                    .getJSONArray("data");
                            noMoreData(array);
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jObject = array.getJSONObject(i);
                                Comics c = new Comics();
                                c.setmDirector(jObject.getString("mDirector"));
                                c.setmPic(jObject.getString("mPic"));
                                c.setmContent(jObject.getString("mContent"));
                                c.setmTitle(jObject.getString("mTitle"));
                                c.setmHits(jObject.getInt("bigbookview"));
                                c.setmQid(jObject.getLong("mId"));
                                c.setmType1(jObject.getInt("mType1"));
                                list.add(c);
                            }

                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            } else {
                                adapter = new SearchResultAdapter(ResultActivity.this, list);
                                listView.setAdapter(adapter);
                            }
                            
                            if (n == 0) {
                            	Message _Msg = mHandler.obtainMessage(REFRESH_DATA_FINISH,list);
            					mHandler.sendMessage(_Msg);
                            } else if (n == 1) {
                            	Message _Msg = mHandler.obtainMessage(LOAD_DATA_FINISH,list);
            					mHandler.sendMessage(_Msg);
                            }

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
	 * 是否已记录过统计数据
	 */
	private boolean hasRecord = false;
    public void onResume() {
    	super.onResume();
    	MobclickAgent.onResume(this);
    	String eventId = "search_value";
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("type", eventId);
		map.put("searchValue",key); 
		MobclickAgent.onEventValue(this, eventId, map, 1);
		try {
			if(!hasRecord){
				MobSaAgent.onEventValue(this, eventId, map, 1);
				hasRecord = true;
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public void onPause() {
    	super.onPause();
    	MobclickAgent.onPause(this);
    }
}
