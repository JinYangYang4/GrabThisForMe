package com.example.grabthisforme.activity.informationFragment.view


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.MainActivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentMessageBinding
import com.example.grabthisforme.activity.informationFragment.adapter.ConversationRecyclerViewAdapter
import com.example.grabthisforme.model.conversation.Conversation

class FragmentConversation : Fragment() {

    private  var _binding: FragmentMessageBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }
    fun initRecyclerView(){
        val messageAdapter = ConversationRecyclerViewAdapter() {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_blankFragment_to_fragmentChat)
        }
        binding.rvMassage.adapter = messageAdapter

        binding.rvMassage.layoutManager = LinearLayoutManager(context)
        val conversationList = Conversation.generateFakeConversations(10,1)
        Log.d("test111", "initRecyclerView: ")
        messageAdapter.submitList(conversationList)
    }

}
