package com.tangem.merchant.application.ui.settingsAddBlc

import android.graphics.Rect
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tangem.merchant.R
import com.tangem.merchant.application.domain.model.BlockchainItem
import ru.dev.gbixahue.eu4d.lib.android._android.views.recycler_view.RvAdapter
import ru.dev.gbixahue.eu4d.lib.android._android.views.recycler_view.RvCallback
import ru.dev.gbixahue.eu4d.lib.android._android.views.recycler_view.RvVH

/**
 * Created by Anton Zhilenkov on 29/06/2020.
 */
class BlcVH(itemView: View, callback: RvCallback<BlockchainItem>?) : RvVH<BlockchainItem>(itemView, callback) {
    private val tvBlcName: TextView = itemView.findViewById(R.id.tvBlcName)
    private val tvBlcAddress: TextView = itemView.findViewById(R.id.tvBlcAddress)
    private val btnDelete: View = itemView.findViewById(R.id.btnDelete)

    override fun onDataBound(data: BlockchainItem) {
        tvBlcName.text = data.blockchain.id
        tvBlcAddress.text = data.address
        btnDelete.setOnClickListener {
            callback?.invoke(adapterPosition, layoutPosition, data)
        }
    }
}

class BlcRvAdapter(
    callback: RvCallback<BlockchainItem>
) : RvAdapter<BlcVH, BlockchainItem>(callback) {
    override fun createViewHolder(view: View, listener: RvCallback<BlockchainItem>?): BlcVH {
        return BlcVH(view, listener)
    }

    override fun getLayoutId(): Int {
        return R.layout.rv_blockchain_item
    }
}

class SpaceItemDivider(private val spaceBetween: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = (view.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
        val itemCount = parent.adapter?.itemCount ?: 0

        when (position) {
            0 -> outRect.set(0, spaceBetween * 2, 0, spaceBetween)
            itemCount - 1 -> outRect.set(0, spaceBetween, 0, spaceBetween * 2)
            else -> outRect.set(0, spaceBetween, 0, spaceBetween)
        }
    }
}