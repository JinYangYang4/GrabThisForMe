package com.example.grabthisforme.activity.fragment_misc.sign_inFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.grabthisforme.activity.fragment_misc.sign_inFragment.adapter.CouponMallRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.sign_inFragment.adapter.SignCalendarRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.sign_inFragment.model.CouponMallItem
import com.example.grabthisforme.activity.fragment_misc.sign_inFragment.model.SignCalendarDay
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentSignInBinding

class FragmentSignIn : Fragment() {
    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private lateinit var calendarAdapter: SignCalendarRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRVCalendar()
        initRvCouponMall()
        initView()
    }

    private fun initView() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun initRVCalendar() {
        calendarAdapter = SignCalendarRecyclerViewAdapter {
            Toast.makeText(requireContext(), "签到成功", Toast.LENGTH_SHORT).show()
        }
        binding.rvCalendar.adapter = calendarAdapter
        binding.rvCalendar.layoutManager = GridLayoutManager(requireContext(), 7)
        binding.rvCalendar.isNestedScrollingEnabled = false
        calendarAdapter.submitList(SignCalendarDay.SignTestDataSingleton.getDefault30DaysSignData())
    }

    private fun initRvCouponMall() {
        val adapter = CouponMallRecyclerViewAdapter {
            Toast.makeText(requireContext(), "兑换成功", Toast.LENGTH_SHORT).show()
        }
        binding.rvCouponMall.adapter = adapter
        binding.rvCouponMall.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvCouponMall.isNestedScrollingEnabled = false
        adapter.submitList(CouponMallItem.CouponTestDataSingleton.getCouponMallTestData())
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
