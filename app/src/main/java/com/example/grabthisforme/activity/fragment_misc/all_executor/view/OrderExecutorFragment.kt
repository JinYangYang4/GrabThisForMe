package com.example.grabthisforme.activity.fragment_misc.all_executor.view

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.MainActivity.view.MainActivity
import com.example.grabthisforme.activity.MainActivity.viewModel.MainViewModel
import com.example.grabthisforme.activity.homeFragment.adapter.OrderDetailViewPager2Adapter
import com.example.grabthisforme.databinding.FragmentOrderBottomSheetBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.math.log

class OrderExecutorFragment : Fragment() {

    private var _binding: FragmentOrderBottomSheetBinding? = null
    private lateinit var sharedViewModel: MainViewModel
    private val binding get() = _binding!!

    companion object {
        private const val ARG_VP2_POSITION = "vp2_position"
        fun newInstance(vp2Position: Int): OrderExecutorFragment {
            val fragment = OrderExecutorFragment()
            val args = Bundle()
            args.putInt(ARG_VP2_POSITION, vp2Position)
            fragment.arguments = args
            return fragment
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
        sharedViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        Log.d("test11", "onViewCreated: ")
        initViewPager()
        initView()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).innerBottomBar()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
    }
    fun initView(){
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun initViewPager() {
        val message = OrderExecutorFragmentArgs.fromBundle(requireArguments()).orderId ?: 0
        val adapter = OrderDetailViewPager2Adapter(this)
        binding.viewpager2.adapter = adapter
        binding.viewpager2.setCurrentItem(message, false)

        val titles = listOf("待收货", "待送货", "历史订单")
        TabLayoutMediator(binding.tabLayout, binding.viewpager2) { tab, position ->
            tab.text = titles.getOrNull(position) ?: ""
        }.attach()
    }
}
