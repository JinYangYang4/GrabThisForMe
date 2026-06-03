package com.example.grabthisforme.activity.fragment_misc.search.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.search.adapter.SearchRecommendationSpacingDecoration
import com.example.grabthisforme.activity.fragment_misc.search.adapter.SearchRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.search.model.SearchContent
import com.example.grabthisforme.activity.fragment_misc.search.viewmodel.SearchViewModel
import com.example.grabthisforme.databinding.FragmentSearchRecommendationBinding

class FragmentRecommendation : Fragment() {
    private val sharedViewModel: SearchViewModel by viewModels({ requireParentFragment() })

    companion object {
        const val ARG_POSITION = "arg_position"

        fun newInstance(position: Int): FragmentRecommendation {
            val fragment = FragmentRecommendation()
            val args = Bundle()
            args.putInt(ARG_POSITION, position)
            fragment.arguments = args
            return fragment
        }
    }

    enum class RecommendationCategory(
        val title: String,
        val subtitle: String,
        val dataProvider: () -> List<SearchContent>,
        val backgroundRes: Int
    ) {
        GUESS_YOU_SEARCH(
            "猜你想搜",
            "校园里最近被点得最多的高频关键词",
            SearchContent.SearchRecommendations::getGuessYouSearch,
            R.drawable.solid_color_background_1
        ),
        DIGITAL_PRODUCTS(
            "数码好物",
            "上课、宿舍和通勤场景都常搜的设备",
            SearchContent.SearchRecommendations::getDigitalProducts,
            R.drawable.solid_color_background_2
        ),
        CLOTHING_SHOES(
            "服饰穿搭",
            "适合学生党日常穿搭的轻松选择",
            SearchContent.SearchRecommendations::getClothingShoes,
            R.drawable.solid_color_background_3
        ),
        HOME_SUPPLIES(
            "宿舍日用",
            "更贴近宿舍收纳和日常生活的常搜词",
            SearchContent.SearchRecommendations::getHomeSupplies,
            R.drawable.solid_color_background_4
        ),
        BOOKS_STATIONERY(
            "书籍文具",
            "备考、上课和记笔记常用的搜索入口",
            SearchContent.SearchRecommendations::getBooksStationery,
            R.drawable.solid_color_background_5
        ),
        BEAUTY_SKINCARE(
            "美妆护肤",
            "清爽、实用、适合日常囤货的热搜词",
            SearchContent.SearchRecommendations::getBeautySkincare,
            R.drawable.solid_color_background_6
        ),
        SPORTS_EQUIPMENT(
            "运动器材",
            "健身、球类和跑步装备都集中在这里",
            SearchContent.SearchRecommendations::getSportsEquipment,
            R.drawable.solid_color_background_7
        ),
        FOOD_PRODUCTS(
            "零食饮品",
            "深夜加餐和宿舍囤货里最常见的关键词",
            SearchContent.SearchRecommendations::getFoodProducts,
            R.drawable.solid_color_background_8
        );

        companion object {
            fun fromPosition(position: Int): RecommendationCategory {
                return entries.getOrElse(position) { GUESS_YOU_SEARCH }
            }
        }
    }

    private var _binding: FragmentSearchRecommendationBinding? = null
    private val binding get() = _binding!!
    private lateinit var searchAdapter: SearchRecyclerViewAdapter
    private var currentPosition = 0
    private val maxVisibleItems = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentPosition = it.getInt(ARG_POSITION, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchRecommendationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        loadData()
    }

    private fun initView() {
        val category = RecommendationCategory.fromPosition(currentPosition)
        binding.tvBadge.text = "校园热搜"
        binding.tvTitle.text = category.title
        binding.tvSubtitle.text = category.subtitle
        searchAdapter = SearchRecyclerViewAdapter { searchContent ->
            sharedViewModel.onSearchItemClick(searchContent)
        }
        binding.rvSearch.apply {
            adapter = searchAdapter
            layoutManager = GridLayoutManager(requireContext(), 1)
            isNestedScrollingEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            if (itemDecorationCount == 0) {
                addItemDecoration(
                    SearchRecommendationSpacingDecoration(
                        spanCount = 1,
                        spacing = resources.getDimensionPixelSize(R.dimen.search_recommendation_grid_spacing)
                    )
                )
            }
        }
    }

    private fun loadData() {
        val category = RecommendationCategory.fromPosition(currentPosition)
        val dataList = category.dataProvider.invoke().take(maxVisibleItems)
        binding.ivBackdrop.setImageResource(category.backgroundRes)
        binding.tvCount.text = "${dataList.size} 个热搜"
        searchAdapter.submitList(dataList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
