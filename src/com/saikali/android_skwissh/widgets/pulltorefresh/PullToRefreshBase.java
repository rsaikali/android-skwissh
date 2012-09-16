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
package com.saikali.android_skwissh.widgets.pulltorefresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.saikali.android_skwissh.R;
import com.saikali.android_skwissh.widgets.pulltorefresh.internal.LoadingLayout;
import com.saikali.android_skwissh.widgets.pulltorefresh.internal.SDK16;

public abstract class PullToRefreshBase<T extends View> extends LinearLayout implements IPullToRefresh<T> {

	// ===========================================================
	// Constants
	// ===========================================================

	static final boolean DEBUG = false;

	static final String LOG_TAG = "PullToRefresh";

	static final float FRICTION = 2.0f;

	public static final int SMOOTH_SCROLL_DURATION_MS = 200;
	public static final int SMOOTH_SCROLL_LONG_DURATION_MS = 325;

	static final int PULL_TO_REFRESH = 0x0;
	static final int RELEASE_TO_REFRESH = 0x1;
	static final int REFRESHING = 0x2;
	static final int MANUAL_REFRESHING = 0x3;

	static final Mode DEFAULT_MODE = Mode.PULL_DOWN_TO_REFRESH;

	static final String STATE_STATE = "ptr_state";
	static final String STATE_MODE = "ptr_mode";
	static final String STATE_CURRENT_MODE = "ptr_current_mode";
	static final String STATE_DISABLE_SCROLLING_REFRESHING = "ptr_disable_scrolling";
	static final String STATE_SHOW_REFRESHING_VIEW = "ptr_show_refreshing_view";
	static final String STATE_SUPER = "ptr_super";

	// ===========================================================
	// Fields
	// ===========================================================

	private int mTouchSlop;
	private float mLastMotionX;
	private float mLastMotionY;
	private float mInitialMotionY;

	private boolean mIsBeingDragged = false;
	private int mState = PULL_TO_REFRESH;
	private Mode mMode = DEFAULT_MODE;

	private Mode mCurrentMode;
	T mRefreshableView;
	private FrameLayout mRefreshableViewWrapper;

	private boolean mShowViewWhileRefreshing = true;
	private boolean mDisableScrollingWhileRefreshing = true;
	private boolean mFilterTouchEvents = true;
	private boolean mOverScrollEnabled = true;

	private Interpolator mScrollAnimationInterpolator;

	private LoadingLayout mHeaderLayout;
	private LoadingLayout mFooterLayout;

	private int mHeaderHeight;

	private OnRefreshListener<T> mOnRefreshListener;
	private OnRefreshListener2<T> mOnRefreshListener2;

	private SmoothScrollRunnable mCurrentSmoothScrollRunnable;

	// ===========================================================
	// Constructors
	// ===========================================================

	public PullToRefreshBase(Context context) {
		super(context);
		this.init(context, null);
	}

	public PullToRefreshBase(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.init(context, attrs);
	}

	public PullToRefreshBase(Context context, Mode mode) {
		super(context);
		this.mMode = mode;
		this.init(context, null);
	}

	@Override
	public void addView(View child, int index, ViewGroup.LayoutParams params) {
		if (DEBUG) {
			Log.d(LOG_TAG, "addView: " + child.getClass().getSimpleName());
		}

		final T refreshableView = this.getRefreshableView();

		if (refreshableView instanceof ViewGroup) {
			((ViewGroup) refreshableView).addView(child, index, params);
		} else
			throw new UnsupportedOperationException("Refreshable View is not a ViewGroup so can't addView");
	}

	@Override
	public final Mode getCurrentMode() {
		return this.mCurrentMode;
	}

	@Override
	public final boolean getFilterTouchEvents() {
		return this.mFilterTouchEvents;
	}

	@Override
	public final Mode getMode() {
		return this.mMode;
	}

	@Override
	public final T getRefreshableView() {
		return this.mRefreshableView;
	}

	@Override
	public final boolean getShowViewWhileRefreshing() {
		return this.mShowViewWhileRefreshing;
	}

