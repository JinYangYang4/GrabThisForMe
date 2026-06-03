package com.example.grabthisforme.activity.fragment_misc.goods_detail.view

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.goods_detail.viewModel.GoodsDetailViewModel
import com.example.grabthisforme.databinding.FragmentGoodsDetailBinding
import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.secondhandGoods.domain.SecondhandGoods
import com.example.grabthisforme.model.store.domain.Store
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GoodsDetailFragment : Fragment() {

    private var _binding: FragmentGoodsDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GoodsDetailViewModel by viewModels()

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
        binding.tvOriginalPrice.paintFlags =
            binding.tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        initClick()
        observeUiState()
    }

    private fun initClick() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.ivShare.setOnClickListener {
            showToast("分享功能开发中")
        }
        binding.ivMore.setOnClickListener {
            showToast("更多功能开发中")
        }
        binding.cardStore.setOnClickListener {
            viewModel.uiState.value.store?.id?.let { storeId ->
                val action = GoodsDetailFragmentDirections.actionGoodsDetailFragmentToStoreFragment(storeId)
                findNavController().navigate(action)
            }
        }
        binding.btnContactStore.setOnClickListener {
            showToast("联系商家功能开发中")
        }
        binding.btnBuyNow.setOnClickListener {
            showToast("下单流程开发中")
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderGoods(state.goods, state.secondhandGoods)
                    renderStore(state.store, state.goods)
                    renderDescription(state.isFallbackData, state.secondhandGoods != null)
                }
            }
        }
    }

    private fun renderGoods(goods: Goods, secondhandGoods: SecondhandGoods?) {
        Glide.with(binding.root)
            .load(goods.pic.takeIf { it.isNotBlank() })
            .placeholder(R.drawable.food_pic)
            .error(R.drawable.food_pic)
            .into(binding.ivGoodsCover)

        binding.tvPrice.text = formatPrice(goods.price)

        val originalPrice = when {
            secondhandGoods != null -> secondhandGoods.originalPrice
            goods.discountPrice > 0.0 && goods.discountPrice > goods.price -> goods.discountPrice
            else -> null
        }
        binding.tvOriginalPrice.isVisible = originalPrice != null
        binding.tvOriginalPrice.text = originalPrice?.let { "参考价 ${formatPrice(it)}" }.orEmpty()

        binding.tvDiscountTag.isVisible = goods.discountTag.isNotBlank()
        binding.tvDiscountTag.text = goods.discountTag

        binding.tvSecondhandBadge.isVisible = secondhandGoods != null
        binding.tvSecondhandBadge.text = "二手优选"

        binding.tvGoodsName.text = goods.name
        binding.tvGoodsMessage.text = goods.message.ifBlank { "适合校园即时下单和日常补给。" }
        binding.tvSales.text = "已售 ${goods.soldCount}"
        binding.tvStatus.text = if (goods.isSoldOut) "当前缺货" else "支持下单"
        binding.tvHotTag.text = if (goods.isHot) "近期热门" else "稳定上新"

        binding.cardSecondhandInfo.isVisible = secondhandGoods != null
        if (secondhandGoods != null) {
            binding.tvQuality.text = secondhandGoods.quality.ifBlank { "成色良好" }
            binding.tvUsedTime.text = secondhandGoods.usedTime?.let { "使用时长 $it" } ?: "刚刚上架"
            binding.tvTradeHint.text = if (secondhandGoods.negotiable) "支持议价" else "一口价"
        }
    }

    private fun renderStore(store: Store?, goods: Goods) {
        binding.tvStoreType.text = store?.type?.takeIf { it.isNotBlank() } ?: "校园好店"
        binding.tvStoreName.text = store?.name?.takeIf { it.isNotBlank() } ?: "校园精选商家"

        val address = store?.address?.takeIf { it.isNotBlank() } ?: "校内配送 / 到店自取"
        binding.tvStoreMeta.text = if (goods.isSoldOut) {
            "$address · 当前库存偏紧"
        } else {
            "$address · 下单节奏友好"
        }

        binding.tvStoreDelivery.text = if (store == null) {
            "支持到店自取，详情以商家页为准"
        } else {
            "起送 ${store.minOrderAmount.toPlainString()} 元 · 配送费 ${store.deliveryFee.toPlainString()} 元"
        }
    }

    private fun renderDescription(isFallbackData: Boolean, isSecondhand: Boolean) {
        binding.tvDetailTips.text = if (isSecondhand) {
            "建议线下当面验货，重点确认成色、配件和交易方式。"
        } else {
            "优先展示适合学生高频购买的商品信息、折扣力度和店铺服务。"
        }

        binding.tvSectionDesc.text = if (isFallbackData) {
            "当前展示的是默认商品示例。商品页和二手页接入真实商品 id 后，这里会自动切换到对应详情。"
        } else {
            "详情页已根据当前商品类型自动适配普通商品或二手商品的信息结构。"
        }
    }

    private fun formatPrice(price: Double): String {
        return String.format(Locale.getDefault(), "￥%.1f", price)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
