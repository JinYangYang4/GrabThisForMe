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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.mainactivity.view.OrderMessageBottomSheetFragment
import com.example.grabthisforme.activity.mainactivity.viewmodel.MainViewModel

import com.example.grabthisforme.activity.homeFragment.adapter.RecyclerViewStoreAdapter
import com.example.grabthisforme.activity.homeFragment.adapter.RecyclerViewTaskAdapter
import com.example.grabthisforme.activity.homeFragment.viewModel.FragmentHomeViewModel
import com.example.grabthisforme.databinding.FragmentHomeBinding
import com.example.grabthisforme.model.order.data.mock.OrderMockData
import com.example.grabthisforme.model.store.domain.Store

class FragmentHome : Fragment() {
    private var _binding : FragmentHomeBinding ?= null
    private val binding get() = _binding!!

    private var lastTouchY = 0f
    private lateinit var viewModel: FragmentHomeViewModel
    private lateinit var sharedViewModel: MainViewModel
    private lateinit var adapter1: RecyclerViewTaskAdapter
    private lateinit var adapter2: RecyclerViewStoreAdapter


    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater,container,false)
        viewModel = ViewModelProvider(this).get(FragmentHomeViewModel::class.java)
        sharedViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        initRecyclerViewTask()
        initRecyclerViewStore()
        binding.llDropdown.setOnClickListener {
            if (viewModel.GetRvTaskIsOpen()){
                closeRvTaskAnimation()
            }else{
                openRvTaskAnimation()
            }

        }
        binding.ivHeadPic.setOnClickListener {
            sharedViewModel.drawerOpenStateToOpen()
        }
        binding.IGet.setOnClickListener {
            Log.d("test1", "onCreateView: ")
            sharedViewModel.toPage(1)
            viewModel.setGiveMode(true)
        }
        binding.iHelpMeGet.setOnClickListener {
            viewModel.setGiveMode(false)
        }
        initOperationBar()
        return binding.root
    }

    fun initOperationBar(){
        binding.llItemOrder.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment_ac(0)
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

        val taskList = OrderMockData.getOrderList()
        adapter1 = RecyclerViewTaskAdapter() { taskId ->
            val orderBottomSheet = OrderMessageBottomSheetFragment.newInstance(taskId.toString())
            orderBottomSheet.show(childFragmentManager, "OrderMessageBottomSheet")
        }
        binding.rvTask.adapter = adapter1
        binding.rvTask.layoutManager = LinearLayoutManager(requireContext())
        adapter1.submitList(taskList)
        binding.rvTask.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val isAtTop = layoutManager.findFirstCompletelyVisibleItemPosition() == 0  // 是否滑到顶部
                super.onScrolled(recyclerView, dx, dy)
                if (viewModel.GetIsAnimating()) return
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

        viewModel.rvTaskIsOpen.observe(viewLifecycleOwner){is_open ->
            if (is_open){
                binding.rvTask.isNestedScrollingEnabled = false
                binding.ivDropdown.setImageResource(R.drawable.ic_pull_up)
            }else{
                binding.rvTask.isNestedScrollingEnabled = true
                binding.ivDropdown.setImageResource(R.drawable.ic_dropdown)
            }
        }
        binding.rvTask.post {
            viewModel.setRvTaskHeight(binding.rvTask.height)
        }

        binding.rvTask.setOnTouchListener { v, event ->

            Log.d("test11", "initRecyclerViewTask: ")
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
    }
    fun initRecyclerViewStore() {
        adapter2 = RecyclerViewStoreAdapter() {store ->
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_blankFragment_to_storeFragment)
        }

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        gridLayoutManager.orientation = GridLayoutManager.VERTICAL

        binding.rvSomeGoods.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
            adapter = adapter2
        }



        val storeList = Store.createVirtualStores()
        adapter2.submitList(storeList)
    }
    private fun showDropdownWithSlideAnimation() {
        if (!viewModel.GetAlreadyShow()){
            viewModel.MakeIsAnimatingTrue()

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
                        viewModel.MakeIsAnimatingFalse()  // 动画结束，解除标记
                    }
                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }
            binding.ivDropdown.startAnimation(set)
            viewModel.MakeAlreadyShowTure()
        }

    }
    private fun hideDropdownWithSlideAnimation() {
        if (viewModel.GetAlreadyShow()&&!viewModel.GetRvTaskIsOpen()){
            viewModel.MakeIsAnimatingTrue()
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
                        viewModel.MakeIsAnimatingFalse()
                    }
                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }
            binding.ivDropdown.startAnimation(set)
            viewModel.MakeAlreadyShowFalse()
        }

    }
    private fun openRvTaskAnimation() {
        val totalItemHeight = calculateRecyclerViewTotalHeight(binding.rvTask)
        val targetHeight = if (totalItemHeight > 0) totalItemHeight else 1500
        val heightAnimator = ValueAnimator.ofInt(viewModel.getRvTaskHeight(), targetHeight).apply {
            duration = 300
            addUpdateListener { anim ->
                val height = anim.animatedValue as Int
                val layoutParams = binding.rvTask.layoutParams
                layoutParams.height = height
                binding.rvTask.layoutParams = layoutParams
            }
        }
        heightAnimator.start()
        viewModel.MakeRvTaskIsOpenTure()
    }
    private fun calculateRecyclerViewTotalHeight(recyclerView: RecyclerView): Int {
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return 0
        val adapter = recyclerView.adapter ?: return 0
        var totalHeight = 0
        for (i in 0 until adapter.itemCount) {
            val viewHolder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(i))
            adapter.onBindViewHolder(viewHolder, i)
            viewHolder.itemView.measure(
                View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            totalHeight += viewHolder.itemView.measuredHeight
        }
        totalHeight += recyclerView.paddingTop + recyclerView.paddingBottom
        return totalHeight
    }
    private fun closeRvTaskAnimation(){
        val heightAnimator = ValueAnimator.ofInt(1500,viewModel.getRvTaskHeight()).apply {
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
        viewModel.MakeRvTaskIsOpenFalse()
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}
