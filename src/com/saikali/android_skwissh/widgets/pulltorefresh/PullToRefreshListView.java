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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.saikali.android_skwissh.R;
import com.saikali.android_skwissh.widgets.pulltorefresh.internal.EmptyViewMethodAccessor;
import com.saikali.android_skwissh.widgets.pulltorefresh.internal.LoadingLayout;

public class PullToRefreshListView extends PullToRefreshAdapterViewBase<ListView> {

	private LoadingLayout mHeaderLoadingView;
	private LoadingLayout mFooterLoadingView;

	private FrameLayout mLvFooterLoadingFrame;

	public PullToRefreshListView(Context context) {
		super(context);
		this.setDisableScrollingWhileRefreshing(false);
	}

	public PullToRefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setDisableScrollingWhileRefreshing(false);
	}

	public PullToRefreshListView(Context context, Mode mode) {
		super(context, mode);
		this.setDisableScrollingWhileRefreshing(false);
	}

	@Override
	public ContextMenuInfo getContextMenuInfo() {
		return ((InternalListView) this.getRefreshableView()).getContextMenuInfo();
	}

	@Override
	public void setPullLabel(String pullLabel, Mode mode) {
		super.setPullLabel(pullLabel, mode);

		if (null != this.mHeaderLoadingView && mode.canPullDown()) {
			this.mHeaderLoadingView.setPullLabel(pullLabel);
		}
		if (null != this.mFooterLoadingView && mode.canPullUp()) {
			this.mFooterLoadingView.setPullLabel(pullLabel);
		}
	}

	@Override
	public void setRefreshingLabel(String refreshingLabel, Mode mode) {
		super.setRefreshingLabel(refreshingLabel, mode);

		if (null != this.mHeaderLoadingView && mode.canPullDown()) {
			this.mHeaderLoadingView.setRefreshingLabel(refreshingLabel);
		}
		if (null != this.mFooterLoadingView && mode.canPullUp()) {
			this.mFooterLoadingView.setRefreshingLabel(refreshingLabel);
		}
	}

	@Override
	public void setReleaseLabel(String releaseLabel, Mode mode) {
		super.setReleaseLabel(releaseLabel, mode);

		if (null != this.mHeaderLoadingView && mode.canPullDown()) {
			this.mHeaderLoadingView.setReleaseLabel(releaseLabel);
		}
		if (null != this.mFooterLoadingView && mode.canPullUp()) {
			this.mFooterLoadingView.setReleaseLabel(releaseLabel);
		}
	}

	protected ListView createListView(Context context, AttributeSet attrs) {
		final ListView lv;
		if (VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
			lv = new InternalListViewSDK9(context, attrs);
		} else {
			lv = new InternalListView(context, attrs);
		}
		return lv;
	}

	@Override
	protected final ListView createRefreshableView(Context context, AttributeSet attrs) {
		ListView lv = this.createListView(context, attrs);

		// Get Styles from attrs
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PullToRefresh);

		// Create Loading Views ready for use later
		FrameLayout frame = new FrameLayout(context);
		this.mHeaderLoadingView = this.createLoadingLayout(context, Mode.PULL_DOWN_TO_REFRESH, a);
		frame.addView(this.mHeaderLoadingView, android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		this.mHeaderLoadingView.setVisibility(View.GONE);
		lv.addHeaderView(frame, null, false);

		this.mLvFooterLoadingFrame = new FrameLayout(context);
		this.mFooterLoadingView = this.createLoadingLayout(context, Mode.PULL_UP_TO_REFRESH, a);
		this.mLvFooterLoadingFrame.addView(this.mFooterLoadingView, android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		this.mFooterLoadingView.setVisibility(View.GONE);

		a.recycle();

		// Set it to this so it can be used in ListActivity/ListFragment
		lv.setId(android.R.id.list);
		return lv;
	}

	@Override
	protected void resetHeader() {

		// If we're not showing the Refreshing view, or the list is empty, then
		// the header/footer views won't show so we use the
		// normal method
		ListAdapter adapter = this.mRefreshableView.getAdapter();
		if (!this.getShowViewWhileRefreshing() || null == adapter || adapter.isEmpty()) {
			super.resetHeader();
			return;
		}

		LoadingLayout originalLoadingLayout;
		LoadingLayout listViewLoadingLayout;

		int scrollToHeight = this.getHeaderHeight();
		int selection;
		boolean scroll;

		switch (this.getCurrentMode()) {
		case PULL_UP_TO_REFRESH:
			originalLoadingLayout = this.getFooterLayout();
			listViewLoadingLayout = this.mFooterLoadingView;
			selection = this.mRefreshableView.getCount() - 1;
			scroll = this.mRefreshableView.getLastVisiblePosition() == selection;
			break;
		case PULL_DOWN_TO_REFRESH:
		default:
			originalLoadingLayout = this.getHeaderLayout();
			listViewLoadingLayout = this.mHeaderLoadingView;
			scrollToHeight *= -1;
			selection = 0;
			scroll = this.mRefreshableView.getFirstVisiblePosition() == selection;
			break;
		}

		// Set our Original View to Visible
		originalLoadingLayout.setVisibility(View.VISIBLE);

		/**
		 * Scroll so the View is at the same Y as the ListView header/footer,
		 * but only scroll if we've pulled to refresh and it's positioned
		 * correctly
		 */
		if (scroll && this.getState() != MANUAL_REFRESHING) {
			this.mRefreshableView.setSelection(selection);
			this.setHeaderScroll(scrollToHeight);
		}

		// Hide the ListView Header/Footer
		listViewLoadingLayout.setVisibility(View.GONE);

		super.resetHeader();
	}

	@Override
	protected void setRefreshingInternal(boolean doScroll) {

		// If we're not showing the Refreshing view, or the list is empty, then
		// the header/footer views won't show so we use the
		// normal method
		ListAdapter adapter = this.mRefreshableView.getAdapter();
		if (!this.getShowViewWhileRefreshing() || null == adapter || adapter.isEmpty()) {
			super.setRefreshingInternal(doScroll);
			return;
		}

		super.setRefreshingInternal(false);

		final LoadingLayout originalLoadingLayout, listViewLoadingLayout;
		final int selection, scrollToY;

		switch (this.getCurrentMode()) {
		case PULL_UP_TO_REFRESH:
			originalLoadingLayout = this.getFooterLayout();
			listViewLoadingLayout = this.mFooterLoadingView;
			selection = this.mRefreshableView.getCount() - 1;
			scrollToY = this.getScrollY() - this.getHeaderHeight();
			break;
		case PULL_DOWN_TO_REFRESH:
		default:
			originalLoadingLayout = this.getHeaderLayout();
			listViewLoadingLayout = this.mHeaderLoadingView;
			selection = 0;
			scrollToY = this.getScrollY() + this.getHeaderHeight();
			break;
		}

		if (doScroll) {
			// We scroll slightly so that the ListView's header/footer is at the
			// same Y position as our normal header/footer
			this.setHeaderScroll(scrollToY);
		}

		// Hide our original Loading View
		originalLoadingLayout.setVisibility(View.INVISIBLE);

		// Show the ListView Loading View and set it to refresh
		listViewLoadingLayout.setVisibility(View.VISIBLE);
		listViewLoadingLayout.refreshing();

		if (doScroll) {
			// Make sure the ListView is scrolled to show the loading
			// header/footer
			this.mRefreshableView.setSelection(selection);

			// Smooth scroll as normal
			this.smoothScrollTo(0);
		}
	}

	protected class InternalListView extends ListView implements EmptyViewMethodAccessor {

		private boolean mAddedLvFooter = false;

		public InternalListView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		public void draw(Canvas canvas) {
			/**
			 * This is a bit hacky, but ListView has got a bug in it when using
			 * Header/Footer Views and the list is empty. This masks the issue
			 * so that it doesn't cause an FC. See Issue #66.
			 */
			try {
				super.draw(canvas);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public ContextMenuInfo getContextMenuInfo() {
			return super.getContextMenuInfo();
		}

		@Override
		public void setAdapter(ListAdapter adapter) {
			// Add the Footer View at the last possible moment
			if (!this.mAddedLvFooter) {
				this.addFooterView(PullToRefreshListView.this.mLvFooterLoadingFrame, null, false);
				this.mAddedLvFooter = true;
			}

			super.setAdapter(adapter);
		}

		@Override
		public void setEmptyView(View emptyView) {
			PullToRefreshListView.this.setEmptyView(emptyView);
		}

		@Override
		public void setEmptyViewInternal(View emptyView) {
			super.setEmptyView(emptyView);
		}

	}

	@TargetApi(9)
	final class InternalListViewSDK9 extends InternalListView {

		public InternalListViewSDK9(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {

			final boolean returnValue = super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);

			// Does all of the hard work...
			OverscrollHelper.overScrollBy(PullToRefreshListView.this, deltaY, scrollY, isTouchEvent);

			return returnValue;
		}
	}

}
