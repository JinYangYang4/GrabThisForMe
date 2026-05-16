package com.example.grabthisforme.activity.fragment_misc.storeFragment.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.activity.fragment_misc.storeFragment.adpter.StoreCategoryRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.storeFragment.adpter.StoreOwnerRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.storeFragment.viewModel.StoreOwnerViewModel
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentStoreOwnerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StoreOwnerFragment : Fragment() {
    private val args : StoreOwnerFragmentArgs by navArgs()
    private var _binding: FragmentStoreOwnerBinding? = null
    private val binding get() = _binding!!

    private lateinit var goodsAdapter: StoreOwnerRecyclerViewAdapter
    private lateinit var categoryAdapter: StoreCategoryRecyclerViewAdapter
    private val viewModel: StoreOwnerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreOwnerBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadStore(args.storeId)
        initClick()
        initCategoryRecyclerView()
        initGoodsRecyclerView()
        initObserve()
        observeStoreDetails()
    }

    private fun observeStoreDetails() {
        viewModel.storeNameText.observe(viewLifecycleOwner) { binding.tvShopName.text = it }
        viewModel.storeSaleCountText.observe(viewLifecycleOwner) { binding.tvSaleCount.text = it }
        viewModel.storeAddressText.observe(viewLifecycleOwner) { binding.tvAddress.text = it }
        viewModel.storeServiceText.observe(viewLifecycleOwner) { binding.tvChatDesc.text = it }
        viewModel.storeNoticeText.observe(viewLifecycleOwner) { binding.tvNoticeContent.text = it }
        viewModel.storeDeliveryText.observe(viewLifecycleOwner) { binding.tvDelivery.text = it }
        viewModel.storeBusinessHoursText.observe(viewLifecycleOwner) { binding.tvBusinessHours.text = it }
    }

    private fun initCategoryRecyclerView() {
        categoryAdapter = StoreCategoryRecyclerViewAdapter { category, position ->
            categoryAdapter.updateSelectedPosition(position)
        }
        binding.rvCategory.layoutManager = LinearLayoutManager(context)
        binding.rvCategory.adapter = categoryAdapter
        binding.rvCategory.itemAnimator = null
    }

    private fun initClick() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.llSearch.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(com.example.grabthisforme.R.id.action_storeOwnerFragment_to_storeSearchFragment)
        }
        binding.ivAddSellGoods.setOnClickListener {
            val action = StoreOwnerFragmentDirections
                .actionStoreOwnerFragmentToCreatGoodsFragment(args.storeId)
            findNavController().navigate(action)
        }
        binding.tvAddGoods.setOnClickListener {

        }
        binding.llStore.setOnClickListener {
            viewModel.setShowStorePage(true)
        }
        binding.llAllGoods.setOnClickListener {
            viewModel.setShowStorePage(false)
        }
    }

    private fun initObserve() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.categoryList.collectLatest { categories ->
                        categoryAdapter.setCategoryList(categories)
                    }
                }
                launch {
                    viewModel.goodsList.collectLatest { goodsList ->
                        Log.d("test11", "initObserve:${goodsList.size}")
                        goodsAdapter.submitList(goodsList)
                    }
                }
            }
        }
    }

    private fun initGoodsRecyclerView() {
        goodsAdapter = StoreOwnerRecyclerViewAdapter(
            onItemClick = { _ ->
            }
        )
        binding.rvGoods.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = goodsAdapter
            itemAnimator = null
            setHasFixedSize(true)
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).innerBottomBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
