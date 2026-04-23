package com.example.grabthisforme.activity.fragment_misc.setfragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.fragment_misc.setfragment.viewmodel.PersonalInfoViewModel
import com.example.grabthisforme.databinding.FragmentPersonalInformationBinding

class FragmentPersonalInformation : Fragment() {

    private var _binding: FragmentPersonalInformationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PersonalInfoViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalInformationBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
    }

    private fun initListener() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 头像点击事件（可选：跳转到更换头像页面）
        binding.ivAvatar.setOnClickListener {

        }
        binding.itemName.setOnClickListener {
        }

        binding.itemGender.setOnClickListener {
        }

        binding.itemRegion.setOnClickListener {
        }

        binding.itemMobile.setOnClickListener {
        }

        binding.itemSignature.setOnClickListener {
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