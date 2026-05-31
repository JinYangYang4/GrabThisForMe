package com.example.grabthisforme.activity.fragment_misc.couponFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.activity.fragment_misc.couponFragment.adapter.CouponRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.couponFragment.viewModel.CouponViewModel
import com.example.grabthisforme.activity.fragment_misc.sign_inFragment.model.Coupon
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentCouponBinding

class CouponFragment : Fragment() {
    private var _binding: FragmentCouponBinding? = null
    private val binding get() = _binding!!

    private lateinit var couponListAdapter: CouponRecyclerViewAdapter
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
                "\u67e5\u770b ${coupon.title} \u4f7f\u7528\u8be6\u60c5",
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.rvCouponList.adapter = couponListAdapter
        binding.rvCouponList.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
    }

    private fun initView() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadCouponData() {
        val couponTestList = listOf(
            Coupon(
                id = 1,
                title = "\u6ee1100\u51cf20\u4f18\u60e0\u5238",
                denomination = 20.0f,
                type = "\u5168\u573a\u901a\u7528",
                desc = "\u6709\u6548\u671f7\u5929\uff0c\u8ba2\u5355\u6ee1100\u5143\u53ef\u7528",
                userStatus = Coupon.UserCouponStatus.UNUSED,
                receiveTime = "",
                expireTime = "\u4eca\u65e5\u8d772\u5929\u540e"
            ),
            Coupon(
                id = 2,
                title = "\u996e\u54c1\u591c\u5bb5\u4e13\u4eab\u5238",
                denomination = 10.0f,
                type = "\u98df\u54c1\u4e13\u7528",
                desc = "\u6709\u6548\u671f5\u5929\uff0c\u6821\u56ed\u9910\u996e\u53ef\u7528",
                userStatus = Coupon.UserCouponStatus.UNUSED,
                receiveTime = "",
                expireTime = "\u672c\u5468\u65e5\u524d"
            ),
            Coupon(
                id = 3,
                title = "\u65e0\u95e8\u69db5\u5143\u60ca\u559c\u5238",
                denomination = 5.0f,
                type = "\u5168\u573a\u901a\u7528",
                desc = "\u65e0\u8ba2\u5355\u91d1\u989d\u9650\u5236\uff0c\u5c0f\u989d\u8dd1\u817f\u4e5f\u80fd\u7528",
                userStatus = Coupon.UserCouponStatus.UNUSED,
                receiveTime = "",
                expireTime = "\u4eca\u65e5\u8d777\u5929\u540e"
            )
        )
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
