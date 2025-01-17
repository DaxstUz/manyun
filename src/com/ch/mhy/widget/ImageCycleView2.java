package com.ch.mhy.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.ch.mhy.R;
import com.ch.mhy.activity.book.ReadingActivity;
import com.ch.mhy.listener.Add;
import com.ch.mhy.net.NetReceiver;
import com.ch.mhy.util.ShowOperate;
import com.ch.mhy.util.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 广告图片自动轮播控件</br>
 * <p/>
 * 
 * <pre>
 *   集合ViewPager和指示器的一个轮播控件，主要用于一般常见的广告图片轮播，具有自动轮播和手动轮播功能
 *   使用：只需在xml文件中使用{@code <com.minking.imagecycleview.ImageCycleView/>} ，
 *   然后在页面中调用  {@link #setImageResources(ArrayList, ImageCycleViewListener) }即可!
 * 
 *   另外提供{@link #startImageCycle() } \ {@link #pushImageCycle() }两种方法，用于在Activity不可见之时节省资源；
 *   因为自动轮播需要进行控制，有利于内存管理
 * </pre>
 *
 * @author minking
 */
public class ImageCycleView2 extends LinearLayout implements ShowOperate {

	/**
	 * 上下文
	 */
	private Context mContext;

	/**
	 * 图片轮播视图
	 */
	public MyViewPager mAdvPager = null;
	// public VerticalViewPager vviewpager = null;

	/**
	 * 滚动图片视图适配器
	 */
	public ImageCycleAdapter mAdvAdapter;

	/**
	 * 滚动图片指示器-视图列表
	 */
	public ImageView[] mImageViews = null;

	/**
	 * 图片滚动当前图片下标
	 */
	public int mImageIndex = 0;

	/**
	 * 手机密度
	 */
	private float mScale;

	public Add add;

	/**
	 * @param context
	 */
	public ImageCycleView2(Context context) {
		super(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public ImageCycleView2(Context context, AttributeSet attrs) {
		super(context, attrs);

		Utils.map = new HashMap<String, Boolean>();
		mContext = context;
		mScale = context.getResources().getDisplayMetrics().density;
		LayoutInflater.from(context).inflate(R.layout.ad_cycle_view2, this);
		mAdvPager = (MyViewPager) findViewById(R.id.adv_pager);
//		mAdvPager.setScroll(false);
		mAdvPager.setOnPageChangeListener(new GuidePageChangeListener());
		mAdvPager.setOffscreenPageLimit(5);// 设置预加载的数量
	}

	ArrayList<String> imageUrlList;

	/**
	 * 装填图片数据
	 *
	 * @param imageUrlList
	 * @param imageCycleViewListener
	 */
	public void setImageResources(ArrayList<String> imageUrlList,
			ImageCycleViewListener imageCycleViewListener, Add add) {
		mImageIndex = Utils.rindex;
		if (mImageIndex < 1) {
			mImageIndex = 1;
		}
		this.add = add;
		this.imageUrlList = imageUrlList;
		// 图片广告数量
		final int imageCount = imageUrlList.size();
		mImageViews = new ImageView[imageCount];
		mAdvAdapter = new ImageCycleAdapter(mContext, imageUrlList,
				imageCycleViewListener, add);
		mAdvPager.setAdapter(mAdvAdapter);
		mAdvPager.setCurrentItem(mImageIndex);
	}

	private Handler mHandler = new Handler();

	/**
	 * 轮播图片状态监听器
	 *
	 * @author minking
	 */
	private final class GuidePageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int state) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int index) {
			mAdvPager.setBackgroundResource(0);
			// 设置当前显示的图片下标
			mImageIndex = index;

			/* 判断是否是缺失页 false为缺失页 */
			boolean isload = true;
			if (imageUrlList.get(index).indexOf(
					"d17a64a81f1f4e388196b80c34dc5feb") > -1) {// 图片缺失
				isload = false;
			}

			/* 是缺失页才开启加载动画，否则不开启 */
			if (isload && !Utils.map.containsKey(imageUrlList.get(index))) {
				Utils.map.put(imageUrlList.get(index), false);
				add.start(imageUrlList.get(index));
			} else {
				if (isload && !Utils.map.get(imageUrlList.get(index))) {
					add.start(imageUrlList.get(index));
				}
			}

			/* 判断是否请求下一话 */
			if (index == imageUrlList.size() - 1) {
				mImageIndex = imageUrlList.size() - 2;
				add.getNewData("asc");
				mAdvPager.setCurrentItem(mImageIndex);
			}

			/* 默认显示第一张 */
			if (index == 0) {
				mImageIndex = 1;
				mAdvPager.setCurrentItem(mImageIndex);
				add.getNewData("desc");
			}

			/* 更新当前显示第几页 */
			if (index <= imageUrlList.size() - 1) {
				add.updateIndex();
			}

			LinearLayout updateView = (LinearLayout) mAdvAdapter
					.getPrimaryItem();
			if (updateView != null) {
				((PhotoView) updateView.getChildAt(0)).reSet();
			}
		}

	}

	/**
	 * 图片资源列表
	 */
	private ArrayList<String> mAdList = new ArrayList<String>();

	boolean flag = true;// 弹出亮度调节框点消失时不进行上下翻页动作标记

	@SuppressLint("ClickableViewAccessibility")
	public class ImageCycleAdapter extends PagerAdapter {

		/**
		 * 图片视图缓存列表
		 */
		private ArrayList<ImageView> mImageViewCacheList;

		/**
		 * 广告图片点击监听器
		 */
		private ImageCycleViewListener mImageCycleViewListener;

		private Context mContext;

		private Add add;

		public ImageCycleAdapter(Context context, ArrayList<String> adList,
				ImageCycleViewListener imageCycleViewListener, Add add) {
			mContext = context;
			mAdList = adList;
			mImageCycleViewListener = imageCycleViewListener;
			mImageViewCacheList = new ArrayList<ImageView>();
			this.add = add;
		}

		@Override
		public int getCount() {
			return mAdList.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == obj;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
//			Log.d("tag", "destroyItem  "+position);
			if (Utils.map.size() > 0) {
				if (imageUrlList.size() > position) {
					Utils.map.remove(imageUrlList.get(position));
					Log.d("tag", "destroyItem  "+position);
				}
			}
			LinearLayout ll = (LinearLayout) object;
			new RecycleBitmapInLayout(true).recycle(ll);
			((ViewPager) container).removeView(ll);
			ll = null;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// 重载该方法，防止其它视图被销毁，防止加载视图卡顿
			super.destroyItem(container, position, object);
		}

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			Log.d("tag", "instantiateItem  "+position);
			String imageUrl = mAdList.get(position);
			LinearLayout rl = new LinearLayout(mContext);
			params.gravity = Gravity.CENTER;
			rl.setLayoutParams(params);
			PhotoView imageView = null;
			imageView = new PhotoView(mContext, (ReadingActivity) add);
			
			LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT);
			lp.gravity = Gravity.CENTER;
			imageView.setLayoutParams(lp);
			imageView.enable();
			imageView.setTag(imageUrl);
			rl.addView(imageView);
			container.addView(rl);
			
			
			mImageCycleViewListener
					.displayImage(imageUrl, imageView, Utils.map);
			return rl;
		}

		private View mCurrentView;

		@Override
		public void setPrimaryItem(ViewGroup container, int position,
				Object object) {
			mCurrentView = (View) object;
		}

		@SuppressWarnings("unused")
		public View getPrimaryItem() {
			return mCurrentView;
		}
	}

	/**
	 * 轮播控件的监听事件
	 *
	 * @author minking
	 */
	public static interface ImageCycleViewListener {

		/**
		 * 加载图片资源
		 *
		 * @param imageURL
		 * @param imageView
		 */
		public void displayImage(String imageURL, PhotoView imageView,
				Map<String, Boolean> map);

		/**
		 * 单击图片事件
		 *
		 * @param position
		 * @param imageView
		 */
		public void onImageClick(int position, View imageView);

	}

	@Override
	public void next() {
		if (mImageViews != null && flag) {
			// 下标等于图片列表长度说明已滚动到最后一张图片,重置下标
			if ((++mImageIndex) >= mImageViews.length - 1) {
				mImageIndex = mImageViews.length - 1;
				add.getNewData("asc");
			} else {
				if (mAdvPager.getChildAt(0) instanceof PhotoView) {
					((PhotoView) mAdvPager.getChildAt(0)).mScale = 1.0f;
				}
				mAdvPager.setCurrentItem(mImageIndex);
			}
		}
	}

	@Override
	public void pre() {
		if (mImageViews != null && flag) {
			// 下标等于图片列表长度说明已滚动到最后一张图片,重置下标
			if ((--mImageIndex) <= 0) {
				mImageIndex = 1;
				add.getNewData("desc");
			} else {
				mAdvPager.setCurrentItem(mImageIndex);
			}
		}
	}

	public void seeByProcess() {
		if (mImageIndex < imageUrlList.size()) {
			mAdvPager.setCurrentItem(mImageIndex);
		}
	}

	/**
	 * 工具类 释放布局中所有Imageview组件占用的图片，可设置是否释放背景图 用于退出时释放资源，调用完成后，请不要刷新界面
	 * 
	 * @author liubing
	 *
	 */
	public class RecycleBitmapInLayout {
		private static final String TAG = "RecycleBitmapInLayout";
		/* 是否释放背景图 true:释放;false:不释放 */
		private boolean flagWithBackgroud = false;

		/**
		 * 
		 * @param flagWithBackgroud
		 *            是否同时释放背景图
		 */
		public RecycleBitmapInLayout(boolean flagWithBackgroud) {
			this.flagWithBackgroud = flagWithBackgroud;
		}

		/**
		 * 释放Imageview占用的图片资源 用于退出时释放资源，调用完成后，请不要刷新界面
		 * 
		 * @param layout
		 *            需要释放图片的布局 *
		 */
		public void recycle(ViewGroup layout) {

			for (int i = 0; i < layout.getChildCount(); i++) {
				// 获得该布局的所有子布局
				View subView = layout.getChildAt(i);
				// 判断子布局属性，如果还是ViewGroup类型，递归回收
				if (subView instanceof ViewGroup) {
					// 递归回收
					recycle((ViewGroup) subView);
				} else {
					// 是Imageview的子例
					if (subView instanceof ImageView) {
						// 回收占用的Bitmap
						recycleImageViewBitMap((ImageView) subView);
						// 如果flagWithBackgroud为true,则同时回收背景图
						if (flagWithBackgroud) {
							recycleBackgroundBitMap((ImageView) subView);
						}
					}
				}
			}
		}

		private void recycleBackgroundBitMap(ImageView view) {
			if (view != null) {
				BitmapDrawable bd = (BitmapDrawable) view.getBackground();
				rceycleBitmapDrawable(bd);
			}
		}

		private void recycleImageViewBitMap(ImageView imageView) {
			if (imageView != null) {
				BitmapDrawable bd = (BitmapDrawable) imageView.getDrawable();
				rceycleBitmapDrawable(bd);
			}
		}

		private void rceycleBitmapDrawable(BitmapDrawable bd) {
			if (bd != null) {
				Bitmap bitmap = bd.getBitmap();
				rceycleBitmap(bitmap);
			}
			bd = null;
		}

		private void rceycleBitmap(Bitmap bitmap) {
			if (bitmap != null && !bitmap.isRecycled()) {
				bitmap.recycle();
				bitmap = null;
			}
		}

	}

}
