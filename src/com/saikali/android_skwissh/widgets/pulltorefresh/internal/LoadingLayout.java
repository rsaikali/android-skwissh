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
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.saikali.android_skwissh.R;
import com.saikali.android_skwissh.widgets.pulltorefresh.PullToRefreshBase.Mode;

public class LoadingLayout extends FrameLayout {

	static final int DEFAULT_ROTATION_ANIMATION_DURATION = 1200;

	private final ImageView mHeaderImage;
	private final Matrix mHeaderImageMatrix;

	private final TextView mHeaderText;
	private final TextView mSubHeaderText;

	private String mPullLabel;
	private String mRefreshingLabel;
	private String mReleaseLabel;

	private float mRotationPivotX, mRotationPivotY;

	private final Animation mRotateAnimation;

	@SuppressWarnings("deprecation")
	public LoadingLayout(Context context, final Mode mode, TypedArray attrs) {
		super(context);
		ViewGroup header = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_header, this);
		this.mHeaderText = (TextView) header.findViewById(R.id.pull_to_refresh_text);
		this.mSubHeaderText = (TextView) header.findViewById(R.id.pull_to_refresh_sub_text);
		this.mHeaderImage = (ImageView) header.findViewById(R.id.pull_to_refresh_image);

		this.mHeaderImage.setScaleType(ScaleType.MATRIX);
		this.mHeaderImageMatrix = new Matrix();
		this.mHeaderImage.setImageMatrix(this.mHeaderImageMatrix);

		final Interpolator interpolator = new LinearInterpolator();
		this.mRotateAnimation = new RotateAnimation(0, 720, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		this.mRotateAnimation.setInterpolator(interpolator);
		this.mRotateAnimation.setDuration(DEFAULT_ROTATION_ANIMATION_DURATION);
		this.mRotateAnimation.setRepeatCount(Animation.INFINITE);
		this.mRotateAnimation.setRepeatMode(Animation.RESTART);

		switch (mode) {
		case PULL_UP_TO_REFRESH:
			// Load in labels
			this.mPullLabel = context.getString(R.string.pull_to_refresh_from_bottom_pull_label);
			this.mRefreshingLabel = context.getString(R.string.pull_to_refresh_from_bottom_refreshing_label);
			this.mReleaseLabel = context.getString(R.string.pull_to_refresh_from_bottom_release_label);
			break;

		case PULL_DOWN_TO_REFRESH:
		default:
			// Load in labels
			this.mPullLabel = context.getString(R.string.pull_to_refresh_pull_label);
			this.mRefreshingLabel = context.getString(R.string.pull_to_refresh_refreshing_label);
			this.mReleaseLabel = context.getString(R.string.pull_to_refresh_release_label);
			break;
		}

		if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderTextColor)) {
			ColorStateList colors = attrs.getColorStateList(R.styleable.PullToRefresh_ptrHeaderTextColor);
			this.setTextColor(null != colors ? colors : ColorStateList.valueOf(0xFF000000));
		}
		if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderSubTextColor)) {
			ColorStateList colors = attrs.getColorStateList(R.styleable.PullToRefresh_ptrHeaderSubTextColor);
			this.setSubTextColor(null != colors ? colors : ColorStateList.valueOf(0xFF000000));
		}
		if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderBackground)) {
			Drawable background = attrs.getDrawable(R.styleable.PullToRefresh_ptrHeaderBackground);
			if (null != background) {
				this.setBackgroundDrawable(background);
			}
		}

		// Try and get defined drawable from Attrs
		Drawable imageDrawable = null;
		if (attrs.hasValue(R.styleable.PullToRefresh_ptrDrawable)) {
			imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawable);
		}

		// If we don't have a user defined drawable, load the default
		if (null == imageDrawable) {
			imageDrawable = context.getResources().getDrawable(R.drawable.default_ptr_drawable);
		}

		// Set Drawable, and save width/height
		this.setLoadingDrawable(imageDrawable);

		this.reset();
	}

	public void reset() {
		this.mHeaderText.setText(this.wrapHtmlLabel(this.mPullLabel));
		this.mHeaderImage.setVisibility(View.VISIBLE);
		this.mHeaderImage.clearAnimation();

		this.resetImageRotation();

		if (TextUtils.isEmpty(this.mSubHeaderText.getText())) {
			this.mSubHeaderText.setVisibility(View.GONE);
		} else {
			this.mSubHeaderText.setVisibility(View.VISIBLE);
		}
	}

	public void releaseToRefresh() {
		this.mHeaderText.setText(this.wrapHtmlLabel(this.mReleaseLabel));
	}

	public void setPullLabel(String pullLabel) {
		this.mPullLabel = pullLabel;
	}

	public void refreshing() {
		this.mHeaderText.setText(this.wrapHtmlLabel(this.mRefreshingLabel));
		this.mHeaderImage.startAnimation(this.mRotateAnimation);

		this.mSubHeaderText.setVisibility(View.GONE);
	}

	public void setRefreshingLabel(String refreshingLabel) {
		this.mRefreshingLabel = refreshingLabel;
	}

	public void setReleaseLabel(String releaseLabel) {
		this.mReleaseLabel = releaseLabel;
	}

	public void pullToRefresh() {
		this.mHeaderText.setText(this.wrapHtmlLabel(this.mPullLabel));
	}

	public void setTextColor(ColorStateList color) {
		this.mHeaderText.setTextColor(color);
		this.mSubHeaderText.setTextColor(color);
	}

	public void setSubTextColor(ColorStateList color) {
		this.mSubHeaderText.setTextColor(color);
	}

	public void setTextColor(int color) {
		this.setTextColor(ColorStateList.valueOf(color));
	}

	public void setLoadingDrawable(Drawable imageDrawable) {
		// Set Drawable, and save width/height
		this.mHeaderImage.setImageDrawable(imageDrawable);
		this.mRotationPivotX = imageDrawable.getIntrinsicWidth() / 2f;
		this.mRotationPivotY = imageDrawable.getIntrinsicHeight() / 2f;
	}

	public void setSubTextColor(int color) {
		this.setSubTextColor(ColorStateList.valueOf(color));
	}

	public void setSubHeaderText(CharSequence label) {
		if (TextUtils.isEmpty(label)) {
			this.mSubHeaderText.setVisibility(View.GONE);
		} else {
			this.mSubHeaderText.setText(label);
			this.mSubHeaderText.setVisibility(View.VISIBLE);
		}
	}

	public void onPullY(float scaleOfHeight) {
		this.mHeaderImageMatrix.setRotate(scaleOfHeight * 90, this.mRotationPivotX, this.mRotationPivotY);
		this.mHeaderImage.setImageMatrix(this.mHeaderImageMatrix);
	}

	private void resetImageRotation() {
		this.mHeaderImageMatrix.reset();
		this.mHeaderImage.setImageMatrix(this.mHeaderImageMatrix);
	}

	private CharSequence wrapHtmlLabel(String label) {
		if (!this.isInEditMode())
			return Html.fromHtml(label);
		else
			return label;
	}
}
