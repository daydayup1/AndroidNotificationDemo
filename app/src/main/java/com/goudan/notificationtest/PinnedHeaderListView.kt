package com.goudan.notificationtest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.*
import kotlin.collections.ArrayList

/**
 * 顶部带固定条目的布局，主体为listView，顶部是个固定view
 */
class PinnedHeaderListView : FrameLayout {

    private lateinit var mListView : ListView
    private lateinit var mAdapter: PinnedHeaderListViewAdapter
    private lateinit var mHeaderView : View

    // 顶部固定view
    private var mHeaderViewVisible = false
    // 顶部固定view的宽度
    private var mHeaderViewWidth: Int = 0
    // 顶部固定view的高度
    private var mHeaderViewHeight: Int = 0
    // listView条目之间的间隔线高度
    private val mDividerHeight = 3

    constructor(context: Context) : super(context) {
        init(context)

    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        initView(context)
        initData()
    }

    /**
     * 初始化布局元素
     */
    private fun initView(context: Context) {
        mListView = ListView(context)
        mAdapter = PinnedHeaderListViewAdapter(context)
        mListView.adapter = mAdapter
        // 这里重新requestLayout是否有必要
        setPinnedHeaderView(mAdapter.getHeaderView())

        mListView.apply {
            divider = ColorDrawable(getContext().resources.getColor(R.color.list_divider_color))
            dividerHeight = mDividerHeight
            setBackgroundColor(resources.getColor(R.color.list_view_bg))
            setOnItemClickListener{
                    parent, _, position, _ ->
                // 这里需要转型成PinnedHeaderListViewAdapter吗？会自动调用子类的getItem吗？
                val data : ItemInfo = (parent.adapter as PinnedHeaderListViewAdapter).getItem(position) as ItemInfo
                when(data.title) {
                    Util.BASE_NOTIFICATION ->
                        Util.createBaseNotification(context)
                    Util.NORMAL_CONTENT_VIEW_NOTIFICATION ->
                        Util.createNormalRemoteViewNotification(context)
                    Util.BIG_CONTENT_VIEW_NOTIFICATION ->
                        Util.createBigRemoteViewNotification(context)
                    Util.ONLY_ONE_PICTURE_REMOTE_VIEW ->
                        Util.createOnlyOnePictureRemoteView(context)
                    Util.BIG_PICTURE_STYLE ->
                        Util.createBigPictureStyle(context)
                    Util.BIG_TEXT_STYLE ->
                        Util.createBigTextStyle(context)
                    Util.INBOX_STYLE ->
                        Util.createInboxStyle(context)
                    Util.MEDIA_STYLE ->
                        Util.createMediaStyle(context)
                    Util.HEAD_UP_NOTIFICATION ->
                        Util.createHeadUpNotification(context)
                    Util.DECORATED_CUSTOM_VIEW_STYLE ->
                        Util.createDecoratedCustomViewStyle(context)
                    Util.DECORATE_MEDIA_CUSTOM_VIEW_STYLE ->
                        Util.createDecoratedMediaCustomViewStyle(context)
                    Util.MESSAGING_STYLE ->
                        Util.createMessagingStyle(context)
                    Util.DIRECTLY_REPLY_NOTIFICATION ->
                        Util.createReplyStyleNotification(context)
                    Util.INCLUDE_PERSON_AND_IMAGE_NOTIFICATION ->
                        Util.createIncludePersonAndImageNotification(context)
                }
            }
            setOnScrollListener(object : AbsListView.OnScrollListener{
                override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int,
                                      totalItemCount: Int) {
                    configureHeaderView(firstVisibleItem)
                }

                override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {

                }
            })
        }
        addView(mListView)
        setBackgroundColor(Color.WHITE)
    }

    private fun initData() {
        val itemList = ArrayList<ItemInfo>()
        val infoMap = Util.getAllInfo()
        val keyList = ArrayList<String>(infoMap.keys)
        keyList.sort()

        for (k in keyList.indices) {
            val key = keyList[k]
            val value = infoMap[key]

            var info = ItemInfo()
            //            info.content = "";
            info.isSection = true
            info.sectionStr = key
            itemList.add(info)

            var apiName: String
            if (value != null) {
                for (i in value.indices) {
                    apiName = value[i]
                    info = ItemInfo()
                    info.title = apiName
                    info.isSection = false
                    info.sectionStr = key
                    itemList.add(info)
                }
            }
        }

        mAdapter.setData(itemList)
    }

    fun configureHeaderView(position: Int) {
        when (mAdapter.getPinnedHeaderState(position)) {
            PinnedHeaderListViewAdapter.PINNED_HEADER_GONE ->
                mHeaderViewVisible = false
            PinnedHeaderListViewAdapter.PINNED_HEADER_VISIBLE -> {
                mAdapter.configurePinnedHeader(mHeaderView, position)
                if (mHeaderView.top != 0) {
                    mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight)
                }
                mHeaderViewVisible = true
            }
            PinnedHeaderListViewAdapter.PINNED_HEADER_PUSHING_UP -> {
                // 当页面上第二个条目是标题类消息时，需要在列表滚动时，不断的判断是否需要移动头部view
                // listView第一个显示的条目的底,也是第二个条目，也就是下一个标题类消息的顶
                val bottom = mListView.getChildAt(0).bottom
                // 头部view的高度
                val headerHeight = mHeaderView.height
                val y : Int = if ((bottom + mDividerHeight) < headerHeight) {
                    // 头部view需要往屏蔽顶端移动显示的距离
                    (bottom + mDividerHeight) - headerHeight
                } else {
                    // 头部不用平移
                    0
                }
                mAdapter.configurePinnedHeader(mHeaderView, position)
                // 未防止重复layout，先判断是否已经在正确位置
                if (mHeaderView.top != y) {
                    mHeaderView.layout(0, y, mHeaderViewWidth, mHeaderViewHeight + y)
                }
                mHeaderViewVisible = true
            }
        }
        requestLayout()
    }

    override fun dispatchDraw(canvas: Canvas?) {
        /**
         * 这里是个关键点，必须将顶部部固定view放在后面绘制，这样才能让固定view显示在顶层
         */
        super.dispatchDraw(canvas)
        if (mHeaderViewVisible) {
            drawChild(canvas, mHeaderView, drawingTime)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec)
        mHeaderViewWidth = mHeaderView.measuredWidth
        mHeaderViewHeight = mHeaderView.measuredHeight
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight)
        configureHeaderView(mListView.firstVisiblePosition)
    }

    private fun setPinnedHeaderView(view : View) {
        mHeaderView = view
        setFadingEdgeLength(0)
        // todo:这里没用吧
        Util.PUSH_CHANNEL
//        requestLayout()
    }

}