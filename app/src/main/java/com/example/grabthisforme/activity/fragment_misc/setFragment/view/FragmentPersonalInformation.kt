package com.example.grabthisforme.activity.fragment_misc.setFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.grabthisforme.activity.MainActivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentPersonalInformationBinding

class FragmentPersonalInformation : Fragment() {

    private var _binding: FragmentPersonalInformationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initListener()
    }

    private fun initData() {
        binding.tvNameValue.text = "张三"
        binding.tvGenderValue.text = "男"
        binding.tvRegionValue.text = "北京市 朝阳区"
        binding.tvMobileValue.text = "138****8888"
        binding.tvAccountValue.text = "user12345678"
        binding.tvSignatureValue.text = "人生如逆旅，我亦是行人"
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