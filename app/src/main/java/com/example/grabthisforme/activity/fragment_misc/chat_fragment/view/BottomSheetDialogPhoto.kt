package com.example.grabthisforme.activity.fragment_misc.chat_fragment.view

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.adapter.PhotoRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.modle.PhotoItem
import com.example.grabthisforme.databinding.BottomSheetDialogPhotoBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BottomSheetDialogPhoto(val type : Int) : BottomSheetDialogFragment(){
    companion object {
        fun newInstance(type: Int): BottomSheetDialogPhoto {
            return BottomSheetDialogPhoto(type)
        }
        const val SELECT_NUM_LIMIT = 1
        const val SELECT_UNLIMIT = 0
    }


    private var _binding: BottomSheetDialogPhotoBinding? = null
    private val binding get() = _binding!!


    private lateinit var photoAdapter: PhotoRecyclerViewAdapter

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    interface OnPhotosSelectedListener {
        fun onPhotosSelected(photos: List<Uri>)
    }

    private var listener: OnPhotosSelectedListener? = null

    fun setOnPhotosSelectedListener(listener: OnPhotosSelectedListener) {
        this.listener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetStyle)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                loadPhotosFromGallery()
            } else {
                Toast.makeText(context, "需要照片权限才能选择图片", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = getDialog()
        if (dialog != null && dialog.window != null) {
            val window = dialog.window
            // 1. 设置窗口全屏并覆盖状态栏
            window!!.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            val layoutParams = window!!.attributes
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            window.attributes = layoutParams

            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.peekHeight = WindowManager.LayoutParams.MATCH_PARENT
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = false
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetDialogPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initClickEvents()
        checkPermissionAndLoadPhotos()
    }

    private fun initRecyclerView() {
        photoAdapter = PhotoRecyclerViewAdapter(type)
        binding.rvPhotos.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = photoAdapter
            isNestedScrollingEnabled = true
        }
    }


    private fun initClickEvents() {
        binding.tvCancel.setOnClickListener {
            dismiss()
        }
        binding.tvConfirm.setOnClickListener {
            val selectedPhotos = photoAdapter.getSelectedPhotos()
            if (selectedPhotos.isEmpty()) {
                Toast.makeText(context, "请至少选择一张照片", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            listener?.onPhotosSelected(selectedPhotos)
            dismiss()
        }
    }


    private fun checkPermissionAndLoadPhotos() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            loadPhotosFromGallery()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun loadPhotosFromGallery() {
        CoroutineScope(Dispatchers.IO).launch {
            val photoUris = mutableListOf<Uri>()
            val context = requireContext()
            val TAG = "PhotoBottomSheet"

            try {
                val projection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.SIZE
                )
                val selection = "${MediaStore.Images.Media.MIME_TYPE} IN (?, ?, ?) AND ${MediaStore.Images.Media.SIZE} > 0"
                val selectionArgs = arrayOf(
                    "image/jpeg",
                    "image/png",
                    "image/webp"
                )

                val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
                context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val maxCount = 1000
                    var count = 0
                    while (cursor.moveToNext() && count < maxCount) {
                        val id = cursor.getLong(idColumn)
                        val contentUri = Uri.parse("${MediaStore.Images.Media.EXTERNAL_CONTENT_URI}/$id")
                        photoUris.add(contentUri)
                        count++
                    }

                }

                val photoItems = photoUris.map { PhotoItem(uri = it, isSelected = false) }
                withContext(Dispatchers.Main) {
                    if (photoItems.isEmpty()) {
                        Toast.makeText(context, "相册中未找到有效图片", Toast.LENGTH_SHORT).show()
                    }
                    photoAdapter.submitList(photoItems)
                }

            } catch (e: Exception) {
                android.util.Log.e(TAG, "加载相册失败: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "加载相册失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        listener = null
    }


}