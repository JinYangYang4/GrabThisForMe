package com.example.grabthisforme.activity.fragment_misc.searchFriendOrGroupOrConversation.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.MainActivity.view.MainActivity
import com.example.grabthisforme.activity.fragment_misc.searchFriendOrGroupOrConversation.viewModel.SearchFriendOrGroupOrConversationFactory
import com.example.grabthisforme.activity.fragment_misc.searchFriendOrGroupOrConversation.viewModel.SearchFriendOrGroupOrConversationViewModel
import com.example.grabthisforme.activity.homeFragment.adapter.SearchHistoryRecyclerViewAdapter
import com.example.grabthisforme.databinding.FragmentSearchFriendGroupConversationBinding
import com.example.grabthisforme.model.AppDataBase.AppDatabase


class FragmentSearchFriendOrGroupOrConversation : Fragment() {
    private var _binding: FragmentSearchFriendGroupConversationBinding? = null
    private var searchInput : String = ""
    private lateinit var searchAdapterHistory : SearchHistoryRecyclerViewAdapter
    private val binding get() = _binding!!
    private var isShow = false
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
        initView()
        initObserve()
        initListener()
    }


    private fun initView() {
        viewModel.loadSearchHistory()
        searchAdapterHistory = SearchHistoryRecyclerViewAdapter() { searchContent ->
            if (isShow) {
                viewModel.deleteByContent(searchContent.content)
            }else{
                searchInput = searchContent.content
                viewModel.addSearchHistory(searchInput)
                viewModel.deleteHistory(searchContent.content)
            }
        }
        setOnItemComponentShowListener(searchAdapterHistory)
        binding.rvHistory.layoutManager =  GridLayoutManager(context, 2)
        binding.rvHistory.adapter = searchAdapterHistory
    }
    private fun initObserve(){
        viewModel.searchHistoryList.observe(viewLifecycleOwner){list ->
            if (list.size == 0){
                binding.tvNotHistory.visibility = View.VISIBLE
            }else{
                binding.tvNotHistory.visibility = View.GONE
            }
            searchAdapterHistory.submitList(ArrayList(list))
        }
        viewModel.isExpanded.observe(viewLifecycleOwner){
            if (it){
                binding.tvExpand.text = "收起"
                binding.ivExpand.setImageResource(R.drawable.ic_pull_up)
            }else{
                binding.tvExpand.text = "展开"
                binding.ivExpand.setImageResource(R.drawable.ic_dropdown)
            }
            viewModel.refreshLimitedList()
        }
    }

    private fun initListener() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.llSearch.setOnClickListener {
            binding.llSearch.setOnClickListener {
                searchInput = binding.etSearch.text?.toString()?.trim() ?: ""
                viewModel.addSearchHistory(searchInput)
                binding.etSearch.text = null
            }
        }
        binding.llExpand.setOnClickListener {
            Log.d("test11", "initListener: ")
            if (viewModel.isExpanded.value!!) {
                binding.tvExpand.text = "收起"
                viewModel.setExpand(false)
            } else {
                viewModel.setExpand(true)
            }
        }
        binding.tvDeleteAll.setOnClickListener {
            isShow = false
            viewModel.clearAllHistories()
            searchAdapterHistory.onComponentShowChanged(isShow)
            binding.llDelete.visibility = View.GONE
        }

        binding.tvComplete.setOnClickListener {
            binding.llDelete.visibility = View.GONE
            isShow = false
            searchAdapterHistory.onComponentShowChanged(isShow)
        }

        binding.ivClear.setOnClickListener {
            binding.llDelete.visibility = View.VISIBLE
            isShow = true
            searchAdapterHistory.onComponentShowChanged(isShow)
        }
    }



    override fun onResume() {
        super.onResume()
        (requireActivity()as MainActivity).innerBottomBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isShow = false

        _binding = null
    }
}