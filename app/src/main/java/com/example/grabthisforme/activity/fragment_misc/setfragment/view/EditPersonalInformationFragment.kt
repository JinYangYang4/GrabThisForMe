package com.example.grabthisforme.activity.fragment_misc.setfragment.view

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.view.BottomSheetDialogPhoto
import com.example.grabthisforme.activity.fragment_misc.setfragment.viewmodel.EditPersonalInformationViewModel
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentEditPersonalInformationBinding
import com.example.grabthisforme.model.user.domain.UserProfile
import com.example.grabthisforme.util.KeyboardScrollHelper
import com.example.grabthisforme.util.ViewAnimationUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditPersonalInformationFragment : Fragment() {
    private var _binding: FragmentEditPersonalInformationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditPersonalInformationViewModel by viewModels()
    private var keyboardScrollHelper: KeyboardScrollHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPersonalInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeState()
        observeSaveResult()
        ViewAnimationUtils.animateStaggeredEntrance(
            binding.accountInfoCard,
            binding.profileCard,
            binding.saveButtonCard
        )
        keyboardScrollHelper = KeyboardScrollHelper(
            rootView = requireView(),
            scrollView = binding.nestedScrollView,
            density = resources.displayMetrics.density,
            onImeHidden = { if (_binding != null) clearInputFocus() }
        ).also { it.setup() }
    }

    private fun initView() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.ivHeadPic.setOnClickListener {
            showPhotoSelector()
        }
        // 昵称与账号名保持一致，不可编辑，随账号名联动
        binding.etDisplayName.isEnabled = false
        binding.etDisplayName.isFocusable = false
        binding.etAccountName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.etDisplayName.setText(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnSaveUserInfo.setOnClickListener {
            clearInputFocus()

            viewModel.saveUserInfo(
                accountName = binding.etAccountName.text?.toString().orEmpty(),
                phone = binding.etPhone.text?.toString().orEmpty(),
                email = binding.etEmail.text?.toString().orEmpty(),
                signature = binding.etSignature.text?.toString().orEmpty(),
                gender = getSelectedGender()
            )
        }
        binding.llFormContainer.setOnClickListener {
            clearInputFocus()
        }
        binding.root.setOnClickListener {
            clearInputFocus()
        }
    }


    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.formState.collectLatest { state ->
                binding.etUserId.setText(state.userId.toString())
                binding.etAccountName.setText(state.accountName)
                binding.etPhone.setText(state.phone)
                binding.etEmail.setText(state.email)
                binding.etSignature.setText(state.signature)
                setSelectedGender(state.gender)
                renderAvatar(state.avatarUrl)
            }
        }
    }

    private fun observeSaveResult() {
        viewModel.saveResult.observe(viewLifecycleOwner) { result ->
            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
            if (result.success) {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun showPhotoSelector() {
        val photoBottomSheet = BottomSheetDialogPhoto.newInstance(BottomSheetDialogPhoto.SELECT_NUM_LIMIT)
        photoBottomSheet.setOnPhotosSelectedListener(object : BottomSheetDialogPhoto.OnPhotosSelectedListener {
            override fun onPhotosSelected(photos: List<Uri>) {
                val avatarUri = photos.firstOrNull() ?: return
                viewModel.updateAvatar(avatarUri.toString())
            }
        })
        photoBottomSheet.show(childFragmentManager, "AvatarPhotoBottomSheet")
    }

    private fun renderAvatar(avatarUrl: String) {
        if (avatarUrl.isBlank()) {
            binding.ivHeadPic.setImageResource(R.drawable.ic_add)
            return
        }
        Glide.with(this)
            .load(avatarUrl)
            .placeholder(R.drawable.ic_add)
            .error(R.drawable.ic_add)
            .into(binding.ivHeadPic)
    }

    private fun getSelectedGender(): Int {
        return when (binding.rgGender.checkedRadioButtonId) {
            binding.rbGenderMale.id -> UserProfile.GENDER_MALE
            binding.rbGenderFemale.id -> UserProfile.GENDER_FEMALE
            else -> UserProfile.GENDER_UNKNOWN
        }
    }

    private fun setSelectedGender(gender: Int) {
        val targetId = when (gender) {
            UserProfile.GENDER_MALE -> binding.rbGenderMale.id
            UserProfile.GENDER_FEMALE -> binding.rbGenderFemale.id
            else -> binding.rbGenderUnknown.id
        }
        binding.rgGender.check(targetId)
    }

    private fun clearInputFocus() {
        val currentFocus = requireActivity().currentFocus ?: return
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        currentFocus.clearFocus()
    }


    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).innerBottomBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        keyboardScrollHelper?.teardown()
        keyboardScrollHelper = null
        _binding = null
    }
}
