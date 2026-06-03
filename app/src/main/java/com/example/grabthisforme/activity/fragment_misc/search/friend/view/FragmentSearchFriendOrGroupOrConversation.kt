package com.example.grabthisforme.activity.fragment_misc.search.friend.view

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.default_entry.view.BlankFragmentDirections
import com.example.grabthisforme.activity.fragment_misc.search.adapter.SearchHistoryRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.search.friend.adapter.SearchFriendOrGroupResultAdapter
import com.example.grabthisforme.activity.fragment_misc.search.friend.viewmodel.SearchFriendOrGroupOrConversationViewModel
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentSearchFriendGroupConversationBinding
import com.example.grabthisforme.ui.liquidglass.components.LiquidGlassActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentSearchFriendOrGroupOrConversation : Fragment() {
    private var _binding: FragmentSearchFriendGroupConversationBinding? = null
    private lateinit var searchAdapterHistory: SearchHistoryRecyclerViewAdapter
    private lateinit var searchResultAdapter: SearchFriendOrGroupResultAdapter
    private val binding get() = _binding!!
    private val viewModel: SearchFriendOrGroupOrConversationViewModel by viewModels()

    interface OnItemComponentShowListener {
        fun onComponentShowChanged(isShow: Boolean)
    }

    private var onItemComponentShowListener: OnItemComponentShowListener? = null

    fun setOnItemComponentShowListener(listener: OnItemComponentShowListener) {
        onItemComponentShowListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchFriendGroupConversationBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initSearchButton()
        initObserve()
        initListener()
    }

    private fun initView() {
        viewModel.loadSearchHistory()

        searchAdapterHistory = SearchHistoryRecyclerViewAdapter { searchContent ->
            if (viewModel.deleteMode.value == true) {
                viewModel.deleteByContent(searchContent.content)
            } else {
                binding.etSearch.setText(searchContent.content)
                binding.etSearch.setSelection(searchContent.content.length)
                viewModel.addSearchHistory(searchContent.content)
                viewModel.updateSearchInput(searchContent.content)
            }
        }
        setOnItemComponentShowListener(searchAdapterHistory)
        binding.rvHistory.layoutManager = GridLayoutManager(context, 2)
        binding.rvHistory.adapter = searchAdapterHistory
        binding.rvHistory.itemAnimator = null
        binding.rvHistory.isNestedScrollingEnabled = false
        binding.rvHistory.addItemDecoration(buildGridSpacingDecoration())

        searchResultAdapter = SearchFriendOrGroupResultAdapter(
            onItemClick = { stableId -> viewModel.onSearchResultClicked(stableId) },
            onActionClick = { stableId -> viewModel.onSearchResultActionClicked(stableId) }
        )
        binding.rvSearchResult.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchResult.adapter = searchResultAdapter
        binding.rvSearchResult.itemAnimator = null
        binding.rvSearchResult.isNestedScrollingEnabled = false
    }

    private fun initSearchButton() {
        binding.llSearch.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.llSearch.setContent {
            SearchActionButton {
                submitSearchInput()
            }
        }
    }

    @Composable
    private fun SearchActionButton(onClick: () -> Unit) {
        LiquidGlassActionButton(
            text = "搜索",
            onClick = onClick,
            modifier = Modifier.widthIn(min = 88.dp),
            tint = Color(0x660C93FF),
            surfaceColor = Color(0x6626D6FF)
        )
    }

    private fun initObserve() {
        viewModel.searchHistoryList.observe(viewLifecycleOwner) { list ->
            searchAdapterHistory.submitList(ArrayList(list))
        }
        viewModel.isExpanded.observe(viewLifecycleOwner) {
            viewModel.refreshLimitedList()
            if (it) {
                binding.ivExpand.setImageResource(R.drawable.ic_pull_up)
            } else {
                binding.ivExpand.setImageResource(R.drawable.ic_dropdown)
            }
        }
        viewModel.searchResultList.observe(viewLifecycleOwner) { list ->
            searchResultAdapter.submitList(list)
        }
        viewModel.searchResultExpanded.observe(viewLifecycleOwner) { expanded ->
            binding.ivResultExpand.setImageResource(
                if (expanded) R.drawable.ic_pull_up else R.drawable.ic_dropdown
            )
        }
        viewModel.openUserDetailId.observe(viewLifecycleOwner) { userId ->
            if (userId == null || userId <= 0L) return@observe
            val action = FragmentSearchFriendOrGroupOrConversationDirections
                .actionFragmentSearchFriendOrGroupOrConversationToUserDetailFragment(userId)
            findNavController().navigate(action)
            viewModel.onUserDetailNavigationConsumed()
        }
        viewModel.openGroupDetailId.observe(viewLifecycleOwner) { groupId ->
            if (groupId == null || groupId <= 0L) return@observe
            val action = FragmentSearchFriendOrGroupOrConversationDirections
                .actionFragmentSearchFriendOrGroupOrConversationToGroupDetailFragment(groupId)
            findNavController().navigate(action)
            viewModel.onGroupDetailNavigationConsumed()
        }
        viewModel.openConversationId.observe(viewLifecycleOwner) { conversationId ->
            if (conversationId.isNullOrBlank()) return@observe
            val action = BlankFragmentDirections.actionBlankFragmentToFragmentChat(conversationId)
            (requireActivity() as MainActivity).NewNavController_navgite(action)
            viewModel.onConversationNavigationConsumed()
        }
    }

    private fun initListener() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.llExpand.setOnClickListener {
            viewModel.setExpand(!(viewModel.isExpanded.value ?: false))
            viewModel.refreshLimitedList()
        }
        binding.tvDeleteAll.setOnClickListener {
            viewModel.clearAllHistories()
            viewModel.setDeleteMode(false)
            searchAdapterHistory.onComponentShowChanged(false)
        }

        binding.tvComplete.setOnClickListener {
            viewModel.setDeleteMode(false)
            searchAdapterHistory.onComponentShowChanged(false)
        }

        binding.ivClear.setOnClickListener {
            viewModel.setDeleteMode(true)
            searchAdapterHistory.onComponentShowChanged(true)
        }

        binding.etSearch.doAfterTextChanged { editable ->
            viewModel.updateSearchInput(editable?.toString().orEmpty())
        }

        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            val searchText = binding.etSearch.text.toString().trim()
            if (searchText.isNotEmpty()) {
                submitSearchInput()
            }
            true
        }

        binding.llResultExpand.setOnClickListener {
            viewModel.toggleSearchResultExpanded()
        }
    }

    private fun buildGridSpacingDecoration(): RecyclerView.ItemDecoration {
        return object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                outRect.top = if (position < 2) 0 else dp2px(10)
                if (position % 2 == 0) {
                    outRect.right = dp2px(6)
                } else {
                    outRect.left = dp2px(6)
                }
            }
        }
    }

    private fun dp2px(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    private fun submitSearchInput() {
        val searchInput = binding.etSearch.text?.toString()?.trim() ?: ""
        viewModel.addSearchHistory(searchInput)
        viewModel.updateSearchInput(searchInput)
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).innerBottomBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchAdapterHistory.onComponentShowChanged(false)
        _binding = null
    }
}
