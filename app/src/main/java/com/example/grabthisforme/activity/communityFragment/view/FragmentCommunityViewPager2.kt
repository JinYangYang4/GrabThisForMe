package com.example.grabthisforme.activity.communityFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.MainActivity.view.MainActivity
import com.example.grabthisforme.activity.communityFragment.adpter.CommunityVP2_RVAdapter
import com.example.grabthisforme.activity.homeFragment.view.FragmentHomeViewPager2
import com.example.grabthisforme.databinding.FragmentCommunityViewpager2Binding
import com.example.grabthisforme.model.Post.Post

class FragmentCommunityViewPager2 : Fragment(){
    private var _binding: FragmentCommunityViewpager2Binding? = null
    private val binding get() = _binding!!

    companion object {
        private const val KEY_TYPE = "task_type"

        fun newInstance(type: Int): FragmentCommunityViewPager2 {
            return FragmentCommunityViewPager2().apply {
                arguments = Bundle().apply {
                    putInt(KEY_TYPE, type)
                }
            }
        }
    }

    private val taskType: Int by lazy {
        arguments?.getInt(KEY_TYPE) ?: 0
    }




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityViewpager2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRV()

    }
    fun initRV(){
        val adapter = CommunityVP2_RVAdapter(){
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_blankFragment_to_postDetailFragment)
        }
        binding.rvTask.adapter = adapter
        binding.rvTask.layoutManager = LinearLayoutManager(requireContext())
        adapter.submitList(Post.getPostList())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}