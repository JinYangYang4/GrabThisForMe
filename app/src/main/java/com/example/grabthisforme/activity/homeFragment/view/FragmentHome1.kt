package com.example.grabthisforme.activity.homeFragment.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.lifecycle.ViewModelProvider
import com.example.grabthisforme.activity.MainActivity.viewModel.MainViewModel
import com.example.grabthisforme.databinding.FragmentHome1Binding



class FragmentHome1 : Fragment() {
    private var _binding : FragmentHome1Binding ?= null
    private val binding get() = _binding!!
    private lateinit var sharedViewModel: MainViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHome1Binding.inflate(inflater,container,false).apply {
             composeViewCardLiquid.setContent {
                 MaterialTheme {
                     Surface {

                     }
                 }
             }
        }
        sharedViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        binding.YGet.setOnClickListener {
            sharedViewModel.toPage(0)
        }
        binding.ivHeadPic.setOnClickListener {
            sharedViewModel.drawerOpenStateToOpen()
        }
        return binding.root
    }


}