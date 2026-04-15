package com.example.grabthisforme.activity.myFragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.LoginActivity.view.LoginActivity
import com.example.grabthisforme.activity.MainActivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentMyBinding


class FragmentMy : Fragment() {
    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }
    fun initView(){
        binding.bgHead.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }
        binding.itemOrder.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment_ac(2)
        }
        binding.itemLike.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_blankFragment_to_myLoveFragment)
        }
        binding.itemTopic.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_blankFragment_to_myTopicFragment)
        }
        binding.itemSet.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_blankFragment_to_setFragment)
        }

    }

}