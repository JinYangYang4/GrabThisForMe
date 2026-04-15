package com.example.grabthisforme.activity.LoginActivity.view

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.LoginActivity.viewmodel.SwitchAccountsViewModel
import com.example.grabthisforme.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentLogin : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private var lastTriggerTime = 0L
    private val debounceThreshold = 100L
    private val binding get() = _binding!!

    private val viewModel: SwitchAccountsViewModel by activityViewModels()
    val bottomSheet = SwitchAccountsBottomSheetDialogFragment.newInstance()
    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initClickListener()
        globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastTriggerTime < debounceThreshold) {
                    return
                }
                lastTriggerTime = currentTime

                val r = Rect()
                view.getWindowVisibleDisplayFrame(r)
                val screenHeight = view.rootView.height
                val keypadHeight = screenHeight - r.bottom
                val isKeyboardClosed = keypadHeight < screenHeight * 0.15


                if (isKeyboardClosed && _binding != null) {
                    clearInputFocus()
                }
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)

    }



    private fun clearInputFocus() {
        val currentFocus = requireActivity().currentFocus ?: return
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)

        _binding?.etUserId?.clearFocus()
        _binding?.etPassword?.clearFocus()
    }

    private fun initView() {}

    private fun initClickListener() {
        binding.root.setOnClickListener {
            clearInputFocus()
        }
        binding.layoutRegister.setOnClickListener {
            (requireActivity() as LoginActivity).intentToRegisterFragment(R.id.action_fragmentLogin_to_fragmentRegister)
        }
        binding.tvForget.setOnClickListener {
            (requireActivity() as LoginActivity).intentToRegisterFragment(R.id.action_fragmentLogin_to_fragmentFindPassword2)
        }
        binding.tvSwitchAccounts.setOnClickListener {
            bottomSheet.show(childFragmentManager, "SwitchAccountsBottomSheet")
        }
    }

    override fun onStop() {
        super.onStop()
        val rootView = view ?: return
        globalLayoutListener?.let { listener ->
            if (rootView.viewTreeObserver.isAlive) {
                rootView.viewTreeObserver.removeOnGlobalLayoutListener(listener)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        globalLayoutListener = null
        _binding = null
    }
}