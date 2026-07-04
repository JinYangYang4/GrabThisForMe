package com.example.grabthisforme.activity.homeFragment.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.mainactivity.core.navigation.AppNavigator
import com.example.grabthisforme.activity.mainactivity.viewmodel.MainViewModel
import com.example.grabthisforme.databinding.FragmentHomeContainerBinding

class FragmentHomeContainer : Fragment() {
    private var _binding : FragmentHomeContainerBinding ?= null
    private val binding get() = _binding!!
    private lateinit var sharedViewModel: MainViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeContainerBinding.inflate(inflater,container,false)
        sharedViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        val containerNavHostFragment = childFragmentManager.findFragmentById(binding.fragmentHomeContainerHost.id) as NavHostFragment

        containerNavHostFragment.navController.navigatorProvider.addNavigator(
            AppNavigator(
                requireContext(),
                containerNavHostFragment.childFragmentManager,
                binding.fragmentHomeContainerHost.id
            )
        )
        containerNavHostFragment.navController.setGraph(R.navigation.nav_graph_home)
        sharedViewModel.page.observe(viewLifecycleOwner){page->
            if (page == 1){
                containerNavHostFragment.navController.navigate(R.id.fragmentHome1)
            }else{
                containerNavHostFragment.navController.navigate(R.id.fragmentHome)
            }
        }
        return binding.root
    }

}
