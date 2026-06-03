package com.example.grabthisforme.activity.fragment_misc.goodsFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.activity.fragment_misc.goodsFragment.adapter.GoodsMarketplaceFilterAdapter
import com.example.grabthisforme.activity.fragment_misc.goodsFragment.adapter.GoodsMarketplaceRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.goodsFragment.model.GoodsMarketplaceSection
import com.example.grabthisforme.activity.fragment_misc.goodsFragment.view.GoodsFragmentDirections
import com.example.grabthisforme.activity.fragment_misc.goodsFragment.viewModel.GoodsMarketplaceViewModel
import com.example.grabthisforme.databinding.FragmentMarketplaceGoodsListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentMarketplaceGoodsList : Fragment() {
    private var _binding: FragmentMarketplaceGoodsListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GoodsMarketplaceViewModel by viewModels()
    private var shouldScrollToTopAfterListUpdate = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarketplaceGoodsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setSection(requireSection())
        initFilterRecyclerView()
        initGoodsRecyclerView()
    }

    private fun initFilterRecyclerView() {
        val adapter = GoodsMarketplaceFilterAdapter {
            shouldScrollToTopAfterListUpdate = true
            viewModel.selectFilter(it.id)
        }
        binding.rvMarketplaceFilter.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvMarketplaceFilter.adapter = adapter
        binding.rvMarketplaceFilter.isNestedScrollingEnabled = false

        viewModel.filterChips.observe(viewLifecycleOwner) { chips ->
            adapter.submitList(chips)
        }
    }

    private fun initGoodsRecyclerView() {
        val adapter = GoodsMarketplaceRecyclerViewAdapter { goods ->
            val action = GoodsFragmentDirections.actionGoodsFragmentToGoodsDetailFragment(goods.goodsId)
            findNavController().navigate(action)
        }
        binding.rvMarketplaceGoods.adapter = adapter
        binding.rvMarketplaceGoods.itemAnimator = null
        binding.rvMarketplaceGoods.isNestedScrollingEnabled = true
        binding.rvMarketplaceGoods.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)

        viewModel.filteredGoodsList.observe(viewLifecycleOwner) { goodsList ->
            if (goodsList == null) {
                binding.emptyState.visibility = View.GONE
                return@observe
            }
            adapter.submitList(goodsList) {
                binding.emptyState.visibility = if (goodsList.isEmpty()) View.VISIBLE else View.GONE
                if (shouldScrollToTopAfterListUpdate) {
                    scrollToTop()
                    shouldScrollToTopAfterListUpdate = false
                }
            }
        }
    }

    private fun requireSection(): GoodsMarketplaceSection {
        val sectionName = requireArguments().getString(ARG_SECTION)
        return GoodsMarketplaceSection.valueOf(sectionName ?: GoodsMarketplaceSection.ALL.name)
    }

    fun scrollToTop() {
        if (_binding == null) return
        binding.rvMarketplaceGoods.stopScroll()
        (binding.rvMarketplaceGoods.layoutManager as? LinearLayoutManager)
            ?.scrollToPositionWithOffset(0, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_SECTION = "goods_marketplace_section"

        fun newInstance(section: GoodsMarketplaceSection): FragmentMarketplaceGoodsList {
            return FragmentMarketplaceGoodsList().apply {
                arguments = bundleOf(ARG_SECTION to section.name)
            }
        }
    }
}
