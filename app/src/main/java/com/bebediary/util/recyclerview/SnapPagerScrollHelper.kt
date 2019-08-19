package com.bebediary.util.recyclerview

import androidx.annotation.NonNull
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView


class SnapPagerScrollListener(
        private val snapHelper: PagerSnapHelper,
        private val type: Int,
        private val notifyOnInit: Boolean,
        private val listener: OnChangeListener
) : RecyclerView.OnScrollListener() {

    private var snapPosition: Int = 0

    companion object {
        const val ON_SCROLL = 0
        const val ON_SETTLED = 1
    }

    interface OnChangeListener {
        fun onSnapped(position: Int)
    }

    init {
        this.snapPosition = RecyclerView.NO_POSITION
    }

    // Methods
    override fun onScrolled(@NonNull recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (type == ON_SCROLL || !hasItemPosition()) {
            notifyListenerIfNeeded(getSnapPosition(recyclerView))
        }
    }

    override fun onScrollStateChanged(@NonNull recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (type == ON_SETTLED && newState == RecyclerView.SCROLL_STATE_IDLE) {
            notifyListenerIfNeeded(getSnapPosition(recyclerView))
        }
    }

    private fun getSnapPosition(recyclerView: RecyclerView): Int {
        val layoutManager = recyclerView.layoutManager ?: return RecyclerView.NO_POSITION

        val snapView = snapHelper.findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION

        return layoutManager.getPosition(snapView)
    }

    private fun notifyListenerIfNeeded(newSnapPosition: Int) {
        if (snapPosition != newSnapPosition) {
            if (notifyOnInit && !hasItemPosition()) {
                listener.onSnapped(newSnapPosition)
            } else if (hasItemPosition()) {
                listener.onSnapped(newSnapPosition)
            }

            snapPosition = newSnapPosition
        }
    }

    private fun hasItemPosition(): Boolean {
        return snapPosition != RecyclerView.NO_POSITION
    }
}