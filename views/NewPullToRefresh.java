package com.sky.redhome.pulltorefresh;

import com.sky.redhome.R;
import com.sky.redhome.RedhomeActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Scroller;

/**
 * Class: PullToRefreshScrollView.java<br>
 * Date: 2013/04/18<br>
 * Author: Haoming Zhang <br>
 * Email: sdqhzhm@gmail.com<br>
 */

public class NewPullToRefresh extends ScrollView {

	final static String TAG = "TestPullToRefresh";
	
	static final int ROTATION_ANIMATION_DURATION = 1200;
	static final int ALPHA_ANIMATION_DURATION = 400;
	
	static final float FRICTION = 2.0f;

	private static final int READY_TO_PULL = 1;
	private static final int PULL_ING = 2;
	private static final int DONT_PULL = 3;
	private static final int REFRESHING = 4;
	private static final String[] STATUS = {"","READY_TO_PULL","PULL_ING","DONT_PULL","REFRESHING","",};

	private int mRefreshState = READY_TO_PULL;
	private boolean mIsBeingDragged = false;
	private boolean mIsRefresh = false;


	private Context mContext;
	private Scroller mScroller;
	TouchTool tool;
	int left, top;
	float startX, startY, currentX, currentY;
	private float mLastMotionX, mLastMotionY;
	private float mInitialMotionX, mInitialMotionY;
	
	private int mTouchSlop;
		
	int bgViewH, iv1W;
	int rootW, rootH;
	View headView;
	View bgView;
	//4-25 添加家在动画
	ImageView mHeaderImage;
	private Matrix mHeaderImageMatrix;
	private Animation mRotateAnimation;
	private float mRotationPivotX, mRotationPivotY;
	private Animation mAlphaAnimation;
	
	private OnRefreshListener mOnRefreshListener;
	
	boolean isHeaderPull = false;
	boolean scrollerType;
	static final int len = 0xc8;

	public NewPullToRefresh(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setOnTouchListener(new TouchListen());
		init(context);
	}

	public NewPullToRefresh(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		mScroller = new Scroller(mContext);
		this.setOnTouchListener(new TouchListen());
		init(context);

	}

	public NewPullToRefresh(Context context) {
		super(context);
		this.setOnTouchListener(new TouchListen());
		init(context);

	}
	
