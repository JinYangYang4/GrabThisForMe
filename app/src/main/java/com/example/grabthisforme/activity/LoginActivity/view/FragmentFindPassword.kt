package com.example.grabthisforme.activity.LoginActivity.view

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.grabthisforme.databinding.FragmentFindPassageBinding

class FragmentFindPassword : Fragment() {
    private var _binding: FragmentFindPassageBinding? = null
    private val binding get() = _binding!!
    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindPassageBinding.inflate(inflater, container, false)
        initClickListener()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(requireView()) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (!imeVisible && _binding != null) {
                clearInputFocus()
            }
            insets
        }
    }
    private fun initClickListener(){
        binding.root.setOnClickListener {
            clearInputFocus()
        }
        binding.tvSwitchAccounts.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }



    private fun clearInputFocus() {
        val currentFocus = requireActivity().currentFocus ?: return
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        currentFocus.clearFocus()
    }


    override fun onDestroyView() {
        super.onDestroyView()

        val rootView = binding.root
        globalLayoutListener?.let { listener ->
            if (rootView.viewTreeObserver.isAlive) {
                rootView.viewTreeObserver.removeOnGlobalLayoutListener(listener)
            }
        }

        globalLayoutListener = null
        _binding = null
    }
}
