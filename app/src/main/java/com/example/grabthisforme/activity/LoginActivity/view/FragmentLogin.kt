package com.example.grabthisforme.activity.LoginActivity.view

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.LoginActivity.viewmodel.SwitchAccountsViewModel
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentLoginBinding
import com.example.grabthisforme.model.auth.data.repository.AuthRepository
import com.example.grabthisforme.model.user.data.repository.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FragmentLogin : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var authRepository: AuthRepository

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
        observeCurrentUser()
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

    private fun observeCurrentUser() {
        lifecycleScope.launch {
            userRepository.currentUser.collect { user ->
                user?.let {
                    binding.etUserId.setText(it.id.toString())
                    binding.tvTitle.text = "欢迎回来，${it.name}"
                }
            }
        }
    }

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
        binding.layoutLogin.setOnClickListener {
            handleLogin()
        }
    }

    private fun handleLogin() {
        val userIdText = binding.etUserId.text?.toString()?.trim()
        val password = binding.etPassword.text?.toString()?.trim()

        if (userIdText.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "请输入账号ID", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "请输入密码", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val result = authRepository.login(
                identifier = userIdText,
                password = password
            )
            if (result.isSuccess) {
                Toast.makeText(requireContext(), "登录成功", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            } else {
                Toast.makeText(
                    requireContext(),
                    result.exceptionOrNull()?.message ?: "登录失败",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
