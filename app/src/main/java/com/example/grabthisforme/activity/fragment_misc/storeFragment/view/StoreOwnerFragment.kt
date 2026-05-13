package com.example.grabthisforme.activity.fragment_misc.storeFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.activity.fragment_misc.storeFragment.adapter.StoreGoodsRecyclerViewAdapter
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentStoreOwnerBinding
import com.example.grabthisforme.model.goods.domain.Goods
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoreOwnerFragment : Fragment() {

    private var _binding: FragmentStoreOwnerBinding? = null
    private val binding get() = _binding!!

    private lateinit var goodsAdapter: StoreGoodsRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreOwnerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClick()
        initRecyclerView()
    }

    private fun initClick() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.llSearch.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(com.example.grabthisforme.R.id.action_storeOwnerFragment_to_storeSearchFragment)
        }
    }

    private fun initRecyclerView() {
        goodsAdapter = StoreGoodsRecyclerViewAdapter(
            onAddClick = { _ -> },
            onItemClick = { _ -> }
        )
        binding.rvOwnerGoods.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = goodsAdapter
            setHasFixedSize(true)
        }
        goodsAdapter.submitList(Goods.get20RepeatGoods())
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
