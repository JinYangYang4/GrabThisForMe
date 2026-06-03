package com.example.grabthisforme.activity.fragment_misc.storeFragment.view

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.fragment_misc.storeFragment.viewModel.StoreViewModel
import com.example.grabthisforme.activity.fragment_misc.search.adapter.SearchHistoryRecyclerViewAdapter
import com.example.grabthisforme.databinding.FragmentStoreSearchBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoreSearchFragment : Fragment() {
    private var _binding: FragmentStoreSearchBinding? = null
    private val binding get() = _binding!!
    private val storeViewModel: StoreViewModel by activityViewModels()
    private lateinit var searchHistoryAdapter:  SearchHistoryRecyclerViewAdapter
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
        _binding = FragmentStoreSearchBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = storeViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        observeSearchHistory()
        storeViewModel.loadSearchHistory()
        initClickListener()
    }

    private fun initRecyclerView() {
        searchHistoryAdapter = SearchHistoryRecyclerViewAdapter(){ searchContent ->
            if (storeViewModel.deleteMode.value == true){
                storeViewModel.deleteByContent(searchContent.content)
            }else{
                storeViewModel.addSearchHistory(searchContent.content)
                storeViewModel.deleteHistory(searchContent.content)
                storeViewModel.clearSearchInput()
            }
        }
        setOnItemComponentShowListener(searchHistoryAdapter)
        binding.rvHistory.adapter = searchHistoryAdapter
        val gridLayoutManager = GridLayoutManager(requireContext(), 2).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        binding.rvHistory.layoutManager = gridLayoutManager
        binding.rvHistory.itemAnimator = null
    }
    private fun initClickListener() {
        binding.ivBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        binding.ivClear.setOnClickListener {
            storeViewModel.setDeleteMode(true)
            onItemComponentShowListener?.onComponentShowChanged(true)
        }
        binding.tvDeleteAll.setOnClickListener {
            storeViewModel.clearAllHistories()
            storeViewModel.setDeleteMode(false)
            onItemComponentShowListener?.onComponentShowChanged(false)
        }
        binding.tvComplete.setOnClickListener {
            storeViewModel.setDeleteMode(false)
            onItemComponentShowListener?.onComponentShowChanged(false)
        }
        binding.llSearch.setOnClickListener {
            val searchInput = binding.etSearch.text?.toString()?.trim() ?: ""
            storeViewModel.addSearchHistory(searchInput)
            storeViewModel.clearSearchInput()
        }
    }
    private fun observeSearchHistory() {
        storeViewModel.searchHistoryList.observe(viewLifecycleOwner) { list ->
            searchHistoryAdapter.submitList(ArrayList(list))
        }
    }
    override fun onResume() {
        super.onResume()
        (requireActivity()as MainActivity).innerBottomBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
