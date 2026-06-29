package com.example.grabthisforme.activity.informationFragment.view

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.informationFragment.adapter.InformationPagerAdapter
import com.example.grabthisforme.activity.informationFragment.viewmodel.InformationViewModel
import com.example.grabthisforme.activity.mainactivity.viewmodel.MainViewModel
import com.example.grabthisforme.databinding.FragmentInformationBinding
import com.example.grabthisforme.ui.menu.AnchoredActionMenuItem
import com.example.grabthisforme.ui.menu.AnchoredActionMenuPopup
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class FragmentInformation : Fragment() {
    private var _binding : FragmentInformationBinding?=null
    private val binding get() = _binding!!
    private val sharedViewModel : MainViewModel by activityViewModels()
    private val viewModel: InformationViewModel by viewModels()
    private var addMenuPopup: AnchoredActionMenuPopup? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInformationBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        addMenuPopup = AnchoredActionMenuPopup(requireContext())
        initVP2()
        initAddMenu()
        initObserve()
    }

    fun initVP2(){
        Log.d("test111", "Fragment onCreateView") // 确认Fragment是否加载
        val adapter = InformationPagerAdapter(this)
        binding.viewpager2.adapter = adapter
        initClickL()

        val titles = listOf(
            "消息",
            "全部",
        )
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
                // 当前正在被滑出的 Tab 索引
                val currentTab = binding.tabLayout.getTabAt(position)
                // 下一个将被滑入的 Tab 索引（如果没有下一页则忽略）
                val nextTab = if (position + 1 < tabCount) binding.tabLayout.getTabAt(position + 1) else null

                // 获取自定义视图中的 TextView
                val currentTextView = currentTab?.customView?.findViewById<TextView>(R.id.tab_text)
                val nextTextView = nextTab?.customView?.findViewById<TextView>(R.id.tab_text)
                 currentTextView?.background?.alpha = ((1 - positionOffset) * 255).toInt()
                 nextTextView?.background?.alpha = (positionOffset * 255).toInt()
            }

            override fun onPageSelected(position: Int) {
                // 滑动结束后，确保选中 Tab 的透明度归位（防止浮点误差）
                for (i in 0 until tabCount) {
                    val tab = binding.tabLayout.getTabAt(i)
                    val textView = tab?.customView?.findViewById<TextView>(R.id.tab_text)
                    textView?.background?.alpha = if (i == position) 255 else 0
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }
    fun initAddMenu(){
        binding.ivAdd.setOnClickListener {
            addMenuPopup?.show(
                anchor = binding.ivAdd,
                items = listOf(
                    AnchoredActionMenuItem(
                        id = "create_group",
                        title = "创建群聊",
                        iconRes = R.drawable.ic_make_talk,
                        iconBackgroundRes = R.drawable.bg_my_quick_icon_misty_mint
                    ),
                    AnchoredActionMenuItem(
                        id = "add_friend",
                        title = "添加好友",
                        iconRes = R.drawable.ic_add_friend,
                        iconBackgroundRes = R.drawable.bg_my_quick_icon_soft_apricot
                    )
                )
            ) { item ->
                when (item.id) {
                    "create_group" -> Unit
                    "add_friend" -> {
                        (requireActivity() as MainActivity)
                            .intentToMiscFragment(R.id.action_blankFragment_to_fragmentSearchFriendOrGroupOrConversation2)
                    }
                }
            }
        }
    }
    fun initObserve(){
        sharedViewModel.currentUser.observe(viewLifecycleOwner){user ->
            Glide.with(this)
                .load(user?.headPic)
                .placeholder(R.drawable.cat)
                .error(R.drawable.cat)
                .into(binding.ivAvatar)
        }
    }

    fun initClickL(){
        binding.llSearch.setOnClickListener {
            (requireActivity()as MainActivity).intentToMiscFragment(R.id.action_blankFragment_to_fragmentSearchFriendOrGroupOrConversation)
        }
        binding.ivAvatar.setOnClickListener {
            sharedViewModel.drawerOpenStateToOpen()
        }
    }

    override fun onDestroyView() {
        addMenuPopup?.dismiss()
        addMenuPopup = null
        super.onDestroyView()
        _binding = null
    }
}
