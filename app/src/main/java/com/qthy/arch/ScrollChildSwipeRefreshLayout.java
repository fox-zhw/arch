package com.qthy.arch;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * @author zhaohw
 * @date 2020/12/3
 */
public class ScrollChildSwipeRefreshLayout extends SwipeRefreshLayout {
	View mScrollUpChild;
	public ScrollChildSwipeRefreshLayout(@NonNull Context context) {
		super(context);
	}
	
	public ScrollChildSwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public boolean canChildScrollUp() {
		if (mScrollUpChild != null) {
			return mScrollUpChild.canScrollVertically(-1);
		}
		return super.canChildScrollUp();
	}
	
	public void setScrollUpChild(View view) {
		mScrollUpChild = view;
	}
}
