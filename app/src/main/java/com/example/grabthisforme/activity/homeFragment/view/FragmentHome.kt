package com.example.grabthisforme.activity.homeFragment.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_SETTLING
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.MainActivity.viewModel.MainViewModel
import com.example.grabthisforme.activity.homeFragment.adapter.RecyclerViewGoodsAdapter
import com.example.grabthisforme.activity.homeFragment.adapter.RecyclerViewTaskAdapter
import com.example.grabthisforme.activity.homeFragment.viewModel.FragmentHomeViewModel
import com.example.grabthisforme.databinding.FragmentHomeBinding
import com.example.grabthisforme.model.goos.Goods
import com.example.grabthisforme.model.rv_task.RecyclerviewTask
import com.example.grabthisforme.model.user.User

class FragmentHome : Fragment() {
    private var _binding : FragmentHomeBinding ?= null
    private val binding get() = _binding!!

    private var lastTouchY = 0f
    private lateinit var viewModel: FragmentHomeViewModel
    private lateinit var sharedViewModel: MainViewModel
    private lateinit var adapter1: RecyclerViewTaskAdapter
    private lateinit var adapter2: RecyclerViewGoodsAdapter


    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater,container,false)
        viewModel = ViewModelProvider(this).get(FragmentHomeViewModel::class.java)
        sharedViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        initRecyclerViewTask()
        initRecyclerViewGoods()
        binding.ivDropdown.setOnClickListener {
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
        }

        return binding.root
    }


    fun shareViewModelObserve(){
    }

    @SuppressLint("ClickableViewAccessibility")
    fun initRecyclerViewTask(){

        val taskList: List<RecyclerviewTask> = listOf(
            RecyclerviewTask(
                Id = 1,
                name = "代购 Mylikes 麦丽素",
                price = 5.0, // 任务酬劳（代购费）
                startTime = 1770000000000, // 配送开始时间：2026-06-03 12:00:00
                endTime = 1770007200000    // 配送截止时间：2026-06-03 14:00:00
            ),
            // 任务2：矿泉水代购（1小时内配送）
            RecyclerviewTask(
                Id = 2,
                name = "买 500ml 矿泉水",
                price = 3.0,
                startTime = 1770003600000, // 2026-06-03 13:00:00
                endTime = 1770007200000    // 2026-06-03 14:00:00
            ),
            // 任务3：零食大礼包（4小时内配送）
            RecyclerviewTask(
                Id = 3,
                name = "代购零食大礼包（含薯片+饼干）",
                price = 10.0,
                startTime = 1770000000000, // 2026-06-03 12:00:00
                endTime = 1770014400000    // 2026-06-03 16:00:00
            ),
            // 任务4：咖啡代购（30分钟内配送）
            RecyclerviewTask(
                Id = 4,
                name = "买一杯冰美式咖啡（不加糖）",
                price = 4.5,
                startTime = 1770005400000, // 2026-06-03 13:30:00
                endTime = 1770007200000    // 2026-06-03 14:00:00
            ),
            // 任务5：水果代购（半天内配送）
            RecyclerviewTask(
                Id = 5,
                name = "买 1 斤草莓（新鲜无坏果）",
                price = 8.0,
                startTime = 1770000000000, // 2026-06-03 12:00:00
                endTime = 1770043200000    // 2026-06-03 22:00:00
            )
        )
        adapter1 = RecyclerViewTaskAdapter() { taskId ->
            Toast.makeText(requireContext(), "点击了", Toast.LENGTH_SHORT).show()
        }
        binding.rvTask.adapter = adapter1
        binding.rvTask.layoutManager = LinearLayoutManager(requireContext())
        taskList.let {
            adapter1.submitList(it)
        }
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
                binding.ivDropdown.setImageResource(R.drawable.ic_pull_up)
            }else{
                binding.ivDropdown.setImageResource(R.drawable.ic_dropdown)
            }
        }
        binding.rvTask.post {
            viewModel.setRvTaskHeight(binding.rvTask.height)
        }
        binding.rvTask.isNestedScrollingEnabled = false
        binding.rvTask.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
    }
    fun initRecyclerViewGoods() {
        adapter2 = RecyclerViewGoodsAdapter { goodsId ->
            Toast.makeText(requireContext(), "点击了商品ID: $goodsId", Toast.LENGTH_SHORT).show()
        }

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        gridLayoutManager.orientation = GridLayoutManager.VERTICAL

        binding.rvSomeGoods.apply {
            layoutManager = gridLayoutManager
            adapter = adapter2
        }



        val goodsList = Goods.get20RepeatGoods()
        adapter2.submitList(goodsList)
    }
    private fun showDropdownWithSlideAnimation() {
        if (!viewModel.GetAlreadyShow()){
            viewModel.MakeIsAnimatingTrue()
            binding.ivDropdown.visibility = View.VISIBLE

            // 动画参数：从上方（-自身高度）滑到当前位置（0），透明度从 0→1
            val slideAnimation = TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,  // X轴起始位置（相对自身）
                Animation.RELATIVE_TO_SELF, 0f,  // X轴结束位置（相对自身）
                Animation.RELATIVE_TO_SELF, -1f, // Y轴起始位置（相对自身，-1 = 上方一个自身高度）
                Animation.RELATIVE_TO_SELF, 0f   // Y轴结束位置（相对自身，0 = 原始位置）
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

            // 淡入动画（透明度 0→1）
            val fadeInAnimation = AlphaAnimation(0f, 1f).apply {
                duration = 300
                fillAfter = true
            }

            // 组合动画：同时执行滑动和淡入
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

            // 动画参数：从当前位置（0）滑到上方（-自身高度），透明度从 1→0
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
    private fun openRvTaskAnimation(){
        val heightAnimator = ValueAnimator.ofInt(viewModel.getRvTaskHeight(), 1500).apply {
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
        viewModel.MakeRvTaskIsOpenTure()
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