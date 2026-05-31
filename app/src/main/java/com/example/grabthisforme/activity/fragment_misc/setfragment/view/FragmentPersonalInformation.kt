package com.example.grabthisforme.activity.fragment_misc.setfragment.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.fragment_misc.setfragment.viewmodel.PersonalInfoViewModel
import com.example.grabthisforme.databinding.FragmentPersonalInformationBinding
import com.example.grabthisforme.util.ViewAnimationUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentPersonalInformation : Fragment() {

    private var _binding: FragmentPersonalInformationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PersonalInfoViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalInformationBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
        initObserve()
        ViewAnimationUtils.animateStaggeredEntrance(binding.infoCard)
    }

    private fun initListener() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.llSetMyInformation.setOnClickListener {
            (requireActivity() as MainActivity).intentToMiscFragment(R.id.action_personalInformation_to_editPersonalInformationFragment)
        }

    }
    fun initObserve(){
        viewModel.headPic.observe(viewLifecycleOwner){headPicUrl ->
            if (headPicUrl.isBlank()) {
                binding.ivAvatar.setImageResource(R.drawable.ic_add)
            }
            Glide.with(this)
                .load(headPicUrl)
                .placeholder(R.drawable.cat)
                .error(R.drawable.cat)
                .into(binding.ivAvatar)

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
