package com.example.grabthisforme.activity.mainactivity.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.viewModels
import com.example.grabthisforme.databinding.FragmentOrderMessageBottomMessageBinding

import com.example.grabthisforme.model.Order.Order
import com.example.grabthisforme.model.goods.domain.Goods
import com.example.grabthisforme.model.user.domain.User
import com.example.grabthisforme.activity.mainactivity.viewmodel.OrderMessageViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class OrderMessageBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentOrderMessageBottomMessageBinding? = null
    private  var order : Order? = null
    private val binding get() = _binding!!
    private val viewModel: OrderMessageViewModel by viewModels()
    companion object {
        private const val ARG_ORDER_DATA = "order_data"
        fun newInstance(orderId: String): OrderMessageBottomSheetFragment {
            val fragment = OrderMessageBottomSheetFragment()
            val args = Bundle()
            args.putString(ARG_ORDER_DATA, orderId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =  FragmentOrderMessageBottomMessageBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        order = getOrderById("1213213")
        initView()
    }
    fun getOrderById(orderId: String) : Order{
        val order = Order(User.getVirtualUser(),orderId, User.getVirtualUser(), Goods.getSingleVirtualGoods())
        return order
    }
    fun initView(){
        order?.let{
            viewModel.updateBuyerName(order!!.buyer.name)
        }
    }


    override fun onStart() {
        super.onStart()
        val bottomSheetDialog = dialog as BottomSheetDialog
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

        bottomSheet?.let {

            val screenHeight = resources.displayMetrics.heightPixels
            val desiredHeight = (screenHeight * 0.7).toInt()

            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
            behavior.peekHeight = desiredHeight
            behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED

            val layoutParams = it.layoutParams
            layoutParams.height = desiredHeight
            it.layoutParams = layoutParams
        }

        dialog?.window?.also { window ->
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