	@Override
	public final boolean hasPullFromTop() {
		return this.mCurrentMode == Mode.PULL_DOWN_TO_REFRESH;
	}

	@Override
	public final boolean isDisableScrollingWhileRefreshing() {
		return this.mDisableScrollingWhileRefreshing;
	}

	@Override
	public final boolean isPullToRefreshEnabled() {
		return this.mMode != Mode.DISABLED;
	}

	@Override
	public final boolean isPullToRefreshOverScrollEnabled() {
		if (VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD)
			return this.mOverScrollEnabled && OverscrollHelper.isAndroidOverScrollEnabled(this.mRefreshableView);
		return false;
	}

	@Override
	public final boolean isRefreshing() {
		return this.mState == REFRESHING || this.mState == MANUAL_REFRESHING;
	}

	@Override
	public final boolean onInterceptTouchEvent(MotionEvent event) {

		if (!this.isPullToRefreshEnabled())
			return false;

		final int action = event.getAction();

		if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
			this.mIsBeingDragged = false;
			return false;
		}

		if (action != MotionEvent.ACTION_DOWN && this.mIsBeingDragged)
			return true;

		switch (action) {
		case MotionEvent.ACTION_MOVE: {
			// If we're refreshing, and the flag is set. Eat all MOVE events
			if (this.mDisableScrollingWhileRefreshing && this.isRefreshing())
				return true;

			if (this.isReadyForPull()) {
				final float y = event.getY();
				final float dy = y - this.mLastMotionY;
				final float yDiff = Math.abs(dy);
				final float xDiff = Math.abs(event.getX() - this.mLastMotionX);

				if (yDiff > this.mTouchSlop && (!this.mFilterTouchEvents || yDiff > xDiff)) {
					if (this.mMode.canPullDown() && dy >= 1f && this.isReadyForPullDown()) {
						this.mLastMotionY = y;
						this.mIsBeingDragged = true;
						if (this.mMode == Mode.BOTH) {
							this.mCurrentMode = Mode.PULL_DOWN_TO_REFRESH;
						}
					} else if (this.mMode.canPullUp() && dy <= -1f && this.isReadyForPullUp()) {
						this.mLastMotionY = y;
						this.mIsBeingDragged = true;
						if (this.mMode == Mode.BOTH) {
							this.mCurrentMode = Mode.PULL_UP_TO_REFRESH;
						}
					}
				}
			}
			break;
		}
		case MotionEvent.ACTION_DOWN: {
			if (this.isReadyForPull()) {
				this.mLastMotionY = this.mInitialMotionY = event.getY();
				this.mLastMotionX = event.getX();
				this.mIsBeingDragged = false;
			}
			break;
		}
		}

