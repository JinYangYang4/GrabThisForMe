package com.example.grabthisforme.activity.fragment_misc.all_executor.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.homeFragment.adapter.OrderDetailViewPager2Adapter
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentOrderBottomSheetBinding
import com.example.grabthisforme.util.ViewAnimationUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OrderExecutorFragment : Fragment() {
    private var _binding: FragmentOrderBottomSheetBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_VP2_POSITION = "vp2_position"

        fun newInstance(vp2Position: Int): OrderExecutorFragment {
            return OrderExecutorFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_VP2_POSITION, vp2Position)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewPager()
        ViewAnimationUtils.animateStaggeredEntrance(binding.statsCard, binding.tabCard)
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).innerBottomBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun initViewPager() {
        val initialPosition = OrderExecutorFragmentArgs.fromBundle(requireArguments()).orderId
        val adapter = OrderDetailViewPager2Adapter(this)
        binding.viewpager2.adapter = adapter
        binding.viewpager2.setCurrentItem(initialPosition, false)
        binding.viewpager2.setPageTransformer(OrderPageTransformer())

        val titles = listOf("待收货", "待送货", "历史订单")
        TabLayoutMediator(binding.tabLayout, binding.viewpager2) { tab, position ->
            tab.text = titles.getOrNull(position).orEmpty()
        }.attach()

        binding.tabLayout.post {
            updateTabBackgrounds(initialPosition)
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                updateTabBackgrounds(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit

            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })

        binding.viewpager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateTabBackgrounds(position)
            }
        })
    }

    private fun updateTabBackgrounds(selectedPosition: Int) {
        for (i in 0 until binding.tabLayout.tabCount) {
            val tab = binding.tabLayout.getTabAt(i) ?: continue
            val selected = i == selectedPosition
            tab.view.background = ContextCompat.getDrawable(
                requireContext(),
                if (selected) R.drawable.bg_order_tab_selected_modern
                else R.drawable.bg_order_tab_unselected_modern
            )
        }
    }

    private class OrderPageTransformer : ViewPager2.PageTransformer {
        override fun transformPage(page: View, position: Float) {
            val absPos = kotlin.math.abs(position)
            page.alpha = 1f - 0.18f * absPos
            page.translationX = -24f * position
            page.scaleY = 1f - 0.04f * absPos
        }
    }
}
