package com.example.grabthisforme.activity.fragment_misc.userDetail.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.activity.fragment_misc.userDetail.adapter.UserCommonGroupAdapter
import com.example.grabthisforme.activity.fragment_misc.userDetail.viewModel.UserDetailViewModel
import com.example.grabthisforme.databinding.FragmentUserDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserDetailFragment : Fragment() {

    private var _binding: FragmentUserDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserDetailViewModel by viewModels()
    private lateinit var commonGroupAdapter: UserCommonGroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initClick()
        observeUi()
    }

    private fun initRecyclerView() {
        commonGroupAdapter = UserCommonGroupAdapter { groupId ->
            viewModel.onCommonGroupClick(groupId)
        }
        binding.rvCommonGroups.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.rvCommonGroups.adapter = commonGroupAdapter
        binding.rvCommonGroups.itemAnimator = null
    }

    private fun initClick() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnPrimaryAction.setOnClickListener {
            viewModel.onPrimaryActionClick()
        }
        binding.btnSecondaryAction.setOnClickListener {
            viewModel.onSecondaryActionClick()
        }
    }

    private fun observeUi() {
        viewModel.uiModel.observe(viewLifecycleOwner) { uiModel ->
            binding.tvName.text = uiModel.name
            binding.tvSignature.text = uiModel.signature
            binding.tvStatus.text = uiModel.statusText
            binding.tvPhone.text = uiModel.phoneText
            binding.tvGender.text = uiModel.genderText
            binding.tvAccountHint.text = uiModel.accountHint
            binding.tvCampusHint.text = uiModel.campusHint
            binding.tvActivityHint.text = uiModel.activityHint
            binding.tvGroupSummary.text = uiModel.groupSummary
            binding.tvPrimaryAction.text = uiModel.primaryActionText
            binding.tvSecondaryAction.text = uiModel.secondaryActionText
        }
        viewModel.commonGroups.observe(viewLifecycleOwner) { groups ->
            commonGroupAdapter.submitList(groups)
        }
        viewModel.openConversationId.observe(viewLifecycleOwner) { conversationId ->
            if (conversationId.isNullOrBlank()) return@observe
            val action = UserDetailFragmentDirections.actionUserDetailFragmentToFragmentChat(conversationId)
            findNavController().navigate(action)
            viewModel.onConversationNavigationConsumed()
        }
        viewModel.openGroupDetailId.observe(viewLifecycleOwner) { groupId ->
            if (groupId == null || groupId <= 0L) return@observe
            val action = UserDetailFragmentDirections.actionUserDetailFragmentToGroupDetailFragment(groupId)
            findNavController().navigate(action)
            viewModel.onGroupDetailNavigationConsumed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
