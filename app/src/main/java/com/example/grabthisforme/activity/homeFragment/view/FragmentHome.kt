package com.example.grabthisforme.activity.homeFragment.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.default_entry.view.BlankFragmentDirections
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.mainactivity.view.OrderMessageBottomSheetFragment
import com.example.grabthisforme.activity.mainactivity.viewmodel.MainViewModel

import com.example.grabthisforme.activity.homeFragment.adapter.RecyclerViewStoreAdapter
import com.example.grabthisforme.activity.homeFragment.adapter.RecyclerViewTaskAdapter
import com.example.grabthisforme.activity.homeFragment.viewModel.FragmentHomeViewModel
import com.example.grabthisforme.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentHome : Fragment() {
    private var _binding : FragmentHomeBinding ?= null
    private val binding get() = _binding!!

    private val homeViewModel: FragmentHomeViewModel by viewModels()
    private val sharedViewModel: MainViewModel by activityViewModels()
    private lateinit var adapter1: RecyclerViewTaskAdapter
    private lateinit var adapter2: RecyclerViewStoreAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater,container,false)
        binding.llDropdown.setOnClickListener {
            if (homeViewModel.GetRvTaskIsOpen()){
                closeRvTaskAnimation()
            }else{
                openRvTaskAnimation()
            }

        }
        binding.ivHeadPic.setOnClickListener {
            sharedViewModel.drawerOpenStateToOpen()
        }
        binding.IGet.setOnClickListener {
            sharedViewModel.toPage(1)
        }
        binding.iHelpMeGet.setOnClickListener {
        }
        initOperationBar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = homeViewModel
        initObserve()
        initRecyclerViewTask()
        initRecyclerViewStore()
    }

    fun initObserve(){
        sharedViewModel.currentUser.observe(viewLifecycleOwner){user ->
            Glide.with(this)
                .load(user?.headPic)
                .placeholder(R.drawable.cat)
                .error(R.drawable.cat)
                .into(binding.ivHeadPic)
        }
    }
    fun initOperationBar(){
        binding.llItemOrder.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment_Order_ac(0)
        }
        binding.llCreateOrder.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_blankFragment_to_createOrderFragment)
        }
        binding.llSignIn.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_blankFragment_to_fragmentSignIn)
        }
        binding.llShowCoupon.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_blankFragment_to_couponFragment)
        }
        binding.llSearch.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_blankFragment_to_searchGoodsFragment)
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    fun initRecyclerViewTask(){

        adapter1 = RecyclerViewTaskAdapter() { taskId ->
            val orderBottomSheet = OrderMessageBottomSheetFragment.newInstance(taskId.toString())
            orderBottomSheet.show(childFragmentManager, "OrderMessageBottomSheet")
        }
        binding.rvTask.adapter = adapter1
        binding.rvTask.layoutManager = LinearLayoutManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.currentTaskOrders.collectLatest { orderList ->
                adapter1.submitList(orderList)
            }
        }

        binding.rvTask.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val isAtTop = layoutManager.findFirstCompletelyVisibleItemPosition() == 0  // 是否滑到顶部
                super.onScrolled(recyclerView, dx, dy)
                if (homeViewModel.GetIsAnimating()) return
                when {
                    dy > 0 -> {
                        showDropdownWithSlideAnimation()
                    }
                    dy < 0 || isAtTop -> {
                        hideDropdownWithSlideAnimation()
                    }
                }
            }
        })

        homeViewModel.rvTaskIsOpen.observe(viewLifecycleOwner){ is_open ->
            if (is_open){
                binding.rvTask.isNestedScrollingEnabled = false
                binding.ivDropdown.setImageResource(R.drawable.ic_pull_up)
            }else{
                binding.rvTask.isNestedScrollingEnabled = true
                binding.ivDropdown.setImageResource(R.drawable.ic_dropdown)
            }
        }
        binding.rvTask.post {
            homeViewModel.setRvTaskHeight(binding.rvTask.height)
        }

        binding.rvTask.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
    }
    fun initRecyclerViewStore() {
        adapter2 = RecyclerViewStoreAdapter(onStoreClickListener = { store ->
            Log.d("test11", "initRecyclerViewStore: ")
            val dir =BlankFragmentDirections.actionBlankFragmentToStoreFragment(store.id)
            (requireActivity() as MainActivity).NewNavController_navgite(dir)
        })

        binding.rvSomeGoods.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
            adapter = adapter2
        }

        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.allStores.collectLatest { storeList ->
                adapter2.submitList(storeList)
            }
        }
    }
    private fun showDropdownWithSlideAnimation() {
        if (!homeViewModel.GetAlreadyShow()){
            homeViewModel.MakeIsAnimatingTrue()

            // 动画参数：从上方（-自身高度）滑到当前位置（0），透明度从 0→1
            val slideAnimation = TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, -1f,
                Animation.RELATIVE_TO_SELF, 0f
            ).apply {
                duration = 300
                fillAfter = true  // 动画结束后保持最终状态
            }
            val heightAnimator = ValueAnimator.ofInt(0, 45).apply {
                duration= 300
                addUpdateListener { anim ->
                    val height = anim.animatedValue as Int
                    binding.ivDropdown.layoutParams.height = height
                    binding.ivDropdown.requestLayout() // 刷新布局，让高度生效
                }
            }
            val fadeInAnimation = AlphaAnimation(0f, 1f).apply {
                duration = 300
                fillAfter = true
            }

            val set = AnimationSet(true).apply {
                heightAnimator.start()
                addAnimation(slideAnimation)
                addAnimation(fadeInAnimation)
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        homeViewModel.MakeIsAnimatingFalse()  // 动画结束，解除标记
                    }
                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }
            binding.ivDropdown.startAnimation(set)
            homeViewModel.MakeAlreadyShowTure()
        }

    }
    private fun hideDropdownWithSlideAnimation() {
        if (homeViewModel.GetAlreadyShow()&&!homeViewModel.GetRvTaskIsOpen()){
            homeViewModel.MakeIsAnimatingTrue()
            val slideAnimation = TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, -1f  // 滑回上方不可见区域
            ).apply {
                duration = 300
                fillAfter = true
            }
            val heightAnimator = ValueAnimator.ofInt(45, 0).apply {
                duration = 300
                addUpdateListener { anim ->
                    val height = anim.animatedValue as Int
                    binding.ivDropdown.layoutParams.height = height
                    binding.ivDropdown.requestLayout()
                }
            }
            // 淡出动画（透明度 1→0）
            val fadeOutAnimation = AlphaAnimation(1f, 0f).apply {
                duration = 300
                fillAfter = true
            }
            // 组合动画：同时执行滑动和淡出
            val set = AnimationSet(true).apply {
                heightAnimator.start()
                addAnimation(slideAnimation)
                addAnimation(fadeOutAnimation)
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        binding.ivDropdown.clearAnimation()  // 清除动画，避免影响下次显示
                        homeViewModel.MakeIsAnimatingFalse()
                    }
                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }
            binding.ivDropdown.startAnimation(set)
            homeViewModel.MakeAlreadyShowFalse()
        }

    }
    private fun openRvTaskAnimation() {
        val totalItemHeight = calculateRecyclerViewTotalHeight(binding.rvTask)
        val heightAnimator = ValueAnimator.ofInt(homeViewModel.getRvTaskHeight(),totalItemHeight).apply {
            duration = 300
            addUpdateListener { anim ->
                val height = anim.animatedValue as Int
                val layoutParams = binding.rvTask.layoutParams
                layoutParams.height = height
                binding.rvTask.layoutParams = layoutParams
            }
        }
        heightAnimator.start()
        homeViewModel.MakeRvTaskIsOpenTure()
    }
    /**
     * 完整计算：item高度 + item上下margin + 分割线 + rvpadding
     */
    private fun calculateRecyclerViewTotalHeight(recyclerView: RecyclerView): Int {
        val adapter = recyclerView.adapter ?: return 0
        val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return 0
        val itemCount = adapter.itemCount
        if (itemCount == 0) return 0

        val widthSpec = View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY)
        var totalContentHeight = 0

        // 1. 遍历所有Item，测量高度 + 拿到layout_margin
        for (i in 0 until itemCount) {
            val holder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(i))
            adapter.onBindViewHolder(holder, i)
            val itemView = holder.itemView

            // 测量item真实高度
            itemView.measure(
                widthSpec,
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            val itemH = itemView.measuredHeight

            // 重点：获取 item 根布局的 Margin
            val itemLp = itemView.layoutParams as ViewGroup.MarginLayoutParams
            val itemMarginVertical = itemLp.topMargin + itemLp.bottomMargin

            totalContentHeight += itemH + itemMarginVertical
        }


        val rvPaddingVertical = recyclerView.paddingTop + recyclerView.paddingBottom

        return totalContentHeight +  rvPaddingVertical
    }
    private fun closeRvTaskAnimation(){
        val heightAnimator = ValueAnimator.ofInt(1500,homeViewModel.getRvTaskHeight()).apply {
            duration = 300
            addUpdateListener { anim ->
                val height = anim.animatedValue as Int
                binding.rvTask.layoutParams.height = height
                binding.rvTask.requestLayout() // 刷新布局，让高度生效
            }
        }

        val set = AnimationSet(true).apply {
            heightAnimator.start()
        }
        binding.rvTask.startAnimation(set)
        homeViewModel.MakeRvTaskIsOpenFalse()
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
