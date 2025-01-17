package com.ch.mhy.activity.comment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ch.comm.resquest.AbsResponseData;
import com.ch.mhy.R;
import com.ch.mhy.activity.book.ShowDetailActivity;
import com.ch.mhy.activity.my.LoginActivity;
import com.ch.mhy.adapter.CommentAdapter;
import com.ch.mhy.entity.CommentInfo;
import com.ch.mhy.pulltorefresh.SingleLayoutListView;
import com.ch.mhy.pulltorefresh.SingleLayoutListView.OnLoadMoreListener;
import com.ch.mhy.pulltorefresh.SingleLayoutListView.OnRefreshListener;
import com.ch.mhy.util.UserUtil;

/**
 * 漫画评论
 * @author xc.li
 * @date 2015-10-14
 */
public class CommentActivity extends Activity implements OnClickListener, ICommentInfoUpdate {
	public static CommentActivity activity;
	private Context context;
	/**
	 * 评论列表
	 */
	private SingleLayoutListView comment_list;
	/**
	 * 评论数据
	 */
	private List<CommentInfo> dataList;
	
	private ImageView no_comment;
	/**
	 * 评论列表适配器
	 */
	private CommentAdapter adapter;
	/**
	 * 发表评论输入框
	 */
	private EditText commentContent;
	/**
	 * 总评论数展示
	 */
	private TextView comment_cnt;
	/**
	 * 漫画Id
	 */
	private String bigbookId = "0";
	
