package com.example.grabthisforme.activity.fragment_misc.storeFragment.view

import android.os.Bundle
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
import com.example.grabthisforme.activity.fragment_misc.storeFragment.adapter.StoreGoodsRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.storeFragment.adpter.StoreCategoryRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.storeFragment.adpter.StoreOwnerRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.storeFragment.viewModel.StoreOwnerViewModel
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentStoreOwnerBinding
import com.example.grabthisforme.extension.setMaxVisibleItems
import com.example.grabthisforme.model.store.domain.Store
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StoreOwnerFragment : Fragment() {
    private val args: StoreOwnerFragmentArgs by navArgs()
    private var _binding: FragmentStoreOwnerBinding? = null
    private val binding get() = _binding!!

    private lateinit var goodsAdapter: StoreOwnerRecyclerViewAdapter
    private lateinit var unselectGoodsAdapter: StoreGoodsRecyclerViewAdapter
    private lateinit var categoryAdapter: StoreCategoryRecyclerViewAdapter
    private val viewModel: StoreOwnerViewModel by viewModels()
    private var currentCategoryList: List<String> =
        listOf(Store.CATEGORY_ALL, Store.CATEGORY_UNCLASSIFIED)

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
        initCategoryManagerDialogResult()
        initClick()
        initCategoryRecyclerView()
        initGoodsRecyclerView()
        initUnselectGoodsRecyclerView()
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
        categoryAdapter = StoreCategoryRecyclerViewAdapter { _, position ->
            categoryAdapter.updateSelectedPosition(position)
            viewModel.selectCategory(categoryAdapter.getItem(position))
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
        binding.tvEdit.setOnClickListener {
            showCategoryManagerDialog()
        }
        binding.tvAddGoods.setOnClickListener {
            viewModel.tryOpenUnselectGoodsView()
        }
        binding.flGrayBg.setOnClickListener {
            viewModel.setOpenUnselectGoodsView(false)
        }
        binding.tvCloseUnselectGoods.setOnClickListener {
            viewModel.setOpenUnselectGoodsView(false)
        }
        binding.llStore.setOnClickListener {
            viewModel.setShowStorePage(true)
        }
        binding.llAllGoods.setOnClickListener {
            viewModel.setShowStorePage(false)
        }
    }

    private fun initObserve() {
        viewModel.isOpenUnselectGoodsView.observe(viewLifecycleOwner) { isOpen ->
            showUnselectGoodsMenu(isOpen)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.categoryList.collectLatest { categories ->
                        currentCategoryList = categories
                        categoryAdapter.setCategoryList(currentCategoryList)
                        categoryAdapter.setSelectedCategory(viewModel.currentSelectedCategory.value)
                        updateAddGoodsState()
                    }
                }
                launch {
                    viewModel.goodsList.collectLatest { goodsList ->
                        goodsAdapter.submitList(goodsList)
                    }
                }
                launch {
                    viewModel.unselectGoodsList.collectLatest { goodsList ->
                        unselectGoodsAdapter.submitList(goodsList)
                        binding.tvUnselectGoodsSize.text = buildUnselectGoodsTitle(goodsList.size)
                        binding.tvUnselectGoodsEmpty.visibility =
                            if (goodsList.isEmpty()) View.VISIBLE else View.GONE
                        binding.rvUnselectGoods.visibility =
                            if (goodsList.isEmpty()) View.GONE else View.VISIBLE
                        binding.rvUnselectGoods.post {
                            binding.rvUnselectGoods.setMaxVisibleItems(5)
                        }
                        updateAddGoodsState()
                    }
                }
            }
        }
    }

    private fun initGoodsRecyclerView() {
        goodsAdapter = StoreOwnerRecyclerViewAdapter(
            onItemClick = { }
        )
        binding.rvGoods.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = goodsAdapter
            itemAnimator = null
            setHasFixedSize(true)
        }
    }

    private fun initUnselectGoodsRecyclerView() {
        unselectGoodsAdapter = StoreGoodsRecyclerViewAdapter(
            onAddClick = { goods ->
                viewModel.addGoodsToCurrentCategory(goods.goodsId)
            },
            onItemClick = { }
        )
        binding.rvUnselectGoods.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = unselectGoodsAdapter
            itemAnimator = null
            setHasFixedSize(true)
        }
    }

    private fun showCategoryManagerDialog() {
        val dialog = CategoryManagerBottomDialogFragment.newInstance(ArrayList(currentCategoryList))
        dialog.show(childFragmentManager, "category_manager_bottom_dialog")
    }

    private fun initCategoryManagerDialogResult() {
        childFragmentManager.setFragmentResultListener(
            CategoryManagerBottomDialogFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val updatedList = bundle.getStringArrayList(
                CategoryManagerBottomDialogFragment.RESULT_KEY_CATEGORY_LIST
            ).orEmpty()
            val renameOldList = bundle.getStringArrayList(
                CategoryManagerBottomDialogFragment.RESULT_KEY_RENAMED_OLD_LIST
            ).orEmpty()
            val renameNewList = bundle.getStringArrayList(
                CategoryManagerBottomDialogFragment.RESULT_KEY_RENAMED_NEW_LIST
            ).orEmpty()
            val renamedCategories = renameOldList.zip(renameNewList).toMap()
            if (updatedList.isNotEmpty()) {
                currentCategoryList = updatedList
                viewModel.updateStoreCategories(updatedList, renamedCategories)
                categoryAdapter.setCategoryList(currentCategoryList)
                categoryAdapter.setSelectedCategory(viewModel.currentSelectedCategory.value)
            }
        }
    }

    private fun showUnselectGoodsMenu(show: Boolean) {
        if (show) {
            binding.flGrayBg.visibility = View.VISIBLE
            binding.flGrayBg.alpha = 0f
            binding.flGrayBg.animate()
                .alpha(1f)
                .setDuration(180)
                .start()

            binding.llUnselectGoods.alpha = 0f
            binding.llUnselectGoods.visibility = View.VISIBLE
            binding.rvUnselectGoods.post {
                binding.rvUnselectGoods.setMaxVisibleItems(5)
                binding.llUnselectGoods.translationY = binding.llUnselectGoods.height.toFloat()
                binding.llUnselectGoods.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(260)
                    .start()
            }
        } else {
            binding.flGrayBg.animate()
                .alpha(0f)
                .setDuration(160)
                .withEndAction {
                    binding.flGrayBg.visibility = View.GONE
                }
                .start()

            binding.llUnselectGoods.animate()
                .translationY(binding.llUnselectGoods.height.toFloat())
                .alpha(0f)
                .setDuration(220)
                .withEndAction {
                    binding.llUnselectGoods.translationY = 0f
                    binding.llUnselectGoods.visibility = View.GONE
                }
                .start()
        }
    }

    private fun updateAddGoodsState() {
        val canOpen = viewModel.currentSelectedCategory.value != Store.CATEGORY_ALL
        binding.tvAddGoods.isEnabled = canOpen
        binding.tvAddGoods.alpha = if (canOpen) 1f else 0.45f
    }

    private fun buildUnselectGoodsTitle(size: Int): String {
        return if (viewModel.currentSelectedCategory.value == Store.CATEGORY_UNCLASSIFIED) {
            "待移入未分类 ${size} 件"
        } else {
            "待加入 ${size} 件"
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
