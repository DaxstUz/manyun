package com.ch.mhy.adapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ch.mhy.R;
import com.ch.mhy.entity.ComicsDetail;
import com.ch.mhy.interf.UpdateBg;

public class BookDownAdapter extends BaseAdapter{

    private List<ComicsDetail> list;

    private LayoutInflater inflater;

    private UpdateBg ub;
    
    public BookDownAdapter(Context content, List<ComicsDetail> list,UpdateBg ub) {
        // TODO Auto-generated constructor stub
        this.list = list;
        this.ub=ub;
        inflater = LayoutInflater.from(content);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

    	final View cv = inflater.inflate(R.layout.book_select_item, null, false);

    	final LinearLayout ll_book_down=(LinearLayout) cv.findViewById(R.id.ll_book_down);
    	TextView tv_bookselect_name = (TextView) cv.findViewById(R.id.tv_bookselect_name);
//    	TextView tv_bookselect_time = (TextView) cv.findViewById(R.id.tv_bookselect_time);
    	
    	String name = list.get(position).getmName();
	   	if(name!=null && name.length()>12){//处理标题过长展示问题，以第一个空格截取
	   		name = name.substring(0,12);
	   	}
   	 
//	   	Log.d("tag", "list.get(position).isFlag()  "+list.get(position).isFlag());
	   	
    	if(list.get(position).isFlag()){
    		ll_book_down.setBackgroundResource(R.drawable.down_select_item);
    		tv_bookselect_name.setTextColor(Color.WHITE);
//    		tv_bookselect_time.setTextColor(Color.WHITE);
    	}
    	
    	if(list.get(position).isIsdown()){
    		ll_book_down.setBackgroundResource(R.drawable.downed_select_item);
    		tv_bookselect_name.setTextColor(Color.WHITE);
//    		tv_bookselect_time.setTextColor(Color.WHITE);
    	}
        tv_bookselect_name.setText(name);
        
        String createTime = list.get(position).getCreateTime();
        String sysDate = list.get(position).getSysDate();
//      tv_bookselect_time.setText(createTime);
        //以下为章节做标"新"处理
	     long days = getDaysBetweenNow(createTime, sysDate);
	     if(days<=30){//判断是否是最新上架的章节，是则标识为‘新’,以更新时间为准，30天内的为新更新
	     	ImageView imgView = new ImageView(cv.getContext());
	     	imgView.setBackgroundResource(R.drawable.new_chp);
	     	ll_book_down.addView(imgView, 0);
	     	//调整标题栏位置
	     	LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	     	lllp.leftMargin = -32;
	     	lllp.gravity = Gravity.CENTER;
	     	tv_bookselect_name.setLayoutParams(lllp);
	    }
        cv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(ub!=null&&!list.get(position).isIsdown()){
					ub.update(position);
				}
			}
		});
        
        return cv;
    }

	/**
	 * 获取与当前日期的天数差
	 * @param strDate
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	private long getDaysBetweenNow(String strDate, String sysDate) {
		long days = 0;
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date strtodate = formatter.parse(strDate);
			Date sysdate = formatter.parse(sysDate);
			long milliseconds = sysdate.getTime() - strtodate.getTime();
	        days = milliseconds / 86400000L;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return days;
	}
}