	private void init(Context context){
		ViewConfiguration config = ViewConfiguration.get(context);
		mTouchSlop = config.getScaledTouchSlop();
		
		
		
		mRotateAnimation = new RotateAnimation(0, 720, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRotateAnimation.setInterpolator(new LinearInterpolator());
		mRotateAnimation.setDuration(ROTATION_ANIMATION_DURATION);
		mRotateAnimation.setRepeatCount(Animation.INFINITE);
		mRotateAnimation.setRepeatMode(Animation.RESTART);
		
		mAlphaAnimation = new AlphaAnimation(1f,0f);
		mAlphaAnimation.setDuration(ALPHA_ANIMATION_DURATION);
		mAlphaAnimation.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation arg0) {
				// TODO Auto-generated method stub
				mHeaderImage.clearAnimation();
				mHeaderImage.setAlpha(0);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
		});

	}
	
	private void onPullImpl(float scaleOfLayout) {
		
		float angle;
		if (mIsBeingDragged) {
//			angle = scaleOfLayout * 90f;
			angle = scaleOfLayout * 400f;
		} else {
			angle = Math.max(0f, Math.min(180f, scaleOfLayout * 360f - 180f));
		}
		Log.i(TAG, "angle " +angle);

		mHeaderImageMatrix.setRotate(-angle, mRotationPivotX, mRotationPivotY);
		mHeaderImage.setImageMatrix(mHeaderImageMatrix);
		mHeaderImage.setAlpha(Math.abs(angle)>254?255:(int)Math.abs(angle));
	}
	
	private void onPull(){
		
		if(mIsRefresh) return ;
		
		final int newScrollValue;
		final int itemDimension;
		final float initialMotionValue, lastMotionValue;
		
		initialMotionValue = mInitialMotionY;
		lastMotionValue = mLastMotionY;
		
		newScrollValue = Math.round(Math.min(initialMotionValue - lastMotionValue, 0) / FRICTION);
		itemDimension = bgView.getHeight();		
		Log.i(TAG, "newScrollValue " +newScrollValue);

		if (newScrollValue != 0) {
			float scale = Math.abs(newScrollValue) / (float) itemDimension;
			onPullImpl(scale);
			Log.i(TAG, "scale " +scale);
		}
	}

	private void refreshingImpl() {
		mHeaderImage.startAnimation(mRotateAnimation);
	}
	

	
	private boolean isReadyForPull(){
		return this.getScrollY() == 0;
	}

	public void computeScroll() {
		Log.i(TAG, "checkpoint 0");

		if (mScroller.computeScrollOffset() && mRefreshState == PULL_ING) {
			Log.i(TAG, "checkpoint 1");
			int x = mScroller.getCurrX();
			int y = mScroller.getCurrY();
			System.out.println("x=" + x);
			System.out.println("y=" + y);
			bgView.layout(0, 0, x + bgView.getWidth(), y);
			invalidate();
			// 重点
			if (!mScroller.isFinished() && scrollerType && y > 200) {// 重点判断
				bgView.setLayoutParams(new RelativeLayout.LayoutParams(bgView
						.getWidth(), y));
			}else {
				mRefreshState = DONT_PULL;
			}

		}else {
			super.computeScroll();
		}
	}
	
	@Override
	public final boolean onInterceptTouchEvent(MotionEvent event) {
		
		//进行判断 增加效率
		if(null == headView){
			headView = RedhomeActivity.headerView;
			bgView = headView.findViewById(R.id.redhome_image);
			mHeaderImage = (ImageView) headView.findViewById(R.id.redhome_loading_img);
			
			mHeaderImage.setScaleType(ScaleType.MATRIX);
			mHeaderImageMatrix = new Matrix();
			mHeaderImage.setImageMatrix(mHeaderImageMatrix);
			
			if (null != mHeaderImage) {
				mRotationPivotX = Math.round(mHeaderImage.getWidth() / 2f);
				mRotationPivotY = Math.round(mHeaderImage.getHeight() / 2f);
			}
		}
		
/*		if (!isPullToRefreshEnabled()) {
			return false;
		}
*/
		final int action = event.getAction();

		if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
			mIsBeingDragged = false;
			return false;
		}

		if (action != MotionEvent.ACTION_DOWN && mIsBeingDragged) {
			return true;
		}

		switch (action) {
			case MotionEvent.ACTION_MOVE: {
				// If we're refreshing, and the flag is set. Eat all MOVE events
/*				if (!mScrollingWhileRefreshingEnabled && isRefreshing()) {
					return true;
				}
*/				
				Log.i("onInterceptTouchEvent", "ACTION_MOVE");
				
				break;
			}
			case MotionEvent.ACTION_DOWN: {
				Log.i("onInterceptTouchEvent", "ACTION_DOWN!! " + this.getScrollY());
				
				if(isReadyForPull()){
					mLastMotionY = mInitialMotionY = event.getY();
					mLastMotionX = mInitialMotionX = event.getX();
					bgViewH = bgView.getHeight();

					mIsBeingDragged = false;
					//5-1添加 防止动画重复
					mHeaderImage.clearAnimation();
				}
				tool = new TouchTool(bgView.getLeft(), bgView.getBottom(),
						bgView.getLeft(), bgView.getBottom() + len);
				
				break;
			}
		}

