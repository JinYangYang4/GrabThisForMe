package com.example.grabthisforme.activity.homeFragment.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide

import com.example.grabthisforme.R
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.mainactivity.viewmodel.MainViewModel

import com.example.grabthisforme.activity.homeFragment.adapter.HomePagerAdapter
import com.example.grabthisforme.databinding.FragmentHome1Binding
import com.example.grabthisforme.model.XAxisValueFormatter.XAxisValueFormatter
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FragmentHome1 : Fragment() {
    private var _binding : FragmentHome1Binding ?= null
    private val binding get() = _binding!!

    private val sharedViewModel: MainViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHome1Binding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.IGet.setOnClickListener {
            sharedViewModel.toPage(0)
        }
        binding.ivHeadPic.setOnClickListener {
            sharedViewModel.drawerOpenStateToOpen()
        }
        initVp2()
        initView()
        initObserve()
        setupRadarChart(binding.radarChart)
        setupHorizontalBarChart(binding.barChart)
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
    fun initView(){
        binding.llItemOrder.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment_Order_ac(1)
        }
        binding.llSignIn.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_blankFragment_to_fragmentSignIn)
        }
        binding.llGoToSecondhandFillInfo.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_blankFragment_to_createSecondHandGoods)
        }
        binding.llGoToSecondhand.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_blankFragment_to_secondHandGoodFragment)
        }
        binding.llSearch.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_blankFragment_to_searchGoodsFragment)
        }

    }

    fun initVp2(){
        val adapter = HomePagerAdapter(this)
        binding.viewpager2.adapter = adapter

        val titles = listOf(
            "全部任务",
            "附近任务",
            "紧急任务",
            "食品类",
            "文件类",
            "包裹类"
        )
        TabLayoutMediator(binding.tabLayout, binding.viewpager2) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }
    private fun setupRadarChart(radarChart: RadarChart) {

        val entries = listOf(
            RadarEntry(4.5f),
            RadarEntry(4.2f),
            RadarEntry(4.8f),
            RadarEntry(3.9f),
            RadarEntry(4.6f)
        )

        // 2. 数据集
        val dataSet = RadarDataSet(entries, "综合能力").apply {
            color = requireContext().getColor(R.color.green_primary)
            fillColor = requireContext().getColor(R.color.green_primary)
            setDrawFilled(true)
            fillAlpha = 180
            lineWidth = 2f
            valueTextSize = 10f
            valueTextColor = requireContext().getColor(R.color.black)
        }

        radarChart.data = RadarData(dataSet)
        radarChart.animateY(800, Easing.EaseOutBack)


        val labels = listOf("接单速度", "准时率", "好评率", "完成率", "信誉等级")

        radarChart.xAxis.apply {
            valueFormatter = XAxisValueFormatter(labels)
            textSize = 12f
            textColor = requireContext().getColor(R.color.black)
        }

        radarChart.yAxis.apply {
            axisMinimum = 0f
            axisMaximum = 5f
            setDrawLabels(false)
        }

        radarChart.description.isEnabled = false
        radarChart.legend.isEnabled = false
        radarChart.invalidate()
    }
    private fun setupHorizontalBarChart(barChart: HorizontalBarChart) {

        val entries = listOf(
            BarEntry(0f, 120f), // 完成数量
            BarEntry(1f, 150f), // 接单数量
            BarEntry(2f, 110f)  // 准时次数
        )
        val dataSet = BarDataSet(entries, "接单统计").apply {
            color = requireContext().getColor(R.color.green_primary)
            valueTextSize = 12f
            valueTextColor = requireContext().getColor(R.color.black)
        }
        barChart.data = BarData(dataSet).apply {
            barWidth = 0.3f
        }
        val labels = listOf("完成数量", "接单数量", "准时次数")

        barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            granularity = 1f
            textSize = 12f
            setDrawGridLines(false)
            position = XAxis.XAxisPosition.BOTTOM
        }

        barChart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = barChart.data.yMax * 1.3f
            setDrawGridLines(false)
        }
        barChart.axisRight.isEnabled = false

        barChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setFitBars(true)
            animateY(800)
            invalidate()
        }
    }
}
