package com.example.grabthisforme.activity.fragment_misc.searchFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.searchFragment.adapter.SearchRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.searchFragment.model.SearchContent
import com.example.grabthisforme.activity.fragment_misc.searchFragment.viewModel.SearchViewModel
import com.example.grabthisforme.activity.homeFragment.adapter.SearchHistoryRecyclerViewAdapter
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
        val dataProvider: () -> List<SearchContent>,
        val backgroundRes: Int
    ) {
        GUESS_YOU_SEARCH(
            "猜你想搜",
            SearchContent.SearchRecommendations::getGuessYouSearch,
            R.drawable.solid_color_background_1
        ),
        DIGITAL_PRODUCTS(
            "数码产品",
            SearchContent.SearchRecommendations::getDigitalProducts,
            R.drawable.solid_color_background_2
        ),
        CLOTHING_SHOES(
            "服饰鞋帽",
            SearchContent.SearchRecommendations::getClothingShoes,
            R.drawable.solid_color_background_3
        ),
        HOME_SUPPLIES(
            "家居用品",
            SearchContent.SearchRecommendations::getHomeSupplies,
            R.drawable.solid_color_background_4
        ),
        BOOKS_STATIONERY(
            "图书文具",
            SearchContent.SearchRecommendations::getBooksStationery,
            R.drawable.solid_color_background_5
        ),
        BEAUTY_SKINCARE(
            "美妆护肤",
            SearchContent.SearchRecommendations::getBeautySkincare,
            R.drawable.solid_color_background_6
        ),
        SPORTS_EQUIPMENT(
            "运动器材",
            SearchContent.SearchRecommendations::getSportsEquipment,
            R.drawable.solid_color_background_7
        ),
        FOOD_PRODUCTS(
            "食品",
            SearchContent.SearchRecommendations::getFoodProducts,
            R.drawable.solid_color_background_1
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
        binding.tvTitle.text = category.title
        searchAdapter = SearchRecyclerViewAdapter() { searchContent ->
            sharedViewModel.onSearchItemClick(searchContent)
        }
        binding.rvSearch.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            isNestedScrollingEnabled = false
        }
    }
    private fun loadData() {
        val category = RecommendationCategory.fromPosition(currentPosition)
        val dataList = category.dataProvider.invoke()
        binding.ivBack.setImageResource(category.backgroundRes)
        searchAdapter.submitList(dataList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}