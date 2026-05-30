package com.example.grabthisforme.activity.myfragment.view

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.LoginActivity.view.LoginActivity
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.mainactivity.viewmodel.MainViewModel
import com.example.grabthisforme.databinding.FragmentMyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentMy : Fragment() {
    private var _binding: FragmentMyBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel : MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.sharedViewModel = sharedViewModel
        initView()
        initObserve()
    }
    fun initObserve(){
        sharedViewModel.currentUser.observe(viewLifecycleOwner){user ->
            Glide.with(this)
                .load(user?.headPic)
                .placeholder(R.drawable.cat)
                .error(R.drawable.cat)
                .into(binding.ivHeadPic)
        }
    }
    fun initView(){
        binding.bgHead.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }
        binding.itemOrder.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment_Order_ac(2)
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
        binding.tvEdit.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_blankFragment_to_personalInformation)
        }

    }

}
