package com.codebrew.tagstrade.util

import android.content.Context
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

class LockableViewPager : ViewPager {
    private var swipeable: Boolean = false

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.swipeable = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean =
            this.swipeable && super.onTouchEvent(event)

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean =
            this.swipeable && super.onInterceptTouchEvent(event)

    fun setSwipeable(swipeable: Boolean) {
        this.swipeable = swipeable
    }
}

