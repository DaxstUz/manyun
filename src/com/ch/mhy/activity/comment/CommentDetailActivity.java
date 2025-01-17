package com.ch.mhy.activity.comment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
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
import com.ch.mhy.activity.my.LoginActivity;
import com.ch.mhy.adapter.CommentReplyAdapter;
import com.ch.mhy.entity.CommentInfo;
import com.ch.mhy.entity.CommentOkInfo;
import com.ch.mhy.entity.CommentReplyInfo;
import com.ch.mhy.fragment.IJoinFragment;
import com.ch.mhy.fragment.ISayFragment;
import com.ch.mhy.pulltorefresh.SingleLayoutListView;
import com.ch.mhy.pulltorefresh.SingleLayoutListView.OnLoadMoreListener;
import com.ch.mhy.pulltorefresh.SingleLayoutListView.OnRefreshListener;
import com.ch.mhy.util.UrlConstant;
import com.ch.mhy.util.UserUtil;
import com.ch.mhy.util.Utils;
import com.ch.mhy.widget.ChHorizontalScrollView;
import com.ch.mhy.widget.CircleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 评论主题及回复
 * @author xc.li
 * @date 2015-10-14
 */
public class CommentDetailActivity extends Activity implements OnClickListener{
	private Context context;
	/**
	 * 回复输入框
	 */
	private EditText commentContent;
	/**
	 * 用户回复数
	 */
	private TextView user_msg_cnt;
	/**
	 * 回复列表
	 */
	private SingleLayoutListView comment_list;
	/**
	 * 回复数据
	 */
	private List<CommentReplyInfo> dataList;
	/**
	 * 回复列表适配器
	 */
	private CommentReplyAdapter adapter;
	/**
	 * 点赞用户列表
	 */
	private LinearLayout ok_user_list_view1;
	private ChHorizontalScrollView hScrollView;
	
	private long topicId;
	private int position;
	private String fromFlag;
	private boolean isFirstBoot = true;
	/**
	 * 是否正在新增回复，用于在新增返回前同时刷新的操作
	 */
	private boolean isAdding = false;
	private int currentPage = 1, okUserPage = 1;
	
