package com.example.grabthisforme.activity.mainactivity.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.mainactivity.viewmodel.OrderMessageViewModel
import com.example.grabthisforme.databinding.FragmentOrderMessageBottomMessageBinding
import com.example.grabthisforme.model.goods.data.repository.GoodsRepository
import com.example.grabthisforme.model.order.data.repository.OrderRepository
import com.example.grabthisforme.model.order.domain.Order
import com.example.grabthisforme.model.order.domain.OrderStatusInfo
import com.example.grabthisforme.model.user.domain.User
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OrderMessageBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentOrderMessageBottomMessageBinding? = null
    private var order: Order? = null
    private val binding get() = _binding!!
    private val viewModel: OrderMessageViewModel by viewModels()

    @Inject
    lateinit var goodsRepository: GoodsRepository

    @Inject
    lateinit var orderRepository: OrderRepository

    companion object {
        private const val ARG_ORDER_DATA = "order_data"

        fun newInstance(orderId: String): OrderMessageBottomSheetFragment {
            return OrderMessageBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ORDER_DATA, orderId)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderMessageBottomMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            order = getOrderById(arguments?.getString(ARG_ORDER_DATA).orEmpty())
            initView()
        }
    }

    private suspend fun getOrderById(orderId: String): Order {
        return orderRepository.getOrder(orderId).firstOrNull() ?: Order(
            sender = User.getVirtualUser(),
            orderId = orderId,
            buyer = User.getVirtualUser(),
            goods = goodsRepository.getSingleDisplayGoods()
        )
    }

    private fun initView() {
        val currentOrder = order ?: return
        val buyer = currentOrder.buyer
        val senderName = currentOrder.sender?.name?.takeIf { it.isNotBlank() } ?: "等待接单"
        val goodsName = currentOrder.goods.name.ifBlank { "校园代取商品" }
        val goodsMessage = currentOrder.goods.message.ifBlank { "暂未填写额外备注，按订单默认流程配送。" }
        val statusUi = buildStatusUi(currentOrder)
        val startTimeText = formatTime(currentOrder.startTime)
        val endTimeText = formatTime(currentOrder.endTime)

        viewModel.updateBuyerName(buyer.name)
        binding.tvBuyerName.text = buyer.name.ifBlank { "匿名同学" }
        binding.tvBuyerSignature.text = buyer.signature?.takeIf { it.isNotBlank() }
            ?: "校园代拿、代买、代送都讲究准时和顺路，这单先帮你整理好了。"
        binding.tvStatus.text = statusUi.label
        binding.tvStatus.setBackgroundResource(statusUi.backgroundRes)
        binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), statusUi.textColorRes))
        binding.tvOrderTag.text = statusUi.tagline
        binding.tvOrderId.text = "订单号 ${currentOrder.orderId.takeLast(8)}"
        binding.tvGoodsTitle.text = goodsName
        binding.tvGoodsSubtitle.text = goodsMessage
        binding.tvGoodsPrice.text = if (currentOrder.goods.price > 0) {
            "预估 ¥${"%.1f".format(currentOrder.goods.price)}"
        } else {
            "价格待确认"
        }
        binding.tvBuyerLabelValue.text = currentOrder.buyer.name.ifBlank { "匿名同学" }
        binding.tvSenderLabelValue.text = senderName
        binding.tvPickupLabelValue.text = currentOrder.atPosition.ifBlank {
            currentOrder.shelfNumber.ifBlank { "待确认取货点" }
        }
        binding.tvDestinationLabelValue.text = currentOrder.aimPosition.ifBlank { "待确认送达点" }
        binding.tvShelfLabelValue.text = currentOrder.shelfNumber.ifBlank { "未填写货架号" }
        binding.tvTimeWindowValue.text = "$startTimeText - $endTimeText"
        binding.tvTimeLeftValue.text = buildTimeLeftText(currentOrder)
        binding.tvReminder.text = buildReminderText(currentOrder)
        binding.tvSectionNote.text = if (currentOrder.isAccepted) {
            "已经有人接单，信息以当前订单页为准。"
        } else {
            "当前还未接单，建议保持电话畅通，方便跑腿同学联系。"
        }

        binding.btnPrimary.setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        val bottomSheetDialog = dialog as? BottomSheetDialog ?: return
        val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(
            com.google.android.material.R.id.design_bottom_sheet
        )

        bottomSheet?.let {
            it.setBackgroundResource(android.R.color.transparent)
            val screenHeight = resources.displayMetrics.heightPixels
            val desiredHeight = (screenHeight * 0.82f).toInt()
            val behavior = BottomSheetBehavior.from(it)
            behavior.peekHeight = desiredHeight
            behavior.skipCollapsed = true
            behavior.isFitToContents = false
            behavior.expandedOffset = max(dpToPx(18), screenHeight - desiredHeight)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            it.layoutParams = it.layoutParams.apply {
                height = desiredHeight
            }
        }

        dialog?.window?.also { window ->
            window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }
    }

    private fun buildStatusUi(order: Order): StatusUi {
        return when {
            order.isExpired() -> StatusUi(
                label = "已超时",
                tagline = "建议尽快确认是否还需要继续配送",
                backgroundRes = R.drawable.bg_order_status_warm,
                textColorRes = R.color.orange_dark
            )

            order.orderStatus == OrderStatusInfo.STATUS_COMPLETED -> StatusUi(
                label = "已完成",
                tagline = "订单已经顺利闭环，可以继续发起新的校园跑腿",
                backgroundRes = R.drawable.bg_chip_mint,
                textColorRes = R.color.green_dark
            )

            order.isAccepted -> StatusUi(
                label = "配送中",
                tagline = "跑腿同学已接单，正在按路线推进",
                backgroundRes = R.drawable.bg_chip_mint,
                textColorRes = R.color.green_dark
            )

            order.orderStatus == OrderStatusInfo.STATUS_PENDING_DELIVERY -> StatusUi(
                label = "待送达",
                tagline = "商品已取到，等待送到指定位置",
                backgroundRes = R.drawable.bg_order_status_badge,
                textColorRes = R.color.text_primary
            )

            else -> StatusUi(
                label = "待接单",
                tagline = "适合校园内顺路代拿，接单后会尽快开始处理",
                backgroundRes = R.drawable.bg_order_status_badge,
                textColorRes = R.color.text_primary
            )
        }
    }

    private fun buildTimeLeftText(order: Order): String {
        if (order.endTime <= 0L) return "时间待确认"
        val diffMillis = order.endTime - System.currentTimeMillis()
        if (diffMillis <= 0L) return "已超时"
        val totalMinutes = diffMillis / 60000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 -> "还剩 ${hours}小时${minutes}分钟"
            else -> "还剩 ${minutes}分钟"
        }
    }

    private fun buildReminderText(order: Order): String {
        val pickup = order.atPosition.ifBlank { "取货点" }
        val destination = order.aimPosition.ifBlank { "送达点" }
        return "从 $pickup 送到 $destination，适合校园内短距离、时效敏感的代拿场景。"
    }

    private fun formatTime(timeMillis: Long): String {
        if (timeMillis <= 0L) return "待确认"
        return SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timeMillis))
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density + 0.5f).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private data class StatusUi(
        val label: String,
        val tagline: String,
        val backgroundRes: Int,
        val textColorRes: Int
    )
}
