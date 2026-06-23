package com.example.grabthisforme.activity.fragment_misc.groupDetail.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.activity.fragment_misc.groupDetail.adapter.GroupMemberAdapter
import com.example.grabthisforme.activity.fragment_misc.groupDetail.viewModel.GroupDetailViewModel
import com.example.grabthisforme.databinding.FragmentGroupDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupDetailFragment : Fragment() {

    private var _binding: FragmentGroupDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GroupDetailViewModel by viewModels()
    private lateinit var memberAdapter: GroupMemberAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initClick()
        observeUi()
    }

    private fun initRecyclerView() {
        memberAdapter = GroupMemberAdapter { userId ->
            viewModel.onMemberClick(userId)
        }
        binding.rvMembers.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.rvMembers.adapter = memberAdapter
        binding.rvMembers.itemAnimator = null
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
            binding.tvGroupName.text = uiModel.groupName
            binding.tvMemberCount.text = uiModel.memberCountText
            binding.tvStatus.text = uiModel.statusText
            binding.tvScene.text = uiModel.sceneText
            binding.tvManager.text = uiModel.managerText
            binding.tvVibe.text = uiModel.vibeText
            binding.tvTips.text = uiModel.tipsText
            binding.tvPrimaryAction.text = uiModel.primaryActionText
            binding.tvSecondaryAction.text = uiModel.secondaryActionText
        }
        viewModel.memberList.observe(viewLifecycleOwner) { members ->
            memberAdapter.submitList(members)
        }
        viewModel.openConversationId.observe(viewLifecycleOwner) { conversationId ->
            if (conversationId.isNullOrBlank()) return@observe
            val action = GroupDetailFragmentDirections.actionGroupDetailFragmentToFragmentChat(conversationId)
            findNavController().navigate(action)
            viewModel.onConversationNavigationConsumed()
        }
        viewModel.openUserDetailId.observe(viewLifecycleOwner) { userId ->
            if (userId == null || userId <= 0L) return@observe
            val action = GroupDetailFragmentDirections.actionGroupDetailFragmentToUserDetailFragment(userId)
            findNavController().navigate(action)
            viewModel.onUserDetailNavigationConsumed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
