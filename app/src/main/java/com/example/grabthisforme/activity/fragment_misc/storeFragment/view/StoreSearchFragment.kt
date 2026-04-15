package com.example.grabthisforme.activity.fragment_misc.storeFragment.view

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.MainActivity.view.MainActivity
import com.example.grabthisforme.activity.fragment_misc.searchFragment.model.SearchContent
import com.example.grabthisforme.activity.fragment_misc.storeFragment.viewModel.StoreViewModel
import com.example.grabthisforme.activity.homeFragment.adapter.SearchHistoryRecyclerViewAdapter
import com.example.grabthisforme.databinding.FragmentStoreSearchBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoreSearchFragment : Fragment() {
    private var _binding: FragmentStoreSearchBinding? = null
    private val binding get() = _binding!!
    private var isShow = false
    private var searchInput : String = ""
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
            if (isShow){
                storeViewModel.deleteByContent(searchContent.content)
            }else{
                searchInput = searchContent.content
                storeViewModel.addSearchHistory(searchInput)
                storeViewModel.deleteHistory(searchContent.content)
            }
        }
        setOnItemComponentShowListener(searchHistoryAdapter)
        binding.rvHistory.adapter = searchHistoryAdapter
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
            isShow = true
            binding.llDelete.visibility = View.VISIBLE
            onItemComponentShowListener?.onComponentShowChanged(isShow)
        }
        binding.tvDeleteAll.setOnClickListener {
            storeViewModel.clearAllHistories()
            isShow = false
            onItemComponentShowListener?.onComponentShowChanged(isShow)
            binding.llDelete.visibility = View.GONE
        }
        binding.tvComplete.setOnClickListener {
            isShow = false
            onItemComponentShowListener?.onComponentShowChanged(isShow)
            binding.llDelete.visibility = View.GONE
        }
        binding.llSearch.setOnClickListener {
            searchInput = binding.etSearch.text?.toString()?.trim() ?: ""
            storeViewModel.addSearchHistory(searchInput)
            binding.etSearch.text = null
        }
    }
    private fun observeSearchHistory() {
        storeViewModel.searchHistoryList.observe(viewLifecycleOwner) { list ->
            searchHistoryAdapter.submitList(ArrayList(list))
            if (list.isNullOrEmpty()) {
                binding.tvNotHistory.visibility = View.VISIBLE
                binding.rvHistory.visibility = View.GONE
                binding.ivClear.visibility = View.GONE
            } else {
                binding.tvNotHistory.visibility = View.GONE
                binding.rvHistory.visibility = View.VISIBLE
                binding.ivClear.visibility = View.VISIBLE
            }
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
