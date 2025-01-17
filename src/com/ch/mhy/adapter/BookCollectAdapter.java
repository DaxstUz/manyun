package com.ch.mhy.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ch.mhy.R;
import com.ch.mhy.entity.ComicsDetail;
import com.ch.mhy.util.UrlConstant;
import com.ch.mhy.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class BookCollectAdapter extends BaseAdapter {

	private List<ComicsDetail> list;

	private LayoutInflater inflater;

	public BookCollectAdapter(Context content, List<ComicsDetail> list) {
		// TODO Auto-generated constructor stub
		this.list = list;
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
	public View getView(int position, View convertView, ViewGroup parent) {
		String imgUrl = list.get(position).getmPic();
			View view = inflater.inflate(R.layout.adapter_collect_item, null, false);
			view.setTag(imgUrl);
			TextView tv_bookreaded_reading = (TextView) view.findViewById(R.id.tv_bookreaded_reading);

			String upmsg="更新到";
			if(list.get(position).getUpdateMessage()!=null){
				 upmsg+=list.get(position).getUpdateMessage() ;
				
			}
			tv_bookreaded_reading.setText(upmsg);
					
			
			TextView tv_bookreaded_name = (TextView) view.findViewById(R.id.tv_bookreaded_name);
			tv_bookreaded_name.setText(list.get(position).getmTitle());
			TextView tv_pf = (TextView) view.findViewById(R.id.tv_pf);
			tv_pf.setText("评分:"+list.get(position).getmFenAll());
			//
			TextView tv_bookreaded_author = (TextView) view.findViewById(R.id.tv_bookreaded_author);
			tv_bookreaded_author.setText("作者：" + list.get(position).getmDirector());

			CheckBox cb_collect=(CheckBox) view.findViewById(R.id.cb_collect);
			cb_collect.setChecked(list.get(position).isIsselect());
			
			LinearLayout ll_collect=(LinearLayout) view.findViewById(R.id.ll_collect);
			if (list.get(position).isFlag()) {
				ll_collect.removeViewAt(0);
			}
			ImageView iv_bookreaded = (ImageView) view.findViewById(R.id.iv_bookreaded);
			ImageLoader.getInstance().displayImage(
					imgUrl, iv_bookreaded,Utils.options3,new ImageLoadingListener() {
						
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							view.setBackgroundResource(R.drawable.ic_loading_small);
						}
						
						@Override
						public void onLoadingFailed(String imageUri, View view,
								FailReason failReason) {
						}
						
						@Override
						public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						}
						
						@Override
						public void onLoadingCancelled(String imageUri, View view) {
						}
					});
			
		return view;
	}
}
