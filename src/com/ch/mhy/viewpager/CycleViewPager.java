package com.ch.mhy.viewpager;

import java.util.ArrayList;
import java.util.List;

import com.ch.mhy.R;
import com.ch.mhy.entity.Comics;

import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 实现可循环，可轮播的viewpager
 */
public class CycleViewPager extends Fragment implements OnPageChangeListener {
//	public class CycleViewPager extends LinearLayout implements OnPageChangeListener {
	
	 /**
     * 上下文
     */
	private List<ImageView> imageViews = new ArrayList<ImageView>();
	private ImageView[] indicators;
	private FrameLayout viewPagerFragmentLayout;
	private LinearLayout indicatorLayout; // 指示器
	private BaseViewPager viewPager;
	private BaseViewPager parentViewPager;
	private ViewPagerAdapter adapter;
	private CycleViewPagerHandler handler;
	private int time = 5000; // 默认轮播时间
	private int currentPosition = 0; // 轮播当前位置
	private boolean isScrolling = false; // 滚动框是否滚动着
	private boolean isCycle = false; // 是否循环
	private boolean isWheel = false; // 是否轮播
	private long releaseTime = 0; // 手指松开、页面不滚动时间，防止手机松开后短时间进行切换
	private int WHEEL = 100; // 转动
	private int WHEEL_WAIT = 101; // 等待
	private ImageCycleViewListener mImageCycleViewListener;
	private List<Comics> infos;
	
	private TextView tv_title;
	
//	public CycleViewPager(Context context) {
//		super(context);
//	}
//	
//	/**
//     * @param context
//     * @param attrs
//     */
//    public CycleViewPager(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        mContext = context;
//        
////        LayoutInflater.from(context).inflate(R.layout.ad_cycle_view, this);
//        View view = LayoutInflater.from(context).inflate(
//				R.layout.view_cycle_viewpager_contet, this);
//
//		viewPager = (BaseViewPager) view.findViewById(R.id.viewPager);
//		tv_title = (TextView) view.findViewById(R.id.tv_title);
//		
//		
//		RelativeLayout.LayoutParams linearParams = (RelativeLayout.LayoutParams) viewPager.getLayoutParams(); // 取控件textView当前的布局参数
//		WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);  
//		linearParams.width =wm.getDefaultDisplay().getWidth();
//		linearParams.height =wm.getDefaultDisplay().getWidth()*100 / 225;
//		viewPager.setLayoutParams(linearParams);
//		
//		indicatorLayout = (LinearLayout) view
//				.findViewById(R.id.layout_viewpager_indicator);
//		
//		viewPagerFragmentLayout = (FrameLayout) view
//				.findViewById(R.id.layout_viewager_content);
//
//		handler = new CycleViewPagerHandler(context) {
//
//			@Override
//			public void handleMessage(Message msg) {
//				super.handleMessage(msg);
//				if (msg.what == WHEEL && imageViews.size() != 0) {
//					if (!isScrolling) {
//						int max = imageViews.size() + 1;
//						int position = (currentPosition + 1) % imageViews.size();
//						viewPager.setCurrentItem(position, true);
//						if (position == max) { // 最后一页时回到第一页
//							viewPager.setCurrentItem(1, false);
//						}
//					}
//
//					releaseTime = System.currentTimeMillis();
//					handler.removeCallbacks(runnable);
//					handler.postDelayed(runnable, time);
//					return;
//				}
//				if (msg.what == WHEEL_WAIT && imageViews.size() != 0) {
//					handler.removeCallbacks(runnable);
//					handler.postDelayed(runnable, time);
//				}
//			}
//		};
//    }
    

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = LayoutInflater.from(getActivity()).inflate(
				R.layout.view_cycle_viewpager_contet, null);

		viewPager = (BaseViewPager) view.findViewById(R.id.viewPager);
		tv_title = (TextView) view.findViewById(R.id.tv_title);
		
		RelativeLayout.LayoutParams linearParams = (RelativeLayout.LayoutParams) viewPager.getLayoutParams(); // 取控件textView当前的布局参数
		linearParams.width =this.getActivity().getWindowManager()
			.getDefaultDisplay().getWidth();
		linearParams.height =this.getActivity()
			.getWindowManager().getDefaultDisplay().getWidth()*100 / 225;
		viewPager.setLayoutParams(linearParams);
		
