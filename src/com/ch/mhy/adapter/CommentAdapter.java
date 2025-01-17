package com.ch.mhy.adapter;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ch.comm.resquest.AbsResponseData;
import com.ch.mhy.R;
import com.ch.mhy.activity.comment.CommentDataUtil;
import com.ch.mhy.activity.comment.CommentDetailActivity;
import com.ch.mhy.activity.my.LoginActivity;
import com.ch.mhy.entity.CommentInfo;
import com.ch.mhy.util.UserUtil;
import com.ch.mhy.util.Utils;
import com.ch.mhy.widget.CircleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CommentAdapter extends BaseAdapter {
	private List<CommentInfo> dataList;
	private Context context;
	private LayoutInflater inflater;
	private String today;
	
	public CommentAdapter(Context context, List<CommentInfo> dataList){
		this.context = context;
		this.dataList = dataList;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		today = formatter.format(System.currentTimeMillis());
	}
	
	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public Object getItem(int position) {
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@SuppressLint({ "ViewHolder", "SimpleDateFormat" })
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final CommentInfo bean = dataList.get(position);
		String headUrl = bean.getImgUrl();
		convertView = inflater.inflate(R.layout.comment_item, null, false);
		CircleImageView user_head = (CircleImageView)convertView.findViewById(R.id.user_head);
		TextView user_name = (TextView)convertView.findViewById(R.id.user_name);
		TextView user_date = (TextView)convertView.findViewById(R.id.user_date);
		TextView user_comment = (TextView)convertView.findViewById(R.id.user_comment);
		ImageView user_ok_img = (ImageView)convertView.findViewById(R.id.user_ok_img);
		final TextView user_ok_cnt = (TextView)convertView.findViewById(R.id.user_ok_cnt);
		final TextView user_msg_cnt = (TextView)convertView.findViewById(R.id.user_msg_cnt);
		
		
		user_head.setTag(headUrl);
		ImageLoader.getInstance().displayImage(headUrl, user_head, Utils.adapterOpt);
		user_name.setText(Html.fromHtml("<B>"+bean.getNickname()+"</B>"));
		String commentTime = bean.getCommentTime().replace(today, "今天");
		commentTime = commentTime.replace(" ", " <font color=\"#f97f82\">");
		user_date.setText(Html.fromHtml(commentTime+"</font>"));
		user_comment.setText(bean.getTopic());
		
		int isGiveMeFine = bean.getIsApprove();
		user_ok_cnt.setText(""+bean.getApproveNum());
		user_msg_cnt.setText(""+bean.getDiscussNum());
		if(1 == isGiveMeFine){
			user_ok_img.setImageResource(R.drawable.ok_hand_red);
		}
		
		LinearLayout comment_layout = (LinearLayout)convertView.findViewById(R.id.comment_layout);
		LinearLayout ok_img_layout = (LinearLayout)convertView.findViewById(R.id.ok_img_layout);
		LinearLayout user_msg_layout = (LinearLayout)convertView.findViewById(R.id.user_msg_layout);
		
		comment_layout.setOnClickListener(new CommentClick(position, bean));
		ok_img_layout.setOnClickListener(new OkClick(bean, user_ok_img));
		user_msg_layout.setOnClickListener(new CommentClick(position, bean));
		
		return convertView;
	}

	/**
	 * 
	 * @author xc.li
	 * @date 2015-10-12
	 */
	class CommentClick implements OnClickListener {
		private int position;
		private CommentInfo bean;

		public CommentClick(int position, CommentInfo bean) {
			this.position = position;
			this.bean = bean;
		}

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(context, CommentDetailActivity.class);
			intent.putExtra("fromFlag", "CommentActivity");
			intent.putExtra("comment", bean);
			intent.putExtra("position", position);
			context.startActivity(intent);
		}

	}
	
	class OkClick implements OnClickListener{
		private CommentInfo bean;
		private ImageView user_ok_img;
		public OkClick(CommentInfo bean, ImageView user_ok_img){
			this.bean = bean;
			this.user_ok_img = user_ok_img;
		}
		@Override
		public void onClick(View v) {//点赞操作
			if(UserUtil.isLogin(context)){//已登录
				int isGiveMeFine = bean.getIsApprove();
				LinearLayout pll = (LinearLayout)user_ok_img.getParent();
				TextView user_ok_cnt = (TextView)pll.getChildAt(1);
				String okCnt = (String)user_ok_cnt.getText();
				if(1 == isGiveMeFine){//删除点赞记录
					bean.setIsApprove(0);
					bean.setApproveNum(bean.getApproveNum()-1);
					user_ok_img.setImageResource(R.drawable.ok_hand_yellow);
					user_ok_cnt.setText(bean.getApproveNum()+"");
					long topicId = bean.getId();
					CommentDataUtil.delOkComment(context, topicId, new AbsResponseData(null){
						@Override
						public void dataBusi(Object data) {
							
						}
					});
				}else{//增加点赞记录
					bean.setIsApprove(1);
					user_ok_img.setImageResource(R.drawable.ok_hand_red);
					bean.setApproveNum(bean.getApproveNum()+1);
					user_ok_cnt.setText(bean.getApproveNum()+"");
					
					HashMap<String, Object> params = new HashMap<String, Object>();
					long topicId = bean.getId();
					params.put("topicId", topicId*1l);
					CommentDataUtil.saveOkComment(context, params, new AbsResponseData(null){
						@Override
						public void dataBusi(Object data) {
							
						}
					});
				}
			}else{//未登录，跳转到登录界面
				Intent  intent=new Intent(context, LoginActivity.class);
				context.startActivity(intent);
			}
		}
		
	}
}
