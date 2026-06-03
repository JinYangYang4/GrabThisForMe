package com.example.grabthisforme.activity.fragment_misc.my_store.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.my_store.adapter.MyStoreRecyclerViewAdapter
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.fragment_misc.my_store.viewmodel.MyStoreViewModel
import com.example.grabthisforme.databinding.FragmentMyStoreBinding
import com.example.grabthisforme.util.ViewAnimationUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentMyStore : Fragment() {
    private var _binding: FragmentMyStoreBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MyStoreViewModel by viewModels()
    private lateinit var storeAdapter: MyStoreRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyStoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        initRecyclerView()
        initView()
        initObserve()
        ViewAnimationUtils.animateStaggeredEntrance(binding.statsCard, binding.storeActionCard)
    }

    private fun initRecyclerView() {
        storeAdapter = MyStoreRecyclerViewAdapter {
            val dir = FragmentMyStoreDirections.actionFragmentMyStoreToStoreOwnerFragment(it.storeId)
            findNavController().navigate(dir)
        }
        binding.rvStoreList.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = storeAdapter
            setHasFixedSize(true)
        }
    }

    private fun initObserve() {
        viewModel.storeList.observe(viewLifecycleOwner) { storeList ->
            storeAdapter.submitList(storeList)
        }
    }

    private fun initView() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.ivCreateMyStore.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_fragmentMyStore_to_registerStoreFragment)
        }
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