		indicatorLayout = (LinearLayout) view
				.findViewById(R.id.layout_viewpager_indicator);

		viewPagerFragmentLayout = (FrameLayout) view
				.findViewById(R.id.layout_viewager_content);

		handler = new CycleViewPagerHandler(getActivity()) {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == WHEEL && imageViews.size() != 0) {
					if (!isScrolling) {
						int max = imageViews.size() + 1;
						int position = (currentPosition + 1) % imageViews.size();
						viewPager.setCurrentItem(position, true);
						if (position == max) { // 最后一页时回到第一页
							viewPager.setCurrentItem(1, false);
						}
					}

					releaseTime = System.currentTimeMillis();
					handler.removeCallbacks(runnable);
					handler.postDelayed(runnable, time);
					return;
				}
				if (msg.what == WHEEL_WAIT && imageViews.size() != 0) {
					handler.removeCallbacks(runnable);
					handler.postDelayed(runnable, time);
				}
			}
		};

		return view;
	}

	public void setData(List<ImageView> views, List<Comics> list, ImageCycleViewListener listener) {
		setData(views, list, listener, 0);
	}

	/**
	 * 初始化viewpager
	 * 
	 * @param views
	 *            要显示的views
	 * @param showPosition
	 *            默认显示位置
	 */
	public void setData(List<ImageView> views, List<Comics> list, ImageCycleViewListener listener, int showPosition) {
		mImageCycleViewListener = listener;
		if(infos!=null){
			infos.clear();
		}
		infos = list;
		this.imageViews.clear();

		if (views.size() == 0) {
			viewPagerFragmentLayout.setVisibility(View.GONE);
			return;
		}

		for (ImageView item : views) {
			this.imageViews.add(item);
		}

		int ivSize = views.size();

		// 设置指示器
		indicators = new ImageView[ivSize];
		if (isCycle)
			indicators = new ImageView[ivSize - 2];
		indicatorLayout.removeAllViews();
		
//		LayoutParams lp=new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
//		lp.gravity=Gravity.RIGHT;
//		LinearLayout ll=new LinearLayout(getActivity());
//		ll.setLayoutParams(lp);
		for (int i = 0; i < indicators.length; i++) {
//			View view = LayoutInflater.from(mContext).inflate(
					View view = LayoutInflater.from(getActivity()).inflate(
					R.layout.view_cycle_viewpager_indicator, null);
			indicators[i] = (ImageView) view.findViewById(R.id.image_indicator);
			indicatorLayout.addView(view);
		}
//		indicatorLayout.addView(ll);
		adapter = new ViewPagerAdapter();

		// 默认指向第一项，下方viewPager.setCurrentItem将触发重新计算指示器指向
		setIndicator(0);

		viewPager.setOffscreenPageLimit(3);
		viewPager.setOnPageChangeListener(this);
		viewPager.setAdapter(adapter);
		if (showPosition < 0 || showPosition >= views.size())
			showPosition = 0;
		if (isCycle) {
			showPosition = showPosition + 1;
		}
		viewPager.setCurrentItem(showPosition);

	}

	/**
	 * 设置指示器居中，默认指示器在右方
	 */
	public void setIndicatorCenter() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
