package com.example.grabthisforme.activity.LoginActivity.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.LoginActivity.adapter.SwitchAccountsRecyclerViewAdapter
import com.example.grabthisforme.databinding.SwitchAccountBottomSheetDialogFragmentBinding
import com.example.grabthisforme.model.user.domain.User
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.grabthisforme.activity.LoginActivity.viewmodel.SwitchAccountsViewModel

class SwitchAccountsBottomSheetDialogFragment : BottomSheetDialogFragment() {
    private var _binding: SwitchAccountBottomSheetDialogFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SwitchAccountsViewModel
    private lateinit var adapter: SwitchAccountsRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SwitchAccountBottomSheetDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(SwitchAccountsViewModel::class.java)
        initRecyclerView()
        observeViewModelData()
        initObserve()
    }
    private fun initObserve(){
        viewModel.testUserList.observe(viewLifecycleOwner){list ->
            adapter.submitList(ArrayList(list))
        }
    }

    private fun initRecyclerView() {
        adapter = SwitchAccountsRecyclerViewAdapter(
            onItemClick = { selectedUser ->
                Toast.makeText(
                    requireContext(),
                    "选中账号：${selectedUser.name}（ID：${selectedUser.id}）",
                    Toast.LENGTH_SHORT
                ).show()
                dismiss()
            },
            onIvCurrentClick = {selectedUser ->
                Toast.makeText(
                    requireContext(),
                    "删除账号：${selectedUser.name}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        binding.rvSwitchAccounts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SwitchAccountsBottomSheetDialogFragment.adapter
        }

    }
    private fun observeViewModelData() {
        viewModel.testUserList.observe(viewLifecycleOwner) { userItems ->
            adapter.submitList(userItems)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): SwitchAccountsBottomSheetDialogFragment {
            return SwitchAccountsBottomSheetDialogFragment()
        }
    }
}
