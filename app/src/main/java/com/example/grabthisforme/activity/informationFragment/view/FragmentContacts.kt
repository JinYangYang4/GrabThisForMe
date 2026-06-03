package com.example.grabthisforme.activity.informationFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.activity.fragment_misc.default_entry.view.BlankFragmentDirections
import com.example.grabthisforme.activity.informationFragment.adapter.AllFriendOrGroupRecyclerViewAdapter
import com.example.grabthisforme.activity.informationFragment.viewmodel.InformationViewModel
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentContactsBinding
import com.example.grabthisforme.model.friendAndGroup.ContactItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentContacts : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InformationViewModel by viewModels({ requireParentFragment() })

    private lateinit var adapter: AllFriendOrGroupRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initObserve()
    }

    private fun initRecyclerView() {
        binding.rvAll.layoutManager = LinearLayoutManager(requireContext())
        adapter = AllFriendOrGroupRecyclerViewAdapter { item ->
            when (item) {
                is ContactItem.FriendItem -> viewModel.onFriendClicked(item.friend)
                is ContactItem.GroupItem -> viewModel.onGroupClicked(item.group)
                else -> Unit
            }
        }
        binding.rvAll.adapter = adapter
    }

    private fun initObserve() {
        viewModel.contactItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
        viewModel.openConversationId.observe(viewLifecycleOwner) { conversationId ->
            if (conversationId.isNullOrBlank()) return@observe
            val action = BlankFragmentDirections.actionBlankFragmentToFragmentChat(conversationId)
            (requireActivity() as MainActivity).NewNavController_navgite(action)
            viewModel.onConversationNavigationConsumed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
