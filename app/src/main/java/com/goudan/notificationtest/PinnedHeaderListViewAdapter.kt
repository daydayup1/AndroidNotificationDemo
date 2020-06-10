package com.goudan.notificationtest

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView

class PinnedHeaderListViewAdapter : BaseAdapter {

    var itemTextViewHeight : Int = 0
    var sectionTextSize : Int = 0
    var sectionTextViewHeight : Int = 0
    var itemTextSize : Int = 0

    private var mData : ArrayList<ItemInfo> = ArrayList()
    private var mContext: Context? = null
    companion object {
        val PINNED_HEADER_GONE = 0
        val PINNED_HEADER_VISIBLE = 1
        val PINNED_HEADER_PUSHING_UP = 2
        val ITEM_TYPE_SECTION = 0
        val ITEM_TYPE_ITEM = 1
        val ITEM_TEXT_PADDING = 30
        val SECTION_TEXT_PADDING = 10
    }

    constructor(context : Context) : super() {
        mContext = context
        itemTextViewHeight  = (context.resources.displayMetrics.density * 55).toInt()
        sectionTextSize = 20
        sectionTextViewHeight = (context.resources.displayMetrics.density * 35).toInt()
        itemTextSize = 16
    }

    fun setData(data : ArrayList<ItemInfo>?) {
        data?.let {
            mData.clear()
            mData.addAll(data)
            notifyDataSetChanged()
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position < 0 || position > mData.size) ITEM_TYPE_ITEM

        if (mData[position].isSection) return ITEM_TYPE_SECTION

        return ITEM_TYPE_ITEM
    }

    override fun getViewTypeCount() = 2

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var data = mData[position]
        when (getItemViewType(position)) {
            ITEM_TYPE_ITEM -> {
                if (convertView == null) {
                    val itemTextView = TextView(mContext).apply {
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            itemTextViewHeight)
                        textSize = itemTextSize.toFloat()
                        gravity = Gravity.CENTER_VERTICAL
                        setTextColor(Color.BLACK)
                        text = data.title
                    }

                    val layout = LinearLayout(mContext).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(ITEM_TEXT_PADDING, 0 , ITEM_TEXT_PADDING, 0)
                        layoutParams = AbsListView.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
                        setBackgroundResource(R.drawable.item_bg_selector)
                    }
                    layout.addView(itemTextView, 0)
                    return layout
                }
                (((convertView as LinearLayout).getChildAt(0)) as TextView).text = data.title
                return convertView
            }
            ITEM_TYPE_SECTION -> {
                val header : View? = convertView ?: getHeaderView()
                (((header as LinearLayout).getChildAt(0)) as TextView).text = data.sectionStr
                return header
            }
            else -> return convertView!!
        }

    }

    override fun getItem(position: Int): Any? {
        return if (position >= 0 && position < mData.size)
            mData[position]
        else null
    }

    override fun getItemId(position: Int) = 0L

    override fun getCount() = mData.size

    fun getHeaderView() : View {
        val textView = TextView(mContext).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, sectionTextViewHeight)
            gravity = Gravity.CENTER_VERTICAL
            textSize = sectionTextSize.toFloat()
            setTextColor(Color.WHITE)
        }
        val headView = LinearLayout(mContext).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(SECTION_TEXT_PADDING, 0, 0, 0)
            layoutParams = AbsListView.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            setBackgroundColor(resources.getColor(R.color.header_bg))
        }
        headView.addView(textView, 0)
        return headView
    }

    /**
     * 检查当前固定在顶部的header的状态
     */
    fun getPinnedHeaderState(position: Int) : Int {
        if (position < 0 || position > count || count == 0) return PINNED_HEADER_GONE

        val currentItem : ItemInfo? = getItem(position) as ItemInfo
        val nextItem : ItemInfo? = getItem(position + 1) as ItemInfo

        val isSection : Boolean = currentItem?.isSection ?: false
        val isNextSection : Boolean = nextItem?.isSection ?: false
        return if (!isSection && isNextSection) PINNED_HEADER_PUSHING_UP else PINNED_HEADER_VISIBLE
    }

    /**
     * 配置头部view的文字和布局信息
     */
    fun configurePinnedHeader(header : View, position: Int) {
        var item : ItemInfo? = getItem(position) as ItemInfo?
        if (item != null && header is LinearLayout) {
            val textView = header.getChildAt(0) as TextView
            textView.text = item.sectionStr
            textView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, sectionTextViewHeight)
        }
    }
}