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
                "\u53ef\u6269\u5c55\u4e3a\u591a\u8d26\u53f7\u5207\u6362\u4e0e\u6821\u56ed\u8eab\u4efd\u7ba1\u7406",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.itemLogout.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "\u5f53\u524d\u9875\u9762\u5df2\u9884\u7559\u9000\u767b\u5165\u5165\u53e3\uff0c\u53ef\u63a5\u7edf\u4e00\u767b\u51fa\u6d41\u7a0b",
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
