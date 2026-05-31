package com.example.grabthisforme.activity.fragment_misc.setfragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentChatBackgroudBinding
import com.example.grabthisforme.util.ViewAnimationUtils

class FragmentChatBackground : Fragment() {
    private var _binding: FragmentChatBackgroudBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBackgroudBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickListener()
        ViewAnimationUtils.animateStaggeredEntrance(binding.backgroundSourceCard)
    }
    private fun initClickListener() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.itemRecommendBg.setOnClickListener {
        }
        binding.itemAlbumSelect.setOnClickListener {
        }
        binding.itemCameraSelect.setOnClickListener {
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