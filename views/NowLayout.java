package com.sky.redhome.views;

import com.nineoldandroids.animation.AnimatorInflater;
import com.nineoldandroids.animation.ObjectAnimator;
import com.sky.redhome.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

public class NowLayout extends LinearLayout implements OnGlobalLayoutListener {

	public NowLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initLayoutObserver();

	}

	public NowLayout(Context context) {
		super(context);
		initLayoutObserver();
	}

	private void initLayoutObserver() {
		setOrientation(LinearLayout.VERTICAL);
		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	@Override
	public void onGlobalLayout() {
		getViewTreeObserver().removeGlobalOnLayoutListener(this);

		final int heightPx = getContext().getResources().getDisplayMetrics().heightPixels;

		final int childCount = getChildCount();

		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);

			int[] location = new int[2];

			child.getLocationOnScreen(location);

			if (location[1] > heightPx) {
				break;
			}

			child.startAnimation(AnimationUtils.loadAnimation(getContext(),
					R.anim.slide_up));

			ObjectAnimator animator = (ObjectAnimator) AnimatorInflater
					.loadAnimator(getContext(), R.animator.rotate_animation);

			animator.setTarget(child);
			animator.start();
		}

	}
	
	@Override
	public void addView(View v){
		super.addView(v);
		
		v.startAnimation(AnimationUtils.loadAnimation(getContext(),
				R.anim.slide_up));

		ObjectAnimator animator = (ObjectAnimator) AnimatorInflater
				.loadAnimator(getContext(), R.animator.rotate_animation);

		animator.setTarget(v);
		animator.start();
	}

}
