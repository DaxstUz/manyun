package com.ch.mhy.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ch.mhy.R;
import com.ch.mhy.activity.my.DownShowActivity;
import com.ch.mhy.adapter.DownBookAdapter;
import com.ch.mhy.db.DBManager;
import com.ch.mhy.db.DBUtil;
import com.ch.mhy.entity.ComicsDetail;
import com.ch.mhy.entity.Down;
import com.ch.mhy.service.DownComicService;
import com.ch.mhy.util.FileUtil;
import com.ch.mhy.util.SDUtil;
import com.ch.mhy.util.Utils;

/**
 * 下载碎片
 *
 * @author DaxstUz 2416738717 2015年5月6日
 */
public class DownloadFragment extends Fragment implements ServiceConnection,OnClickListener {

	private ListView lv_down_show;

	private DBManager manager;

	List<Down> list = new ArrayList<Down>();

	MyReceiver receiver;

	private DownBookAdapter adapter;

	/* 通过Binder，实现Activity与Service通信 */
	private DownComicService.MsgBinder mBinderService;
	
	/*
	 * 全选操作部分
	 */
	private LinearLayout ll_down_operate;
	private Button btn_selectall;
	private Button btn_delete;
	private boolean isselect=false;

	// private View view ;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// if (view != null) {
		// ViewGroup parent = (ViewGroup) view.getParent();
		// if (parent != null)
		// parent.removeView(view);
		// return view;
		// }
		// try {
		// view = inflater.inflate(R.layout.fragment_download, container,
		// false);
		// } catch (InflateException e) {
		//
		// }

		View view = inflater.inflate(R.layout.fragment_download, null);

		manager = new DBManager(DownloadFragment.this.getActivity(),
				DBUtil.ReadName, null, DBUtil.Code);
		lv_down_show = (ListView) view.findViewById(R.id.lv_down_show);
		
		ll_down_operate = (LinearLayout) view.findViewById(R.id.ll_down_operate);
        btn_selectall = (Button) view.findViewById(R.id.btn_selectall);
        btn_delete = (Button) view.findViewById(R.id.btn_delete);
        btn_selectall.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        

		list = manager.queryDown("select * from down GROUP BY mId ", null);

		for (Down down : list) {
			ComicsDetail cd = down.getCd();
			cd.setFlag(true);
			down.setCd(cd);
		}

		adapter = new DownBookAdapter(DownloadFragment.this.getActivity(), list);
		lv_down_show.setAdapter(adapter);

		/**
		 * 添加行点击事件
		 */
		lv_down_show.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub

				ComicsDetail cd = list.get(position).getCd();
				if (cd.isFlag()) {
					Intent intent = new Intent(DownloadFragment.this
							.getActivity(), DownShowActivity.class);
					intent.putExtra("ben", list.get(position).getCd().getmId()
							+ "");
					DownloadFragment.this.startActivityForResult(intent, 0);
				} else {
					list.get(position).getCd().setIsselect(!list.get(position).getCd().isIsselect());

//					list.remove(position);
					adapter.notifyDataSetChanged();
				}
			}
		});

		// 注册接收器
		receiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.update");
		DownloadFragment.this.getActivity().registerReceiver(receiver, filter);

		// 绑定service， 用于通知后台开启下载任务
		Intent service = new Intent(DownloadFragment.this.getActivity(),
				DownComicService.class);
		DownloadFragment.this.getActivity().bindService(service,
				DownloadFragment.this, Context.BIND_AUTO_CREATE);

		return view;
	}

	protected void createDialog(final List<Down> downs) {
		AlertDialog.Builder builder = new Builder(
				DownloadFragment.this.getActivity());
		builder.setMessage("是否同时删除本话下载的图片文件？");
		builder.setTitle("提示");
		builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				for (Down down2 : downs) {
					doDeleteFile(down2);
					/**
					 * 把本地数据库记录删除
					 */
					manager = new DBManager(
							DownloadFragment.this.getActivity(),
							DBUtil.ReadName, null, DBUtil.Code);
					manager.deleteByMid(down2.getCd().getmId());
					manager.closeDB();
				}
//				
//				if (Utils.updateCollectInfo != null) {
//					Utils.updateCollectInfo.updateDowns();
//				}
				
				manager = new DBManager(DownloadFragment.this.getActivity(), DBUtil.ReadName, null, DBUtil.Code);
//				manager.deleteDowns(downs);
				list.clear();
				list.addAll(manager.queryDown("select * from down GROUP BY mId ", null));
				manager.closeDB();
				adapter.notifyDataSetChanged();
				if(Utils.updateCollectInfo!=null){
					Utils.updateCollectInfo.updateDowns();
				}
				
				dialog.dismiss();
				
			}
		});
		builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				/**
				 * 把本地数据库记录删除
				 */
