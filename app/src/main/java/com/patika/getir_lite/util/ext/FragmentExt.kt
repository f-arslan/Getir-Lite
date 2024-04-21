package com.patika.getir_lite.util.ext

import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.patika.getir_lite.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun Fragment.scopeWithLifecycle(block: suspend CoroutineScope.() -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED, block = block)
    }
}

fun Fragment.safeNavigate(direction: NavDirections) {
    if (isAdded) {
        this.findNavController().navigate(direction)
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

fun Fragment.showCustomDialog(
    @LayoutRes layoutRes: Int,
    buttonSetup: Dialog.() -> Unit
) {
    val dialog = Dialog(requireContext())
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(false)
    dialog.setContentView(layoutRes)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )

    dialog.buttonSetup()

    dialog.show()
}

fun Fragment.showCancelDialog(onYesClick: () -> Unit) {
    showCustomDialog(R.layout.dialog_basket_delete) {
        val noButton: Button = findViewById(R.id.btn_no)
        val yesButton: Button = findViewById(R.id.btn_yes)

        noButton.setOnClickListener {
            dismiss()
        }

        yesButton.setOnClickListener {
            onYesClick()
            dismiss()
        }
    }
}

fun Fragment.showCompleteDialog(onComplete: () -> Unit) {
    showCustomDialog(R.layout.dialog_complete_order) {
        val completeButton: Button = findViewById(R.id.btn_complete)

        completeButton.setOnClickListener {
            onComplete()
            dismiss()
        }
    }
}
