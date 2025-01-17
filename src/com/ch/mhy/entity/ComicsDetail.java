/*** Eclipse Class Decompiler plugin, copyright (c) 2012 Chao Chen (cnfree2000@hotmail.com) ***/
package com.ch.mhy.entity;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;

import com.umeng.message.proguard.T;

public class ComicsDetail implements Serializable, Comparator<ComicsDetail>{
	public String bigbookId;
	
	// 晴天漫画话ID
	public Integer mQid;

	// 晴天漫画本ID
	public Integer mId;

	// 话名称
	public String mName;

	// 话请求路径
	public String mUrl;

	// 话排序
	public Integer mNo;
	
	// 话排序前
	public Integer pmNo=-10000;
	
	// 话排序后
	public Integer nmNo=10000;

	public String CreateTime;
	
	public String readTime;//阅读时间

	public String mTitle;

	// 漫画作者
	public String mDirector;

	// 漫画封面
	public String mPic;

	public Integer mTotal;// 总共多少话

	// 联载
	public String mLianzai;

	// 分类id号
	public Integer mType1;

	// 内容简洁
	public String mContent;

	public boolean flag; // 是否被选中 在我的收藏里 表示是否是编辑状态

	private Double gradescore = 0.0; // 评分

	private Integer totalPage; // 总共多少页

	private String partSize; // 这一话的大小

	// 评论分数
	public Integer mFenAll;
	
	//漫画 更新
    public String updateMessage;

	// 本地化信息
	// 是否已本地化，默认0-未本地化
	private String mIf;

	// 本地地址
	public String localUrl;

	// 服务器IP
	private String ip;

	// 端口号
	public String port;

	public Integer rIndex = 0;// 看到第几话   默认是第一话
	
	//最大排序号
	public Integer maxNo;
	
	//最小排序号
	public Integer minNo;

	private boolean isdown; //是否被下载

	//系统时间
	private String sysDate;
		
	
	private boolean isselect; //是否选中checkbox
	
	
	public Integer getrIndex() {
		return rIndex;
	}

	public void setrIndex(Integer rIndex) {
		this.rIndex = rIndex;
	}

	public String getmIf() {
		return mIf;
	}

	public void setmIf(String mIf) {
		this.mIf = mIf;
	}

	public String getLocalUrl() {
		return localUrl;
	}

	public void setLocalUrl(String localUrl) {
		this.localUrl = localUrl;
	}

	public String getIp() {
		return ip;
	}

	public String getBigbookId() {
		return bigbookId;
	}

	public void setBigbookId(String bigbookId) {
		this.bigbookId = bigbookId;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public Integer getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(Integer totalPage) {
		this.totalPage = totalPage;
	}

	public String getPartSize() {
		return partSize;
	}

	public void setPartSize(String partSize) {
		this.partSize = partSize;
	}

	public Double getGradescore() {
		return gradescore;
	}

	public void setGradescore(Double gradescore) {
		this.gradescore = gradescore;
	}

	public String getCreateTime() {
		return CreateTime;
	}

	public void setCreateTime(String createTime) {
		CreateTime = createTime;
	}

	public Integer getmId() {
		return mId;
	}

	public void setmId(Integer mId) {
		this.mId = mId;
	}

	public Integer getmQid() {
		return mQid;
	}

	public void setmQid(Integer mQid) {
		this.mQid = mQid;
	}

	public Integer getmNo() {
		return mNo;
	}

	public void setmNo(Integer mNo) {
		this.mNo = mNo;
	}

	public String getmUrl() {
		return mUrl;
	}

	public void setmUrl(String mUrl) {
		this.mUrl = mUrl;
	}

	public String getmName() {
		return mName;
	}

	public void setmName(String mName) {
		this.mName = mName;
	}

	public String getmTitle() {
		return mTitle;
	}

	public void setmTitle(String mTitle) {
		this.mTitle = mTitle;
	}

	public String getmDirector() {
		return mDirector;
	}

	public void setmDirector(String mDirector) {
		this.mDirector = mDirector;
	}

	public String getmPic() {
		return mPic;
	}

	public void setmPic(String mPic) {
		this.mPic = mPic;
	}

	public Integer getmTotal() {
		return mTotal;
	}

	public void setmTotal(Integer mTotal) {
		this.mTotal = mTotal;
	}

	public String getmLianzai() {
		return mLianzai;
	}

	public void setmLianzai(String mLianzai) {
		this.mLianzai = mLianzai;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public Integer getmType1() {
		return mType1;
	}

	public void setmType1(Integer mType1) {
		this.mType1 = mType1;
	}

	public String getmContent() {
		return mContent;
	}

	public void setmContent(String mContent) {
		this.mContent = mContent;
	}

	public Integer getmFenAll() {
		return mFenAll;
	}

	public void setmFenAll(Integer mFenAll) {
		this.mFenAll = mFenAll;
	}

	public Integer getMaxNo() {
		return maxNo;
	}

	public void setMaxNo(Integer maxNo) {
		this.maxNo = maxNo;
	}

	public boolean isIsdown() {
		return isdown;
	}

	public void setIsdown(boolean isdown) {
		this.isdown = isdown;
	}

	public String getSysDate() {
		return sysDate;
	}

	public void setSysDate(String sysDate) {
		this.sysDate = sysDate;
	}
	
	public Integer getMinNo() {
		return minNo;
	}

	public void setMinNo(Integer minNo) {
		this.minNo = minNo;
	}

	@Override
	public int compare(ComicsDetail lhs, ComicsDetail rhs) {
		// TODO Auto-generated method stub
		if(lhs.getmNo()>rhs.getmNo()){
			return 1;
		}
		if(lhs.getmNo()<rhs.getmNo()){
			return -1;
		}
		return 0;
	}

	public String getReadTime() {
		return readTime;
	}

	public void setReadTime(String readTime) {
		this.readTime = readTime;
	}

	public boolean isIsselect() {
		return isselect;
	}

	public void setIsselect(boolean isselect) {
		this.isselect = isselect;
	}

	public Integer getPmNo() {
		return pmNo;
	}

	public void setPmNo(Integer pmNo) {
		this.pmNo = pmNo;
	}

	public Integer getNmNo() {
		return nmNo;
	}

	public void setNmNo(Integer nmNo) {
		this.nmNo = nmNo;
	}

	public String getUpdateMessage() {
		return updateMessage;
	}

	public void setUpdateMessage(String updateMessage) {
		this.updateMessage = updateMessage;
	}

	
}