//		return mIsBeingDragged;
		return super.onInterceptTouchEvent(event);
		
	}

	public class TouchListen implements OnTouchListener {
		
		int topPadding;

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			Log.i(TAG,"STATU　"+STATUS[mRefreshState]);
			int action = event.getAction();		
			Log.i(TAG, "Action " + action);
			
/*			if (event.getAction() == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0) {
				return false;
			}
*/			
			switch (action) {
/*			需要处理 ScrollView 与 内部卡片 有关于  ACTION_DOWN 的监听冲突，
			如果卡片获取到ACTION_DOWN之后 ScrollView是无法获取此事件的
*/			case MotionEvent.ACTION_DOWN:
				Log.i("ListView2", "ACTION_DOWN!!!");
				if (isReadyForPull()) {
					mLastMotionY = mInitialMotionY = event.getY();
					mLastMotionX = mInitialMotionX = event.getX();
					bgViewH = bgView.getHeight();

//					return true;
				}
				break;
				
			case MotionEvent.ACTION_MOVE:
				
				if(isReadyForPull() && !mIsBeingDragged){
					final float y = event.getY(), x = event.getX();
					final float diff, oppositeDiff, absDiff;
					
					diff = y - mLastMotionY;
					oppositeDiff = x - mLastMotionX;
					absDiff = Math.abs(diff);
					
					if (absDiff > mTouchSlop && (absDiff > Math.abs(oppositeDiff)) ) {
						if (diff >= 1f) {
							mLastMotionY = y;
							mLastMotionX = x;
							mIsBeingDragged = true;
						}						
					}

				}
								 
				if(mIsBeingDragged){
					// topPadding 下拉距离？
					topPadding = (int) (((event.getY() - mInitialMotionY) - 80) / 1.7);

					//changX 下拉为正 上滑为负
					int changX = (int) (event.getY() - mLastMotionY);
					mLastMotionY = event.getY();
					
					Log.e(TAG, " changX==" + changX);					
					Log.e(TAG, " topPadding==" + topPadding);				
			
						
					if (headView.isShown() && headView.getTop() >= 0) {
						if (tool != null) {
							int t = tool.getScrollY(mLastMotionY - mInitialMotionY);
							Log.i(TAG,"t "+t+" hgView.getBottom "+bgView.getBottom()+" headView.getBottom() "+(headView.getBottom()+len));
//							if (t >= bgView.getBottom() && t <= headView.getBottom() + len) {
							if (t <= headView.getBottom() + len && topPadding >= -8) {
								Log.i(TAG, "checkpoint 2");
								//4-25 加入 刷新动画	并且修改滑动间距的计算方式							
								onPull();
								bgView.setLayoutParams(new RelativeLayout.LayoutParams(
										bgView.getWidth(), t));
							}
						}
						scrollerType = false;
					}			
					
					mRefreshState = PULL_ING;
					return true;
				}					
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				Log.i("ListView2", "ACTION_UP!!!");
				scrollerType = true;
				if(mIsBeingDragged){
					Log.i(TAG, "checkpoint 3");
					mIsBeingDragged = false;
					//松开时下拉距离的判断
					if(topPadding > 200 && !mIsRefresh){
						Log.i(TAG,"松开并且进行下拉刷新");
						callRefreshListener();
					}else{
						resetImpl();
					}
					
					
					mScroller.startScroll(bgView.getLeft(), bgView.getBottom(),
							0 - bgView.getLeft(),bgViewH - bgView.getBottom(), 200);					
//					return true;
				}

				invalidate();
				break;
			}

			return false;
		}

	}
	
	private void resetImpl(){
		mHeaderImage.clearAnimation();
		mHeaderImage.startAnimation(mAlphaAnimation);
/*		if (null != mHeaderImageMatrix) {
			mHeaderImageMatrix.reset();
			mHeaderImage.setImageMatrix(mHeaderImageMatrix);
		}
*/	}
	public void setOnRefreshListener(OnRefreshListener listener){
		mOnRefreshListener = listener;		
	}
	public static interface OnRefreshListener{
		public void onRefresh();
	}
	private void callRefreshListener() {
		if (null != mOnRefreshListener) {
			mOnRefreshListener.onRefresh();
			mHeaderImage.startAnimation(mRotateAnimation);
			mIsRefresh = true;
		}
	}
	public void onRefreshComplete(){
		mIsRefresh = false;
		resetImpl();
	}
	
}
