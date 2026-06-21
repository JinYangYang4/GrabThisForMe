package com.example.grabthisforme.activity.communityFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.activity.communityFragment.adpter.CommunityVP2_RVAdapter
import com.example.grabthisforme.activity.communityFragment.model.CommunityFeedArgs
import com.example.grabthisforme.activity.communityFragment.viewmodel.CommunityViewModel
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.view.PhotoPreviewDialog
import com.example.grabthisforme.activity.fragment_misc.default_entry.view.BlankFragmentDirections
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentCommunityViewpager2Binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentCommunityViewPager2 : Fragment() {
    private var _binding: FragmentCommunityViewpager2Binding? = null
    private val binding get() = _binding!!

    private val viewModel: CommunityViewModel by viewModels()
    private lateinit var adapter: CommunityVP2_RVAdapter

    companion object {
        fun newInstance(title: String, mode: String, categoryKey: String?): FragmentCommunityViewPager2 {
            return FragmentCommunityViewPager2().apply {
                arguments = Bundle().apply {
                    putString(CommunityFeedArgs.TITLE, title)
                    putString(CommunityFeedArgs.MODE, mode)
                    putString(CommunityFeedArgs.CATEGORY_KEY, categoryKey)
                }
            }
        }
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
                val imageUris = post.imageUrls
                if (imageUris.isEmpty()) return@CommunityVP2_RVAdapter
                val initialIndex = clickedPosition.coerceIn(0, imageUris.lastIndex)
                PhotoPreviewDialog
                    .newInstance(imageUris, initialIndex)
                    .show(childFragmentManager, "PhotoPreviewDialog")
            }
        )
        binding.rvTask.adapter = adapter
        binding.rvTask.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTask.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                if (lastVisible >= adapter.itemCount - 4) {
                    viewModel.loadMore()
                }
            }
        })
    }

    private fun initObserve() {
        viewModel.postList.observe(viewLifecycleOwner) { posts ->
            adapter.submitList(posts)
        }
        viewModel.emptyMessage.observe(viewLifecycleOwner) { message ->
            binding.tvEmptyHint.text = message
        }
        viewModel.emptyVisible.observe(viewLifecycleOwner) { visible ->
            binding.tvEmptyHint.isVisible = visible
            binding.rvTask.isVisible = !visible
        }
        viewModel.initialLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
