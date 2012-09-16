/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.saikali.android_skwissh.widgets.pulltorefresh.internal;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.saikali.android_skwissh.R;
import com.saikali.android_skwissh.widgets.pulltorefresh.PullToRefreshBase;

public class IndicatorLayout extends FrameLayout implements AnimationListener {

	static final int DEFAULT_ROTATION_ANIMATION_DURATION = 150;

	private Animation mInAnim, mOutAnim;
	private ImageView mArrowImageView;

	private final Animation mRotateAnimation, mResetRotateAnimation;

	public IndicatorLayout(Context context, PullToRefreshBase.Mode mode) {
		super(context);

		this.mArrowImageView = new ImageView(context);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
		lp.topMargin = lp.bottomMargin = lp.leftMargin = lp.rightMargin = this.getResources().getDimensionPixelSize(R.dimen.indicator_internal_padding);
		this.addView(this.mArrowImageView, lp);

		int inAnimResId, outAnimResId;
		switch (mode) {
		case PULL_UP_TO_REFRESH:
			inAnimResId = R.anim.slide_in_from_bottom;
			outAnimResId = R.anim.slide_out_to_bottom;
			this.setBackgroundResource(R.drawable.indicator_bg_bottom);
			this.mArrowImageView.setImageResource(R.drawable.arrow_up);
			break;
		default:
		case PULL_DOWN_TO_REFRESH:
			inAnimResId = R.anim.slide_in_from_top;
			outAnimResId = R.anim.slide_out_to_top;
			this.setBackgroundResource(R.drawable.indicator_bg_top);
			this.mArrowImageView.setImageResource(R.drawable.arrow_down);
			break;
		}

		this.mInAnim = AnimationUtils.loadAnimation(context, inAnimResId);
		this.mInAnim.setAnimationListener(this);

		this.mOutAnim = AnimationUtils.loadAnimation(context, outAnimResId);
		this.mOutAnim.setAnimationListener(this);

		final Interpolator interpolator = new LinearInterpolator();
		this.mRotateAnimation = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		this.mRotateAnimation.setInterpolator(interpolator);
		this.mRotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
		this.mRotateAnimation.setFillAfter(true);

		this.mResetRotateAnimation = new RotateAnimation(-180, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		this.mResetRotateAnimation.setInterpolator(interpolator);
		this.mResetRotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
		this.mResetRotateAnimation.setFillAfter(true);

	}

	public final boolean isVisible() {
		Animation currentAnim = this.getAnimation();
		if (null != currentAnim)
			return this.mInAnim == currentAnim;

		return this.getVisibility() == View.VISIBLE;
	}

	public void hide() {
		this.startAnimation(this.mOutAnim);
	}

	public void show() {
		this.mArrowImageView.clearAnimation();
		this.startAnimation(this.mInAnim);
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (animation == this.mOutAnim) {
			this.mArrowImageView.clearAnimation();
			this.setVisibility(View.GONE);
		} else if (animation == this.mInAnim) {
			this.setVisibility(View.VISIBLE);
		}

		this.clearAnimation();
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// NO-OP
	}

	@Override
	public void onAnimationStart(Animation animation) {
		this.setVisibility(View.VISIBLE);
	}

	public void releaseToRefresh() {
		this.mArrowImageView.startAnimation(this.mRotateAnimation);
	}

	public void pullToRefresh() {
		this.mArrowImageView.startAnimation(this.mResetRotateAnimation);
	}

}
