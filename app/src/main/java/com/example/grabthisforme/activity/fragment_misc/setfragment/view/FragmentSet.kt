package com.example.grabthisforme.activity.fragment_misc.setfragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentSetBinding
import com.example.grabthisforme.util.ViewAnimationUtils

class FragmentSet : Fragment() {
    private var _binding: FragmentSetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickEvents()
        ViewAnimationUtils.animateStaggeredEntrance(
            binding.accountSettings,
            binding.privacySettings,
            binding.accountOperations
        )
    }

    private fun initClickEvents() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.itemProfile.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_setFragment_to_personalInformation)
        }
        binding.itemAccountSecurity.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_setFragment_to_fragmentAccountSecurity)
        }

        binding.itemChat.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_setFragment_to_fragmentChatBackground)
        }
        binding.itemSwitchAccount.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "可扩展为多账号切换与校园身份管理",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.itemLogout.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "当前页面已预留退登入入口，可接统一登出流程",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).innerBottomBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
