package com.example.grabthisforme.activity.fragment_misc.storeFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.storeFragment.adapter.AlreadySelectGoodsRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.storeFragment.adapter.StoreGoodsRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.storeFragment.adpter.StoreCategoryRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.storeFragment.viewModel.StoreViewModel
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentStoreBinding
import com.example.grabthisforme.extension.setMaxVisibleItems
import com.example.grabthisforme.model.store.domain.Store
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StoreFragment : Fragment() {

    private var _binding: FragmentStoreBinding? = null
    private val binding get() = _binding!!
    private val storeViewModel: StoreViewModel by activityViewModels()
    private lateinit var goodsAdapter: StoreGoodsRecyclerViewAdapter
    private lateinit var alreadySelectAdapter: AlreadySelectGoodsRecyclerViewAdapter

    private lateinit var categoryAdapter: StoreCategoryRecyclerViewAdapter
    private var currentStoreId: Long? = null

    private val args: StoreFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = storeViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storeViewModel.loadStore(args.storeId)
        initCategoryRecyclerView()
        initGoodsRecyclerView()
        initAlreadySelectRecyclerView()
        showGoods()
        initObserve()
        observeStoreDetails()
        initClickListener()
        handleBackPressed()
    }

    private fun observeStoreDetails() {
        storeViewModel.storeNameText.observe(viewLifecycleOwner) { binding.tvShopName.text = it }
        storeViewModel.storeSaleCountText.observe(viewLifecycleOwner) { binding.tvSaleCount.text = it }
        storeViewModel.storeAddressText.observe(viewLifecycleOwner) { binding.tvAddress.text = it }
        storeViewModel.storeServiceText.observe(viewLifecycleOwner) { binding.tvChatDesc.text = it }
        storeViewModel.storeNoticeText.observe(viewLifecycleOwner) { binding.tvNoticeContent.text = it }
        storeViewModel.storeDeliveryText.observe(viewLifecycleOwner) { binding.tvDelivery.text = it }
        storeViewModel.storeBusinessHoursText.observe(viewLifecycleOwner) { binding.tvBusinessHours.text = it }
        storeViewModel.priceTotalText.observe(viewLifecycleOwner) { binding.tvPriceTotal.text = it }
    }

    private fun handleBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (storeViewModel.isOpenMySelectGoosView.value == true) {
                    storeViewModel.setMySelectGoosView(false)
                } else {
                    isEnabled = false
                    requireActivity().onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

    private fun initClickListener() {
        binding.llStore.setOnClickListener {
            storeViewModel.setShowStorePage(true)
        }
        binding.llAllGoods.setOnClickListener {
            storeViewModel.setShowStorePage(false)
        }
        binding.ivBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        binding.llPriceTotal.setOnClickListener {
            storeViewModel.setMySelectGoosView(!(storeViewModel.isOpenMySelectGoosView.value ?: false))
        }
        binding.flGrayBg.setOnClickListener {
            storeViewModel.setMySelectGoosView(false)
        }
        binding.llSearch.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_storeFragment_to_storeSearchFragment)
        }
        binding.ivCustomerService.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_storeFragment_to_fragmentChat)
        }
        binding.ivAddToLove.setOnClickListener {}
        binding.tvDeleteAll.setOnClickListener {
            storeViewModel.clearSelectedGoods()
        }
    }

    private fun initObserve() {
        storeViewModel.isOpenMySelectGoosView.observe(viewLifecycleOwner) { isOpen ->
            showGoodsMenu(isOpen)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    storeViewModel.currentStore.collectLatest { store ->
                        val storeId = store?.id
                        if (currentStoreId != storeId) {
                            currentStoreId = storeId
                            storeViewModel.clearSelectedGoods()
                        }
                    }
                }
                launch {
                    storeViewModel.storeCategories.collectLatest { categories ->
                        categoryAdapter.setCategoryList(categories)
                        categoryAdapter.setSelectedCategory(storeViewModel.currentSelectedCategory.value)
                    }
                }
                launch {
                    storeViewModel.goodsList.collectLatest { goodsList ->
                        goodsAdapter.submitList(goodsList)
                    }
                }
            }
        }
        storeViewModel.currentAlreadySelectList.observe(viewLifecycleOwner) { selectedList ->
            if (selectedList.isEmpty()) {
                storeViewModel.setMySelectGoosView(false)
            }
            alreadySelectAdapter.submitList(selectedList)
            binding.rvAlreadySelect.post {
                binding.rvAlreadySelect.setMaxVisibleItems(4)
            }
        }
    }

    private fun showGoods() {
        storeViewModel.setShowStorePage(false)
    }

    private fun initGoodsRecyclerView() {
        goodsAdapter = StoreGoodsRecyclerViewAdapter(
            onAddClick = { goods ->
                storeViewModel.addGoods(goods)
            },
            onItemClick = { }
        )
        binding.rvGoods.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = goodsAdapter
            itemAnimator = null
            setHasFixedSize(true)
        }
    }

    private fun initCategoryRecyclerView() {
        categoryAdapter = StoreCategoryRecyclerViewAdapter { _, position ->
            categoryAdapter.updateSelectedPosition(position)
            storeViewModel.selectCategory(categoryAdapter.getItem(position))
        }
        binding.rvCategory.layoutManager = LinearLayoutManager(context)
        binding.rvCategory.adapter = categoryAdapter
        binding.rvCategory.itemAnimator = null
        categoryAdapter.setCategoryList(
            listOf(Store.CATEGORY_ALL, Store.CATEGORY_UNCLASSIFIED)
        )
    }

    private fun showGoodsMenu(show: Boolean) {
        if (show) {
            binding.llAlreadySelectGoods.alpha = 0f
            binding.llAlreadySelectGoods.visibility = View.VISIBLE
            binding.rvAlreadySelect.post {
                binding.rvAlreadySelect.itemAnimator = null
                binding.rvAlreadySelect.setMaxVisibleItems(4)
                binding.llAlreadySelectGoods.translationY = binding.llAlreadySelectGoods.height.toFloat()

                binding.llAlreadySelectGoods.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(280)
                    .withStartAction {
                        binding.rvAlreadySelect.alpha = 1f
                    }
                    .start()
            }
        } else {
            binding.llAlreadySelectGoods.animate()
                .translationY(binding.llAlreadySelectGoods.height.toFloat())
                .setDuration(240)
                .withEndAction {
                    binding.llAlreadySelectGoods.translationY = 0f
                    binding.llAlreadySelectGoods.alpha = 0f
                }
                .start()
        }
    }

    private fun initAlreadySelectRecyclerView() {
        alreadySelectAdapter = AlreadySelectGoodsRecyclerViewAdapter(
            onItemClick = { },
            onMinusClick = { item ->
                storeViewModel.decreaseSelectedGoods(item)
            },
            onPlusClick = { item ->
                storeViewModel.increaseSelectedGoods(item)
            }
        )

        binding.rvAlreadySelect.layoutManager = LinearLayoutManager(context)
        binding.rvAlreadySelect.adapter = alreadySelectAdapter
        alreadySelectAdapter.submitList(emptyList())
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
