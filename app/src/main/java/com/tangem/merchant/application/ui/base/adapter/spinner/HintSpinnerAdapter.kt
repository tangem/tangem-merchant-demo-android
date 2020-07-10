package com.tangem.merchant.application.ui.base.adapter.spinner

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.tangem.merchant.R
import ru.dev.gbixahue.eu4d.lib.android._android.views.colorFrom
import ru.dev.gbixahue.eu4d.lib.android._android.views.inflate

/**
 * Created by Anton Zhilenkov on 28/06/2020.
 */
open class HintSpinnerAdapter<T>(
    context: Context,
    protected val itemList: MutableList<T> = mutableListOf(),
    hint: Int? = null
) : ArrayAdapter<T>(context, android.R.layout.simple_list_item_1, itemList) {

    protected var hint: String? = if (hint == null) null else context.getString(hint)

    fun setItemList(list: List<T>) {
        itemList.clear()
        itemList.addAll(list)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(parent.context)
        val inflatedView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false) as TextView
        val textView: TextView = convertView as? TextView ?: inflatedView

        textView.text = if (hint != null && position == 0) hint else getLabelFor(itemList[position - 1])
        return textView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(parent.context)
        val inflatedView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false) as TextView
        val textView = convertView as? TextView ?: inflatedView

        textView.text = if (hint != null && position == 0) hint else getLabelFor(itemList[position - 1])
        return textView
    }

    override fun getCount(): Int {
        val count = super.getCount()
        return if (hint != null) count + 1 else count
    }

    override fun isEnabled(position: Int): Boolean {
        val isEnabled = super.isEnabled(position)
        return if (hint != null) position != 0 && isEnabled else isEnabled
    }

    open fun getLabelFor(item: T): String = item.toString()

    fun hasHint(): Boolean = hint != null
}

open class BaseHintAdapter<T>(
    context: Context,
    itemList: MutableList<T>,
    hint: Int
) : HintSpinnerAdapter<T>(context, itemList, hint) {

    var hasChanges = false

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewId = if (!hasChanges) R.layout.sp_item else R.layout.sp_item_selected
        val layout = convertView ?: parent.inflate(viewId)

        val tvHint = layout.findViewById<TextView>(R.id.tvHint)
        val tvItem = layout.findViewById<TextView>(R.id.tvItem)

        tvHint.text = hint
        if (position > 0) tvItem?.text = getLabelFor(itemList[position - 1])

        return layout
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val layout: View = convertView ?: parent.inflate(R.layout.sp_item_dropdown)
        val textView = layout.findViewById<TextView>(R.id.tvDropDownItem)
        if (hint != null && position == 0) {
            textView.text = hint
            textView.setTextColor(textView.colorFrom(R.color.textSecondary))
        } else {
            textView.text = getLabelFor(itemList[position - 1])
        }

        layout.setOnTouchListener { _, _ ->
            hasChanges = true
            return@setOnTouchListener false
        }
        return layout
    }

    companion object {
        fun <T> setItemSelectedListener(spinner: Spinner, listener: ItemSelectedListener<T>) {
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val adapter = spinner.adapter as? BaseHintAdapter<T> ?: return
                    adapter.hasChanges = position != 0

                    if (position != 0) {
                        val itemPosition = position - 1
                        val item: T? = adapter.getItem(itemPosition) as? T
                        item?.let { listener(it, position) }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }
}
typealias ItemSelectedListener<T> = (T, Int) -> Unit

