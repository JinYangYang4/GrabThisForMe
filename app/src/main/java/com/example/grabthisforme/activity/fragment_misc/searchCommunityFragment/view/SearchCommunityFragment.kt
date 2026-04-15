package com.example.grabthisforme.activity.fragment_misc.searchCommunityFragment.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.grabthisforme.activity.MainActivity.view.MainActivity
import com.example.grabthisforme.activity.fragment_misc.searchCommunityFragment.viewModle.SearchCommunityViewModel
import com.example.grabthisforme.activity.fragment_misc.searchCommunityFragment.viewModle.SearchCommunityViewModelFactory
import com.example.grabthisforme.activity.fragment_misc.searchFragment.adapter.SearchRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.searchFragment.model.SearchContent
import com.example.grabthisforme.activity.homeFragment.adapter.SearchHistoryRecyclerViewAdapter

import com.example.grabthisforme.databinding.FragmentSearchCommnunityBinding
import com.example.grabthisforme.model.AppDataBase.AppDatabase


class SearchCommunityFragment : Fragment() {
    private var _binding: FragmentSearchCommnunityBinding? = null
    private val binding get() = _binding!!
    private var searchHistory : List<SearchContent> = emptyList()
    private var searchRecomment : List<SearchContent> = emptyList()

    private lateinit var searchAdapterHistory : SearchHistoryRecyclerViewAdapter
    private lateinit var adapter_recomment:SearchRecyclerViewAdapter
    private lateinit var ViewModel: SearchCommunityViewModel
    private var searchInput : String = ""
    private var isShow = false
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

        initViews()
        initClickEvents()
        initViewModel()
        initRecyclerView()
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
            if (list.size == 0){
                binding.tvNotHistory.visibility = View.VISIBLE
            }else{
                binding.tvNotHistory.visibility = View.GONE
            }
        }

    }

    private fun initViews() {
        binding.llDelete.visibility = View.GONE
        binding.tvNotHistory.visibility = View.VISIBLE
    }

    private fun initClickEvents() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.llSearch.setOnClickListener {
            binding.llSearch.setOnClickListener {
                searchInput = binding.etSearch.text?.toString()?.trim() ?: ""
                ViewModel.addSearchHistory(searchInput)
                binding.etSearch.text = null
            }
        }

        binding.ivClear.setOnClickListener {
            isShow = true
            searchAdapterHistory.onComponentShowChanged(isShow)
            binding.llDelete.visibility = View.VISIBLE
        }

        binding.tvDeleteAll.setOnClickListener {
            deleteAllHistory()
        }

        binding.tvComplete.setOnClickListener {
            binding.llDelete.visibility = View.GONE
            isShow = false
            searchAdapterHistory.onComponentShowChanged(isShow)
        }

        binding.ivRefresh.setOnClickListener {
            refreshRecommendList()
        }

        binding.ivInner.setOnClickListener {
            val isVisible = binding.rvRecomment.visibility == View.VISIBLE
            binding.rvRecomment.visibility = if (isVisible) View.GONE else View.VISIBLE
        }


        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            val searchText = binding.etSearch.text.toString().trim()
            if (searchText.isNotEmpty()) {
                saveSearchHistory(searchText)
            }
            true
        }
    }


    private fun initRecyclerView() {
        binding.rvHistory.layoutManager = GridLayoutManager(context, 2)
        searchAdapterHistory = SearchHistoryRecyclerViewAdapter() { searchContent ->
            if (isShow) {
                ViewModel.deleteByContent(searchContent.content)
            }else{
                searchInput = searchContent.content
                ViewModel.addSearchHistory(searchInput)
                ViewModel.deleteHistory(searchContent.content)
            }
        }
        setOnItemComponentShowListener(searchAdapterHistory)

        binding.rvHistory.adapter = searchAdapterHistory
        searchAdapterHistory.submitList(ViewModel.searchHistoryList.value)

        binding.rvRecomment.layoutManager = GridLayoutManager(context, 2)
        adapter_recomment = SearchRecyclerViewAdapter(){searchContent ->
            searchInput = searchContent.content
            ViewModel.addSearchHistory(searchInput)
            ViewModel.deleteHistory(searchContent.content)
        }
        binding.rvRecomment.adapter = adapter_recomment
        searchRecomment = ViewModel.searchRecomment.value!!
        adapter_recomment.submitList(searchRecomment)
    }


    private fun saveSearchHistory(keyword: String) {
    }
    private fun deleteAllHistory() {
        isShow = false
        ViewModel.clearAllHistories()
        searchAdapterHistory.onComponentShowChanged(isShow)
        binding.llDelete.visibility = View.GONE
    }
    private fun refreshRecommendList() {
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).innerBottomBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isShow = false
        searchAdapterHistory.onComponentShowChanged(isShow)
        onItemComponentShowListener = null
        _binding = null
    }
}