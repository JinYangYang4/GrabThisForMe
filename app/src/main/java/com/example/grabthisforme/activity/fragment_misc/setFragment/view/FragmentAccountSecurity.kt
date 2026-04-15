package com.example.grabthisforme.activity.fragment_misc.setFragment.view


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import com.example.grabthisforme.databinding.FragmentAccountSecurityBinding

class FragmentAccountSecurity : Fragment() {
    private var _binding: FragmentAccountSecurityBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountSecurityBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewData()
        initClickListener()
    }
    private fun initViewData() {
        binding.tvAccount.text = "user12345678"
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
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}