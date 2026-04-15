package com.example.grabthisforme.activity.informationFragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.databinding.FragmentFriendBinding
import com.example.grabthisforme.activity.informationFragment.adapter.AllFriendOrGroupRecyclerViewAdapter
import com.example.grabthisforme.model.friendAndGroup.ContactItem
import com.example.grabthisforme.model.friendAndGroup.Friend
import com.example.grabthisforme.model.user.User
import com.example.grabthisforme.model.friendAndGroup.Group

class FragmentFriend : Fragment() {

    private var _binding: FragmentFriendBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AllFriendOrGroupRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()

        // 加载好友和群组数据
        val contactItems = generateFakeContactItems()
        adapter.submitList(contactItems)
    }

    private fun initRecyclerView() {
        binding.rvAll.layoutManager = LinearLayoutManager(requireContext())
        adapter = AllFriendOrGroupRecyclerViewAdapter { id ->
        }
        binding.rvAll.adapter = adapter
    }

    private fun generateFakeContactItems(): List<ContactItem> {
        // 假设的好友数据
        val fakeFriends = listOf(
            User(name = "张三", id = 1, headPic = "https://example.com/avatar1.png"),
            User(name = "李四", id = 2, headPic = "https://example.com/avatar2.png"),
            User(name = "王五", id = 3, headPic = "https://example.com/avatar3.png")
        )

        // 假设的群组数据
        val fakeGroups = listOf(
            Group(groupId = 1, groupName = "技术交流群", members = fakeFriends),
            Group(groupId = 2, groupName = "同学聚会", members = fakeFriends),
            Group(groupId = 1, groupName = "技术交流群", members = fakeFriends),
            Group(groupId = 1, groupName = "技术交流群", members = fakeFriends),
            Group(groupId = 2, groupName = "同学聚会", members = fakeFriends),
            Group(groupId = 1, groupName = "技术交流群", members = fakeFriends),
            Group(groupId = 1, groupName = "技术交流群", members = fakeFriends),
            Group(groupId = 2, groupName = "同学聚会", members = fakeFriends),
            Group(groupId = 1, groupName = "技术交流群", members = fakeFriends),
            Group(groupId = 1, groupName = "技术交流群", members = fakeFriends),
            Group(groupId = 2, groupName = "同学聚会", members = fakeFriends),
            Group(groupId = 1, groupName = "技术交流群", members = fakeFriends),
            Group(groupId = 1, groupName = "技术交流群", members = fakeFriends),
            Group(groupId = 2, groupName = "同学聚会", members = fakeFriends),
            Group(groupId = 1, groupName = "技术交流群", members = fakeFriends),
            Group(groupId = 2, groupName = "同学聚会", members = fakeFriends)
        )

        val contactItems = mutableListOf<ContactItem>()

        // 添加好友分组头
        contactItems.add(ContactItem.FriendHeader("我的好友"))
        fakeFriends.forEach { user ->
            contactItems.add(
                ContactItem.FriendItem(
                    Friend(friendId = user.id, who = user, status = Friend.FriendStatus.ACCEPTED)
                )
            )
        }

        contactItems.add(ContactItem.GroupHeader("我的聊群"))
        fakeGroups.forEach { group ->
            contactItems.add(ContactItem.GroupItem(group))
        }

        return contactItems
    }

}
