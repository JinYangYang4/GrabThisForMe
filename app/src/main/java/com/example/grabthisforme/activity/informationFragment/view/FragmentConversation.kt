package com.example.grabthisforme.activity.informationFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.activity.fragment_misc.default_entry.view.BlankFragmentDirections
import com.example.grabthisforme.activity.informationFragment.adapter.ConversationRecyclerViewAdapter
import com.example.grabthisforme.activity.informationFragment.ui_model.ConversationListItemUiModel
import com.example.grabthisforme.activity.informationFragment.viewmodel.InformationViewModel
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentMessageBinding
import com.example.grabthisforme.ui.menu.BubbleArrowMenuPopup
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentConversation : Fragment() {

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InformationViewModel by viewModels({ requireParentFragment() })

    private var conversationMenuPopup: BubbleArrowMenuPopup? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        conversationMenuPopup = BubbleArrowMenuPopup(requireContext())
        initRecyclerView()
        initObserve()
    }

    private fun initRecyclerView() {
        val conversationAdapter = ConversationRecyclerViewAdapter(
            clickListener = { conversationId ->
                val action = BlankFragmentDirections.actionBlankFragmentToFragmentChat(conversationId)
                (requireActivity() as MainActivity).NewNavController_navgite(action)
            },
            longClickListener = { anchor, item ->
                showConversationMenu(anchor, item)
            }
        )
        binding.rvMassage.adapter = conversationAdapter
        binding.rvMassage.layoutManager = LinearLayoutManager(context)
    }

    private fun showConversationMenu(anchor: View, item: ConversationListItemUiModel) {
        conversationMenuPopup?.show(
            anchor = anchor,
            items = listOf("隐藏", "标记为已读")
        ) { _, title ->
            when (title) {
                "隐藏" -> viewModel.hideConversation(item.conversationId)
                "标记为已读" -> viewModel.markConversationAsRead(item.conversationId)
            }
        }
    }

    private fun initObserve() {
        viewModel.conversations.observe(viewLifecycleOwner) { conversations ->
            (binding.rvMassage.adapter as? ConversationRecyclerViewAdapter)?.submitList(conversations)
        }
    }

    override fun onDestroyView() {
        conversationMenuPopup?.dismiss()
        conversationMenuPopup = null
        super.onDestroyView()
        _binding = null
    }
}
