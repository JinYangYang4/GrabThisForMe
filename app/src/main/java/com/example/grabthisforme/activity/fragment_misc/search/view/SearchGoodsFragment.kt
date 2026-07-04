package com.example.grabthisforme.activity.fragment_misc.search.view

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.search.adapter.SearchHistoryRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.search.adapter.SearchRecommendationViewPager2Adapter
import com.example.grabthisforme.activity.fragment_misc.search.viewmodel.SearchViewModel
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentSearchGoodsBinding
import com.example.grabthisforme.ui.liquidglass.components.LiquidGlassActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class SearchGoodsFragment : Fragment() {

    private lateinit var searchAdapterHistory: SearchHistoryRecyclerViewAdapter
    private var _binding: FragmentSearchGoodsBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SearchViewModel by viewModels()
    private var pageChangeCallback: ViewPager2.OnPageChangeCallback? = null

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
        _binding = FragmentSearchGoodsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = sharedViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerViewHistory()
        observeSearchHistory()
        initSearchHistory()
        initClickListener()
        initSearchButton()
        initViewPager2()
        observeSearchItemClick()
    }

    private fun observeSearchHistory() {
        sharedViewModel.searchHistoryList.observe(viewLifecycleOwner) { list ->
            searchAdapterHistory.submitList(ArrayList(list))
            sharedViewModel.setDeleteMode(false)
        }
    }

    private fun observeSearchItemClick() {
        sharedViewModel.selectedSearchContent.observe(viewLifecycleOwner) { searchContent ->
            searchContent ?: return@observe
            sharedViewModel.addSearchHistory(searchContent.content)
            sharedViewModel.deleteHistory(searchContent.content)
            sharedViewModel.clearSearchInput()
        }
    }

    private fun initSearchHistory() {
        sharedViewModel.loadSearchHistory()
    }

    private fun initSearchButton() {
        binding.llSearch.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.llSearch.setContent {
            SearchActionButton {
                (requireActivity() as MainActivity)
                    .intentToMiscFragment(R.id.action_searchGoodsFragment_to_goodsFragment)
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

    @SuppressLint("ClickableViewAccessibility")
    private fun initViewPager2() {
        val viewPager2 = binding.viewpager2
        viewPager2.adapter = SearchRecommendationViewPager2Adapter(this)
        viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager2.offscreenPageLimit = 3

        val sidePeek = dp2px(26)
        val pageMargin = dp2px(12)
        val recyclerView = viewPager2.getChildAt(0) as RecyclerView
        recyclerView.apply {
            clipToPadding = false
            clipChildren = false
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            setPadding(sidePeek, 0, sidePeek, 0)
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val position = parent.getChildAdapterPosition(view)
                    val itemCount = parent.adapter?.itemCount ?: 0
                    outRect.left = if (position == 0) 0 else pageMargin / 2
                    outRect.right = if (position == itemCount - 1) 0 else pageMargin / 2
                }
            })
        }

        viewPager2.setPageTransformer(
            CompositePageTransformer().apply {
                addTransformer(MarginPageTransformer(pageMargin))
                addTransformer { page, position ->
                    val offset = abs(position).coerceAtMost(1f)
                    page.translationX = -pageMargin * position
                    page.scaleX = 1f - (0.03f * offset)
                    page.scaleY = 1f - (0.07f * offset)
                    page.alpha = 0.76f + ((1f - offset) * 0.24f)
                }
            }
        )

        val titles = FragmentRecommendation.RecommendationCategory.entries.map { it.title }
        TabLayoutMediator(binding.tabLayout, binding.viewpager2) { tab, position ->
            val customView = LayoutInflater.from(requireContext())
                .inflate(R.layout.tab_pill_item, binding.tabLayout, false)
            val textView = customView.findViewById<TextView>(R.id.tab_text)
            textView.text = titles[position]
            tab.customView = customView
        }.attach()

        binding.tabLayout.post {
            applyTabSpacing()
        }


        pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                for (i in 0 until tabCount) {
                    val tab = binding.tabLayout.getTabAt(i)
                    val textView = tab?.customView?.findViewById<TextView>(R.id.tab_text)
                    textView?.background?.alpha = if (i == position) 255 else 0
                }
            }
            val tabCount = binding.tabLayout.tabCount
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                val currentTab = binding.tabLayout.getTabAt(position)
                val nextTab = if (position + 1 < tabCount)binding.tabLayout.getTabAt(position + 1) else null
                val currentTextView = currentTab?.customView?.findViewById<TextView>(R.id.tab_text)
                val nextTextView = nextTab?.customView?.findViewById<TextView>(R.id.tab_text)
                currentTextView?.background?.alpha = ((1 - positionOffset) * 255).toInt()
                nextTextView?.background?.alpha = (positionOffset * 255).toInt()
            }
        }
        pageChangeCallback?.let(viewPager2::registerOnPageChangeCallback)
    }

    private fun applyTabSpacing() {
        val tabMargin = dp2px(8)
        for (index in 0 until binding.tabLayout.tabCount) {
            val tab = binding.tabLayout.getTabAt(index) ?: continue
            val params = tab.view.layoutParams as? ViewGroup.MarginLayoutParams ?: continue
            params.marginEnd = tabMargin
            tab.view.layoutParams = params
        }
    }


    private fun dp2px(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    private fun initRecyclerViewHistory() {
        searchAdapterHistory = SearchHistoryRecyclerViewAdapter { searchContent ->
            if (sharedViewModel.deleteMode.value == true) {
                sharedViewModel.deleteByContent(searchContent.content)
            } else {
                sharedViewModel.addSearchHistory(searchContent.content)
                sharedViewModel.deleteHistory(searchContent.content)
                sharedViewModel.clearSearchInput()
            }
        }
        setOnItemComponentShowListener(searchAdapterHistory)
        binding.rvHistory.adapter = searchAdapterHistory
        binding.rvHistory.layoutManager = GridLayoutManager(requireContext(), 2).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        binding.rvHistory.itemAnimator = null
        binding.rvHistory.isNestedScrollingEnabled = false
        binding.rvHistory.addItemDecoration(object : RecyclerView.ItemDecoration() {
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
        })
    }

    private fun initClickListener() {
        binding.ivBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        binding.ivClear.setOnClickListener {
            sharedViewModel.setDeleteMode(true)
            onItemComponentShowListener?.onComponentShowChanged(true)
        }
        binding.tvDeleteAll.setOnClickListener {
            sharedViewModel.clearAllHistories()
            sharedViewModel.setDeleteMode(false)
            onItemComponentShowListener?.onComponentShowChanged(false)
        }
        binding.tvComplete.setOnClickListener {
            sharedViewModel.setDeleteMode(false)
            onItemComponentShowListener?.onComponentShowChanged(false)
        }
    }

    private fun submitSearchInput() {
        val searchInput = binding.etSearch.text?.toString()?.trim() ?: ""
        sharedViewModel.addSearchHistory(searchInput)
        sharedViewModel.clearSearchInput()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).innerBottomBar()
    }

    override fun onDestroyView() {
        pageChangeCallback?.let(binding.viewpager2::unregisterOnPageChangeCallback)
        pageChangeCallback = null
        onItemComponentShowListener?.onComponentShowChanged(false)
        onItemComponentShowListener = null
        super.onDestroyView()
        _binding = null
    }
}

