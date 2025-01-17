package com.ch.mhy.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ch.mhy.R;
import com.ch.mhy.entity.Comics;
import com.ch.mhy.util.UrlConstant;
import com.ch.mhy.viewpager.BaseViewPager;

/**
 * 广告图片自动轮播控件</br>
 * <p/>
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
public class ImageCycleView extends LinearLayout {

    /**
     * 上下文
     */
    private Context mContext;

    /**
     * 图片轮播视图
     */
    private ViewPager mAdvPager = null;
//    private BaseViewPager mAdvPager = null;

    /**
     * 滚动图片视图适配器
     */
    private ImageCycleAdapter mAdvAdapter;

    /**
     * 图片轮播指示器控件
     */
    private ViewGroup mGroup;

    private TextView tv_home_lbname;

    /**
     * 图片轮播指示器-个图
     */
    private ImageView mImageView = null;

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

    /**
     * @param context
     */
    public ImageCycleView(Context context) {
        super(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public ImageCycleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mScale = context.getResources().getDisplayMetrics().density;
        LayoutInflater.from(context).inflate(R.layout.ad_cycle_view, this);
        mAdvPager = (ViewPager) findViewById(R.id.adv_pager);
        mAdvPager = (BaseViewPager) findViewById(R.id.adv_pager);
        mAdvPager.setOnPageChangeListener(new GuidePageChangeListener());
        mAdvPager.setCurrentItem(2);
        mAdvPager.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        // 开始图片滚动
                        startImageTimerTask();
                        break;
                    default:
                        // 停止图片滚动
                        stopImageTimerTask();
                        break;
                }
                return false;
            }
        });
        // 滚动图片右下指示器视图
        mGroup = (ViewGroup) findViewById(R.id.viewGroup);
        tv_home_lbname = (TextView) findViewById(R.id.tv_home_lbname);
    }

    /**
     * 装填图片数据
     *
     * @param imageUrlList
     * @param imageCycleViewListener
     */
    public void setImageResources(List<Comics> imageUrlList, ImageCycleViewListener imageCycleViewListener) {
        // 清除所有子视图
        mGroup.removeAllViews();
        // 图片广告数量
        final int imageCount = imageUrlList.size();
        mImageViews = new ImageView[imageCount];
        for (int i = 1; i < imageCount-1; i++) {
            mImageView = new ImageView(mContext);
            int imageParams = (int) (mScale * 5 + 0.5f);// XP与DP转换，适应不同分辨率
            int imagePadding = (int) (mScale * 2 + 0.5f);
            
            LayoutParams lp=new LayoutParams(imageParams, imageParams);
            lp.gravity=Gravity.CENTER;
            mImageView.setLayoutParams(lp);
            mImageView.setPadding(imagePadding, imagePadding, imagePadding, imagePadding);
            mImageViews[i] = mImageView;
            if (i == 1) {
                mImageViews[i].setBackgroundResource(R.drawable.banner_dian_focus);
            } else {
                mImageViews[i].setBackgroundResource(R.drawable.banner_dian_blur);
            }
            mGroup.addView(mImageViews[i]);
        }
        mAdvAdapter = new ImageCycleAdapter(mContext, imageUrlList, imageCycleViewListener);
        mAdvPager.setAdapter(mAdvAdapter);
        mImageIndex=1;
        mAdvPager.setCurrentItem(mImageIndex);
        startImageTimerTask();
    }

    /**
     * 开始轮播(手动控制自动轮播与否，便于资源控制)
     */
    public void startImageCycle() {
        startImageTimerTask();
    }

    /**
     * 暂停轮播——用于节省资源
     */
    public void pushImageCycle() {
        stopImageTimerTask();
    }

    /**
     * 开始图片滚动任务
     */
    private void startImageTimerTask() {
        stopImageTimerTask();
        // 图片每3秒滚动一次
        mHandler.postDelayed(mImageTimerTask, 3000);
    }

    /**
     * 停止图片滚动任务
     */
    private void stopImageTimerTask() {
        mHandler.removeCallbacks(mImageTimerTask);
    }

    private Handler mHandler = new Handler();

    /**
     * 图片自动轮播Task
     */
    private Runnable mImageTimerTask = new Runnable() {

        @Override
        public void run() {
            if (mImageViews != null) {
                // 下标等于图片列表长度说明已滚动到最后一张图片,重置下标
                if ((++mImageIndex) == mImageViews.length-1) {
                	Log.d("tag", "mImageIndex "+mImageIndex);
                    mImageIndex = 1;
                }
                mAdvPager.setCurrentItem(mImageIndex);
            }
        }
    };

    /**
     * 轮播图片状态监听器
     *
     * @author minking
     */
    private final class GuidePageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE)
                startImageTimerTask(); // 开始下次计时
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int index) {
        	
        	Log.d("tag", "index"+index);
        	
        	if(index==0){
        		index=mImageViews.length-2;
        		mAdvPager.setCurrentItem(index);
        	}

        	// 设置当前显示的图片下标
        	if(index==mImageViews.length-1){
        		index=1;
        		mAdvPager.setCurrentItem(index);
        	}
        	mImageIndex=index;
        	 tv_home_lbname.setText(mAdList.get(mImageIndex).getmTitle());

        	   mImageViews[index].setBackgroundResource(R.drawable.banner_dian_focus);
            // 设置图片滚动指示器背景
            for (int i = 1; i < mImageViews.length-1; i++) {
                if (index != i) {
                    mImageViews[i].setBackgroundResource(R.drawable.banner_dian_blur);
                }
            }

        }

    }
    
    /**
     * 图片资源列表
     */
    private List<Comics> mAdList = new ArrayList<Comics>();

    private class ImageCycleAdapter extends PagerAdapter {

        /**
         * 图片视图缓存列表
         */
        private ArrayList<ImageView> mImageViewCacheList;


        /**
         * 广告图片点击监听器
         */
        private ImageCycleViewListener mImageCycleViewListener;

        private Context mContext;

        public ImageCycleAdapter(Context context, List<Comics> adList, ImageCycleViewListener imageCycleViewListener) {
            mContext = context;
            mAdList = adList;
            mImageCycleViewListener = imageCycleViewListener;
            mImageViewCacheList = new ArrayList<ImageView>();
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
        public Object instantiateItem(ViewGroup container, final int position) {
            
            ImageView imageView = null;
            if (mImageViewCacheList.isEmpty()) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);

            } else {
                imageView = mImageViewCacheList.remove(0);
            }
            // 设置图片点击监听
            imageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mImageCycleViewListener.onImageClick(position, v);
                }
            });
            
            if(mAdList.get(position).getPicPath()!=null){
            	String imageUrl = UrlConstant.hostServer + mAdList.get(position).getPicPath();
            	imageView.setTag(imageUrl);
            	container.addView(imageView);
            	mImageCycleViewListener.displayImage(imageUrl, imageView);
            	tv_home_lbname.setText(mAdList.get(position).getmTitle());
            }

            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ImageView view = (ImageView) object;
            container.removeView(view);
            mImageViewCacheList.add(view);
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
        public void displayImage(String imageURL, ImageView imageView);

        /**
         * 单击图片事件
         *
         * @param position
         * @param imageView
         */
        public void onImageClick(int position, View imageView);
    }

}
