package com.fenchtose.grabimagedemo.views.widgets;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Jay Rambhia on 25/06/15.
 */
public class FABScrollBehavior extends FloatingActionButton.Behavior {

    private static final android.view.animation.Interpolator INTERPOLATOR =
            new FastOutSlowInInterpolator();
    private boolean mIsAnimatingOut = false;

    public FABScrollBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child,
                                    View directChild, View target, int nestedScrollAxis) {
        return nestedScrollAxis == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directChild, target, nestedScrollAxis);
    }


    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child,
                               View target, int dxConsumed, int dyConsumed,
                               int dxUnconsumed, int dyUnconsumed) {
        if (dyConsumed > 0 && !this.mIsAnimatingOut && child.getVisibility() == View.VISIBLE) {
            animateOut(child);
        } else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE) {
            animateIn(child);
        }
    }

    private void animateOut(final FloatingActionButton fab) {
        ViewCompat.animate(fab).scaleX(0f).scaleY(0f).alpha(0f)
                .setInterpolator(INTERPOLATOR).withLayer()
                .setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {
                        mIsAnimatingOut = true;
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        mIsAnimatingOut = false;
                        fab.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(View view) {
                        mIsAnimatingOut = false;
                    }
                }).start();
    }

    private void animateIn(FloatingActionButton fab) {
        fab.setVisibility(View.VISIBLE);
        ViewCompat.animate(fab).scaleX(1f).scaleY(1f).alpha(1f)
                .setInterpolator(INTERPOLATOR).withLayer().setListener(null)
                .start();
    }
}