	private boolean isFirstBoot = true;
	/**
	 * 是否正在新增回复，用于在新增返回前同时刷新的操作
	 */
	private boolean isAdding = false;
	private int currentPage = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_comment);
		context = this;
		activity = this;
		
		LinearLayout root_view = (LinearLayout)this.findViewById(R.id.root_view);
		root_view.setOnClickListener(this);
		
		no_comment = (ImageView)this.findViewById(R.id.no_comment);
		
		//获取漫画ID
		bigbookId = this.getIntent().getExtras().getString("bigbookId");
		
		initBtn();
		initCommentCnt();
		initListView();
	}
	
	private void initCommentCnt() {
		// 查询漫画评论总数
		CommentDataUtil.loadCommentCnt(this, bigbookId, new AbsResponseData(null) {
			@Override
			public void dataBusi(Object data) {
				if(data != null){
					TextView comment_cnt = (TextView) CommentActivity.this.findViewById(R.id.comment_cnt);
					try {
						JSONObject jo = new JSONObject(data.toString());
						String val = jo.get("object").toString();
						if(!"".equals(val) && !"0".equals(val)){
							comment_cnt.setText("评论" + val);
						}else{
							no_comment.setVisibility(View.VISIBLE);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}

		});
	}
	
	/**
	 * 初始化评论数据列表
	 */
	private void initListView(){
		dataList = new ArrayList<CommentInfo>();
		adapter = new CommentAdapter(this, dataList);
		comment_list = (SingleLayoutListView)this.findViewById(R.id.comment_list);
		comment_list.setAdapter(adapter);
		
		comment_list.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				if(!isAdding){
					currentPage = 1;
					CommentDataUtil.loadCommentList(context, bigbookId, currentPage, new ResponseData(1, CommentInfo.class));
				}
			}
		});
		
		
		comment_list.setOnLoadListener(new OnLoadMoreListener() {
			@Override
			public void onLoadMore() {
				if(!isAdding){
					CommentDataUtil.loadCommentList(context, bigbookId, currentPage, new ResponseData(2, CommentInfo.class));
				}
			}
		});
		comment_list.setCanLoadMore(true);
		comment_list.setCanRefresh(true);
		comment_list.setAutoLoadMore(true);
		
		CommentDataUtil.loadCommentList(context, bigbookId, currentPage, new ResponseData(1, CommentInfo.class));
	}
	/**
	 * 初始化发表评论按钮及事件
	 */
	private void initBtn(){
		commentContent = (EditText)this.findViewById(R.id.commentContent);
		commentContent.clearFocus();
		//点击输入框事件
		commentContent.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!isFirstBoot && !UserUtil.isLogin(context) && commentContent.isFocused()){//未登录，跳转到登录界面
					Toast.makeText(context, "亲，您还未登录哦，请先登录吧~#~", Toast.LENGTH_LONG).show();
					commentContent.setFocusable(false);
					Intent  intent=new Intent(context, LoginActivity.class);
					startActivity(intent);
					commentContent.setFocusable(true);
					commentContent.setFocusableInTouchMode(true);
				}else{
					isFirstBoot = false;
				}
			}
		});
		
		final Bundle b = this.getIntent().getExtras();
		
		comment_cnt = (TextView)this.findViewById(R.id.comment_cnt);
		Button commentBtn = (Button)this.findViewById(R.id.commentBtn);
		commentBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(UserUtil.isLogin(context)){//未登录，跳转到登录界面
					String value = commentContent.getText().toString();
					if(value != null && value.length()>0){
						final CommentInfo info = new CommentInfo();
						info.setBigbookId(Integer.valueOf(bigbookId));
						info.setTopic(value);
						info.setNickname(UserUtil.getScreen_name(context));
						info.setImgUrl(UserUtil.getHeadurl(context));
						info.setUserId(UserUtil.getOpenId(context));
						info.setUserType(UserUtil.getPlatForm(context));
						info.setCreateTime(System.currentTimeMillis());
						info.setApproveNum(0);
						info.setDiscussNum(0);
						info.setIsApprove(0);
						
						info.setComicName(b.getString("bookName"));
						info.setComicUrl(b.getString("faceUrl"));
						
						isAdding = true;
						//保存评论
						CommentDataUtil.saveComment(context, info, new AbsResponseData(null){
							@Override
							public void dataBusi(Object data) {
								if(!"0".equals(data)){
									/*info.setId(Long.valueOf(data.toString()));
									dataList.add(0, info);
									adapter.notifyDataSetChanged();*/
									
									currentPage = 1;
									CommentDataUtil.loadCommentList(context, bigbookId, currentPage, new ResponseData(1, CommentInfo.class));
								
									no_comment.setVisibility(View.GONE);
									//更新评论总数
									String ccCnt = (String)comment_cnt.getText();
									if(ccCnt == null || "".equals(ccCnt)){
										ccCnt = "0";
									}
									comment_cnt.setText("评论"+(Integer.valueOf(ccCnt.replace("评论", ""))+1));
									ShowDetailActivity.activity.setCommentCnt(1);
								}else{ 
									Toast.makeText(context, "孩子，这么勤快啊，请休息10秒再评论吧#@~", Toast.LENGTH_LONG).show();
								}
								isAdding = false;
							}
						});
						
						commentContent.setText("");
						//隐藏输入键盘
						InputMethodManager imm = (InputMethodManager)  
							getSystemService(Context.INPUT_METHOD_SERVICE);  
						imm.hideSoftInputFromWindow(v.getWindowToken(), 0); 
					}else{
						Toast.makeText(context, "亲，请先输入您要说的话哦！", Toast.LENGTH_SHORT).show();
					}
				}else{
					Toast.makeText(context, "亲，您还未登录哦，请先登录吧~#~", Toast.LENGTH_LONG).show();
					Intent  intent=new Intent(context, LoginActivity.class);
					startActivity(intent);
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		if (v.getId() != R.id.commentContent) {//点击隐藏输入键盘
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}else{
			commentContent.setFocusable(true);
			commentContent.setFocusableInTouchMode(true);
			commentContent.requestFocus();
		}
	}
	/**
	 * 点击返回
	 * @param view
	 */
	public void onclick(View view){
		CommentActivity.this.finish();
	}
	/**
	 * 用于其他界面更新评论主题列表的点赞数及评论回复数
	 * @param position
	 * @param okCnt
	 * @param discussCnt
	 */
	public void updateView(int position, int okCnt, int discussCnt){
		CommentInfo info = dataList.get(position);
		info.setApproveNum(info.getApproveNum()+okCnt);
		if(okCnt>0){
			info.setIsApprove(1);
		}else if(okCnt<0){
			info.setIsApprove(0);
		}
		info.setDiscussNum(info.getDiscussNum()+discussCnt);
		adapter.notifyDataSetChanged();
	}
	/**
	 * type: 
	 * 1-评论列表数据；
	 * 数据处理回调
	 */
	class ResponseData extends AbsResponseData{
		private int type;
		public ResponseData(int type, Class<?> clz) {
			super(clz);
			this.type = type;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void dataBusi(Object data) {
			switch (type) {
			case 1:
				dataList.clear();
				dataList.addAll((List<CommentInfo>)data);
				if(dataList.size()>0){
					currentPage++;
				}
				adapter.notifyDataSetChanged();
				comment_list.onRefreshComplete(); // 加载更多完成
				comment_list.onLoadMoreComplete(); // 加载更多完成
				break;
			case 2:
				List<CommentInfo> list = (List<CommentInfo>)data;
				if(list.size()>0){
					dataList.addAll(list);
					adapter.notifyDataSetChanged();
					currentPage++;
				}
				comment_list.onLoadMoreComplete(); // 加载更多完成
				break;
			default:
				break;
			}
		}
	}
	
}
