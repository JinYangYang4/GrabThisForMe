package com.example.grabthisforme.activity.fragment_misc.setfragment.view


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.grabthisforme.activity.fragment_misc.setfragment.viewmodel.AccountSecurityViewModel
import com.example.grabthisforme.databinding.FragmentAccountSecurityBinding
import com.example.grabthisforme.util.ViewAnimationUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentAccountSecurity : Fragment() {
    private var _binding: FragmentAccountSecurityBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AccountSecurityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountSecurityBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewData()
        initClickListener()
        initObserve()
        ViewAnimationUtils.animateStaggeredEntrance(binding.llToast, binding.passwordFormCard)
    }
    private fun initViewData() {
        binding.etOriginalPwd.doAfterTextChanged { editable ->
            viewModel.updateOriginalPassword(editable?.toString().orEmpty())
        }
        binding.etNewPwd.doAfterTextChanged { editable ->
            viewModel.updateNewPassword(editable?.toString().orEmpty())
        }
        binding.etOriginalPwd.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.etOriginalPwd.clearFocus()
                true
            } else {
                false
            }
        }
        binding.etNewPwd.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.etNewPwd.clearFocus()
                true
            } else {
                false
            }
        }
    }
    private fun initClickListener() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.llSubmit.setOnClickListener {
            viewModel.submitPasswordChange()
        }
    }

    private fun initObserve() {
        viewModel.canSubmit.observe(viewLifecycleOwner) { canSubmit ->
            binding.llSubmit.isEnabled = canSubmit
            binding.llSubmit.isClickable = canSubmit
        }
        viewModel.submitResult.observe(viewLifecycleOwner) { result ->
            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
            if (result.success) {
                binding.etOriginalPwd.setText("")
                binding.etNewPwd.setText("")
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
