package com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.adapter.ConditionRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.adapter.SecondhandGoodsRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.secondhand_goodsFragment.viewModel.SecondHandsViewModel
import com.example.grabthisforme.databinding.FragmentGoodsRvBinding
import com.example.grabthisforme.model.goods.domain.Goods
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentRecyclerViewGoods : Fragment() {
    private var _binding: FragmentGoodsRvBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SecondHandsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoodsRvBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initCategories()
        viewModel.setGoodsCategory(requireGoodsCategory())
        initConditionRecyclerView()
        initRecyclerView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initConditionRecyclerView() {
        val adapter = ConditionRecyclerViewAdapter {
            viewModel.switchCategory(it.id)
            binding.rvGoodsCondition.smoothScrollToPosition((it.id - 1).coerceAtLeast(0).toInt())
        }

        binding.rvGoodsCondition.adapter = adapter
        binding.rvGoodsCondition.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        viewModel.categoryList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(ArrayList(list))
        }
    }

    private fun initRecyclerView() {
        val adapter = SecondhandGoodsRecyclerViewAdapter {}
        binding.RvOrder.adapter = adapter
        binding.RvOrder.itemAnimator = null
        binding.RvOrder.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        viewModel.filteredGoodsList.observe(viewLifecycleOwner) { goodsList ->
            adapter.submitList(goodsList)
            binding.tv1.visibility = if (goodsList.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun requireGoodsCategory(): Goods.GoodsCategory {
        val categoryName = requireArguments().getString(ARG_GOODS_CATEGORY)
        return Goods.GoodsCategory.valueOf(categoryName ?: Goods.GoodsCategory.OTHER.name)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_GOODS_CATEGORY = "goods_category"

        fun newInstance(category: Goods.GoodsCategory): FragmentRecyclerViewGoods {
            return FragmentRecyclerViewGoods().apply {
                arguments = bundleOf(ARG_GOODS_CATEGORY to category.name)
            }
        }
    }
}
