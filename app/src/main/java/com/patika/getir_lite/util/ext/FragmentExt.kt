package com.patika.getir_lite.util.ext

import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.patika.getir_lite.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun Fragment.scopeWithLifecycle(block: suspend CoroutineScope.() -> Unit) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED, block = block)
    }
}

fun ViewBinding.makeSnackbar(text: String?) {
    val snackbar = Snackbar.make(
        root,
        text ?: root.context.getString(R.string.generic_error),
        Snackbar.LENGTH_SHORT
    )
    val snackbarView = snackbar.view
    val textView: TextView? =
        snackbarView.findViewById(com.google.android.material.R.id.snackbar_text)
    textView?.typeface = Typeface.create("open-sans-medium", Typeface.BOLD)
    textView?.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

    val params = snackbarView.layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(
        params.leftMargin + 16,
        params.topMargin,
        params.rightMargin + 16,
        params.bottomMargin + 48
    )
    snackbarView.layoutParams = params
    snackbar.show()
}
