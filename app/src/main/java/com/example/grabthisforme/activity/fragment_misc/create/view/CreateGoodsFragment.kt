package com.example.grabthisforme.activity.fragment_misc.create.view

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.bumptech.glide.Glide
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.view.BottomSheetDialogPhoto
import com.example.grabthisforme.activity.fragment_misc.create.model.CreateGoodsRegistration
import com.example.grabthisforme.activity.fragment_misc.create.viewModel.CreateGoodsViewModel
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentCreatGoodsBinding
import com.example.grabthisforme.util.KeyboardScrollHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateGoodsFragment : Fragment() {
    private val args : CreateGoodsFragmentArgs by navArgs()
    private var _binding: FragmentCreatGoodsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateGoodsViewModel by viewModels()
    private var keyboardScrollHelper: KeyboardScrollHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatGoodsBinding.inflate(inflater, container, false)
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
        binding.ivGoodsPic.setOnClickListener {
            showPhotoSelector()
        }
        binding.btnCreateGoods.setOnClickListener {
            clearInputFocus()
            viewModel.submitCreateGoods(
                CreateGoodsRegistration(
                    name = binding.etGoodsName.text?.toString()?.trim().orEmpty(),
                    description = binding.etGoodsDescription.text?.toString()?.trim().orEmpty(),
                    categoryText = binding.etGoodsCategory.text?.toString()?.trim().orEmpty(),
                    priceText = binding.etGoodsPrice.text?.toString()?.trim().orEmpty(),
                    discountPriceText = binding.etGoodsDiscountPrice.text?.toString()?.trim().orEmpty(),
                    tagText = binding.etGoodsTag.text?.toString()?.trim().orEmpty(),
                    stockText = binding.etGoodsStock.text?.toString()?.trim().orEmpty(),
                    imageUrl = viewModel.goodsPic.value.orEmpty()
                ),
                storeId = args.storeId
            )
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

    private fun observeGoodsPic() {
        viewModel.goodsPic.observe(viewLifecycleOwner) { photoUrl ->
            renderGoodsPicture(photoUrl)
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
        photoBottomSheet.show(childFragmentManager, "CreateGoodsPhotoBottomSheet")
    }

    private fun renderGoodsPicture(photoUrl: String) {
        if (photoUrl.isBlank()) {
            binding.ivGoodsPic.setImageResource(R.drawable.market_icon_photo)
            return
        }
        Glide.with(this)
            .load(photoUrl)
            .placeholder(R.drawable.market_icon_photo)
            .error(R.drawable.market_icon_photo)
            .into(binding.ivGoodsPic)
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
