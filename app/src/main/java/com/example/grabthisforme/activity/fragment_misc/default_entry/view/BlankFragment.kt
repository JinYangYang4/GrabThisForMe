package com.example.grabthisforme.activity.fragment_misc.default_entry.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.mainactivity.view.MainActivity

class BlankFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_blank, container, false)
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as? MainActivity)?.showBottomBar()

    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}