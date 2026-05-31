package com.example.grabthisforme.activity.fragment_misc.create.view

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import androidx.fragment.app.viewModels
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.view.BottomSheetDialogPhoto
import com.example.grabthisforme.activity.fragment_misc.create.viewModel.CreateOrderViewModel
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentCreateOrderBinding
import com.example.grabthisforme.util.KeyboardScrollHelper
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class CreateOrderFragment : Fragment() {
    private var _binding: FragmentCreateOrderBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateOrderViewModel by viewModels()
    private var keyboardScrollHelper: KeyboardScrollHelper? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateOrderBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeCreateResult()
        observeGoodsPic()
        keyboardScrollHelper = KeyboardScrollHelper(
            rootView = requireView(),
            scrollView = binding.nestedScrollView,
            density = resources.displayMetrics.density,
            onImeHidden = { if (_binding != null) clearInputFocus() }
        ).also { it.setup() }
    }
    private fun clearInputFocus() {
        val currentFocus = requireActivity().currentFocus ?: return
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        currentFocus.clearFocus()
    }

    private fun observeCreateResult() {
        viewModel.createResult.observe(viewLifecycleOwner) { result ->
            Toast.makeText(requireContext(), result.message, Toast.LENGTH_SHORT).show()
            if (result.success) {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun observeGoodsPic() {
        viewModel.goodsPic.observe(viewLifecycleOwner) { photoUrl ->
            renderGoodsPicture(photoUrl)
        }
    }

    private fun initView() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.rbBuyGoods.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.setBuyGoodsMode(true)
            }
        }

        binding.rbGetExpress.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.setBuyGoodsMode(false)
            }
        }

        binding.btnCreateOrder.isEnabled = true

        binding.btnMinus.setOnClickListener {
            val current = binding.tvSaleNumber.text.toString().toLongOrNull() ?: 1L
            binding.tvSaleNumber.text = current.minus(1).coerceAtLeast(1L).toString()
        }

        binding.btnPlus.setOnClickListener {
            val current = binding.tvSaleNumber.text.toString().toLongOrNull() ?: 1L
            binding.tvSaleNumber.text = current.plus(1).coerceAtMost(999L).toString()
        }
        binding.ivGoodsPic.setOnClickListener {
            showPhotoSelector()
        }

        binding.btnCreateOrder.setOnClickListener {
            clearInputFocus()
            if (viewModel.buyGoodsMode.value == true) {
                viewModel.submitBuyGoodsOrder(
                    goodsName = binding.tiGoodsName.text?.toString()?.trim().orEmpty(),
                    goodsPriceText = binding.tiGoodsPrice.text?.toString()?.trim().orEmpty(),
                    goodsMessage = binding.tiGoodsMessage.text?.toString()?.trim().orEmpty(),
                    goodsPic = viewModel.goodsPic.value.orEmpty(),
                    saleNumber = binding.tvSaleNumber.text.toString().toLongOrNull() ?: 1L,
                    aimPosition = binding.tiAimPosition.text?.toString()?.trim().orEmpty(),
                    shelfNumber = binding.tiShelfNumber.text?.toString()?.trim().orEmpty(),
                    startTimeText = binding.etStartTime.text?.toString()?.trim().orEmpty(),
                    endTimeText = binding.etEndTime.text?.toString()?.trim().orEmpty()
                )
            } else {
                viewModel.submitExpressOrder(
                    expressNo = binding.etExpressNo.text?.toString()?.trim().orEmpty(),
                    expressCompany = binding.tiExpressCompany.text?.toString()?.trim().orEmpty(),
                    expressPosition = binding.tiExpressPosition.text?.toString()?.trim().orEmpty(),
                    pickupCode = binding.etPickupCode.text?.toString()?.trim().orEmpty(),
                    remark = binding.tiExpressRemark.text?.toString()?.trim().orEmpty(),
                    startTimeText = binding.etExpressStartTime.text?.toString()?.trim().orEmpty(),
                    endTimeText = binding.etExpressEndTime.text?.toString()?.trim().orEmpty()
                )
            }
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

            // 显示选择器
            timePicker.show()
        }
        binding.etExpressStartTime.setOnClickListener {
            val timePicker = TimePickerBuilder(requireActivity()) { date, _ ->
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
                val selectTime = format.format(date)
                viewModel.setExpressStartTime(selectTime)
            }
                .setType(booleanArrayOf(true, true, true, true, true, false))
                .setCancelText("取消")
                .setSubmitText("确认")
                .setTitleText("选择服务时间")
                .setTitleColor(Color.BLACK)
                .setSubmitColor(Color.parseColor("#FF5722"))
                .build()
                .setOnDismissListener {
                    clearInputFocus()
                }

            timePicker.show()
        }
        binding.etExpressEndTime.setOnClickListener {
            val timePicker = TimePickerBuilder(requireActivity()) { date, _ ->
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
                val selectTime = format.format(date)
                viewModel.setExpressEndTime(selectTime)
            }
                .setType(booleanArrayOf(true, true, true, true, true, false))
                .setCancelText("取消")
                .setSubmitText("确认")
                .setTitleText("选择服务时间")
                .setTitleColor(Color.BLACK)
                .setSubmitColor(Color.parseColor("#FF5722")) // 主题色
                .build()

            timePicker.show()
        }
        binding.root.setOnClickListener {
            clearInputFocus()
        }
        binding.llNested.setOnClickListener {
            clearInputFocus()
        }
    }

    private fun showPhotoSelector() {
        val photoBottomSheet = BottomSheetDialogPhoto.newInstance(BottomSheetDialogPhoto.SELECT_NUM_LIMIT)
        photoBottomSheet.setOnPhotosSelectedListener(object : BottomSheetDialogPhoto.OnPhotosSelectedListener {
            override fun onPhotosSelected(photos: List<Uri>) {
                val goodsUri = photos.firstOrNull() ?: return
                viewModel.setGoodsPic(goodsUri.toString())
            }
        })
        photoBottomSheet.show(childFragmentManager, "OrderGoodsPhotoBottomSheet")
    }

    private fun renderGoodsPicture(photoUrl: String) {
        if (photoUrl.isBlank()) {
            binding.ivGoodsPic.setImageResource(R.drawable.ic_add)
            return
        }
        Glide.with(this)
            .load(photoUrl)
            .placeholder(R.drawable.ic_add)
            .error(R.drawable.ic_add)
            .into(binding.ivGoodsPic)
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
