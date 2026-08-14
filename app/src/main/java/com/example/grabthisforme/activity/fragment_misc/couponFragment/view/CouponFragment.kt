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
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentCouponBinding
import com.example.grabthisforme.util.ViewAnimationUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CouponFragment : Fragment() {
    private var _binding: FragmentCouponBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CouponViewModel by viewModels()
    private lateinit var adapter: CouponRecyclerViewAdapter
    private var lastMessageId = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        _binding = FragmentCouponBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CouponRecyclerViewAdapter(viewModel::buy)
        binding.rvCouponList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCouponList.adapter = adapter
        binding.rvCouponList.isNestedScrollingEnabled = false

        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnMyCoupons.setOnClickListener { viewModel.loadMine() }
        binding.btnExpiredCoupons.setOnClickListener { viewModel.loadMine() }
        binding.btnCouponMarket.setOnClickListener { viewModel.loadMarket() }

        viewModel.items.observe(viewLifecycleOwner, adapter::submitList)
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            binding.couponFiltersCard.isEnabled = !loading
            binding.couponListCard.alpha = if (loading) 0.65f else 1f
        }
        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (message.eventId != lastMessageId) {
                lastMessageId = message.eventId
                Toast.makeText(requireContext(), message.text, Toast.LENGTH_SHORT).show()
            }
        }
        ViewAnimationUtils.animateStaggeredEntrance(binding.couponFiltersCard, binding.couponListCard)
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
