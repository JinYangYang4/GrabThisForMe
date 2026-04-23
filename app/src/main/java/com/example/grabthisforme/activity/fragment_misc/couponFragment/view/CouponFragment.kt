package com.example.grabthisforme.activity.fragment_misc.couponFragment.view



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.fragment_misc.couponFragment.adapter.CouponRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.couponFragment.viewModel.CouponViewModel
import com.example.grabthisforme.activity.fragment_misc.sign_inFragment.model.Coupon
import com.example.grabthisforme.databinding.FragmentCouponBinding

class CouponFragment : Fragment() {
    private var _binding: FragmentCouponBinding? = null
    private val binding get() = _binding!!

    private lateinit var couponListAdapter :CouponRecyclerViewAdapter
    private val viewModel: CouponViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCouponBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRVCoupon()
        initView()
        loadCouponData()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).innerBottomBar()
    }

    private fun initRVCoupon() {
        couponListAdapter = CouponRecyclerViewAdapter { coupon ->
            Toast.makeText(
                requireContext(),
                "查看 ${coupon.title} 使用详情",
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.rvCouponList.adapter = couponListAdapter
        binding.rvCouponList.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.VERTICAL,false)
    }
    fun initView(){
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    private fun loadCouponData() {
        val couponTestList = mutableListOf<Coupon>().apply {
            add(
                Coupon(
                    id = 1,
                    title = "满100减20优惠券",
                    denomination = 20.0f,
                    type = "全场通用",
                    desc = "有效期7天，订单满100元可用，逾期自动失效",
                    userStatus = Coupon.UserCouponStatus.UNUSED,
                    receiveTime = "",
                    expireTime = ""
                )
            )
            add(
                Coupon(
                    id = 2,
                    title = "满50减10优惠券",
                    denomination = 10.0f,
                    type = "食品专用",
                    desc = "有效期15天，订单满50元可用，逾期自动失效",
                    userStatus = Coupon.UserCouponStatus.UNUSED,
                    receiveTime = "",
                    expireTime = ""
                )
            )
            add(
                Coupon(
                    id = 3,
                    title = "无门槛5元优惠券",
                    denomination = 5.0f,
                    type = "全场通用",
                    desc = "有效期3天，无订单金额限制，逾期自动失效",
                    userStatus = Coupon.UserCouponStatus.UNUSED,
                    receiveTime = "",
                    expireTime = ""
                )
            )
        }
        couponListAdapter.submitList(couponTestList)
        handleEmptyData(couponTestList)
    }
    private fun handleEmptyData(couponList: List<Coupon>) {
        viewModel.updateEmptyState(couponList.isEmpty())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}