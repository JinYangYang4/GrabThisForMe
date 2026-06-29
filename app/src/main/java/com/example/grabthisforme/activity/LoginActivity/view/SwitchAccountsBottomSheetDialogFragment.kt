package com.example.grabthisforme.activity.LoginActivity.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.activity.LoginActivity.adapter.SwitchAccountsRecyclerViewAdapter
import com.example.grabthisforme.activity.LoginActivity.viewmodel.SwitchAccountsViewModel
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.SwitchAccountBottomSheetDialogFragmentBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
        viewModel = ViewModelProvider(requireActivity())[SwitchAccountsViewModel::class.java]
        initRecyclerView()
        observeViewModelData()
    }

    private fun initRecyclerView() {
        adapter = SwitchAccountsRecyclerViewAdapter(
            onItemClick = { selectedUser ->
                viewModel.switchToUser(selectedUser.userId)
            },
            onIvCurrentClick = { selectedUser ->
                viewModel.deleteUser(selectedUser.userId)
            }
        )

        binding.rvSwitchAccounts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SwitchAccountsBottomSheetDialogFragment.adapter
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun observeViewModelData() {
        viewModel.testUserList.observe(viewLifecycleOwner) { userItems ->
            adapter.submitList(userItems)
        }

        viewModel.switchAccountSuccess.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                Toast.makeText(
                    requireContext(),
                    "已切换到 ${user.name}",
                    Toast.LENGTH_SHORT
                ).show()
                dismiss()
                navigateToMainActivity()
            }
        }

        viewModel.switchAccountError.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.onSwitchAccountErrorConsumed()
            }
        }

        viewModel.deleteAccountMessage.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.onDeleteAccountMessageConsumed()
            }
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
