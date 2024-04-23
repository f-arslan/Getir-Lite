package com.patika.getir_lite.util.ext

import android.app.Dialog
import android.graphics.Color
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

/**
 * Launches a coroutine linked to the Fragment's View LifecycleOwner that only runs
 * when the lifecycle is at least in the STARTED state. This extension ensures that
 * coroutines started in this scope do not run when the fragment is not actively
 * in the STARTED state or beyond, preventing potential crashes or leaks.
 *
 * @param block The suspending block of code to execute, scoped to [CoroutineScope].
 */
fun Fragment.scopeWithLifecycle(block: suspend CoroutineScope.() -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED, block = block)
    }
}

/**
 * Performs a navigation operation to a specified [NavDirections] if the fragment
 * is currently added to its host. This check ensures that navigation only occurs
 * when the fragment is actively attached, preventing IllegalStateException.
 *
 * @param direction The navigation direction that specifies the action and the destination to navigate to.
 */
fun Fragment.safeNavigate(direction: NavDirections) {
    if (isAdded) {
        this.findNavController().navigate(direction)
    }
}

/**
 * Displays a Snackbar with a specified text. If the text is null, a generic error message
 * is displayed. This function extends [ViewBinding] to allow any fragment or activity
 * with a view binding to easily display snackbars.
 *
 * @param text The message to display in the snackbar. If null, a generic error message is used.
 */
fun ViewBinding.makeSnackbar(text: String?) {
    val snackbar = Snackbar.make(
        root,
        text ?: root.context.getString(R.string.generic_error),
        Snackbar.LENGTH_SHORT
    )
    val snackbarView = snackbar.view
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


/**
 * Displays a custom dialog with the specified layout resource. The dialog's appearance and functionality
 * can be customized via the [buttonSetup] block, allowing for specific button actions and other configurations.
 *
 * @param layoutRes The layout resource ID for the content view of the dialog.
 * @param buttonSetup A lambda function to configure dialog buttons and actions.
 */
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

fun Fragment.showCompleteDialog(text: String, onComplete: () -> Unit) {
    showCustomDialog(R.layout.dialog_complete_order) {
        val completeButton: Button = findViewById(R.id.btn_complete)
        val priceText: TextView = findViewById(R.id.tv_total_price)
        priceText.text = getString(R.string.total_price, text)

        completeButton.setOnClickListener {
            onComplete()
            dismiss()
        }
    }
}
