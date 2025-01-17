package com.ch.mhy.activity.my;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ch.mhy.R;
import com.ch.mhy.activity.book.DownSelectActivity;
import com.ch.mhy.activity.book.ReadingActivity;
import com.ch.mhy.activity.book.ShowDetailActivity;
import com.ch.mhy.adapter.DownShowAdapter;
import com.ch.mhy.db.DBManager;
import com.ch.mhy.db.DBUtil;
import com.ch.mhy.entity.Comics;
import com.ch.mhy.entity.ComicsDetail;
import com.ch.mhy.entity.Down;
import com.ch.mhy.net.NetReceiver;
import com.ch.mhy.net.NetReceiver.NetState;
import com.ch.mhy.service.DownComicService;
import com.ch.mhy.util.FileUtil;
import com.ch.mhy.util.SDUtil;
import com.ch.mhy.util.Utils;

/**
 * 查看某一本漫画的下载详情
 * 
 * @author liuwenli
 *
 */
public class DownShowActivity extends Activity implements ServiceConnection {

	private ListView lv_downshow;// 显示下载项

	private DBManager manager = null;// 数据库管理

	private DownShowAdapter adapter;

	private TextView tv_show_title;

	private TextView tv_my_edit;

	/**
	 * 用来标记编辑/删除切换
	 */
	private boolean flag = true;

	private Comics comics;// 漫画基本信息

	private Toast toast;// 添加wifi提醒

	private SharedPreferences sharedPreferences;

	private String mId;// 漫画ID号

	private boolean flag2 = true;// 设置默认是可以下载的

	/**
	 * 判断是否更新UI 如果点击全部暂停就不接受更新ui
	 */
	private boolean isUpdate = true;
	/**
	 * 更新ui广播
	 */
	private MyReceiver receiver;

	/**
	 * 适配器数据源
	 */
	private List<Down> list = new ArrayList<Down>();

	/* 通过Binder，实现Activity与Service通信 */
	private DownComicService.MsgBinder mBinderService;

	private boolean isShow = false;// 判断是否有弹出框

