package me.blog.korn123.easydiary.views

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseIntArray
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks
import com.github.ksoichiro.android.observablescrollview.ScrollState
import com.github.ksoichiro.android.observablescrollview.Scrollable
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView

class FastScrollObservableRecyclerView : FastScrollRecyclerView, Scrollable {

    constructor(context: Context) : super(context) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    // Fields that should be saved onSaveInstanceState
    private var mPrevFirstVisiblePosition = 0
    private var mPrevFirstVisibleChildHeight = -1
    private var mPrevScrolledChildrenHeight = 0
    private var mPrevScrollY = 0
    private var mScrollY = 0
    private var mChildrenHeights: SparseIntArray? = null

    // Fields that don't need to be saved onSaveInstanceState
    private var mCallbacks: ObservableScrollViewCallbacks? = null
    private var mScrollState: ScrollState? = null
    private var mFirstScroll = false
    private var mDragging = false
    private var mIntercepted = false
    private var mPrevMoveEvent: MotionEvent? = null
    private var mTouchInterceptionViewGroup: ViewGroup? = null



    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        mPrevFirstVisiblePosition = ss.prevFirstVisiblePosition
        mPrevFirstVisibleChildHeight = ss.prevFirstVisibleChildHeight
        mPrevScrolledChildrenHeight = ss.prevScrolledChildrenHeight
        mPrevScrollY = ss.prevScrollY
        mScrollY = ss.scrollY
        mChildrenHeights = ss.childrenHeights
        super.onRestoreInstanceState(ss.superState)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.prevFirstVisiblePosition = mPrevFirstVisiblePosition
        ss.prevFirstVisibleChildHeight = mPrevFirstVisibleChildHeight
        ss.prevScrolledChildrenHeight = mPrevScrolledChildrenHeight
        ss.prevScrollY = mPrevScrollY
        ss.scrollY = mScrollY
        ss.childrenHeights = mChildrenHeights
        return ss
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (mCallbacks != null) {
            if (childCount > 0) {
                val firstVisiblePosition = getChildPosition(getChildAt(0))
                val lastVisiblePosition = getChildPosition(getChildAt(childCount - 1))
                var i = firstVisiblePosition
                var j = 0
                while (i <= lastVisiblePosition) {
                    if (mChildrenHeights!!.indexOfKey(i) < 0 || getChildAt(j).height !== mChildrenHeights!![i]) {
                        mChildrenHeights!!.put(i, getChildAt(j).height)
                    }
                    i++
                    j++
                }
                val firstVisibleChild = getChildAt(0)
                if (firstVisibleChild != null) {
                    if (mPrevFirstVisiblePosition < firstVisiblePosition) {
                        // scroll down
                        var skippedChildrenHeight = 0
                        if (firstVisiblePosition - mPrevFirstVisiblePosition != 1) {
                            for (i in firstVisiblePosition - 1 downTo mPrevFirstVisiblePosition + 1) {
                                skippedChildrenHeight += if (0 < mChildrenHeights!!.indexOfKey(i)) {
                                    mChildrenHeights!![i]
                                } else {
                                    // Approximate each item's height to the first visible child.
                                    // It may be incorrect, but without this, scrollY will be broken
                                    // when scrolling from the bottom.
                                    firstVisibleChild.height
                                }
                            }
                        }
                        mPrevScrolledChildrenHeight += mPrevFirstVisibleChildHeight + skippedChildrenHeight
                        mPrevFirstVisibleChildHeight = firstVisibleChild.height
                    } else if (firstVisiblePosition < mPrevFirstVisiblePosition) {
                        // scroll up
                        var skippedChildrenHeight = 0
                        if (mPrevFirstVisiblePosition - firstVisiblePosition != 1) {
                            for (i in mPrevFirstVisiblePosition - 1 downTo firstVisiblePosition + 1) {
                                skippedChildrenHeight += if (0 < mChildrenHeights!!.indexOfKey(i)) {
                                    mChildrenHeights!![i]
                                } else {
                                    // Approximate each item's height to the first visible child.
                                    // It may be incorrect, but without this, scrollY will be broken
                                    // when scrolling from the bottom.
                                    firstVisibleChild.height
                                }
                            }
                        }
                        mPrevScrolledChildrenHeight -= firstVisibleChild.height + skippedChildrenHeight
                        mPrevFirstVisibleChildHeight = firstVisibleChild.height
                    } else if (firstVisiblePosition == 0) {
                        mPrevFirstVisibleChildHeight = firstVisibleChild.height
                        mPrevScrolledChildrenHeight = 0
                    }
                    if (mPrevFirstVisibleChildHeight < 0) {
                        mPrevFirstVisibleChildHeight = 0
                    }
                    mScrollY = mPrevScrolledChildrenHeight - firstVisibleChild.top
                    mPrevFirstVisiblePosition = firstVisiblePosition
                    mCallbacks!!.onScrollChanged(mScrollY, mFirstScroll, mDragging)
                    if (mFirstScroll) {
                        mFirstScroll = false
                    }
                    mScrollState = if (mPrevScrollY < mScrollY) {
                        //down
                        ScrollState.UP
                    } else if (mScrollY < mPrevScrollY) {
                        //up
                        ScrollState.DOWN
                    } else {
                        ScrollState.STOP
                    }
                    mPrevScrollY = mScrollY
                }
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (mCallbacks != null) {
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    run {
                        mDragging = true
                        mFirstScroll = mDragging
                    }
                    mCallbacks!!.onDownMotionEvent()
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (mCallbacks != null) {
            when (ev.actionMasked) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    mIntercepted = false
                    mDragging = false
                    mCallbacks!!.onUpOrCancelMotionEvent(mScrollState)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (mPrevMoveEvent == null) {
                        mPrevMoveEvent = ev
                    }
                    val diffY = ev.y - mPrevMoveEvent!!.y
                    mPrevMoveEvent = MotionEvent.obtainNoHistory(ev)
                    if (currentScrollY - diffY <= 0) {
                        // Can't scroll anymore.
                        if (mIntercepted) {
                            // Already dispatched ACTION_DOWN event to parents, so stop here.
                            return false
                        }

                        // Apps can set the interception target other than the direct parent.
                        val parent: ViewGroup
                        parent = mTouchInterceptionViewGroup ?: getParent() as ViewGroup

                        // Get offset to parents. If the parent is not the direct parent,
                        // we should aggregate offsets from all of the parents.
                        var offsetX = 0f
                        var offsetY = 0f
                        var v: View = this
                        while (v != null && v !== parent) {
                            offsetX += (v.left - v.scrollX).toFloat()
                            offsetY += (v.top - v.scrollY).toFloat()
                            v = v.parent as View
                        }
                        val event = MotionEvent.obtainNoHistory(ev)
                        event.offsetLocation(offsetX, offsetY)
                        if (parent.onInterceptTouchEvent(event)) {
                            mIntercepted = true

                            // If the parent wants to intercept ACTION_MOVE events,
                            // we pass ACTION_DOWN event to the parent
                            // as if these touch events just have began now.
                            event.action = MotionEvent.ACTION_DOWN

                            // Return this onTouchEvent() first and set ACTION_DOWN event for parent
                            // to the queue, to keep events sequence.
                            post { parent.dispatchTouchEvent(event) }
                            return false
                        }
                        // Even when this can't be scrolled anymore,
                        // simply returning false here may cause subView's click,
                        // so delegate it to super.
                        return super.onTouchEvent(ev)
                    }
                }
            }
        }
        return super.onTouchEvent(ev)
    }

