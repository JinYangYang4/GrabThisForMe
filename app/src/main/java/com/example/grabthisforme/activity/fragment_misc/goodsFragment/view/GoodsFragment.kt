package com.example.grabthisforme.activity.fragment_misc.goodsFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.goodsFragment.adapter.GoodsMarketplacePagerAdapter
import com.example.grabthisforme.activity.fragment_misc.goodsFragment.model.GoodsMarketplaceSection
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentGoodsMarketplaceBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GoodsFragment : Fragment() {
    private var _binding: FragmentGoodsMarketplaceBinding? = null
    private val binding get() = _binding!!
    private var appBarOffsetChangedListener: AppBarLayout.OnOffsetChangedListener? = null
    private var pageChangeCallback: androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoodsMarketplaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewPager()
        initView()
        initAppBarAnimation()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).innerBottomBar()
    }

    private fun initView() {
        val navigateToSearch = {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_goodsFragment_to_searchGoodsFragment)
        }
        binding.flBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        binding.clSearch.setOnClickListener { navigateToSearch() }
        binding.flSearchEntry.setOnClickListener { navigateToSearch() }
    }

    private fun initAppBarAnimation() {
        appBarOffsetChangedListener = AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val totalScrollRange = appBarLayout.totalScrollRange.takeIf { it > 0 } ?: return@OnOffsetChangedListener
            val collapseRatio = (-verticalOffset / totalScrollRange.toFloat()).coerceIn(0f, 1f)
            val searchIconProgress = ((collapseRatio - 0.55f) / 0.45f).coerceIn(0f, 1f)
            val labelProgress = 1f - searchIconProgress

            binding.tvInspirationMarketplace.alpha = labelProgress
            binding.tvInspirationMarketplace.scaleX = 0.92f + (0.08f * labelProgress)
            binding.tvInspirationMarketplace.scaleY = 0.92f + (0.08f * labelProgress)

            binding.flCollapsedSearchIcon.alpha = searchIconProgress
            binding.flCollapsedSearchIcon.scaleX = 0.88f + (0.12f * searchIconProgress)
            binding.flCollapsedSearchIcon.scaleY = 0.88f + (0.12f * searchIconProgress)
        }
        appBarOffsetChangedListener?.let(binding.appBar::addOnOffsetChangedListener)
    }

    private fun initViewPager() {
        val sections = GoodsMarketplaceSection.entries.toList()
        val adapter = GoodsMarketplacePagerAdapter(this, sections)
        binding.vpGoodsContent.adapter = adapter
        binding.vpGoodsContent.offscreenPageLimit = 2
        TabLayoutMediator(binding.tlGoodsCategory, binding.vpGoodsContent) { tab, position ->
            val customView = LayoutInflater.from(requireContext())
                .inflate(R.layout.tab_pill_item, binding.tlGoodsCategory, false)
            val textView = customView.findViewById<TextView>(R.id.tab_text)
            textView.text = sections.getOrNull(position)?.title.orEmpty()
            tab.customView = customView
        }.attach()

        binding.tlGoodsCategory.post {
            applyTabSpacing()
            updateTabSelection(0, 0f)
        }
        pageChangeCallback = object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateTabSelection(position, 0f)
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                updateTabSelection(position, positionOffset)
            }
        }
        pageChangeCallback?.let(binding.vpGoodsContent::registerOnPageChangeCallback)

    }

    private fun applyTabSpacing() {
        val tabMargin = dp2px(8)
        for (index in 0 until binding.tlGoodsCategory.tabCount) {
            val tab = binding.tlGoodsCategory.getTabAt(index) ?: continue
            val params = tab.view.layoutParams as? ViewGroup.MarginLayoutParams ?: continue
            params.marginEnd = tabMargin
            tab.view.layoutParams = params
        }
    }

    private fun dp2px(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density + 0.5f).toInt()
    }

    private fun updateTabSelection(position: Int, positionOffset: Float) {
        val tabCount = binding.tlGoodsCategory.tabCount
        val currentTab = binding.tlGoodsCategory.getTabAt(position)
        val nextTab = if (position + 1 < tabCount) binding.tlGoodsCategory.getTabAt(position + 1) else null
        val currentTextView = currentTab?.customView?.findViewById<TextView>(R.id.tab_text)
        val nextTextView = nextTab?.customView?.findViewById<TextView>(R.id.tab_text)

        for (i in 0 until tabCount) {
            val tab = binding.tlGoodsCategory.getTabAt(i)
            val textView = tab?.customView?.findViewById<TextView>(R.id.tab_text)
            textView?.background?.alpha = 0
        }
        currentTextView?.background?.alpha = ((1 - positionOffset) * 255).toInt()
        nextTextView?.background?.alpha = (positionOffset * 255).toInt()
    }


    override fun onDestroyView() {
        appBarOffsetChangedListener?.let { binding.appBar.removeOnOffsetChangedListener(it) }
        pageChangeCallback?.let(binding.vpGoodsContent::unregisterOnPageChangeCallback)
        pageChangeCallback = null
        appBarOffsetChangedListener = null
        super.onDestroyView()
        _binding = null
    }
}
