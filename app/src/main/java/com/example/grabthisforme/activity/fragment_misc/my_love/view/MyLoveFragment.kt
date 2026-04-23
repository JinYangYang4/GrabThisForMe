package com.example.grabthisforme.activity.fragment_misc.my_love.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.communityFragment.adpter.CommunityVP2_RVAdapter
import com.example.grabthisforme.databinding.FragmentMyLoveBinding
import com.example.grabthisforme.model.Post.Post

class MyLoveFragment : Fragment() {
    private var _binding: FragmentMyLoveBinding? = null
    private val binding get() = _binding!!

    private lateinit var topicAdapter: CommunityVP2_RVAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyLoveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickEvents()
        initRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).innerBottomBar()
    }
    private fun initClickEvents() {
        binding.ivBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = RecyclerView.VERTICAL
        binding.rvTopic.layoutManager = layoutManager
        topicAdapter = CommunityVP2_RVAdapter(){
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_myLoveFragment_to_postDetailFragment)
        }
        binding.rvTopic.adapter = topicAdapter
        binding.rvTopic.setHasFixedSize(true)
        topicAdapter.submitList(Post.getPostList())
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}