package com.example.grabthisforme.activity.LoginActivity.view

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Looper
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
import com.example.grabthisforme.activity.LoginActivity.viewmodel.SwitchAccountsViewModel
import com.example.grabthisforme.databinding.FragmentRegisterBinding
import com.example.grabthisforme.model.user.domain.User
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import java.util.UUID

@AndroidEntryPoint
class FragmentRegister : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SwitchAccountsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClick()
        ViewCompat.setOnApplyWindowInsetsListener(requireView()) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (!imeVisible && _binding != null) {
                clearInputFocus()
            }
            insets
        }
    }
    override fun onStop() {
        super.onStop()

    }
    fun initClick(){
        binding.layoutRegister.setOnClickListener {
            getUserItem()
        }
        binding.tvSwitchAccounts.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.root.setOnClickListener {
            clearInputFocus()
        }
    }
    private fun getUserItem() {
        val context = context ?: return
        if (binding == null) return
        val userName = binding.etUserName.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString()?.trim() ?: ""
        val passwordMakeSure = binding.etPasswordMakeSure.text?.toString()?.trim() ?: ""
        val isInputValid = when {
            userName.isEmpty() -> {
                showInputError(binding.tilName, "用户名不能为空")
                false
            }
            password.isEmpty() -> {
                showInputError(binding.tilPassword, "密码不能为空")
                false
            }
            passwordMakeSure.isEmpty() -> {
                showInputError(binding.tilPasswordMakeSure, "确认密码不能为空")
                false
            }
            password != passwordMakeSure -> {
                showInputError(binding.tilPasswordMakeSure, "两次输入的密码不一致")
                binding.etPasswordMakeSure.setText("") // 直接操作EditText更简洁
                false
            }
            else -> true
        }
        if (!isInputValid) return
        val userId = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE
        val newUser = User(
            name = userName,
            id = userId,
            passwordHash = password,
            headPic = ""
        )

        viewModel.insertUser(newUser)
        clearInputFocus()


        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(context, "用户创建成功：$userName", Toast.LENGTH_SHORT).show()
        } else {
            Looper.prepare()
            Toast.makeText(context, "用户创建成功：$userName", Toast.LENGTH_SHORT).show()
            Looper.loop()
        }
    }
    private fun showInputError(textInputLayout: TextInputLayout, errorMsg: String) {
        textInputLayout.error = errorMsg
        textInputLayout.postDelayed({
            textInputLayout.error = null
        }, 3000)
        textInputLayout.editText?.requestFocus()
    }


    private fun clearInputFocus() {
        val currentFocus = requireActivity().currentFocus ?: return
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        currentFocus.clearFocus()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
