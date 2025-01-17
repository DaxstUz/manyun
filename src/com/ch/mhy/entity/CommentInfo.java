package com.ch.mhy.entity;

import android.annotation.SuppressLint;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 评论实体
 * @author xc.li
 * @date 2015-10-15
 */
public class CommentInfo implements Serializable{
	private static final long serialVersionUID = 1L;

	private long id;
	
	private int bigbookId; //关联漫画ID
	
	private String userId; //用户ID
	
	private String userType; //用户登陆类型1:QQ 2:微信 3:微博
	
	private String nickname; //昵称
	
	private String topic;   //主题内容
	
	private String imgUrl;  //图片URL
	
	private long createTime;//发表时间

	private int approveNum;
	private int discussNum;
	private int isApprove;
	
	private String comicName;
	private String comicUrl;
	
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getBigbookId() {
		return bigbookId;
	}

	public void setBigbookId(int bigbookId) {
		this.bigbookId = bigbookId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	@SuppressLint("SimpleDateFormat") 
	public String getCommentTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String date = formatter.format(new Date(createTime));
		return date;
	}
	
	public long getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public int getApproveNum() {
		return approveNum;
	}

	public void setApproveNum(int approveNum) {
		this.approveNum = approveNum;
	}

	public int getDiscussNum() {
		return discussNum;
	}

	public void setDiscussNum(int discussNum) {
		this.discussNum = discussNum;
	}

	public int getIsApprove() {
		return isApprove;
	}

	public void setIsApprove(int isApprove) {
		this.isApprove = isApprove;
	}

	public String getComicName() {
		return comicName;
	}

	public void setComicName(String comicName) {
		this.comicName = comicName;
	}

	public String getComicUrl() {
		return comicUrl;
	}

	public void setComicUrl(String comicUrl) {
		this.comicUrl = comicUrl;
	}

	@Override
	public String toString() {
		return "CommentInfo [id=" + id + ", bigbookId=" + bigbookId
				+ ", userId=" + userId + ", userType=" + userType
				+ ", nickname=" + nickname + ", topic=" + topic + ", imgUrl="
				+ imgUrl + ", createTime=" + createTime + ", approveNum="
				+ approveNum + ", discussNum=" + discussNum + ", isApprove="
				+ isApprove + ", comicName=" + comicName + ", comicUrl="
				+ comicUrl + "]";
	}

	
}
