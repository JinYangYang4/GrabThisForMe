package com.example.grabthisforme.activity.fragment_misc.search.community.view

import android.os.Bundle
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.fragment_misc.search.adapter.SearchHistoryRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.search.adapter.SearchRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.search.community.viewmodel.SearchCommunityViewModel
import com.example.grabthisforme.activity.fragment_misc.search.community.viewmodel.SearchCommunityViewModelFactory
import com.example.grabthisforme.activity.fragment_misc.search.model.SearchContent

import com.example.grabthisforme.databinding.FragmentSearchCommnunityBinding
import com.example.grabthisforme.model.AppDataBase.AppDatabase
import com.example.grabthisforme.ui.liquidglass.components.LiquidGlassActionButton



class SearchCommunityFragment : Fragment() {
    private var _binding: FragmentSearchCommnunityBinding? = null
    private val binding get() = _binding!!
    private var searchHistory : List<SearchContent> = emptyList()
    private var searchRecomment : List<SearchContent> = emptyList()

    private lateinit var searchAdapterHistory : SearchHistoryRecyclerViewAdapter
    private lateinit var adapter_recomment:SearchRecyclerViewAdapter
    private lateinit var ViewModel: SearchCommunityViewModel
    interface OnItemComponentShowListener {
        fun onComponentShowChanged(isShow: Boolean)
    }
    private var onItemComponentShowListener: OnItemComponentShowListener? = null

    fun setOnItemComponentShowListener(listener: OnItemComponentShowListener) {
        this.onItemComponentShowListener = listener
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchCommnunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val searchHistoryDao = AppDatabase.getInstance(requireContext()).searchDao()
        val factory = SearchCommunityViewModelFactory(searchHistoryDao)
        ViewModel = ViewModelProvider(this,factory)[SearchCommunityViewModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = ViewModel

        initViews()
        initClickEvents()
        initViewModel()
        initRecyclerView()
        initSearchButton()
        initAiEntryButton()
        observeViewModelData()
    }
    fun initViewModel(){
        ViewModel.initSearchRecomment()
        ViewModel.loadSearchHistory()
    }
    private fun observeViewModelData() {
        ViewModel.searchRecomment.observe(viewLifecycleOwner) { recommendList ->
            adapter_recomment.submitList(recommendList)
        }
        ViewModel.searchHistoryList.observe(viewLifecycleOwner){list ->
            searchAdapterHistory.submitList(ArrayList(list))
        }

    }

    private fun initViews() {
        ViewModel.setDeleteMode(false)
    }

    private fun initAiEntryButton() {
        binding.llIntentToAi.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.llIntentToAi.setContent {
            IntentToAiButton()
        }
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

    private fun initClickEvents() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.ivClear.setOnClickListener {
            ViewModel.setDeleteMode(true)
            searchAdapterHistory.onComponentShowChanged(true)
        }

        binding.tvDeleteAll.setOnClickListener {
            deleteAllHistory()
        }

        binding.tvComplete.setOnClickListener {
            ViewModel.setDeleteMode(false)
            searchAdapterHistory.onComponentShowChanged(false)
        }

        binding.ivRefresh.setOnClickListener {
            refreshRecommendList()
        }

        binding.ivInner.setOnClickListener {
            ViewModel.toggleRecommendVisible()
        }


        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            val searchText = binding.etSearch.text.toString().trim()
            if (searchText.isNotEmpty()) {
                submitSearchInput()
            }
            true
        }
    }


    private fun initRecyclerView() {
        binding.rvHistory.layoutManager = GridLayoutManager(context, 2)
        searchAdapterHistory = SearchHistoryRecyclerViewAdapter() { searchContent ->
            if (ViewModel.deleteMode.value == true) {
                ViewModel.deleteByContent(searchContent.content)
            }else{
                ViewModel.addSearchHistory(searchContent.content)
                ViewModel.deleteHistory(searchContent.content)
                ViewModel.clearSearchInput()
            }
        }
        setOnItemComponentShowListener(searchAdapterHistory)

        binding.rvHistory.adapter = searchAdapterHistory
        binding.rvHistory.itemAnimator = null
        binding.rvHistory.isNestedScrollingEnabled = false
        binding.rvHistory.addItemDecoration(buildGridSpacingDecoration())
        searchAdapterHistory.submitList(ViewModel.searchHistoryList.value)

        binding.rvRecomment.layoutManager = GridLayoutManager(context, 2)
        adapter_recomment = SearchRecyclerViewAdapter(){searchContent ->
            ViewModel.addSearchHistory(searchContent.content)
            ViewModel.deleteHistory(searchContent.content)
            ViewModel.clearSearchInput()
        }
        binding.rvRecomment.adapter = adapter_recomment
        binding.rvRecomment.itemAnimator = null
        binding.rvRecomment.isNestedScrollingEnabled = false
        binding.rvRecomment.addItemDecoration(buildGridSpacingDecoration())
        searchRecomment = ViewModel.searchRecomment.value!!
        adapter_recomment.submitList(searchRecomment)
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
        ViewModel.addSearchHistory(searchInput)
        ViewModel.clearSearchInput()
    }

    private fun deleteAllHistory() {
        ViewModel.clearAllHistories()
        ViewModel.setDeleteMode(false)
        searchAdapterHistory.onComponentShowChanged(false)
    }
    private fun refreshRecommendList() {
    }

    @Composable
    private fun IntentToAiButton() {
        LiquidGlassActionButton(
            text = "让 AI 帮我想",
            onClick = {},
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            tint = Color(0x660C93FF),
            surfaceColor = Color(0x6626D6FF)
        )
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).innerBottomBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchAdapterHistory.onComponentShowChanged(false)
        onItemComponentShowListener = null
        _binding = null
    }
}
