package com.example.grabthisforme.activity.communityFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.communityFragment.adpter.CommunityPagerAdapter
import com.example.grabthisforme.activity.communityFragment.model.CommunityTabs
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.mainactivity.viewmodel.MainViewModel
import com.example.grabthisforme.databinding.FragmentCommunityBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentCommunity : Fragment() {
    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVP2()
        val targetView = view.findViewById<ImageView>(R.id.iv_Add)
        initAddMenu(targetView.id)
        initClickListener()
        initObserve()
    }

    private fun initObserve() {
        sharedViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            Glide.with(this)
                .load(user?.headPic)
                .placeholder(R.drawable.cat)
                .error(R.drawable.cat)
                .into(binding.ivAvatar)
        }
    }

    private fun initVP2() {
        val adapter = CommunityPagerAdapter(this)
        binding.viewpager2.adapter = adapter
        val titles = CommunityTabs.items.map { it.title }
        TabLayoutMediator(binding.tabLayout, binding.viewpager2) { tab, position ->
            val customView = LayoutInflater.from(requireContext())
                .inflate(R.layout.tab_pill_item, binding.tabLayout, false)
            val textView = customView.findViewById<TextView>(R.id.tab_text)
            textView.text = titles[position]
            tab.customView = customView
        }.attach()

        val tabCount = binding.tabLayout.tabCount
        binding.viewpager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                val currentTab = binding.tabLayout.getTabAt(position)
                val nextTab = if (position + 1 < tabCount) {
                    binding.tabLayout.getTabAt(position + 1)
                } else {
                    null
                }
                val currentTextView = currentTab?.customView?.findViewById<TextView>(R.id.tab_text)
                val nextTextView = nextTab?.customView?.findViewById<TextView>(R.id.tab_text)
                currentTextView?.background?.alpha = ((1 - positionOffset) * 255).toInt()
                nextTextView?.background?.alpha = (positionOffset * 255).toInt()
            }

            override fun onPageSelected(position: Int) {
                for (i in 0 until tabCount) {
                    val tab = binding.tabLayout.getTabAt(i)
                    val textView = tab?.customView?.findViewById<TextView>(R.id.tab_text)
                    textView?.background?.alpha = if (i == position) 255 else 0
                }
            }
        })
    }

    private fun initClickListener() {
        binding.llSearch.setOnClickListener {
            (requireActivity() as MainActivity)
                .intentToMiscFragment(R.id.action_blankFragment_to_searchCommunityFragment)
        }
        binding.ivAvatar.setOnClickListener {
            sharedViewModel.drawerOpenStateToOpen()
        }
    }

    private fun initAddMenu(targetView: Int) {
        binding.ivAdd.setOnClickListener {
            val menuDialog = CommunityLeftBottomMenuDialog.newInstance(targetView)
            parentFragmentManager.let { manager ->
                menuDialog.show(manager, "left_bottom_menu")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
