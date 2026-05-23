package com.example.grabthisforme.activity.fragment_misc.my_love.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.communityFragment.adpter.CommunityVP2_RVAdapter
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.view.PhotoPreviewDialog
import com.example.grabthisforme.activity.fragment_misc.my_love.viewmodel.MyLoveViewModel
import com.example.grabthisforme.databinding.FragmentMyLoveBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyLoveFragment : Fragment() {
    private var _binding: FragmentMyLoveBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyLoveViewModel by viewModels()

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
        initObserve()
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
        topicAdapter = CommunityVP2_RVAdapter(
            clickListener = { postId ->
                val action = MyLoveFragmentDirections.actionMyLoveFragmentToPostDetailFragment(postId)
                (requireActivity() as MainActivity).NewNavController_navgite(action)
            },
            onPostImageClick = { post, clickedPosition ->
                val imageUris = post.images
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                if (imageUris.isEmpty()) return@CommunityVP2_RVAdapter
                val initialIndex = clickedPosition.coerceIn(0, imageUris.lastIndex)
                PhotoPreviewDialog
                    .newInstance(imageUris, initialIndex)
                    .show(childFragmentManager, "PhotoPreviewDialog")
            }
        )
        binding.rvTopic.adapter = topicAdapter
        binding.rvTopic.setHasFixedSize(true)
    }

    private fun initObserve() {
        viewModel.likedPosts.observe(viewLifecycleOwner) { likedPosts ->
            topicAdapter.submitList(likedPosts)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