	private LinearLayout ll_downinfo_operate;
	private boolean isselect = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_downshow);
		// 注册接收器
		receiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.test");
		this.registerReceiver(receiver, filter);

		sharedPreferences = this.getSharedPreferences("userConfig", 0);
		mId = (String) getIntent().getSerializableExtra("ben");// 上一页面选中的下载漫画
		// 当设置了wifi下载，而当前的网络不是wifi连接就变为不可下载
		if (sharedPreferences.getBoolean("wifidown", false)) {
			if (NetReceiver.isConnected(this) != NetReceiver.NetState.NET_WIFI) {
				flag2 = false;
			}
		}
		// 绑定service， 用于通知后台下载任务
		Intent service = new Intent(DownShowActivity.this,
				DownComicService.class);
		DownShowActivity.this.bindService(service, DownShowActivity.this,
				Context.BIND_AUTO_CREATE);

		initView();
	}

	private void initView() {
		ll_downinfo_operate = (LinearLayout) findViewById(R.id.ll_downinfo_operate);

		tv_my_edit = (TextView) findViewById(R.id.tv_my_edit);// 编辑
		tv_show_title = (TextView) findViewById(R.id.tv_show_title);// 标题
		lv_downshow = (ListView) findViewById(R.id.lv_downshow);// 下载项

		synchronized (this) {
			manager = new DBManager(this, DBUtil.ReadName, null, DBUtil.Code);
			/**
			 * 获取数据源
			 */
			list.addAll(manager.queryDown(
					"select * from down  where mId =? order by mNo",
					new String[] { mId + "" }));
			manager.closeDB();
		}

		for (Down i : list) {
			ComicsDetail cd = i.getCd();// 得到漫画
			cd.setFlag(flag);// 设置编辑状态
			i.setCd(cd);
		}
		comics = new Comics();
		String title = list.get(0).getCd().getmTitle();
		comics.setmTitle(title);
		comics.setmPic(list.get(0).getCd().getmPic());
		comics.setmDirector(list.get(0).getCd().getmDirector());
		comics.setmLianzai(list.get(0).getCd().getmLianzai());
		comics.setUpdateMessage(list.get(0).getCd().getUpdateMessage());
		comics.setmQid(Long.valueOf(list.get(0).getCd().getmId().toString()));
		comics.setmType1(list.get(0).getCd().getmType1());
		comics.setmContent(list.get(0).getCd().getmContent());

		title = title.length() > 6 ? title.substring(0, 6) : title;
		tv_show_title.setText(title);

		adapter = new DownShowAdapter(this, list);// 初始化适配显示
		lv_downshow.setAdapter(adapter);

		lv_downshow.setOnItemClickListener(new OnItemClickListener() {// 暂停，开始
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Down down = (Down) adapter.getItem(position);
						ComicsDetail cd = down.getCd();
						try {
							if (!cd.isFlag()) {// 删除下载项，加入提示删除本漫画
								list.get(position)
										.getCd()
										.setIsselect(
												!list.get(position).getCd()
														.isIsselect());
								adapter.notifyDataSetChanged();

							} else {
								switch (down.getIsdonw()) {
								case 0:// 表示已经完成的直接跳到阅读页面
									ComicsDetail rdinfo=list.get(position).getCd();
									
									manager = new DBManager(DownShowActivity.this,
											DBUtil.ReadName, null, DBUtil.Code);
									// 先查询下本地有没有，如果有的话就是续看功能
									ComicsDetail cdd = manager.queryByMid(rdinfo.getmId());

									if (cdd != null) {
										if(cdd.getmQid().equals(rdinfo.getmQid())){
											rdinfo=cdd;
										}
									}
									
									Intent startlook = new Intent(
											DownShowActivity.this,
											ReadingActivity.class);
									startlook.putExtra("mh", rdinfo);
									startlook.putExtra("comefrom", "0");
									DownShowActivity.this
											.startActivityForResult(startlook,
													1);
									break;
								case 1: // 现在是正在下载状态，点一下变成暂停
									down.setIsdonw(2);// 状态变成正为暂停
									mBinderService.endDownload(down);
									break;
								case 3: // 现在是暂停等待状态
									down.setIsdonw(2);// 状态设为暂停
									mBinderService.endDownload(down);
									break;
								case 2:// 处于暂停状态，点击变为开始状态
									if (!flag2) {
										DownShowActivity.this
												.showTextToast("您设置了wifi下下载！");
										return;
									}
									down.setIsdonw(3);// 默认图标设为下载等待
									mBinderService.startDownload(down);
									isUpdate = true;

									break;
								default:
									break;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {

						}
					}
				});
	}

	// 不重复显示Toast的方法
	private void showTextToast(String msg) {
		if (toast == null) {
			toast = Toast.makeText(getApplicationContext(), msg, 500);
		} else {
			toast.setText(msg);
		}
		toast.show();
	}

	private long opreatetime = 0;// 记录点击时间

	private long starttime = 0;// 记录点击时间
	private long endtime = 0;// 记录点击时间

	public void onclick(View view) {
		switch (view.getId()) {
		case R.id.btn_return_back:
			this.finish();
			break;
		case R.id.ll_down_add:// 加入漫画章节
			if (NetReceiver.isConnected(this) != NetState.NET_NO) {
				Intent intent1 = new Intent(this, DownSelectActivity.class);
				comics.setmIf("1");
				intent1.putExtra("mh", comics);
				this.startActivityForResult(intent1, 0);
			} else {
				Utils.showMsg(this, "没联网...");
			}
			break;
		case R.id.ll_down_start:// 全部开始下载

			if (System.currentTimeMillis() - starttime > 1500) {
				starttime = System.currentTimeMillis();

				isUpdate = true;
				// 添加wifi下载提示
				if (!flag2) {
					DownShowActivity.this.showTextToast("您设置了wifi下下载！");
					return;
				}
				try {
					List<Down> downList = new ArrayList<Down>();
					for (Down down : list) {
						if (down.getIsdonw().intValue() != 0) {
							down.setIsdonw(3);
							downList.add(down);
						}
					}
					if (downList.size() > 0) {
						mBinderService.addDownload(downList);
						synchronized (downList) {
							manager = new DBManager(DownShowActivity.this,
									DBUtil.ReadName, null, DBUtil.Code);
							manager.updateDown(downList);
							manager.closeDB();
							adapter.notifyDataSetChanged();
						}
					}

				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			break;
		case R.id.ll_down_stop:// 全部停止
			if (System.currentTimeMillis() - endtime > 1500) {
				endtime = System.currentTimeMillis();

				// 设置不可更新ui
				isUpdate = false;

				try {
					if (list.size() > 0) {
						mBinderService.endDownloadAll(list.get(0));
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				/**
				 * 找出需要暂停的章节
				 */
				List<Down> updateList = new ArrayList<Down>();
				for (Down down : list) {
					if (down.getIsdonw().intValue() != 0) {
						down.setIsdonw(2);
						updateList.add(down);
					}
				}

				/**
				 * 更新到本地数据库
				 */
				if (updateList.size() > 0) {
					synchronized (updateList) {
						manager = new DBManager(DownShowActivity.this,
								DBUtil.ReadName, null, DBUtil.Code);
						manager.updateDown(updateList);
						manager.closeDB();
						adapter.notifyDataSetChanged();
					}
				}
			}
			break;
		case R.id.ll_read_start:// 开始阅读
			Intent intent = new Intent(this, ShowDetailActivity.class);
			intent.putExtra("manhua", comics);
			intent.putExtra("eventId", "my_book_click");
			this.startActivity(intent);
			break;
		case R.id.ll_my_edit:

			flag = !flag;
			if (flag) {
				tv_my_edit.setText("编辑");
			} else {
				tv_my_edit.setText("取消");
			}

			for (Down i : list) {
				ComicsDetail cd = i.getCd();
				cd.setFlag(flag);
				i.setCd(cd);
			}
			adapter.notifyDataSetChanged();

			if (flag) {
				ll_downinfo_operate.setVisibility(View.GONE);
			} else {
				ll_downinfo_operate.setVisibility(View.VISIBLE);
			}

			break;

		case R.id.btn_selectall:// 点击全选
			isselect = !isselect;
			for (Down comicsDetail : list) {
				comicsDetail.getCd().setIsselect(isselect);
			}
			adapter.notifyDataSetChanged();
			break;
		case R.id.btn_delete:// 点击删除
			final List<Down> detellist = new ArrayList<Down>();

			for (Down comicsDetail : list) {
				if (comicsDetail.getCd().isIsselect()) {
					detellist.add(comicsDetail);
				}
			}
			/*
			 * 把要删除的记录，在本地数据库里进行删除，然后在把数据里的数据读取出来，刷新界面
			 */
			if (detellist.size() > 0 && !isShow) {
				if (!isShow) {
					isShow = true;
					try {
						mBinderService.endDownload(detellist);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				/**
				 * 提示是否删除
				 */
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("确认删除吗？");
				builder.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								isShow = false;
							}
						});

				builder.setPositiveButton("确认",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								createDialog(detellist);
							}
						});
				AlertDialog ad = builder.create();
				ad.setCanceledOnTouchOutside(false);
				ad.show();
			} else {
				if (System.currentTimeMillis() - opreatetime > 2000 && !isShow) {
					Utils.showMsg(this, "没有选择删除项！");
					opreatetime = System.currentTimeMillis();
				}
			}

			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		isUpdate = true;// 打开ui更新
		list.clear();
		manager = new DBManager(this, DBUtil.ReadName, null, DBUtil.Code);
		/**
		 * 获取数据源
		 */
		list.addAll(manager.queryDown(
				"select * from down  where mId =? order by mNo",
				new String[] { mId + "" }));
		manager.closeDB();

		for (Down i : list) {
			ComicsDetail cd = i.getCd();// 得到漫画
			cd.setFlag(flag);// 设置编辑状态
			i.setCd(cd);
		}
		adapter.notifyDataSetChanged();
	}

	protected void createDialog(final List<Down> downs) {

		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage("是否同时删除本话下载的图片文件？");
		builder.setTitle("提示");
		builder.setCancelable(false);
		builder.setPositiveButton("是", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				doDeleteFile(downs);

				synchronized (downs) {
					manager = new DBManager(DownShowActivity.this,
							DBUtil.ReadName, null, DBUtil.Code);
					manager.deleteDowns(downs);
					list.clear();
					list.addAll(manager.queryDown(
							"select * from down  where mId =? order by mNo",
							new String[] { mId + "" }));
					manager.closeDB();
				}

				adapter.notifyDataSetChanged();

				if (Utils.updateCollectInfo != null) {
					Utils.updateCollectInfo.updateDowns();
				}

				dialog.dismiss();
				isShow = false;
			}
		});
		builder.setNegativeButton("否", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				/**
				 * 删除本地数据库记录
				 */

				synchronized (downs) {
					manager = new DBManager(DownShowActivity.this,
							DBUtil.ReadName, null, DBUtil.Code);
					manager.deleteDowns(downs);
					list.clear();
					list.addAll(manager.queryDown(
							"select * from down  where mId =? order by mNo",
							new String[] { mId + "" }));
					manager.closeDB();
				}

				adapter.notifyDataSetChanged();

				if (Utils.updateCollectInfo != null) {
					Utils.updateCollectInfo.updateDowns();
				}
				dialog.dismiss();
				isShow = false;
			}
		});
		builder.create().setCanceledOnTouchOutside(false);
		builder.create().show();
	}

	/**
	 * 删除本地文件
	 * 
	 * @param downs
	 *            要删除的漫画
	 */
	private void doDeleteFile(final List<Down> downs) {
		for (Down down : downs) {
			String path = SDUtil.getSecondExterPath() + "/manyun/"
					+ down.getCd().getmTitle() + "/" + down.getCd().getmName();
			FileUtil.deleteDirectory(path);
		}
	}

	private Down down;

	public class MyReceiver extends BroadcastReceiver {
		// 自定义一个广播接收器
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Bundle bundle = intent.getExtras();

			boolean isfull = (Boolean) bundle.getSerializable("isfull");
			if (!isfull) {
				down = (Down) bundle.getSerializable("down");
				int positon = 0;
				for (Down d : list) {
					if (isUpdate
							&& d.getCd().getmQid()
									.equals(down.getCd().getmQid())) {
						d.setDowns(down.getDowns());
						publishProgress(positon, down.getDowns());
						d.setIsdonw(down.getIsdonw());
						publishStatus(positon, null);
						if (down.getIsdonw() == 0) {
							d.setIsdonw(0);
							publishStatus(positon, null);

						}
						publishDowninfo(positon);

					}
					positon++;
				}
			} else {
				Utils.showMsg(DownShowActivity.this, "内存不足！");
			}

		}

		public MyReceiver() {
			// 构造函数，做一些初始化工作，本例中无任何作用
		}
	}

	/**
	 * |更新进度条
	 * 
	 * @param positionInAdapter
	 *            //更新的位置
	 * @param progress
	 *            //进度值
	 */
	public void publishProgress(final int positionInAdapter, final int progress) {
		if (positionInAdapter >= lv_downshow.getFirstVisiblePosition()
				&& positionInAdapter <= lv_downshow.getLastVisiblePosition()) {
			int positionInListView = positionInAdapter
					- lv_downshow.getFirstVisiblePosition();
			ProgressBar item = (ProgressBar) lv_downshow.getChildAt(
					positionInListView).findViewById(R.id.progressBar);
			item.setProgress(progress);
			if (list.get(positionInAdapter).getCd().getmUrl()
					.split(Utils.split).length == progress) {
				item.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * 更新下载的状态
	 * 
	 * @param positionInAdapter
	 * @param status
	 */
	public void publishStatus(final int positionInAdapter, final String status) {
		if (positionInAdapter >= lv_downshow.getFirstVisiblePosition()
				&& positionInAdapter <= lv_downshow.getLastVisiblePosition()) {
			int positionInListView = positionInAdapter
					- lv_downshow.getFirstVisiblePosition();
			ImageView item = (ImageView) lv_downshow.getChildAt(
					positionInListView).findViewById(R.id.iv_book_auth);
			// 下载状态 0表示完成，1表示继续下载,2表示暂停
			switch (list.get(positionInAdapter).getIsdonw()) {
			case 0:
				item.setBackgroundResource(R.drawable.downread);
				break;
			case 1:
				item.setBackgroundResource(R.drawable.downstop);
				break;
			case 2:
				item.setBackgroundResource(R.drawable.downstart);
				break;
			case 3:
				item.setBackgroundResource(R.drawable.downwait);
				break;
			default:
				break;
			}
		}

	}

	/**
	 * 更新下载百分比
	 * 
	 * @param positionInAdapter
	 */
	public void publishDowninfo(final int positionInAdapter) {
		if (positionInAdapter >= lv_downshow.getFirstVisiblePosition()
				&& positionInAdapter <= lv_downshow.getLastVisiblePosition()) {
			int positionInListView = positionInAdapter
					- lv_downshow.getFirstVisiblePosition();
			TextView tv_book_downinfo = (TextView) lv_downshow.getChildAt(
					positionInListView).findViewById(R.id.tv_book_downinfo);

			NumberFormat numberFormat = NumberFormat.getInstance();// 设置精确到小数点后2位
			numberFormat.setMaximumFractionDigits(0);
			String result = numberFormat.format((float) down.getDowns()
					/ (float) list.get(positionInAdapter).getCd().getmUrl()
							.split(Utils.split).length * 100);
			String str = result + "%";
			tv_book_downinfo.setText(str);
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// TODO Auto-generated method stub
		mBinderService = (DownComicService.MsgBinder) service;
		Log.d("tag", "onServiceConnected");
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		// TODO Auto-generated method stub
		Log.d("tag", "onServiceDisconnected");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unbindService(this);
		this.unregisterReceiver(receiver);
	}
}
