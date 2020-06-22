package com.tangem.merchant.common.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment

/**
 * Created by Anton Zhilenkov on 09/04/2020.
 */
fun Context.copyToClipboard(value: Any, label: String = "") {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return

    val clip: ClipData = ClipData.newPlainText(label, value.toString())
    clipboard.setPrimaryClip(clip)
}

fun Context.shareText(text: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}

fun Fragment.shareText(text: String) {
    requireContext().shareText(text)
}