//				manager = new DBManager(
//						DownloadFragment.this.getActivity(),
//						DBUtil.ReadName, null, DBUtil.Code);
//				manager.deleteByMid(down.getCd().getmId());
//				manager.closeDB();
//				
//				if (Utils.updateCollectInfo != null) {
//					Utils.updateCollectInfo.updateDowns();
//				}
				
				manager = new DBManager(DownloadFragment.this.getActivity(), DBUtil.ReadName, null, DBUtil.Code);
				manager.deleteDowns(downs);
				list.clear();
				list.addAll(manager.queryDown("select * from down GROUP BY mId ", null));
				manager.closeDB();
				adapter.notifyDataSetChanged();
				if(Utils.updateCollectInfo!=null){
					Utils.updateCollectInfo.updateDowns();
				}
				
			}
		});
		builder.create().show();
	}

	private void doDeleteFile(Down down) {

		// if (flag) {// 删除目录
		String path = SDUtil.getSecondExterPath() + "/manyun/"
				+ down.getCd().getmTitle();
		FileUtil.deleteDirectory(path);
		// }
		//
		// list.remove(position);
		// adapter.notifyDataSetChanged();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		this.getActivity().unregisterReceiver(receiver);
		this.getActivity().unbindService(this);
	}

	public class MyReceiver extends BroadcastReceiver {
		// 自定义一个广播接收器
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

			Bundle bundle = intent.getExtras();
			String over = (String) bundle.getSerializable("isover");
			if ("over".equals(over.split(Utils.split)[0])) {
				// adapter.notifyDataSetChanged();

				int positon = 0;

				String status = "下载完成";
				for (Down d : list) {
					if (d.getCd().getmId().toString().equals(over.split(Utils.split)[1])) {
						publishProgress(positon, status);

					}
					positon++;
				}
			}
		}

		public MyReceiver() {
			// 构造函数，做一些初始化工作，本例中无任何作用
		}

	}

	/**
	 * 更新下载状态
	 * 
	 * @param positionInAdapter
	 * @param status
	 */
	public void publishProgress(final int positionInAdapter, final String status) {
		if (positionInAdapter >= lv_down_show.getFirstVisiblePosition()
				&& positionInAdapter <= lv_down_show.getLastVisiblePosition()) {
			int positionInListView = positionInAdapter
					- lv_down_show.getFirstVisiblePosition();
			TextView item = (TextView) lv_down_show.getChildAt(
					positionInListView).findViewById(R.id.progressBar);
			item.setText(status);
		}

	}

	/**
	 * 更新是否显示删除图标
	 * 
	 * @param flag
	 */
	public void updateStatus(boolean flag) {
		if(ll_down_operate!=null){
    		if(flag){
    			ll_down_operate.setVisibility(View.GONE);
    		}else{
    			ll_down_operate.setVisibility(View.VISIBLE);
    		}
    	}
		
		for (Down down : list) {
			ComicsDetail comicsDetail = down.getCd();
			comicsDetail.setFlag(flag);
		}

		if(adapter!=null){
    		adapter.notifyDataSetChanged();
    	}

	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
		mBinderService = (DownComicService.MsgBinder) service;
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		synchronized (this) {
			manager = new DBManager(DownloadFragment.this.getActivity(),
					DBUtil.ReadName, null, DBUtil.Code);
			list.clear();
			list.addAll(manager.queryDown("select * from down GROUP BY mId ", null));
			manager.closeDB();
		}

		for (Down down : list) {
			down.getCd().setFlag(true);
		}

		adapter.notifyDataSetChanged();
	}

	private long opreatetime=0;//记录点击时间
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_selectall://点击全选
			isselect=!isselect;
			for (Down down : list) {
				down.getCd().setIsselect(isselect);
			}
			adapter.notifyDataSetChanged();
			break;
		case R.id.btn_delete://点击删除
			final List<Down> detellist=new ArrayList<Down>();
			
			for (Down down : list) {
				if(down.getCd().isIsselect()){
					detellist.add(down);
				}
			}
			
			
			/*
			 * 把要删除的记录，在本地数据库里进行删除，然后在把数据里的数据读取出来，刷新界面
			 */
			if(detellist.size()>0){
        		/**
        		 * 提示是否删除
        		 */
        		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        		builder.setTitle("确认删除吗？");
        		builder.setNegativeButton("取消", null);
        		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
							try {
								mBinderService.endDownloadAll(detellist);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						createDialog(detellist);
					}
				});
        		builder.create().show();
			}else{
				if(System.currentTimeMillis()-opreatetime>2000){
					Utils.showMsg(getActivity(), "没有选择删除项！");
					opreatetime=System.currentTimeMillis();
				}
			}
			
			break;

		default:
			break;
		}
	}
}
