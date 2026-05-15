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
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.fragment_misc.storeFragment.adapter.AlreadySelectGoodsRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.storeFragment.adapter.StoreGoodsRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.storeFragment.adpter.StoreCategoryRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.storeFragment.viewModel.StoreViewModel
import com.example.grabthisforme.databinding.FragmentStoreBinding
import com.example.grabthisforme.extension.setMaxVisibleItems
import com.example.grabthisforme.model.goods.domain.Goods
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StoreFragment : Fragment() {

    private var _binding: FragmentStoreBinding? = null
    private val binding get() = _binding!!
    private val storeViewModel : StoreViewModel by activityViewModels()
    private lateinit var goodsAdapter : StoreGoodsRecyclerViewAdapter
    private lateinit var alreadySelectAdapter : AlreadySelectGoodsRecyclerViewAdapter

    private lateinit var categoryAdapter: StoreCategoryRecyclerViewAdapter
    private var currentAlreadySelectList = mutableListOf<Goods>()
    private var currentStoreId: Long? = null

    private val args : StoreFragmentArgs by navArgs()

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
        initCLickListener()
        handleBackPressed()
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
    private fun initCLickListener(){
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
    }
    private fun initObserve(){
        storeViewModel.isOpenMySelectGoosView.observe(viewLifecycleOwner){isOpen ->
            showGoodsMenu(isOpen)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    storeViewModel.currentStore.collectLatest { store ->
                        val storeId = store?.id
                        if (currentStoreId != storeId) {
                            currentStoreId = storeId
                            currentAlreadySelectList = mutableListOf()
                            alreadySelectAdapter.submitList(emptyList())
                        }
                        val categories = store?.category?.ifEmpty { null }
                        if (!categories.isNullOrEmpty()) {
                            categoryAdapter.setCategoryList(categories)
                        }
                    }
                }
                launch {
                    storeViewModel.goodsList.collectLatest { goodsList ->
                        goodsAdapter.submitList(goodsList)
                        if (currentAlreadySelectList.isEmpty()) {
                            currentAlreadySelectList = goodsList.toMutableList()
                            alreadySelectAdapter.submitList(currentAlreadySelectList.toList())
                        }
                    }
                }
            }
        }
    }
    private fun showGoods(){
        storeViewModel.setShowStorePage(false)
    }
    private fun initGoodsRecyclerView() {
        goodsAdapter = StoreGoodsRecyclerViewAdapter(
            onAddClick = { goods ->
                 storeViewModel.addGoods(goods.price)
            },
            onItemClick = { goods ->
            }
        )
        binding.rvGoods.apply {
            layoutManager =LinearLayoutManager(context)
            adapter = goodsAdapter
            itemAnimator = null
            setHasFixedSize(true)
        }
    }
    private fun initCategoryRecyclerView() {
        categoryAdapter = StoreCategoryRecyclerViewAdapter { category, position ->
            categoryAdapter.updateSelectedPosition(position)
        }
        binding.rvCategory.layoutManager = LinearLayoutManager(context)
        binding.rvCategory.adapter = categoryAdapter
        binding.rvCategory.itemAnimator = null
        val categoryList = listOf(
            "全部", "零食饮料", "生鲜果蔬", "粮油调味", "日用百货", "休闲食品"
        )
        categoryAdapter.setCategoryList(categoryList)
    }
    private fun showGoodsMenu(show: Boolean) {
        if (show) {
            binding.llAlreadySelectGoods.alpha = 0f
            binding.llAlreadySelectGoods.visibility = View.VISIBLE
            binding.rvAlreadySelect.post {
                binding.rvAlreadySelect.setMaxVisibleItems(6)
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
            onItemClick = { goods ->
            },
            onMinusClick = { goods ->
                val index = currentAlreadySelectList.indexOfFirst { it.id == goods.id }
                if (index < 0) return@AlreadySelectGoodsRecyclerViewAdapter
                if (goods.selectedCount > 1) {
                    goods.selectedCount--
                    alreadySelectAdapter.notifyItemChanged(index)
                } else {
                    currentAlreadySelectList.removeAt(index)
                    alreadySelectAdapter.submitList(currentAlreadySelectList.toList())
                }
            },
            onPlusClick = { goods ->
                val index = currentAlreadySelectList.indexOfFirst { it.id == goods.id }
                if (index < 0) return@AlreadySelectGoodsRecyclerViewAdapter
                goods.selectedCount++
                alreadySelectAdapter.notifyItemChanged(index)
            }
        )
        binding.rvAlreadySelect.layoutManager = LinearLayoutManager(context)
        binding.rvAlreadySelect.adapter = alreadySelectAdapter
        alreadySelectAdapter.submitList(currentAlreadySelectList.toList())
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
