package com.ch.mhy.adapter;

import java.util.List;

import com.ch.mhy.R;
import com.ch.mhy.activity.my.WonRecActivity;
import com.ch.mhy.entity.WonRecEntity;
import com.ch.mhy.service.DownAPKService;
import com.ch.mhy.util.UrlConstant;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 精彩推荐adpater
 * 
 * @author DaxstUz 2416738717 2015年8月31日
 *
 */
public class WonRecAdapter extends BaseAdapter {
	private Context context;
	private List<WonRecEntity> list;

	public WonRecAdapter(Context context, List<WonRecEntity> list) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.list = list;
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = LayoutInflater.from(context).inflate(
				R.layout.adapter_wonrec_item, null, false);

		/*
		 * 设置图片的显示宽、高
		 */
		ImageView iv_appinfo = (ImageView) view.findViewById(R.id.iv_appinfo);
		LayoutParams linearParams = iv_appinfo.getLayoutParams(); // 取控件textView当前的布局参数
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		linearParams.width = wm.getDefaultDisplay().getWidth();
		linearParams.height = wm.getDefaultDisplay().getWidth() * 100 / 225;
		iv_appinfo.setLayoutParams(linearParams);

		ImageLoader.getInstance().displayImage(
				UrlConstant.Ip1 + UrlConstant.Port1
						+ list.get(position).getPicUrl(), iv_appinfo);

		TextView tv_wonname = (TextView) view.findViewById(R.id.tv_wonname);
		TextView tv_content = (TextView) view.findViewById(R.id.tv_content);
		tv_content.setText(list.get(position).getContent());
		tv_wonname.setText(list.get(position).getName());
		LinearLayout ll_btn_down = (LinearLayout) view
				.findViewById(R.id.ll_btn_down);

		if (list.get(position).getUrl() != null
				&& list.get(position).getUrl().length() > 0) {
			ll_btn_down.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					DialogInterface.OnClickListener dialog = new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (which == DialogInterface.BUTTON_POSITIVE) {// 确定按键的点击事件
								Intent intentt = new Intent(context,
										DownAPKService.class);
								intentt.putExtra("apk_url", list.get(position)
										.getUrl());
								context.startService(intentt);
								Toast.makeText(context, "正在后台进行下载，稍后会自动安装",
										Toast.LENGTH_SHORT).show();
							} else if (which == DialogInterface.BUTTON_NEGATIVE) {// 取消按键的点击事件
							}
						}
					};
					new AlertDialog.Builder(context).setTitle("温馨提示")
							.setMessage("确认下载").setNegativeButton("取消", dialog)
							.setPositiveButton("确定", dialog).show();

				}
			});
		}else{
			ll_btn_down.setVisibility(View.GONE);
		}

		return view;
	}

}