    override fun setScrollViewCallbacks(listener: ObservableScrollViewCallbacks?) {
        mCallbacks = listener
    }

    override fun setTouchInterceptionViewGroup(viewGroup: ViewGroup?) {
        mTouchInterceptionViewGroup = viewGroup
    }

    override fun scrollVerticallyTo(y: Int) {
        val firstVisibleChild = getChildAt(0)
        if (firstVisibleChild != null) {
            val baseHeight = firstVisibleChild.height
            val position = y / baseHeight
            scrollVerticallyToPosition(position)
        }
    }

    /**
     *
     * Same as [.scrollToPosition] but it scrolls to the position not only make
     * the position visible.
     *
     * It depends on `LayoutManager` how [.scrollToPosition] works,
     * and currently we know that [LinearLayoutManager.scrollToPosition] just
     * make the position visible.
     *
     * In LinearLayoutManager, scrollToPositionWithOffset() is provided for scrolling to the position.
     * This method checks which LayoutManager is set,
     * and handles which method should be called for scrolling.
     *
     * Other know classes (StaggeredGridLayoutManager and GridLayoutManager) are not tested.
     *
     * @param position position to scroll
     */
    fun scrollVerticallyToPosition(position: Int) {
        val lm = layoutManager
        if (lm != null && lm is LinearLayoutManager) {
            (lm as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
        } else {
            scrollToPosition(position)
        }
    }

    override fun getCurrentScrollY(): Int {
        return mScrollY
    }

    private fun init() {
        mChildrenHeights = SparseIntArray()
    }

    /**
     * This saved state class is a Parcelable and should not extend
     * [android.view.View.BaseSavedState] nor [android.view.AbsSavedState]
     * because its super class AbsSavedState's constructor
     * [android.view.AbsSavedState.AbsSavedState] currently passes null
     * as a class loader to read its superstate from Parcelable.
     * This causes [android.os.BadParcelableException] when restoring saved states.
     *
     *
     * The super class "RecyclerView" is a part of the support library,
     * and restoring its saved state requires the class loader that loaded the RecyclerView.
     * It seems that the class loader is not required when restoring from RecyclerView itself,
     * but it is required when restoring from RecyclerView's subclasses.
     */
    internal class SavedState : Parcelable {
        var prevFirstVisiblePosition = 0
        var prevFirstVisibleChildHeight = -1
        var prevScrolledChildrenHeight = 0
        var prevScrollY = 0
        var scrollY = 0
        var childrenHeights: SparseIntArray? = null

        // This keeps the parent(RecyclerView)'s state
        var superState: Parcelable?

        /**
         * Called by EMPTY_STATE instantiation.
         */
        private constructor() {
            superState = null
        }

        /**
         * Called by onSaveInstanceState.
         */
        constructor(superState: Parcelable?) {
            this.superState = if (superState !== EMPTY_STATE) superState else null
        }

        /**
         * Called by CREATOR.
         */
        private constructor(`in`: Parcel) {
            // Parcel 'in' has its parent(RecyclerView)'s saved state.
            // To restore it, class loader that loaded RecyclerView is required.
            val superState = `in`.readParcelable<Parcelable>(RecyclerView::class.java.getClassLoader())
            this.superState = superState ?: EMPTY_STATE
            prevFirstVisiblePosition = `in`.readInt()
            prevFirstVisibleChildHeight = `in`.readInt()
            prevScrolledChildrenHeight = `in`.readInt()
            prevScrollY = `in`.readInt()
            scrollY = `in`.readInt()
            childrenHeights = SparseIntArray()
            val numOfChildren = `in`.readInt()
            if (0 < numOfChildren) {
                for (i in 0 until numOfChildren) {
                    val key = `in`.readInt()
                    val value = `in`.readInt()
                    childrenHeights!!.put(key, value)
                }
            }
        }

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            out.writeParcelable(superState, flags)
            out.writeInt(prevFirstVisiblePosition)
            out.writeInt(prevFirstVisibleChildHeight)
            out.writeInt(prevScrolledChildrenHeight)
            out.writeInt(prevScrollY)
            out.writeInt(scrollY)
            val numOfChildren = if (childrenHeights == null) 0 else childrenHeights!!.size()
            out.writeInt(numOfChildren)
            if (0 < numOfChildren) {
                for (i in 0 until numOfChildren) {
                    out.writeInt(childrenHeights!!.keyAt(i))
                    out.writeInt(childrenHeights!!.valueAt(i))
                }
            }
        }

        companion object {
            val EMPTY_STATE: SavedState = SavedState()
            @SuppressLint("ParcelCreator")
            val CREATOR: Parcelable.Creator<SavedState?> = object : Parcelable.Creator<SavedState?> {
                override fun createFromParcel(`in`: Parcel): SavedState? {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}