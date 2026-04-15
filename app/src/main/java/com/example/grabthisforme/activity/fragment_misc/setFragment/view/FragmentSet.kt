package com.example.grabthisforme.activity.fragment_misc.setFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.MainActivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentSetBinding

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
        }

        binding.itemLogout.setOnClickListener {
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