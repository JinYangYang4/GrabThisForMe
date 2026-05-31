package com.example.grabthisforme.activity.fragment_misc.create.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.view.BottomSheetDialogPhoto
import com.example.grabthisforme.activity.fragment_misc.create.viewModel.CreateSecondHandGoodsViewModel
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentCreateSecondhandGoodsBinding
import com.example.grabthisforme.util.KeyboardScrollHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateSecondHandGoods : Fragment() {
    private var _binding: FragmentCreateSecondhandGoodsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateSecondHandGoodsViewModel by viewModels()
    private var keyboardScrollHelper: KeyboardScrollHelper? = null

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
        bindCategoryOptions()
        observeQualityOptions()
        observeSelectedPhoto()
        observeCreateResult()
        keyboardScrollHelper = KeyboardScrollHelper(
            rootView = requireView(),
            scrollView = binding.nestedScrollView,
            density = resources.displayMetrics.density,
            onImeHidden = { if (_binding != null) clearInputFocus() }
        ).also { it.setup() }
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
        binding.root.setOnClickListener {
            clearInputFocus()
        }
        binding.llNested.setOnClickListener {
            clearInputFocus()
        }
        binding.ivSecondhandPic.setOnClickListener {
            openPhotoPicker()
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
                quality = binding.spSecondhandQuality.text?.toString()?.trim().orEmpty(),
                usedTime = binding.itSecondhandUsedTime.text?.toString()?.trim().orEmpty(),
                remark = binding.itSecondhandRemark.text?.toString()?.trim().orEmpty(),
                saleNumberText = binding.tvSaleNumber.text?.toString()?.trim().orEmpty(),
                pic = binding.ivSecondhandPic.tag?.toString().orEmpty(),
                categoryText = binding.spSecondhandCategory.text?.toString()?.trim().orEmpty()
            )
        }
    }

    private fun bindCategoryOptions() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.secondhand_category).toList()
        )
        binding.spSecondhandCategory.setAdapter(adapter)
    }

    private fun observeQualityOptions() {
        viewModel.qualityList.observe(viewLifecycleOwner) { qualityList ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                qualityList
            )
            binding.spSecondhandQuality.setAdapter(adapter)
        }
    }

    private fun observeSelectedPhoto() {
        viewModel.selectedPhotoUri.observe(viewLifecycleOwner) { photoUrl ->
            if (photoUrl == null) {
                binding.ivSecondhandPic.scaleType = ImageView.ScaleType.CENTER_INSIDE
                binding.ivSecondhandPic.setImageResource(R.drawable.market_icon_photo)
                binding.ivSecondhandPic.tag = ""
                return@observe
            }
            binding.ivSecondhandPic.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.market_icon_photo)
                .error(R.drawable.market_icon_photo)
                .into(binding.ivSecondhandPic)
            binding.ivSecondhandPic.tag = photoUrl.toString()
        }
    }

    private fun openPhotoPicker() {
        val dialog = BottomSheetDialogPhoto.newInstance(BottomSheetDialogPhoto.SELECT_NUM_LIMIT)
        dialog.setOnPhotosSelectedListener(object : BottomSheetDialogPhoto.OnPhotosSelectedListener {
            override fun onPhotosSelected(photos: List<android.net.Uri>) {
                photos.firstOrNull()?.let { viewModel.selectPhoto(it) }
            }
        })
        dialog.show(childFragmentManager, "select_secondhand_photo")
    }

    private fun clearInputFocus() {
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
        keyboardScrollHelper?.teardown()
        keyboardScrollHelper = null
        _binding = null
    }
}