	private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				CommentDataUtil.loadOkCommentList(context, topicId, okUserPage, new AbsResponseData(CommentOkInfo.class){
					@SuppressWarnings("unchecked")
					@Override
					public void dataBusi(Object data) {
						List<CommentOkInfo> list = (List<CommentOkInfo>)data;
						if(list.size()>0){
							for(int i=0; i<list.size(); i++){
								View child = createUserView(list.get(i).getImgUrl()); 
								ok_user_list_view1.addView(child);
							}
							okUserPage++;
						}
						hScrollView.setIsQry(false);
					}
				});
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_comment_detail);
		context = this;
		
		commentContent = (EditText)this.findViewById(R.id.commentContent);
		
		LinearLayout root_view = (LinearLayout)this.findViewById(R.id.root_view);
		root_view.setOnClickListener(this);
		
		initBtn();
		initCommentView();
		initListView();
	}
	/**
	 * 初始化评论主题
	 */
	private void initCommentView(){
		Bundle b = this.getIntent().getExtras();
		CommentInfo bean = (CommentInfo)b.get("comment");
		
		topicId = bean.getId();
		position = b.getInt("position");
		fromFlag = b.getString("fromFlag");
				
		ImageView user_head = (ImageView)this.findViewById(R.id.user_head);
		TextView user_name = (TextView)this.findViewById(R.id.user_name);
		TextView user_date = (TextView)this.findViewById(R.id.user_date);
		TextView user_comment = (TextView)this.findViewById(R.id.user_comment);
		ImageView user_ok_img = (ImageView)this.findViewById(R.id.user_ok_img);
		TextView user_ok_cnt = (TextView)this.findViewById(R.id.user_ok_cnt);
		TextView user_msg_cnt = (TextView)this.findViewById(R.id.user_msg_cnt);
		
		String headUrl = bean.getImgUrl();
		
		ImageLoader.getInstance().displayImage(headUrl, user_head, Utils.adapterOpt);
		user_name.setText(Html.fromHtml("<B>"+bean.getNickname()+"</B>"));
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String today = formatter.format(System.currentTimeMillis());
		String commentTime = bean.getCommentTime().replace(today, "今天");
		commentTime = commentTime.replace(" ", " <font color=\"#f97f82\">");
		user_date.setText(Html.fromHtml(commentTime+"</font>"));
		user_comment.setText(bean.getTopic());
		user_ok_cnt.setText(bean.getApproveNum()+"");
		user_msg_cnt.setText(bean.getDiscussNum()+"");
		int isGiveMeFine = bean.getIsApprove();
		if(1 == isGiveMeFine){//该查看用户是否已点赞
			user_ok_img.setImageResource(R.drawable.ok_hand_red);
		}
		LinearLayout ok_img_layout = (LinearLayout)this.findViewById(R.id.ok_img_layout);
		ok_img_layout.setOnClickListener(new OkClick(bean, user_ok_img));
		/*
		LinearLayout user_msg_layout = (LinearLayout)this.findViewById(R.id.user_msg_layout);
		
		user_msg_layout.setOnClickListener(new CommentClick(bean));*/
	}
	/**
	 * 初始化回复评论按钮及事件
	 */
	private void initBtn(){
		commentContent = (EditText)this.findViewById(R.id.commentContent);
		commentContent.clearFocus();
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
					commentContent.setFocusable(true);
					commentContent.setFocusableInTouchMode(true);
				}
			}
		});
		user_msg_cnt = (TextView)this.findViewById(R.id.user_msg_cnt);
		Button commentBtn = (Button)this.findViewById(R.id.commentBtn);
		commentBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(UserUtil.isLogin(context)){//已登录
					String value = commentContent.getText().toString();
					if(value != null && value.length()>0){
						final CommentReplyInfo info = new CommentReplyInfo();
						info.setTopicId(topicId);
						info.setDiscuss(value);
						info.setNickname(UserUtil.getScreen_name(context));
						info.setImgUrl(UserUtil.getHeadurl(context));
						info.setUserId(UserUtil.getOpenId(context));
						info.setUserType(UserUtil.getPlatForm(context));
						info.setCreateTime(System.currentTimeMillis());
						
						isAdding = true;
						CommentDataUtil.saveCommentReply(context, info, new AbsResponseData(null){
							@Override
							public void dataBusi(Object data) {
								if(!"0".equals(data)){
									/*info.setId(Long.valueOf(data.toString()));
									dataList.add(0, info);
									adapter.notifyDataSetChanged();*/
									currentPage = 1;
									CommentDataUtil.loadCommentReplyList(context, topicId, currentPage, new ResponseData(1, CommentReplyInfo.class));
									String ccCnt = (String)user_msg_cnt.getText();
									user_msg_cnt.setText(""+(Integer.valueOf(ccCnt)+1));
									callbackUpdateUi(0, 1);
								}else{ 
									Toast.makeText(context, "孩子，这么卖力啊，请休息10秒再回复吧#@~", Toast.LENGTH_LONG).show();
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
	/**
	 * 初始化回复列表、点赞用户列表
	 */
	private void initListView(){
		dataList = new ArrayList<CommentReplyInfo>();
		adapter = new CommentReplyAdapter(this, dataList);
		comment_list = (SingleLayoutListView)this.findViewById(R.id.comment_list);
		comment_list.setAdapter(adapter);
		comment_list.setOnRefreshListener(new OnRefreshListener() {
			@Override
			public void onRefresh() {
				if(!isAdding){
					currentPage = 1;
					CommentDataUtil.loadCommentReplyList(context, topicId, currentPage, new ResponseData(1, CommentReplyInfo.class));
				}
			}
		});

		comment_list.setOnLoadListener(new OnLoadMoreListener() {
			@Override
			public void onLoadMore() {
				if(!isAdding){
					CommentDataUtil.loadCommentReplyList(context, topicId, currentPage, new ResponseData(2, CommentReplyInfo.class));
				}
			}
		});
		comment_list.setCanLoadMore(true);
		comment_list.setCanRefresh(true);
		comment_list.setAutoLoadMore(true);
		CommentDataUtil.loadCommentReplyList(context, topicId, currentPage, new ResponseData(1, CommentReplyInfo.class));
		
		hScrollView = (ChHorizontalScrollView)this.findViewById(R.id.ok_user_sv);
		hScrollView.setHandler(mHandler);
		ok_user_list_view1 = (LinearLayout)this.findViewById(R.id.ok_user_list_view);
		CommentDataUtil.loadOkCommentList(context, topicId, okUserPage, new AbsResponseData(CommentOkInfo.class){
			@SuppressWarnings("unchecked")
			@Override
			public void dataBusi(Object data) {
				List<CommentOkInfo> list = (List<CommentOkInfo>)data;
				if(list.size()>0){
					for(int i=0; i<list.size(); i++){
						View child = createUserView(list.get(i).getImgUrl()); 
						ok_user_list_view1.addView(child);
					}
					okUserPage++;
				}
				hScrollView.setIsQry(false);
			}
		});
	}
	
	private View createUserView(String headUrl){
		LayoutInflater inflater = this.getLayoutInflater();
		View view = inflater.inflate(R.layout.comment_ok_user_item, null, false);
		view.setTag(headUrl);
		CircleImageView user_head = (CircleImageView)view.findViewById(R.id.ok_user_head);
		ImageLoader.getInstance().displayImage(headUrl, user_head, Utils.adapterOpt);
		return view;
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() != R.id.commentContent) {//点击隐藏输入键盘
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}
	}
	
	public void onclick(View view){
		CommentDetailActivity.this.finish();
	}
	
	/**
	 * 点击评论图片，弹出输入键盘
	 * @author xc.li
	 * @date 2015-10-12
	 */
	class CommentClick implements OnClickListener{
		private CommentInfo bean;
		public CommentClick(CommentInfo bean){
			this.bean = bean;
		}
		@Override
		public void onClick(View v) {
			if(UserUtil.isLogin(context)){//已登录，弹出评论键盘，输入回复信息
				commentContent.setFocusable(true);
				commentContent.setFocusableInTouchMode(true);
				commentContent.requestFocus();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
			}else{//未登录，跳转到登录界面
				Toast.makeText(context, "亲，您还未登录哦，请先登录吧~#~", Toast.LENGTH_LONG).show();
				Intent  intent=new Intent(context, LoginActivity.class);
				startActivity(intent);
			}
		}
		
	}

	/**
	 * 回调更新UI
	 * @param okCnt
	 * @param discussCnt
	 */
	private void callbackUpdateUi(int okCnt, int discussCnt) {
		if("CommentActivity".equals(fromFlag)){
			CommentActivity.activity.updateView(position, okCnt, discussCnt);
		}else if("IJoinFragment".equals(fromFlag)){
			IJoinFragment.activity.updateView(position, okCnt, discussCnt);
		}else if("ISayFragment".equals(fromFlag)){
			ISayFragment.activity.updateView(position, okCnt, discussCnt);
		}
	}
	
	/**
	 * 点赞按钮事件
	 * @author xc.li
	 * @date 2015-10-13
	 */
	class OkClick implements OnClickListener {
		private CommentInfo bean;
		private ImageView user_ok_img;

		public OkClick(CommentInfo bean, ImageView user_ok_img) {
			this.bean = bean;
			this.user_ok_img = user_ok_img;
		}

		@Override
		public void onClick(View v) {// 点赞操作
			if(UserUtil.isLogin(context)){//已登录
				int isGiveMeFine = bean.getIsApprove();
				//LinearLayout pll = (LinearLayout) user_ok_img.getParent();
				//TextView user_ok_cnt = (TextView) pll.getChildAt(0);
				TextView user_ok_cnt = (TextView) CommentDetailActivity.this.findViewById(R.id.user_ok_cnt);
				String okCnt = (String) user_ok_cnt.getText();
				if (1 == isGiveMeFine) {//是否已点赞
					bean.setIsApprove(0);
					user_ok_img.setImageResource(R.drawable.ok_hand_yellow);
					user_ok_cnt.setText((Integer.valueOf(okCnt) - 1) + "");
					
					CommentDataUtil.delOkComment(context, bean.getId(), new AbsResponseData(null){
						@Override
						public void dataBusi(Object data) {
							callbackUpdateUi(-1, 0);
							for(int i=0; i<ok_user_list_view1.getChildCount(); i++){
								View v = ok_user_list_view1.getChildAt(i);
								if(v.getTag().equals(UserUtil.getHeadurl(context))){
									ok_user_list_view1.removeView(v);
								}
							}
						}
					});
				} else {
					bean.setIsApprove(1);
					user_ok_img.setImageResource(R.drawable.ok_hand_red);
					user_ok_cnt.setText((Integer.valueOf(okCnt) + 1) + "");
					
					HashMap<String, Object> params = new HashMap<String, Object>();
					params.put("topicId", bean.getId());
					CommentDataUtil.saveOkComment(context, params, new AbsResponseData(null){
						@Override
						public void dataBusi(Object data) {
							callbackUpdateUi(1, 0);
							View child = createUserView(UserUtil.getHeadurl(context)); 
							ok_user_list_view1.addView(child);
						}
					});
				}
			}else{//未登录，跳转到登录界面
				Intent  intent=new Intent(context, LoginActivity.class);
				startActivity(intent);
			}
		}
	}
	
	/**
	 * type:
	 * 1-评论回复列表；3-点赞用户列表
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
				dataList.addAll((List<CommentReplyInfo>)data);
				View reply_divider = CommentDetailActivity.this.findViewById(R.id.reply_divider);
				if(dataList.size()==0){
					reply_divider.setVisibility(View.GONE);
				}else{
					reply_divider.setVisibility(View.VISIBLE);
					currentPage++;
				}
				adapter.notifyDataSetChanged();
				comment_list.onRefreshComplete(); // 加载更多完成
				comment_list.onLoadMoreComplete(); // 加载更多完成
				break;
			case 2:
				List<CommentReplyInfo> list = (List<CommentReplyInfo>)data;
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
