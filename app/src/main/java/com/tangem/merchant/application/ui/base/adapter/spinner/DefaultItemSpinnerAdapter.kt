package com.tangem.merchant.application.ui.base.adapter.spinner

import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.Spinner
import android.widget.TextView
import com.tangem.merchant.R
import ru.dev.gbixahue.eu4d.lib.android._android.views.inflate

/**
 * Created by Anton Zhilenkov on 25/06/2020.
 * The last item is the default value
 */
class DefaultItemSpinnerAdapter(
    private val itemList: List<String>,
    private val spinner: Spinner,
    selectedPosition: Int = itemList.size - 1,
    listener: ((String, Int) -> Unit)? = null
) : BaseAdapter() {

    var onItemSelectedListener: ((String, Int) -> Unit)? = null
        set(value) {
            field = value
            spinner.onItemSelectedListener = if (field == null) null else createSelectedListener(field!!, count)
        }

    init {
        spinner.adapter = this
        spinner.setSelection(selectedPosition)
        onItemSelectedListener = listener
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewId = if (position != count) R.layout.sp_item_selected else R.layout.sp_item
        val container = parent.inflate<View>(viewId)
        container.findViewById<TextView>(R.id.tvHint)?.text = getItem(count)
        container.findViewById<TextView>(R.id.tvItem)?.text = getItem(position)
        return container
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val container = convertView ?: parent.inflate(R.layout.sp_item_dropdown)
        container.findViewById<TextView>(R.id.tvDropDownItem)?.text = getItem(position)
        return container
    }


    override fun getItem(position: Int): String? = itemList[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getCount(): Int = itemList.size - 1

    fun createSelectedListener(selectedListener: (String, Int) -> Unit, itemCount: Int): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            var pos: Int = itemCount
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (pos == position) return
                pos = position

                val item = getItem(position) ?: return
                selectedListener(item, position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }
}