package com.example.grabthisforme.activity.LoginActivity.view

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.LoginActivity.viewmodel.SwitchAccountsViewModel
import com.example.grabthisforme.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentLogin : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SwitchAccountsViewModel by activityViewModels()
    val bottomSheet = SwitchAccountsBottomSheetDialogFragment.newInstance()

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
        ViewCompat.setOnApplyWindowInsetsListener(requireView()) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (!imeVisible && _binding != null) {
                clearInputFocus()
            }
            insets
        }
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}