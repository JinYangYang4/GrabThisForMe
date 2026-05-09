package com.example.grabthisforme.activity.fragment_misc.register_store.view

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.view.BottomSheetDialogPhoto
import com.example.grabthisforme.activity.fragment_misc.register_store.viewmodel.RegisterStoreViewModel
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentStoreOwnerRegisterBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class RegisterStoreFragment : Fragment() {
    private var _binding: FragmentStoreOwnerRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel : RegisterStoreViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreOwnerRegisterBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeCreateResult()
        ViewCompat.setOnApplyWindowInsetsListener(requireView()) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (!imeVisible && _binding != null) {
                clearInputFocus()
            }
            insets
        }
    }

    private fun observeCreateResult() {
        viewModel.createResult.observe(viewLifecycleOwner) { result ->
            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
            if (result.success) {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun initView() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.ivHeadPic.setOnClickListener {
            showPhotoSelector()
        }
        binding.llFormContainer.setOnClickListener {
            clearInputFocus()
        }
        binding.root.setOnClickListener {
            clearInputFocus()
        }
        binding.btnRegisterStoreOwner.setOnClickListener {
            clearInputFocus()
            viewModel.submitRegisterStore(
                storeName = binding.etStoreName.text?.toString()?.trim().orEmpty(),
                storeType = binding.etStoreType.text?.toString()?.trim().orEmpty(),
                storeAddress = binding.etStoreAddress.text?.toString()?.trim().orEmpty(),
                phone = binding.etPhone.text?.toString()?.trim().orEmpty(),
                startTimeText = binding.etStartTime.text?.toString()?.trim().orEmpty(),
                endTimeText = binding.etEndTime.text?.toString()?.trim().orEmpty(),
                minOrderAmountText = binding.etMinOrderAmount.text?.toString()?.trim().orEmpty(),
                deliveryFeeText = binding.etDeliveryFee.text?.toString()?.trim().orEmpty(),
                pic = binding.ivHeadPic.tag?.toString().orEmpty(),
                tagsText = binding.etStoreTags.text?.toString()?.trim().orEmpty(),
                isOpen = binding.switchIsOpen.isChecked
            )
        }
        binding.etStartTime.setOnClickListener {
            val timePicker = TimePickerBuilder(requireActivity()) { date, _ ->
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
                val selectTime = format.format(date)
                viewModel.setStartTime(selectTime)
            }
                .setType(booleanArrayOf(true, true, true, true, true, false))
                .setCancelText("取消")
                .setSubmitText("确认")
                .setTitleText("选择服务时间")
                .setTitleColor(Color.BLACK)
                .setSubmitColor(Color.parseColor("#FF5722"))
                .build()

            timePicker.show()
        }
        binding.etEndTime.setOnClickListener {
            val timePicker = TimePickerBuilder(requireActivity()) { date, _ ->
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
                val selectTime = format.format(date)
                viewModel.setEndTime(selectTime)
            }
                .setType(booleanArrayOf(true, true, true, true, true, false)) // 显示：年、月、日、时、分（关闭秒）
                .setCancelText("取消")
                .setSubmitText("确认")
                .setTitleText("选择服务时间")
                .setTitleColor(Color.BLACK)
                .setSubmitColor(Color.parseColor("#FF5722")) // 主题色
                .build()
            timePicker.show()
        }
    }

    private fun showPhotoSelector() {
        val photoBottomSheet = BottomSheetDialogPhoto.newInstance(BottomSheetDialogPhoto.SELECT_NUM_LIMIT)
        photoBottomSheet.setOnPhotosSelectedListener(object : BottomSheetDialogPhoto.OnPhotosSelectedListener {
            override fun onPhotosSelected(photos: List<Uri>) {
                val avatarUri = photos.firstOrNull() ?: return
                renderAvatar(avatarUri.toString())
            }
        })
        photoBottomSheet.show(childFragmentManager, "StorePhotoBottomSheet")
    }

    private fun renderAvatar(avatarUrl: String) {
        if (avatarUrl.isBlank()) {
            binding.ivHeadPic.setImageResource(com.example.grabthisforme.R.drawable.ic_add)
            binding.ivHeadPic.tag = null
            return
        }
        binding.ivHeadPic.tag = avatarUrl
        Glide.with(this)
            .load(avatarUrl)
            .placeholder(com.example.grabthisforme.R.drawable.ic_add)
            .error(com.example.grabthisforme.R.drawable.ic_add)
            .into(binding.ivHeadPic)
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
        _binding = null
    }
}