		return this.mIsBeingDragged;
	}

	@Override
	public final void onRefreshComplete() {
		if (this.mState != PULL_TO_REFRESH) {
			this.resetHeader();
		}
	}

	@Override
	public final boolean onTouchEvent(MotionEvent event) {

		if (!this.isPullToRefreshEnabled())
			return false;

		// If we're refreshing, and the flag is set. Eat the event
		if (this.mDisableScrollingWhileRefreshing && this.isRefreshing())
			return true;

		if (event.getAction() == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0)
			return false;

		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE: {
			if (this.mIsBeingDragged) {
				this.mLastMotionY = event.getY();
				this.pullEvent();
				return true;
			}
			break;
		}

		case MotionEvent.ACTION_DOWN: {
			if (this.isReadyForPull()) {
				this.mLastMotionY = this.mInitialMotionY = event.getY();
				return true;
			}
			break;
		}

		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP: {
			if (this.mIsBeingDragged) {
				this.mIsBeingDragged = false;

				if (this.mState == RELEASE_TO_REFRESH) {
					if (null != this.mOnRefreshListener) {
						this.setRefreshingInternal(true);
						this.mOnRefreshListener.onRefresh(this);
						return true;

					} else if (null != this.mOnRefreshListener2) {
						this.setRefreshingInternal(true);
						if (this.mCurrentMode == Mode.PULL_DOWN_TO_REFRESH) {
							this.mOnRefreshListener2.onPullDownToRefresh(this);
						} else if (this.mCurrentMode == Mode.PULL_UP_TO_REFRESH) {
							this.mOnRefreshListener2.onPullUpToRefresh(this);
						}
						return true;
					} else {
						// If we don't have a listener, just reset
						this.resetHeader();
						return true;
					}
				}

				this.smoothScrollTo(0);
				return true;
			}
			break;
		}
		}

		return false;
	}

	@Override
	public final void setDisableScrollingWhileRefreshing(boolean disableScrollingWhileRefreshing) {
		this.mDisableScrollingWhileRefreshing = disableScrollingWhileRefreshing;
	}

	@Override
	public final void setFilterTouchEvents(boolean filterEvents) {
		this.mFilterTouchEvents = filterEvents;
	}

	@Override
	public void setLastUpdatedLabel(CharSequence label) {
		if (null != this.mHeaderLayout) {
			this.mHeaderLayout.setSubHeaderText(label);
		}
		if (null != this.mFooterLayout) {
			this.mFooterLayout.setSubHeaderText(label);
		}

		// Refresh Height as it may have changed
		this.refreshLoadingViewsHeight();
	}

	@Override
	public void setLoadingDrawable(Drawable drawable) {
		this.setLoadingDrawable(drawable, Mode.BOTH);
	}

	@Override
	public void setLoadingDrawable(Drawable drawable, Mode mode) {
		if (null != this.mHeaderLayout && mode.canPullDown()) {
			this.mHeaderLayout.setLoadingDrawable(drawable);
		}
		if (null != this.mFooterLayout && mode.canPullUp()) {
			this.mFooterLayout.setLoadingDrawable(drawable);
		}

		// The Loading Height may have changed, so refresh
		this.refreshLoadingViewsHeight();
	}

	@Override
	public void setLongClickable(boolean longClickable) {
		this.getRefreshableView().setLongClickable(longClickable);
	}

	@Override
	public final void setMode(Mode mode) {
		if (mode != this.mMode) {
			if (DEBUG) {
				Log.d(LOG_TAG, "Setting mode to: " + mode);
			}
			this.mMode = mode;
			this.updateUIForMode();
		}
	}

	@Override
	public final void setOnRefreshListener(OnRefreshListener<T> listener) {
		this.mOnRefreshListener = listener;
	}

	@Override
	public final void setOnRefreshListener(OnRefreshListener2<T> listener) {
		this.mOnRefreshListener2 = listener;
	}

	@Override
	public void setPullLabel(String pullLabel) {
		this.setPullLabel(pullLabel, Mode.BOTH);
	}

	@Override
	public void setPullLabel(String pullLabel, Mode mode) {
		if (null != this.mHeaderLayout && mode.canPullDown()) {
			this.mHeaderLayout.setPullLabel(pullLabel);
		}
		if (null != this.mFooterLayout && mode.canPullUp()) {
			this.mFooterLayout.setPullLabel(pullLabel);
		}
	}

	@Override
	public final void setPullToRefreshEnabled(boolean enable) {
		this.setMode(enable ? DEFAULT_MODE : Mode.DISABLED);
	}

	@Override
	public final void setPullToRefreshOverScrollEnabled(boolean enabled) {
		this.mOverScrollEnabled = enabled;
	}

	@Override
	public final void setRefreshing() {
		this.setRefreshing(true);
	}

	@Override
	public final void setRefreshing(boolean doScroll) {
		if (!this.isRefreshing()) {
			this.setRefreshingInternal(doScroll);
			this.mState = MANUAL_REFRESHING;
		}
	}

	@Override
	public void setRefreshingLabel(String refreshingLabel) {
		this.setRefreshingLabel(refreshingLabel, Mode.BOTH);
	}

	@Override
	public void setRefreshingLabel(String refreshingLabel, Mode mode) {
		if (null != this.mHeaderLayout && mode.canPullDown()) {
			this.mHeaderLayout.setRefreshingLabel(refreshingLabel);
		}
		if (null != this.mFooterLayout && mode.canPullUp()) {
			this.mFooterLayout.setRefreshingLabel(refreshingLabel);
		}
	}

	@Override
	public void setReleaseLabel(String releaseLabel) {
		this.setReleaseLabel(releaseLabel, Mode.BOTH);
	}

	@Override
	public void setReleaseLabel(String releaseLabel, Mode mode) {
		if (null != this.mHeaderLayout && mode.canPullDown()) {
			this.mHeaderLayout.setReleaseLabel(releaseLabel);
		}
		if (null != this.mFooterLayout && mode.canPullUp()) {
			this.mFooterLayout.setReleaseLabel(releaseLabel);
		}
	}

	@Override
	public void setScrollAnimationInterpolator(Interpolator interpolator) {
		this.mScrollAnimationInterpolator = interpolator;
	}

	@Override
	public final void setShowViewWhileRefreshing(boolean showView) {
		this.mShowViewWhileRefreshing = showView;
	}

	/**
	 * Used internally for adding view. Need because we override addView to
	 * pass-through to the Refreshable View
	 */
	protected final void addViewInternal(View child, int index, ViewGroup.LayoutParams params) {
		super.addView(child, index, params);
	}

	/**
	 * Used internally for adding view. Need because we override addView to
	 * pass-through to the Refreshable View
	 */
	protected final void addViewInternal(View child, ViewGroup.LayoutParams params) {
		super.addView(child, -1, params);
	}

	protected LoadingLayout createLoadingLayout(Context context, Mode mode, TypedArray attrs) {
		return new LoadingLayout(context, mode, attrs);
	}

	/**
	 * This is implemented by derived classes to return the created View. If you
	 * need to use a custom View (such as a custom ListView), override this
	 * method and return an instance of your custom class.
	 * 
	 * Be sure to set the ID of the view in this method, especially if you're
	 * using a ListActivity or ListFragment.
	 * 
	 * @param context
	 *            Context to create view with
	 * @param attrs
	 *            AttributeSet from wrapped class. Means that anything you
	 *            include in the XML layout declaration will be routed to the
	 *            created View
	 * @return New instance of the Refreshable View
	 */
	protected abstract T createRefreshableView(Context context, AttributeSet attrs);

	protected final LoadingLayout getFooterLayout() {
		return this.mFooterLayout;
	}

	protected final int getHeaderHeight() {
		return this.mHeaderHeight;
	}

	protected final LoadingLayout getHeaderLayout() {
		return this.mHeaderLayout;
	}

	protected FrameLayout getRefreshableViewWrapper() {
		return this.mRefreshableViewWrapper;
	}

	protected final int getState() {
		return this.mState;
	}

	/**
	 * Allows Derivative classes to handle the XML Attrs without creating a
	 * TypedArray themsevles
	 * 
	 * @param a
	 *            - TypedArray of PullToRefresh Attributes
	 */
	protected void handleStyledAttributes(TypedArray a) {
	}

	/**
	 * Implemented by derived class to return whether the View is in a mState
	 * where the user can Pull to Refresh by scrolling down.
	 * 
	 * @return true if the View is currently the correct mState (for example,
	 *         top of a ListView)
	 */
	protected abstract boolean isReadyForPullDown();

	/**
	 * Implemented by derived class to return whether the View is in a mState
	 * where the user can Pull to Refresh by scrolling up.
	 * 
	 * @return true if the View is currently in the correct mState (for example,
	 *         bottom of a ListView)
	 */
	protected abstract boolean isReadyForPullUp();

	/**
	 * Called when the UI needs to be updated to the 'Pull to Refresh' state
	 */
	protected void onPullToRefresh() {
		switch (this.mCurrentMode) {
		case PULL_UP_TO_REFRESH:
			this.mFooterLayout.pullToRefresh();
			break;
		case PULL_DOWN_TO_REFRESH:
			this.mHeaderLayout.pullToRefresh();
			break;
		}
	}

	/**
	 * Called when the UI needs to be updated to the 'Release to Refresh' state
	 */
	protected void onReleaseToRefresh() {
		switch (this.mCurrentMode) {
		case PULL_UP_TO_REFRESH:
			this.mFooterLayout.releaseToRefresh();
			break;
		case PULL_DOWN_TO_REFRESH:
			this.mHeaderLayout.releaseToRefresh();
			break;
		}
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle) {
			Bundle bundle = (Bundle) state;

			this.mMode = Mode.mapIntToMode(bundle.getInt(STATE_MODE, 0));
			this.mCurrentMode = Mode.mapIntToMode(bundle.getInt(STATE_CURRENT_MODE, 0));

			this.mDisableScrollingWhileRefreshing = bundle.getBoolean(STATE_DISABLE_SCROLLING_REFRESHING, true);
			this.mShowViewWhileRefreshing = bundle.getBoolean(STATE_SHOW_REFRESHING_VIEW, true);

			// Let super Restore Itself
			super.onRestoreInstanceState(bundle.getParcelable(STATE_SUPER));

			final int viewState = bundle.getInt(STATE_STATE, PULL_TO_REFRESH);
			if (viewState == REFRESHING) {
				this.setRefreshingInternal(true);
				this.mState = viewState;
			}
			return;
		}

		super.onRestoreInstanceState(state);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putInt(STATE_STATE, this.mState);
		bundle.putInt(STATE_MODE, this.mMode.getIntValue());
		bundle.putInt(STATE_CURRENT_MODE, this.mCurrentMode.getIntValue());
		bundle.putBoolean(STATE_DISABLE_SCROLLING_REFRESHING, this.mDisableScrollingWhileRefreshing);
		bundle.putBoolean(STATE_SHOW_REFRESHING_VIEW, this.mShowViewWhileRefreshing);
		bundle.putParcelable(STATE_SUPER, super.onSaveInstanceState());
		return bundle;
	}

	protected void resetHeader() {
		this.mState = PULL_TO_REFRESH;
		this.mIsBeingDragged = false;

		if (this.mMode.canPullDown()) {
			this.mHeaderLayout.reset();
		}
		if (this.mMode.canPullUp()) {
			this.mFooterLayout.reset();
		}

		this.smoothScrollTo(0);
	}

	protected final void setHeaderScroll(int y) {
		this.scrollTo(0, y);
	}

	protected void setRefreshingInternal(boolean doScroll) {
		this.mState = REFRESHING;

		if (this.mMode.canPullDown()) {
			this.mHeaderLayout.refreshing();
		}
		if (this.mMode.canPullUp()) {
			this.mFooterLayout.refreshing();
		}

		if (doScroll) {
			if (this.mShowViewWhileRefreshing) {
				this.smoothScrollTo(this.mCurrentMode == Mode.PULL_DOWN_TO_REFRESH ? -this.mHeaderHeight : this.mHeaderHeight);
			} else {
				this.smoothScrollTo(0);
			}
		}
	}

	/**
	 * Smooth Scroll to Y position using the default duration of
	 * {@value #SMOOTH_SCROLL_DURATION_MS} ms.
	 * 
	 * @param y
	 *            - Y position to scroll to
	 */
	protected final void smoothScrollTo(int y) {
		this.smoothScrollTo(y, SMOOTH_SCROLL_DURATION_MS);
	}

	/**
	 * Smooth Scroll to Y position using the specific duration
	 * 
	 * @param y
	 *            - Y position to scroll to
	 * @param duration
	 *            - Duration of animation in milliseconds
	 */
	protected final void smoothScrollTo(int y, long duration) {
		if (null != this.mCurrentSmoothScrollRunnable) {
			this.mCurrentSmoothScrollRunnable.stop();
		}

		if (this.getScrollY() != y) {
			if (null == this.mScrollAnimationInterpolator) {
				// Default interpolator is a Decelerate Interpolator
				this.mScrollAnimationInterpolator = new DecelerateInterpolator();
			}
			this.mCurrentSmoothScrollRunnable = new SmoothScrollRunnable(this.getScrollY(), y, duration);
			this.post(this.mCurrentSmoothScrollRunnable);
		}
	}

	/**
	 * Updates the View State when the mode has been set. This does not do any
	 * checking that the mode is different to current state so always updates.
	 */
	protected void updateUIForMode() {
		// Remove Header, and then add Header Loading View again if needed
		if (this == this.mHeaderLayout.getParent()) {
			this.removeView(this.mHeaderLayout);
		}
		if (this.mMode.canPullDown()) {
			this.addViewInternal(this.mHeaderLayout, 0, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		}

		// Remove Footer, and then add Footer Loading View again if needed
		if (this == this.mFooterLayout.getParent()) {
			this.removeView(this.mFooterLayout);
		}
		if (this.mMode.canPullUp()) {
			this.addViewInternal(this.mFooterLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		}

		// Hide Loading Views
		this.refreshLoadingViewsHeight();

		// If we're not using Mode.BOTH, set mCurrentMode to mMode, otherwise
		// set it to pull down
		this.mCurrentMode = (this.mMode != Mode.BOTH) ? this.mMode : Mode.PULL_DOWN_TO_REFRESH;
	}

	private void addRefreshableView(Context context, T refreshableView) {
		this.mRefreshableViewWrapper = new FrameLayout(context);
		this.mRefreshableViewWrapper.addView(refreshableView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		this.addViewInternal(this.mRefreshableViewWrapper, new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
	}

	@SuppressWarnings("deprecation")
	private void init(Context context, AttributeSet attrs) {
		this.setOrientation(LinearLayout.VERTICAL);

		ViewConfiguration config = ViewConfiguration.get(context);
		this.mTouchSlop = config.getScaledTouchSlop();

		// Styleables from XML
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PullToRefresh);

		if (a.hasValue(R.styleable.PullToRefresh_ptrMode)) {
			this.mMode = Mode.mapIntToMode(a.getInteger(R.styleable.PullToRefresh_ptrMode, 0));
		}

		// Refreshable View
		// By passing the attrs, we can add ListView/GridView params via XML
		this.mRefreshableView = this.createRefreshableView(context, attrs);
		this.addRefreshableView(context, this.mRefreshableView);

		// We need to create now layouts now
		this.mHeaderLayout = this.createLoadingLayout(context, Mode.PULL_DOWN_TO_REFRESH, a);
		this.mFooterLayout = this.createLoadingLayout(context, Mode.PULL_UP_TO_REFRESH, a);

		// Styleables from XML
		if (a.hasValue(R.styleable.PullToRefresh_ptrHeaderBackground)) {
			Drawable background = a.getDrawable(R.styleable.PullToRefresh_ptrHeaderBackground);
			if (null != background) {
				this.setBackgroundDrawable(background);
			}
		}
		if (a.hasValue(R.styleable.PullToRefresh_ptrAdapterViewBackground)) {
			Drawable background = a.getDrawable(R.styleable.PullToRefresh_ptrAdapterViewBackground);
			if (null != background) {
				this.mRefreshableView.setBackgroundDrawable(background);
			}
		}
		if (a.hasValue(R.styleable.PullToRefresh_ptrOverScroll)) {
			this.mOverScrollEnabled = a.getBoolean(R.styleable.PullToRefresh_ptrOverScroll, true);
		}

		// Let the derivative classes have a go at handling attributes, then
		// recycle them...
		this.handleStyledAttributes(a);
		a.recycle();

		// Finally update the UI for the modes
		this.updateUIForMode();
	}

	private boolean isReadyForPull() {
		switch (this.mMode) {
		case PULL_DOWN_TO_REFRESH:
			return this.isReadyForPullDown();
		case PULL_UP_TO_REFRESH:
			return this.isReadyForPullUp();
		case BOTH:
			return this.isReadyForPullUp() || this.isReadyForPullDown();
		}
		return false;
	}

	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}

		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	/**
	 * Actions a Pull Event
	 * 
	 * @return true if the Event has been handled, false if there has been no
	 *         change
	 */
	private boolean pullEvent() {

		final int newHeight;
		final int oldHeight = this.getScrollY();

		switch (this.mCurrentMode) {
		case PULL_UP_TO_REFRESH:
			newHeight = Math.round(Math.max(this.mInitialMotionY - this.mLastMotionY, 0) / FRICTION);
			break;
		case PULL_DOWN_TO_REFRESH:
		default:
			newHeight = Math.round(Math.min(this.mInitialMotionY - this.mLastMotionY, 0) / FRICTION);
			break;
		}

		this.setHeaderScroll(newHeight);

		if (newHeight != 0) {

			float scale = Math.abs(newHeight) / (float) this.mHeaderHeight;
			switch (this.mCurrentMode) {
			case PULL_UP_TO_REFRESH:
				this.mFooterLayout.onPullY(scale);
				break;
			case PULL_DOWN_TO_REFRESH:
				this.mHeaderLayout.onPullY(scale);
				break;
			}

			if (this.mState == PULL_TO_REFRESH && this.mHeaderHeight < Math.abs(newHeight)) {
				this.mState = RELEASE_TO_REFRESH;
				this.onReleaseToRefresh();
				return true;

			} else if (this.mState == RELEASE_TO_REFRESH && this.mHeaderHeight >= Math.abs(newHeight)) {
				this.mState = PULL_TO_REFRESH;
				this.onPullToRefresh();
				return true;
			}
		}

		return oldHeight != newHeight;
	}

	/**
	 * Re-measure the Loading Views height, and adjust internal padding as
	 * necessary
	 */
	private void refreshLoadingViewsHeight() {
		if (this.mMode.canPullDown()) {
			this.measureView(this.mHeaderLayout);
			this.mHeaderHeight = this.mHeaderLayout.getMeasuredHeight();
		} else if (this.mMode.canPullUp()) {
			this.measureView(this.mFooterLayout);
			this.mHeaderHeight = this.mFooterLayout.getMeasuredHeight();
		} else {
			this.mHeaderHeight = 0;
		}

		// Hide Loading Views
		switch (this.mMode) {
		case DISABLED:
			this.setPadding(0, 0, 0, 0);
		case BOTH:
			this.setPadding(0, -this.mHeaderHeight, 0, -this.mHeaderHeight);
			break;
		case PULL_UP_TO_REFRESH:
			this.setPadding(0, 0, 0, -this.mHeaderHeight);
			break;
		case PULL_DOWN_TO_REFRESH:
		default:
			this.setPadding(0, -this.mHeaderHeight, 0, 0);
			break;
		}
	}

	public static enum Mode {
		/**
		 * Disable all Pull-to-Refresh gesture handling
		 */
		DISABLED(0x0),

		/**
		 * Only allow the user to Pull Down from the top to refresh, this is the
		 * default.
		 */
		PULL_DOWN_TO_REFRESH(0x1),

		/**
		 * Only allow the user to Pull Up from the bottom to refresh.
		 */
		PULL_UP_TO_REFRESH(0x2),

		/**
		 * Allow the user to both Pull Down from the top, and Pull Up from the
		 * bottom to refresh.
		 */
		BOTH(0x3);

		/**
		 * Maps an int to a specific mode. This is needed when saving state, or
		 * inflating the view from XML where the mode is given through a attr
		 * int.
		 * 
		 * @param modeInt
		 *            - int to map a Mode to
		 * @return Mode that modeInt maps to, or PULL_DOWN_TO_REFRESH by
		 *         default.
		 */
		public static Mode mapIntToMode(int modeInt) {
			switch (modeInt) {
			case 0x0:
				return DISABLED;
			case 0x1:
			default:
				return PULL_DOWN_TO_REFRESH;
			case 0x2:
				return PULL_UP_TO_REFRESH;
			case 0x3:
				return BOTH;
			}
		}

		private int mIntValue;

		// The modeInt values need to match those from attrs.xml
		Mode(int modeInt) {
			this.mIntValue = modeInt;
		}

		/**
		 * @return true if this mode permits Pulling Down from the top
		 */
		boolean canPullDown() {
			return this == PULL_DOWN_TO_REFRESH || this == BOTH;
		}

		/**
		 * @return true if this mode permits Pulling Up from the bottom
		 */
		boolean canPullUp() {
			return this == PULL_UP_TO_REFRESH || this == BOTH;
		}

		int getIntValue() {
			return this.mIntValue;
		}

	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	/**
	 * Simple Listener that allows you to be notified when the user has scrolled
	 * to the end of the AdapterView. See (
	 * {@link PullToRefreshAdapterViewBase#setOnLastItemVisibleListener}.
	 * 
	 * @author Chris Banes
	 * 
	 */
	public static interface OnLastItemVisibleListener {

		/**
		 * Called when the user has scrolled to the end of the list
		 */
		public void onLastItemVisible();

	}

	/**
	 * Simple Listener to listen for any callbacks to Refresh.
	 * 
	 * @author Chris Banes
	 */
	public static interface OnRefreshListener<V extends View> {

		/**
		 * onRefresh will be called for both Pull Down from top, and Pull Up
		 * from Bottom
		 */
		public void onRefresh(final PullToRefreshBase<V> refreshView);

	}

	/**
	 * An advanced version of the Listener to listen for callbacks to Refresh.
	 * This listener is different as it allows you to differentiate between Pull
	 * Ups, and Pull Downs.
	 * 
	 * @author Chris Banes
	 */
	public static interface OnRefreshListener2<V extends View> {

		/**
		 * onPullDownToRefresh will be called only when the user has Pulled Down
		 * from the top, and released.
		 */
		public void onPullDownToRefresh(final PullToRefreshBase<V> refreshView);

		/**
		 * onPullUpToRefresh will be called only when the user has Pulled Up
		 * from the bottom, and released.
		 */
		public void onPullUpToRefresh(final PullToRefreshBase<V> refreshView);

	}

	final class SmoothScrollRunnable implements Runnable {

		static final int ANIMATION_DELAY = 10;

		private final Interpolator mInterpolator;
		private final int mScrollToY;
		private final int mScrollFromY;
		private final long mDuration;

		private boolean mContinueRunning = true;
		private long mStartTime = -1;
		private int mCurrentY = -1;

		public SmoothScrollRunnable(int fromY, int toY, long duration) {
			this.mScrollFromY = fromY;
			this.mScrollToY = toY;
			this.mInterpolator = PullToRefreshBase.this.mScrollAnimationInterpolator;
			this.mDuration = duration;
		}

		@Override
		public void run() {

			/**
			 * Only set mStartTime if this is the first time we're starting,
			 * else actually calculate the Y delta
			 */
			if (this.mStartTime == -1) {
				this.mStartTime = System.currentTimeMillis();
			} else {

				/**
				 * We do do all calculations in long to reduce software float
				 * calculations. We use 1000 as it gives us good accuracy and
				 * small rounding errors
				 */
				long normalizedTime = (1000 * (System.currentTimeMillis() - this.mStartTime)) / this.mDuration;
				normalizedTime = Math.max(Math.min(normalizedTime, 1000), 0);

				final int deltaY = Math.round((this.mScrollFromY - this.mScrollToY) * this.mInterpolator.getInterpolation(normalizedTime / 1000f));
				this.mCurrentY = this.mScrollFromY - deltaY;
				PullToRefreshBase.this.setHeaderScroll(this.mCurrentY);
			}

			// If we're not at the target Y, keep going...
			if (this.mContinueRunning && this.mScrollToY != this.mCurrentY) {
				if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
					SDK16.postOnAnimation(PullToRefreshBase.this, this);
				} else {
					PullToRefreshBase.this.postDelayed(this, ANIMATION_DELAY);
				}
			}
		}

		public void stop() {
			this.mContinueRunning = false;
			PullToRefreshBase.this.removeCallbacks(this);
		}
	}

}
