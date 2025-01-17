/*** Eclipse Class Decompiler plugin, copyright (c) 2012 Chao Chen (cnfree2000@hotmail.com) ***/
package com.ch.mhy.entity;

import java.io.Serializable;
import java.util.List;

public class Comics implements Serializable {

    //晴天漫画ID
    private Long mQid;

    //漫画标题
    private String mTitle;

    //漫画作者
    private String mDirector;

    //联载
    private String mLianzai;

    //内容简洁
    private String mContent;

    //分类id号
    private Integer mType1;

    //创建日期
    private String mDate;

    //漫画封面
    private String mPic;

    //漫画日期
    private String mDate2;
    
    //漫画 更新
    private String updateMessage;

    //最后更新时间
    private Integer mLast;

    //是否已经完结  0表示完结   1表示未完结
    private String mType5;

    //评论人数
    private Integer mFenNumb;

    //评论分数
    private Integer mFenAll;

    //人气
    private Integer mHits;

    private Integer mTotal;//总共多少话
    
    private Integer gradescore=0;
     
    //本地化信息
  //是否已本地化，默认0-未本地化
  	private String mIf;
  	
  	//本地地址
  	private String localUrl;
  	
  	//服务器IP
  	private String ip;
  	
  	//端口号
  	private String port;
    
  	/*
  	 * app下载信息
  	 */
  	private String firstTurnPath;
  	private String firstTurnType;
  	private String firstTurnTitle;
  	
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

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public Integer getGradescore() {
		return gradescore;
	}

	public void setGradescore(Integer gradescore) {
		this.gradescore = gradescore;
	}

	List<ComicsDetail> adapter; //章节数


    public List<ComicsDetail> getAdapter() {
		return adapter;
	}

	public void setAdapter(List<ComicsDetail> adapter) {
		this.adapter = adapter;
	}

	private String picPath;//轮播图

    public Long getmQid() {
        return mQid;
    }

    public void setmQid(Long mQid) {
        this.mQid = mQid;
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

    public String getmLianzai() {
        return mLianzai;
    }

    public void setmLianzai(String mLianzai) {
        this.mLianzai = mLianzai;
    }

    public String getmContent() {
        return mContent;
    }

    public void setmContent(String mContent) {
        this.mContent = mContent;
    }

    public Integer getmType1() {
        return mType1;
    }

    public void setmType1(Integer mType1) {
        this.mType1 = mType1;
    }


    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }

    public String getmPic() {
        return mPic;
    }

    public void setmPic(String mPic) {
        this.mPic = mPic;
    }


    public Integer getmLast() {
        return mLast;
    }

    public void setmLast(Integer mLast) {
        this.mLast = mLast;
    }

    public String getmType5() {
        return mType5;
    }

    public void setmType5(String mType5) {
        this.mType5 = mType5;
    }

    public Integer getmFenNumb() {
        return mFenNumb;
    }

    public void setmFenNumb(Integer mFenNumb) {
        this.mFenNumb = mFenNumb;
    }

    public Integer getmFenAll() {
        return mFenAll;
    }

    public void setmFenAll(Integer mFenAll) {
        this.mFenAll = mFenAll;
    }

    public Integer getmHits() {
        return mHits;
    }

    public void setmHits(Integer mHits) {
        this.mHits = mHits;
    }

    public String getmDate2() {
        return mDate2;
    }

    public void setmDate2(String mDate2) {
        this.mDate2 = mDate2;
    }

    public Integer getmTotal() {
        return mTotal;
    }

    public void setmTotal(Integer mTotal) {
        this.mTotal = mTotal;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

	public String getUpdateMessage() {
		return updateMessage;
	}

	public void setUpdateMessage(String updateMessage) {
		this.updateMessage = updateMessage;
	}

	public String getFirstTurnPath() {
		return firstTurnPath;
	}

	public void setFirstTurnPath(String firstTurnPath) {
		this.firstTurnPath = firstTurnPath;
	}

	public String getFirstTurnType() {
		return firstTurnType;
	}

	public void setFirstTurnType(String firstTurnType) {
		this.firstTurnType = firstTurnType;
	}

	public String getFirstTurnTitle() {
		return firstTurnTitle;
	}

	public void setFirstTurnTitle(String firstTurnTitle) {
		this.firstTurnTitle = firstTurnTitle;
	}


	
}