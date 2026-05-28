package com.example.grabthisforme.activity.fragment_misc.my_store.view

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
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
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.view.BottomSheetDialogPhoto
import com.example.grabthisforme.activity.fragment_misc.my_store.viewmodel.RegisterStoreViewModel
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentStoreOwnerRegisterBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class RegisterStoreFragment : Fragment() {
    private var _binding: FragmentStoreOwnerRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RegisterStoreViewModel by viewModels()
    private var nestedScrollBaseBottomPadding = 0

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
        nestedScrollBaseBottomPadding = binding.nestedScrollView.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(requireView()) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val systemBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val keyboardSpace = (imeBottom - systemBottom).coerceAtLeast(0)
            binding.nestedScrollView.setPadding(
                binding.nestedScrollView.paddingLeft,
                binding.nestedScrollView.paddingTop,
                binding.nestedScrollView.paddingRight,
                nestedScrollBaseBottomPadding + keyboardSpace
            )
            if (imeVisible && _binding != null) {
                binding.nestedScrollView.postDelayed({
                    scrollFocusedInputIntoView()
                }, KEYBOARD_SCROLL_DELAY_MS)
            }
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
        binding.llNested.setOnClickListener {
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
            buildTimePicker(
                title = "选择开始营业时间",
                onSelected = viewModel::setStartTime
            ).show()
        }
        binding.etEndTime.setOnClickListener {
            buildTimePicker(
                title = "选择结束营业时间",
                onSelected = viewModel::setEndTime
            ).show()
        }
    }

    private fun buildTimePicker(
        title: String,
        onSelected: (String) -> Unit
    ) = TimePickerBuilder(requireActivity()) { date, _ ->
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
        onSelected(format.format(date))
    }
        .setType(booleanArrayOf(true, true, true, true, true, false))
        .setCancelText("取消")
        .setSubmitText("确认")
        .setTitleText(title)
        .setTitleColor(Color.BLACK)
        .setSubmitColor(Color.parseColor("#FF5722"))
        .build()

    private fun scrollFocusedInputIntoView() {
        val currentFocus = requireActivity().currentFocus ?: return
        val scrollView = binding.nestedScrollView
        if (!isDescendantOf(currentFocus, scrollView)) return

        val focusedRect = Rect()
        currentFocus.getDrawingRect(focusedRect)
        scrollView.offsetDescendantRectToMyCoords(currentFocus, focusedRect)

        val visibleTop = scrollView.scrollY
        val visibleBottom = visibleTop + scrollView.height - scrollView.paddingBottom
        val extraSpacing = KEYBOARD_FOCUS_SPACING_DP.dpToPx()

        when {
            focusedRect.bottom + extraSpacing > visibleBottom -> {
                val targetY = focusedRect.bottom - scrollView.height + scrollView.paddingBottom + extraSpacing
                scrollView.smoothScrollTo(0, targetY.coerceAtLeast(0))
            }
            focusedRect.top - extraSpacing < visibleTop -> {
                scrollView.smoothScrollTo(0, (focusedRect.top - extraSpacing).coerceAtLeast(0))
            }
        }
    }

    private fun isDescendantOf(child: View, parent: View): Boolean {
        var current: View? = child
        while (current != null) {
            if (current == parent) return true
            current = current.parent as? View
        }
        return false
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
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
            binding.ivHeadPic.setImageResource(R.drawable.market_icon_photo)
            binding.ivHeadPic.tag = null
            return
        }
        binding.ivHeadPic.tag = avatarUrl
        Glide.with(this)
            .load(avatarUrl)
            .placeholder(R.drawable.market_icon_photo)
            .error(R.drawable.market_icon_photo)
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

    companion object {
        private const val KEYBOARD_SCROLL_DELAY_MS = 120L
        private const val KEYBOARD_FOCUS_SPACING_DP = 24
    }
}
