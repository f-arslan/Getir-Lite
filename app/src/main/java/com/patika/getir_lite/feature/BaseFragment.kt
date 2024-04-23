package com.patika.getir_lite.feature

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * An abstract base fragment that handles view binding for fragments. This class ensures that the view binding is
 * initialized and cleared correctly to avoid memory leaks.
 *
 * @param T The type of the view binding associated with this fragment.
 */
abstract class BaseFragment<T : ViewBinding> : Fragment() {
    private var _binding: T? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateBinding(inflater, container)
        safeOnCreateView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.onMain()
    }

    /**
     * Must be implemented to inflate the binding using the layout inflater and container provided.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container The parent view that the fragment's UI should be attached to.
     * @return The initialized binding.
     */
    protected abstract fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): T

    /**
     * Abstract method that subclasses must implement to perform operations on the main thread after the view is created.
     * This method is invoked in onViewCreated and operates directly on the binding.
     */
    protected abstract fun T.onMain()

    /**
     * Optional method that can be overridden by subclasses to perform additional setup after the view is created
     * but before any logic is applied to the binding in onViewCreated.
     */
    protected open fun safeOnCreateView() = Unit

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