//		params.addRule(RelativeLayout.ALIGN_PARENT_END);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE); 
		indicatorLayout.setLayoutParams(params);
	}

	/**
	 * 是否循环，默认不开启，开启前，请将views的最前面与最后面各加入一个视图，用于循环
	 * 
	 * @param isCycle
	 *            是否循环
	 */
	public void setCycle(boolean isCycle) {
		this.isCycle = isCycle;
	}

	/**
	 * 是否处于循环状态
	 * 
	 * @return
	 */
	public boolean isCycle() {
		return isCycle;
	}

	/**
	 * 设置是否轮播，默认不轮播,轮播一定是循环的
	 * 
	 * @param isWheel
	 */
	public void setWheel(boolean isWheel) {
		this.isWheel = isWheel;
		isCycle = true;
		if (isWheel) {
			handler.postDelayed(runnable, time);
		}
	}

	/**
	 * 是否处于轮播状态
	 * 
	 * @return
	 */
	public boolean isWheel() {
		return isWheel;
	}

	final Runnable runnable = new Runnable() {

		@Override
		public void run() {
//					if (mContext != null
			if (getActivity() != null && !getActivity().isFinishing()
					&& isWheel) {
				long now = System.currentTimeMillis();
				// 检测上一次滑动时间与本次之间是否有触击(手滑动)操作，有的话等待下次轮播
				if (now - releaseTime > time - 500) {
					handler.sendEmptyMessage(WHEEL);
				} else {
					handler.sendEmptyMessage(WHEEL_WAIT);
				}
			}
		}
	};

	/**
	 * 释放指示器高度，可能由于之前指示器被限制了高度，此处释放
	 */
	public void releaseHeight() {
		getView().getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
//		this.getLayoutParams().height = RelativeLayout.LayoutParams.MATCH_PARENT;
		refreshData();
	}

	/**
	 * 设置轮播暂停时间，即没多少秒切换到下一张视图.默认5000ms
	 * 
	 * @param time
	 *            毫秒为单位
	 */
	public void setTime(int time) {
		this.time = time;
	}

	/**
	 * 刷新数据，当外部视图更新后，通知刷新数据
	 */
	public void refreshData() {
		if (adapter != null)
			adapter.notifyDataSetChanged();
	}

	/**
	 * 隐藏CycleViewPager
	 */
	public void hide() {
		viewPagerFragmentLayout.setVisibility(View.GONE);
	}

	/**
	 * 返回内置的viewpager
	 * 
	 * @return viewPager
	 */
	public BaseViewPager getViewPager() {
		return viewPager;
	}

	/**
	 * 页面适配器 返回对应的view
	 * 
	 * @author Yuedong Li
	 * 
	 */
	private class ViewPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return imageViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public View instantiateItem(ViewGroup container, final int position) {
			ImageView v = imageViews.get(position);
			if (mImageCycleViewListener != null) {
				v.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						mImageCycleViewListener.onImageClick(infos.get(currentPosition - 1), currentPosition, v);
					}
				});
			}
			container.addView(v);
			return v;
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		if (arg0 == 1) { // viewPager在滚动
			isScrolling = true;
			return;
		} else if (arg0 == 0) { // viewPager滚动结束
			if (parentViewPager != null)
				parentViewPager.setScrollable(true);

			releaseTime = System.currentTimeMillis();

			viewPager.setCurrentItem(currentPosition, false);
			
		}
		isScrolling = false;
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
		int max = imageViews.size() - 1;
		int position = arg0;
		currentPosition = arg0;
		if (isCycle) {
			if (arg0 == 0) {
				currentPosition = max - 1;
			} else if (arg0 == max) {
				currentPosition = 1;
			}
			position = currentPosition - 1;
		}
		setIndicator(position);
	}

	/**
	 * 设置viewpager是否可以滚动
	 * 
	 * @param enable
	 */
	public void setScrollable(boolean enable) {
		viewPager.setScrollable(enable);
	}

	/**
	 * 返回当前位置,循环时需要注意返回的position包含之前在views最前方与最后方加入的视图，即当前页面试图在views集合的位置
	 * 
	 * @return
	 */
	public int getCurrentPostion() {
		return currentPosition;
	}

	/**
	 * 设置指示器
	 * 
	 * @param selectedPosition
	 *            默认指示器位置
	 */
	private void setIndicator(int selectedPosition) {
		for (int i = 0; i < indicators.length; i++) {
			indicators[i]
					.setBackgroundResource(R.drawable.icon_point);
		}
		if (indicators.length > selectedPosition)
			indicators[selectedPosition]
					.setBackgroundResource(R.drawable.icon_point_pre);
		tv_title.setText(infos.get(selectedPosition).getmTitle());
	}

	/**
	 * 如果当前页面嵌套在另一个viewPager中，为了在进行滚动时阻断父ViewPager滚动，可以 阻止父ViewPager滑动事件
	 * 父ViewPager需要实现ParentViewPager中的setScrollable方法
	 */
	public void disableParentViewPagerTouchEvent(BaseViewPager parentViewPager) {
		if (parentViewPager != null)
			parentViewPager.setScrollable(false);
	}

	
	/**
	 * 轮播控件的监听事件
	 * 
	 * @author minking
	 */
	public static interface ImageCycleViewListener {

		/**
		 * 单击图片事件
		 * 
		 * @param position
		 * @param imageView
		 */
		public void onImageClick(Comics info, int postion, View imageView);
	}
}