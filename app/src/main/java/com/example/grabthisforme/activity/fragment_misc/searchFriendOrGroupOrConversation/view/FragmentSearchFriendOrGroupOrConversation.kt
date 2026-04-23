package com.example.grabthisforme.activity.fragment_misc.searchFriendOrGroupOrConversation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.fragment_misc.searchFriendOrGroupOrConversation.viewModel.SearchFriendOrGroupOrConversationFactory
import com.example.grabthisforme.activity.fragment_misc.searchFriendOrGroupOrConversation.viewModel.SearchFriendOrGroupOrConversationViewModel
import com.example.grabthisforme.activity.homeFragment.adapter.SearchHistoryRecyclerViewAdapter
import com.example.grabthisforme.databinding.FragmentSearchFriendGroupConversationBinding
import com.example.grabthisforme.model.AppDataBase.AppDatabase


class FragmentSearchFriendOrGroupOrConversation : Fragment() {
    private var _binding: FragmentSearchFriendGroupConversationBinding? = null
    private lateinit var searchAdapterHistory : SearchHistoryRecyclerViewAdapter
    private val binding get() = _binding!!
    lateinit var viewModel: SearchFriendOrGroupOrConversationViewModel
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
        _binding = FragmentSearchFriendGroupConversationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val searchHistoryDao = AppDatabase.getInstance(requireContext()).searchDao()
        val factory = SearchFriendOrGroupOrConversationFactory(searchHistoryDao)
        viewModel = ViewModelProvider(this,factory)[SearchFriendOrGroupOrConversationViewModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        initView()
        initObserve()
        initListener()
    }


    private fun initView() {
        viewModel.loadSearchHistory()
        searchAdapterHistory = SearchHistoryRecyclerViewAdapter() { searchContent ->
            if (viewModel.deleteMode.value == true) {
                viewModel.deleteByContent(searchContent.content)
            }else{
                viewModel.addSearchHistory(searchContent.content)
                viewModel.deleteHistory(searchContent.content)
                viewModel.clearSearchInput()
            }
        }
        setOnItemComponentShowListener(searchAdapterHistory)
        binding.rvHistory.layoutManager =  GridLayoutManager(context, 2)
        binding.rvHistory.adapter = searchAdapterHistory
    }
    private fun initObserve(){
        viewModel.searchHistoryList.observe(viewLifecycleOwner){list ->
            searchAdapterHistory.submitList(ArrayList(list))
        }
        viewModel.isExpanded.observe(viewLifecycleOwner){
            viewModel.refreshLimitedList()
            if (it){
                binding.ivExpand.setImageResource(R.drawable.ic_pull_up)
            }else{
                binding.ivExpand.setImageResource(R.drawable.ic_dropdown)
            }
        }

    }

    private fun initListener() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.llSearch.setOnClickListener {
            val searchInput = binding.etSearch.text?.toString()?.trim() ?: ""
            viewModel.addSearchHistory(searchInput)
            viewModel.clearSearchInput()
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
    }



    override fun onResume() {
        super.onResume()
        (requireActivity()as MainActivity).innerBottomBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchAdapterHistory.onComponentShowChanged(false)
        _binding = null
    }
}