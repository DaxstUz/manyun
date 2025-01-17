package com.ch.mhy.activity.cate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ch.mhy.R;
import com.ch.mhy.activity.book.ShowDetailActivity;
import com.ch.mhy.adapter.AuthorBooksListAdapter;
import com.ch.mhy.entity.AuthorType;
import com.ch.mhy.entity.Comics;
import com.ch.mhy.pulltorefresh.SingleLayoutListView;
//import com.ch.mhy.pulltorefresh.PullToRefreshLayout.OnRefreshListener;
import com.ch.mhy.pulltorefresh.SingleLayoutListView.OnLoadMoreListener;
import com.ch.mhy.pulltorefresh.SingleLayoutListView.OnRefreshListener;
import com.ch.mhy.util.NetUtil;
import com.ch.mhy.util.UrlConstant;
import com.ch.mhy.util.Utils;
import com.umeng.analytics.MobclickAgent;

/**
 * 显示某一类型的漫画
 *
 * @author DaxstUz 2416738717 2015年5月15日
 */
public class ShowBooksActivity extends Activity {
	private static final int LOAD_DATA_FINISH = 10;
	private static final int REFRESH_DATA_FINISH = 11;
	
    private int currentPage = 1;

    private AuthorBooksListAdapter adapter;
    private List<Comics> list;
    private SingleLayoutListView listview;
    private TextView tv_books_title;
    private String getDataUrl;
    int totalPage;

    private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case REFRESH_DATA_FINISH:
				listview.onRefreshComplete(); // 加载更多完成
				break;
			case LOAD_DATA_FINISH:
				listview.onLoadMoreComplete(); // 加载更多完成
				break;
			}
		};
	};
	
	private String typeName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showbooks);

    
        tv_books_title = (TextView) findViewById(R.id.tv_books_title);

        AuthorType at = (AuthorType) getIntent().getSerializableExtra("author");
        tv_books_title.setText(at.getTypeName());
        typeName = at.getTypeName();
        
        int cate = (Integer) getIntent().getSerializableExtra("cate");
        /*根据不同的cate加载不同的漫画数据*/
        switch (cate) {
            case 3://表示从作者分类跳转过来的
                getDataUrl = UrlConstant.UrlAuthorList;
                break;
            case 2: //表示从精选分类跳转过来的
                getDataUrl = UrlConstant.UrlChoiceList;
                break;
            case 0://表示是从首页调整过来的
                getDataUrl = UrlConstant.UrlBookList;
                break;
            default://表示从提出分类调整过来的
                getDataUrl = UrlConstant.UrlCategoryList;
                break;
        }

        initView();

        initData(at.getTypeId());
    }
    
    /**
     * 初始化控件
     */
    private void initView() {
        list = new ArrayList<Comics>();
        listview = (SingleLayoutListView) this.findViewById(R.id.mListView);

        adapter = new AuthorBooksListAdapter(this, list);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
            	if(id<0) {// 点击的是headerView或者footerView 
					return; 
				} 
				int realPosition=(int)id; 
                Intent intent = new Intent(ShowBooksActivity.this,
                        ShowDetailActivity.class);
                intent.putExtra("manhua", list.get(realPosition));
                intent.putExtra("eventId", "my_book_click");
                ShowBooksActivity.this.startActivity(intent);
            }
        });
        
        listview.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
				updateData(listview, 0);
			}
		});
        
        listview.setOnLoadListener(new OnLoadMoreListener() {
			@Override
			public void onLoadMore() {
			     updateData(listview, 1);
			}
		});
        listview.setCanLoadMore(true);
        listview.setCanRefresh(true);
        listview.setAutoLoadMore(true);
    }

    /**
     * 刷新数据源
     *
     * @param response
     */
    private void updateList(JSONObject response) {
        try {
            totalPage = response.getJSONObject("object")
                    .getJSONObject("pageInfo").getInt("totalPageCount");
            JSONArray array = response.getJSONObject("object").getJSONArray(
                    "data");
            if(array.length()==0){
            	listview.setmIsMore(false);
            }else{
            	listview.setmIsMore(true);
            }
            for (int i = 0; i < array.length(); i++) {
                JSONObject joObject = array.getJSONObject(i);
                Comics co = new Comics();
                co.setmPic(joObject.getString("mPic"));
                co.setmContent(joObject.getString("mContent"));
                co.setmDirector(joObject.getString("mDirector"));
                co.setmLianzai(joObject.getString("mLianzai"));
                co.setUpdateMessage(joObject.getString("updateMessage"));
                co.setmLianzai(joObject.getString("mLianzai"));
                co.setmQid(joObject.getLong("mId"));
                co.setmTitle(joObject.getString("mTitle"));
                co.setmType1(joObject.getInt("mType1"));
                co.setmType5(joObject.getString("mType5"));
                co.setmHits(joObject.getInt("mHits"));
                list.add(co);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject params = new JSONObject();

    /**
     * 获取所有的作品
     *
     * @param typeId
     */
    private void initData(Integer typeId) {
        initParam(typeId);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
                getDataUrl, params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                updateList(response);
                adapter.notifyDataSetChanged();
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
     * 初始化查询参数
     *
     * @param typeId
     */
    private void initParam(Integer typeId) {
        try {
            params.put("pageSize", Utils.PageSize);
            params.put("currentPage", currentPage);
            params.put("orderBy", "");
            params.put("object", typeId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onclick(View view) {
        switch (view.getId()) {
            case R.id.btn_show_back:
                ShowBooksActivity.this.finish();
                break;

            default:
                break;
        }
    }

    private void updateData(final SingleLayoutListView pullToRefreshLayout,
                            final int n) {

        if (n == 0) {
            currentPage = 1;
        } else {
            ++currentPage;
        }
        
        try {
			params.put("currentPage", currentPage);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.POST,
                getDataUrl, params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                if (n != 1) {
                    if (list != null) {
                        list.clear();
                    }
                }
                updateList(response);
                adapter.notifyDataSetChanged();
                if (n == 0) {
                	Message _Msg = mHandler.obtainMessage(REFRESH_DATA_FINISH,list);
					mHandler.sendMessage(_Msg);
                } else if (n == 1) {
                	Message _Msg = mHandler.obtainMessage(LOAD_DATA_FINISH,list);
					mHandler.sendMessage(_Msg);
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
    
    public void onResume() {
    	super.onResume();
    	MobclickAgent.onResume(this);
    	String eventId = this.getIntent().getStringExtra("eventId");
		if(eventId != null){
			HashMap<String,String> map = new HashMap<String,String>();
			map.put("type", eventId);
			map.put("typeName", typeName); 
			MobclickAgent.onEvent(this, eventId, map); 
			if("sort_author".equals(eventId)){//作者排行统计分析
				MobclickAgent.onEventValue(this, eventId, map, 1);
			}
		}
    }
    public void onPause() {
    	super.onPause();
    	MobclickAgent.onPause(this);
    }
    	
}
