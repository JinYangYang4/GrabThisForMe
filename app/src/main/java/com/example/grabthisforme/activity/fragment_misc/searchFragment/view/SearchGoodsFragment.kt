package com.example.grabthisforme.activity.fragment_misc.searchFragment.view

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.fragment_misc.searchFragment.adapter.SearchRecommendationViewPager2Adapter
import com.example.grabthisforme.activity.fragment_misc.searchFragment.viewModel.SearchViewModel
import com.example.grabthisforme.activity.homeFragment.adapter.SearchHistoryRecyclerViewAdapter
import com.example.grabthisforme.databinding.FragmentSearchGoodsBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchGoodsFragment : Fragment(){
    private val TOTAL_PAGE_COUNT = 8
    private val FIRST_TARGET_PAGE = 7
    private val LAST_PAGE_POSITION = TOTAL_PAGE_COUNT - 1


    private var leftPadding : Int = 0
    private var rightPadding : Int = 0
    private lateinit var searchAdapterHistory : SearchHistoryRecyclerViewAdapter
    private var _binding: FragmentSearchGoodsBinding? = null
    private val binding get() = _binding!!
    private  val sharedViewModel: SearchViewModel by viewModels()

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
        initViewPager2()
        observeSearchItemClick()
    }
    private fun observeSearchHistory() {
        sharedViewModel.searchHistoryList.observe(viewLifecycleOwner) { list ->
            searchAdapterHistory.submitList(ArrayList(list))
            Log.d("test11", "observeSearchHistory: ${list.size}")
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
        sharedViewModel. loadSearchHistory()
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun initViewPager2() {
        val viewPager2 = binding.viewpager2
        viewPager2.adapter = SearchRecommendationViewPager2Adapter(this)
        viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager2.offscreenPageLimit = 2
        val fullPageWidth = dp2px(150)
        val sidePageWidth = fullPageWidth / 2
        val pageMargin = dp2px(10)
        val recyclerView = viewPager2.getChildAt(0) as RecyclerView
        recyclerView.apply {
            clipToPadding = false
            clipChildren = false
            setPadding(0, 0, sidePageWidth, 0)
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    outRect.left = pageMargin / 2
                    outRect.right = pageMargin / 2
                }
            })
        }


        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                recyclerView.setPadding(
                    leftPadding,
                    recyclerView.paddingTop,
                    rightPadding,
                    recyclerView.paddingBottom
                )

            }
            override fun onPageSelected(position: Int) {
                leftPadding = if (position == FIRST_TARGET_PAGE) sidePageWidth else 0
                rightPadding = if (position == LAST_PAGE_POSITION) 0 else sidePageWidth
                super.onPageSelected(position)
                Log.d("test11", "onPageSelected: $position")
                recyclerView.requestLayout()
            }
        })
        val titles = listOf(
            "猜你想搜",
            "数码产品",
            "服饰鞋帽",
            "家居用品",
            "图书文具",
            "美妆护肤",
            "运动器材",
            "食品"
        )
        TabLayoutMediator(binding.tabLayout, binding.viewpager2) { tab, position ->
            tab.text = titles[position]
        }.attach()

    }
    private fun dp2px(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }
    private fun initRecyclerViewHistory() {
        searchAdapterHistory = SearchHistoryRecyclerViewAdapter(){ searchContent ->
            if (sharedViewModel.deleteMode.value == true){
                sharedViewModel.deleteByContent(searchContent.content)
            }else{
                sharedViewModel.addSearchHistory(searchContent.content)
                sharedViewModel.deleteHistory(searchContent.content)
                sharedViewModel.clearSearchInput()
            }
        }
        setOnItemComponentShowListener(searchAdapterHistory)
        binding.rvHistory.adapter = searchAdapterHistory
        val gridLayoutManager = GridLayoutManager(requireContext(), 2).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        binding.rvHistory.layoutManager = gridLayoutManager

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
        binding.llSearch.setOnClickListener {
            val searchInput = binding.etSearch.text?.toString()?.trim() ?: ""
            sharedViewModel.addSearchHistory(searchInput)
            sharedViewModel.clearSearchInput()
        }
    }





    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).innerBottomBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onItemComponentShowListener?.onComponentShowChanged(false)
        onItemComponentShowListener = null
        _binding = null
    }
}