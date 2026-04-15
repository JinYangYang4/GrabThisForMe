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
import androidx.fragment.app.Fragment
import com.example.grabthisforme.databinding.FragmentFindPassageBinding

class FragmentFindPassword : Fragment() {
    private var _binding: FragmentFindPassageBinding? = null
    private val binding get() = _binding!!
    private val debounceThreshold: Long = 200
    private var lastTriggerTime: Long = 0
    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindPassageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initKeyboardListener()
    }

    private fun initKeyboardListener() {
        val rootView = binding.root
        globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastTriggerTime < debounceThreshold) {
                    return
                }
                lastTriggerTime = currentTime

                val r = Rect()
                rootView.getWindowVisibleDisplayFrame(r)
                val screenHeight = rootView.rootView.height
                val keypadHeight = screenHeight - r.bottom
                val isKeyboardClosed = keypadHeight < screenHeight * 0.15

                if (isKeyboardClosed && _binding != null) {
                    clearInputFocus()
                }
            }
        }
        rootView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    private fun clearInputFocus() {
        binding.etUserId.clearFocus()
        binding.etPhone.clearFocus()
        binding.etVerificationCode.clearFocus()
        binding.etNewPassword.clearFocus()
        val currentFocus = requireActivity().currentFocus ?: return
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
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