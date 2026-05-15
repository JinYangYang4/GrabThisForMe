package com.example.grabthisforme.activity.fragment_misc.storeFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.grabthisforme.databinding.FragmentGoodsDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentGoodsDetail : Fragment() {

    private var _binding: FragmentGoodsDetailBinding? = null
    private val binding get() = _binding!!

    private val goodsId: Long by lazy {
        arguments?.getLong(ARG_GOODS_ID, -1L) ?: -1L
    }

    companion object {
        private const val ARG_GOODS_ID = "arg_goods_id"

        fun newInstance(goodsId: Long): FragmentGoodsDetail {
            return FragmentGoodsDetail().apply {
                arguments = Bundle().apply {
                    putLong(ARG_GOODS_ID, goodsId)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoodsDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClick()
        renderGoodsHint()
    }

    private fun initClick() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.ivShare.setOnClickListener {
            showToast("分享功能开发中")
        }
        binding.icCart.setOnClickListener {
            showToast("购物车功能开发中")
        }
        binding.icMore.setOnClickListener {
            showToast("更多功能开发中")
        }
        binding.btnAddCart.setOnClickListener {
            showToast("已加入购物车")
        }
        binding.btnBuyNow.setOnClickListener {
            showToast("立即购买功能开发中")
        }
    }

    private fun renderGoodsHint() {
        if (goodsId > 0L) {
            binding.cartBadge.text = "1"
        } else {
            binding.cartBadge.text = "0"
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
