package com.example.grabthisforme.activity.communityFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.communityFragment.adpter.CommunityVP2_RVAdapter
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.view.PhotoPreviewDialog
import com.example.grabthisforme.activity.communityFragment.viewmodel.CommunityViewModel
import com.example.grabthisforme.activity.fragment_misc.default_entry.view.BlankFragmentDirections
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentCommunityViewpager2Binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentCommunityViewPager2 : Fragment(){
    private var _binding: FragmentCommunityViewpager2Binding? = null
    private val binding get() = _binding!!

    private val viewModel: CommunityViewModel by viewModels()
    private lateinit var adapter: CommunityVP2_RVAdapter

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
        initObserve()

    }
    private fun initRV() {
        adapter = CommunityVP2_RVAdapter(
            clickListener = { postId ->
                val dir = BlankFragmentDirections.actionBlankFragmentToPostDetailFragment(postId)
                (requireActivity() as MainActivity).NewNavController_navgite(dir)
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
        binding.rvTask.adapter = adapter
        binding.rvTask.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun initObserve() {
        viewModel.postList.observe(viewLifecycleOwner) { posts ->
            adapter.submitList(posts)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
