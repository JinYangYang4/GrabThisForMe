package com.example.grabthisforme.activity.fragment_misc.create.view

import android.app.ActionBar
import android.content.Context
import android.graphics.Color
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.R
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.example.grabthisforme.activity.fragment_misc.create.viewModel.CreateOrderViewModel
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentCreateOrderBinding
import java.text.SimpleDateFormat
import java.util.Locale


class CreateOrderFragment : Fragment() {
    private var _binding: FragmentCreateOrderBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateOrderViewModel by viewModels()


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
        ViewCompat.setOnApplyWindowInsetsListener(requireView()) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (!imeVisible && _binding != null) {
                clearInputFocus()
            }
            insets
        }
    }
    private fun clearInputFocus() {
        val currentFocus = requireActivity().currentFocus ?: return
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        currentFocus.clearFocus()
    }
    fun initView(){
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

        binding.etStartTime.setOnClickListener {
            val timePicker = TimePickerBuilder(requireContext()) { date, v ->
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
            val timePicker = TimePickerBuilder(requireContext()) { date, v ->
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
            val timePicker = TimePickerBuilder(requireContext()) { date, v ->
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
            val timePicker = TimePickerBuilder(requireContext()) { date, v ->
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

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).innerBottomBar()
    }





    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}