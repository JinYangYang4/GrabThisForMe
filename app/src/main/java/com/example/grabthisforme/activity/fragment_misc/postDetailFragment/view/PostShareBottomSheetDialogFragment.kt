package com.example.grabthisforme.activity.fragment_misc.postDetailFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.postDetailFragment.adapter.SharePostRecyclerviewAdapter
import com.example.grabthisforme.databinding.PostShareBottomSheetDialogFragmentBinding
import com.example.grabthisforme.model.friendAndGroup.Friend
import com.example.grabthisforme.model.friendAndGroup.Group
import com.example.grabthisforme.model.friendAndGroup.SelectableItem
import com.example.grabthisforme.model.user.User

import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PostShareBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: PostShareBottomSheetDialogFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PostShareBottomSheetDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        setClickListeners()
    }
    private fun initRecyclerView() {
        val rvShareToUser = binding.rvShareToUser

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rvShareToUser.layoutManager = layoutManager

        val shareAdapter = SharePostRecyclerviewAdapter { itemId ->

        }
        rvShareToUser.adapter = shareAdapter
        rvShareToUser.setHasFixedSize(true)
        val testData = buildTestSelectableItems()
        shareAdapter.submitList(testData)
    }

    private fun buildTestSelectableItems(): List<SelectableItem> {
        return listOf(
            // 好友项1
           SelectableItem.SelectableFriend(
                Friend(
                    friendId = 1001L,
                    who = User(
                        id = 1001L,
                        name = "神秘下头男",
                        headPic = ""
                    ),
                    status = Friend.FriendStatus.ACCEPTED
                )
            ),
            // 好友项2
          SelectableItem.SelectableFriend(
                Friend(
                    friendId = 1002L,
                    who = User(
                        id = 1002L,
                        name = "隔壁老王",
                        headPic = ""
                    ),
                    status = Friend.FriendStatus.ACCEPTED
                )
            ),

           SelectableItem.SelectableGroup(
               Group(
                   groupId = 2001L,
                   groupName = "摸鱼小分队",
                   members = listOf(
                       User(id = 1001L, name = "神秘下头男",headPic = ""),
                       User(id = 1002L, name = "隔壁老王",headPic = "")

                   )
               )
            ),
          SelectableItem.SelectableGroup(
                Group(
                    groupId = 2002L,
                    groupName = "程序员交流群",
                    members = emptyList()
                )
            )
        )
    }
    private fun setClickListeners() {
        binding.llCancel.setOnClickListener {
            dismiss()
        }

        binding.llSure.setOnClickListener {
            onShareClick()
        }
    }

    private fun onShareClick() {
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): PostShareBottomSheetDialogFragment {
            return PostShareBottomSheetDialogFragment()
        }
    }
}