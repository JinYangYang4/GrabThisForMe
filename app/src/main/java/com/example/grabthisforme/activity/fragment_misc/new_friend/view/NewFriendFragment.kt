package com.example.grabthisforme.activity.fragment_misc.new_friend.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.activity.fragment_misc.new_friend.viewmodel.NewFriendViewModel
import com.example.grabthisforme.activity.fragment_misc.search.friend.adapter.SearchFriendOrGroupResultAdapter
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentNewFriendBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewFriendFragment : Fragment() {

    private var _binding: FragmentNewFriendBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NewFriendViewModel by viewModels()

    private lateinit var adapter: SearchFriendOrGroupResultAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initListener()
        initObserve()
    }

    private fun initRecyclerView() {
        adapter = SearchFriendOrGroupResultAdapter(
            onItemClick = { stableId -> viewModel.onItemClick(stableId) },
            onActionClick = { stableId -> viewModel.onActionClick(stableId) }
        )
        binding.rvNewFriend.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNewFriend.adapter = adapter
        binding.rvNewFriend.itemAnimator = null
    }

    private fun initListener() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun initObserve() {
        viewModel.requestItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
        viewModel.openUserDetailId.observe(viewLifecycleOwner) { userId ->
            if (userId == null || userId <= 0L) return@observe
            val action = NewFriendFragmentDirections.actionNewFriendFragmentToUserDetailFragment(userId)
            findNavController().navigate(action)
            viewModel.onUserDetailNavigationConsumed()
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
