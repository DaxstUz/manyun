//package com.ch.mhy.widget;
//
//import com.ch.mhy.R;
//import com.ch.mhy.activity.book.ReadingActivity;
//import com.ch.mhy.util.Utils;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Matrix;
//import android.graphics.PointF;
//import android.graphics.drawable.Drawable;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.MotionEvent;
//import android.view.ScaleGestureDetector;
//import android.view.View;
//import android.widget.ImageView;
//
///**
// * 自定义可以缩放的ImageView
// * @author DaxstUz 2416738717
// * 2015年6月2日
// *
// */
//public class TouchImageView extends ImageView {  
//	
//	private boolean isLarge=false;
//	
//	private float  defaultDistance;
//	
//    public float getDefaultDistance() {
//		return defaultDistance;
//	}
//
//	public boolean isLarge() {
//		return isLarge;
//	}
//
//    /**  图片长度*/ 
//    float mImageWidth;
//    /**  图片高度 */ 
//    float mImageHeight;
//  
//    /**  模板Matrix，用以初始化 */ 
//   public  Matrix matrix;  
//  
//    // We can be in one of these 3 states  
//    static final int NONE = 0;  
//    static final int DRAG = 1;  
//    static final int ZOOM = 2;  
//    int mode = NONE;  
//  
//    // Remember some things for zooming  
//    public PointF last = new PointF();  
//    public PointF start = new PointF();  
//    float minScale = 0.1f;  
//    float maxScale = 50f;  
//    float[] m;  
//  
//    int viewWidth, viewHeight;  
//    static final int CLICK = 3;  
//    public float saveScale = 1f;  //默认为1f
//    protected float origWidth, origHeight;  
//    int oldMeasuredWidth, oldMeasuredHeight;  
//  
//    public ScaleGestureDetector mScaleDetector;  
//  
//    Context context;  
//  
//    public TouchImageView(Context context) {  
//        super(context);  
//        sharedConstructing(context);  
//    }  
//    
//    private ImageCycleView2 ra;
//    private boolean flag;
//    public TouchImageView(Context context,ImageCycleView2 ra,boolean flag) {  
//    	super(context);  
//    	sharedConstructing(context);  
//    	this.ra=ra;
//    	this.flag=flag;
//    }  
//  
//    public TouchImageView(Context context, AttributeSet attrs) {  
//        super(context, attrs);  
//        sharedConstructing(context);  
//    }  
//      
//    public void sharedConstructing(Context context) {  
//        super.setClickable(true);  
//        this.context = context;  
//        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener()); 
//        matrix = new Matrix();  
//        m = new float[9];  
//        setImageMatrix(matrix);  
//        setScaleType(ScaleType.MATRIX);  
//        
//        setOnTouchListener(new OnTouchListener() {  
//        	int count = 0;
//        	float distance = 0;
//            @Override  
//            public boolean onTouch(View v, MotionEvent event) {  
//                mScaleDetector.onTouchEvent(event);  
//                PointF curr = new PointF(event.getX(), event.getY());  
//                count++;
//                switch (event.getAction()) {  
//                    case MotionEvent.ACTION_DOWN:  
//                        last.set(curr);  
//                        start.set(last);  
//                        mode = DRAG;  
//                        
//                        
//                    	if(((ReadingActivity)ra.add).getLayout().getChildAt(1) instanceof View){
//                   		 if (((ReadingActivity)ra.add).getLayout().getChildAt(1).findViewById(R.id.sb_light)!=null) {
//                   			 flag = false;
//                   			 ((ReadingActivity)ra.add).getLayout().removeViewAt(1);
//                   			 return flag;
//							}
//                   	}else{
//                   		flag = true;
//                   	}
//                   	if(saveScale>1){
//                   		ra.mAdvPager.setScroll(true);
//                    	}
//                        break;  
//                          
//                    case MotionEvent.ACTION_MOVE:  
//                        if (mode == DRAG) {  
//                        	ra.mAdvPager.setScroll(true);
//                            float deltaX = curr.x - last.x;  
//                            float deltaY = curr.y - last.y;  
//                            float fixTransX = getFixDragTrans(deltaX, viewWidth, origWidth * saveScale);  
//                            float fixTransY = getFixDragTrans(deltaY, viewHeight, origHeight * saveScale);  
//                            matrix.postTranslate(fixTransX, fixTransY);  
//                            fixTrans();  
//                            last.set(curr.x, curr.y); 
//                            
//                            distance = Math.abs(deltaX-deltaY);
//                            
//                            float[] values = new float[9];
//                            getImageMatrix().getValues(values);
//                            
//                            float sc = values[0];
//                            float leftX = values[2];
//                            float topY = values[5];
//                            
//                            boolean isLeft = (deltaX>0 && leftX>=0);
//                            boolean isRight = (deltaX<0 &&(int) (leftX + mImageWidth*sc) <= viewWidth);
//                            boolean isTop = (deltaY>0 && topY >= 10);
//                            boolean isBot = (deltaY<0 && topY + mImageHeight + 20 <= viewHeight);
//                            if(isLeft || isRight || (isBot && isLeft) || (isBot && isRight)
//                            	|| (isTop && isLeft) || (isTop && isRight)){
//                            	ra.mAdvPager.setScroll(false);
//                            }
////                            else{
////                            	moveTo(TouchImageView.this, curr, deltaX, deltaY);
////                            }
//                            count=0;
//                        }  
//                        
////                        if (view.mode == view.DRAG) { 
////                        	
////                            float deltaX = curr.x - view.last.x;
////                            float deltaY = curr.y - view.last.y;
////                            
////                            float[] values = new float[9];
////                            view.getImageMatrix().getValues(values);
////                            
////                            float sc = values[0];
////                            float leftX = values[2];
////                            float topY = values[5];
////                            
////                            boolean isLeft = (deltaX>0 && leftX>=0);
////                            boolean isRight = (deltaX<0 &&(int) (leftX + view.mImageWidth*sc) <= width);
////                            boolean isTop = (deltaY>0 && topY >= 10);
////                            boolean isBot = (deltaY<0 && topY + mImageHeight + 20 <= height);
////                            if(isLeft || isRight || (isBot && isLeft) || (isBot && isRight)
////                            	|| (isTop && isLeft) || (isTop && isRight)){
////                            	ra.mAdvPager.setScroll(false);
////                            }else{
////                            	moveTo(view, curr, deltaX, deltaY);
////                            }
////                            count=0;
////                        }
//                        break;  
//  
//                    case MotionEvent.ACTION_UP:  
//                        mode = NONE;  
//                        
//                        if(count<=3&&distance<=0.2)
//                            ((ReadingActivity)ra.add).onSingleTapUp(event);
//                       	count=0;
//                       	distance=0;
//                       	
//                        int xDiff = (int) Math.abs(curr.x - start.x);  
//                        int yDiff = (int) Math.abs(curr.y - start.y);  
//                        if (xDiff < CLICK && yDiff < CLICK)  
//                            performClick();  
//                        break;  
//  
//                    case MotionEvent.ACTION_POINTER_UP:  
//                        mode = NONE;  
//                        break;  
//                }  
//                  
//                setImageMatrix(matrix);  
//                invalidate();  
//                return true; // indicate event was handled  
//            }  
//  
//            
////        	private void moveTo(TouchImageView view, PointF curr,
////					float deltaX, float deltaY) {
////				distance = Math.abs(deltaX-deltaY);
////				fixTransX = view.getFixDragTrans(deltaX, view.viewWidth, view.origWidth * view.saveScale);  
////				fixTransY = view.getFixDragTrans(deltaY, view.viewHeight, view.origHeight * view.saveScale);  
////				view.matrix.postTranslate(fixTransX, fixTransY);  
////				view.fixTrans();  
////				view.last.set(curr.x, curr.y);
////			}
//        			
//        });  
//    }  
//  
//    public void setMaxZoom(float x) {  
//        maxScale = x;  
//    }  
//  
//    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {  
//        @Override  
//        public boolean onScaleBegin(ScaleGestureDetector detector) {  
//            mode = ZOOM;  
//            return true;  
//        }  
//  
//        @Override  
//        public boolean onScale(ScaleGestureDetector detector) {  
//            float mScaleFactor = detector.getScaleFactor(); 
//            float origScale = saveScale;  
//            saveScale *= mScaleFactor;  
//            if (saveScale > maxScale) {  
//                saveScale = maxScale;  
//                mScaleFactor = maxScale / origScale;  
//            } else if (saveScale < minScale) {  
//                saveScale = minScale;  
//                mScaleFactor = minScale / origScale;  
//            }  
//  
//            if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight)  
//                matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2, viewHeight / 2);  
//            else  
//                matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());  
//  
//            fixTrans();  
//            return true;  
//        }  
//    }  
//  
//    void fixTrans() {  
//        matrix.getValues(m);  
//        float transX = m[Matrix.MTRANS_X];  
//        float transY = m[Matrix.MTRANS_Y];  
//          
//        float fixTransX = getFixTrans(transX, viewWidth, origWidth * saveScale);  
//        float fixTransY = getFixTrans(transY, viewHeight, origHeight * saveScale);  
//  
//        if (fixTransX != 0 || fixTransY != 0)  
//            matrix.postTranslate(fixTransX, fixTransY);  
//    }  
//  
//    float getFixTrans(float trans, float viewSize, float contentSize) {  
//        float minTrans, maxTrans;  
//        if (contentSize <= viewSize) {  
//            minTrans = 0;  
//            maxTrans = viewSize - contentSize;  
//        } else {  
//            minTrans = viewSize - contentSize;  
//            maxTrans = 0;  
//        }  
//  
//        if (trans < minTrans)  
//            return -trans + minTrans;  
//        if (trans > maxTrans)  
//            return -trans + maxTrans;  
//        return 0;  
//    }  
//      
//    float getFixDragTrans(float delta, float viewSize, float contentSize) {  
//        if (contentSize <= viewSize) {  
//            return 0;  
//        }  
//        return delta;  
//    }  
//  
//    @Override  
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {  
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);  
//        viewWidth = MeasureSpec.getSize(widthMeasureSpec);  
//        viewHeight = MeasureSpec.getSize(heightMeasureSpec); 
//        
////        Log.d("tag", "viewWidth: " + viewWidth + " viewHeight : " + viewHeight);  
//          
//        // Rescales image on rotation  
//        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight  
//                || viewWidth == 0 || viewHeight == 0)  
//            return;  
//        oldMeasuredHeight = viewHeight;  
//        oldMeasuredWidth = viewWidth;  
//        if (saveScale == 1) {
//            //Fit to screen.  
//            float scale;  
//  
//            Drawable drawable = getDrawable();  
//            if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)  
//                return;  
//            int bmWidth = drawable.getIntrinsicWidth();  
//            int bmHeight = drawable.getIntrinsicHeight();  
//              
////            Log.d("bmSize", "bmWidth: " + bmWidth + " bmHeight : " + bmHeight);  
//  
//            float scaleX = (float) viewWidth / (float) bmWidth;  
//            float scaleY = (float) viewHeight / (float) bmHeight;  
//            scale = Math.min(scaleX, scaleY);  
//            matrix.setScale(scale, scale);  
//  
//            // Center the image  
//            float redundantYSpace = (float) viewHeight - (scale * (float) bmHeight);  
//            float redundantXSpace = (float) viewWidth - (scale * (float) bmWidth);  
//            redundantYSpace /= (float) 2;  
//            redundantXSpace /= (float) 2;  
//  
//            matrix.postTranslate(redundantXSpace, redundantYSpace);  
//  
//            origWidth = viewWidth - 2 * redundantXSpace;  
//            origHeight = viewHeight - 2 * redundantYSpace;  
//            setImageMatrix(matrix);  
//        }  
//        
////        setBmCenter();
//        
//        fixTrans();  
//    }
//
//    
////    public void setBmCenter(){
////    	if (saveScale >= 1) {
////            //Fit to screen.  
////            float scale;  
////  
////            Drawable drawable = getDrawable();  
////            if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)  
////                return;  
////            int bmWidth = drawable.getIntrinsicWidth();  
////            int bmHeight = drawable.getIntrinsicHeight();  
////              
////            Log.d("bmSize", "bmWidth: " + bmWidth + " bmHeight : " + bmHeight);  
////  
////            float scaleX = (float) viewWidth / (float) bmWidth;  
////            float scaleY = (float) viewHeight / (float) bmHeight;  
////            scale = Math.min(scaleX, scaleY);  
////            matrix.setScale(scale, scale);  
////  
////            // Center the image  
////            float redundantYSpace = (float) viewHeight - (scale * (float) bmHeight);  
////            float redundantXSpace = (float) viewWidth - (scale * (float) bmWidth);  
////            redundantYSpace /= (float) 2;  
////            redundantXSpace /= (float) 2;  
////  
////            matrix.postTranslate(redundantXSpace, redundantYSpace);  
////  
////            origWidth = viewWidth - 2 * redundantXSpace;  
////            origHeight = viewHeight - 2 * redundantYSpace;  
////            setImageMatrix(matrix);  
////        }  
////    }
//    
////    private int[] wh = Utils.getScreenDispaly();
//    
//	@Override
//	public void setImageBitmap(Bitmap bm) {
//		super.setImageBitmap(bm);
//		//设置完图片后，获取该图片的坐标变换矩阵
//		//matrix.set(getImageMatrix());
//        float[] values=new float[9];
//        matrix.getValues(values);
//        //图片宽度为屏幕宽度除缩放倍数
//        mImageWidth=bm.getWidth();
//        mImageHeight=bm.getHeight();
//	}  
//    
//    
//}  