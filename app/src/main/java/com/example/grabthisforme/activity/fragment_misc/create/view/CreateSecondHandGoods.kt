package com.example.grabthisforme.activity.fragment_misc.create.view


import android.content.Context
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
import com.example.grabthisforme.activity.fragment_misc.create.viewModel.CreateSecondHandGoodsViewModel
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentCreateSecondhandGoodsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateSecondHandGoods : Fragment() {
    private var _binding: FragmentCreateSecondhandGoodsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateSecondHandGoodsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateSecondhandGoodsBinding.inflate(inflater, container, false)
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
        binding.llNested.setOnClickListener {
            clearInputFocus()
        }

        binding.btnPublishSecondhand.isEnabled = true

        binding.btnMinus.setOnClickListener {
            val current = binding.tvSaleNumber.text.toString().toLongOrNull() ?: 1L
            binding.tvSaleNumber.text = current.minus(1).coerceAtLeast(1L).toString()
        }

        binding.btnPlus.setOnClickListener {
            val current = binding.tvSaleNumber.text.toString().toLongOrNull() ?: 1L
            binding.tvSaleNumber.text = current.plus(1).coerceAtMost(999L).toString()
        }

        binding.btnPublishSecondhand.setOnClickListener {
            clearInputFocus()
            viewModel.submitSecondhandGoods(
                name = binding.itSecondhandName.text?.toString()?.trim().orEmpty(),
                message = binding.itSecondhandMessage.text?.toString()?.trim().orEmpty(),
                secondhandPriceText = binding.itSecondhandPrice.text?.toString()?.trim().orEmpty(),
                originalPriceText = binding.itSecondhandOriginalPrice.text?.toString()?.trim().orEmpty(),
                quality = binding.itSecondhandQuality.text?.toString()?.trim().orEmpty(),
                usedTime = binding.itSecondhandUsedTime.text?.toString()?.trim().orEmpty(),
                remark = binding.itSecondhandRemark.text?.toString()?.trim().orEmpty(),
                saleNumberText = binding.tvSaleNumber.text?.toString()?.trim().orEmpty(),
                pic = binding.ivSecondhandPic.tag?.toString().orEmpty(),
                categoryText = binding.spSecondhandCategory.selectedItem?.toString().orEmpty()
            )
        }
    }

    private fun clearInputFocus(){
        val currentFocus = requireActivity().currentFocus ?: return
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
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
