package com.example.grabthisforme.activity.fragment_misc.chat_fragment.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.grabthisforme.databinding.FragmentChatBinding

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.core.widget.doAfterTextChanged
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.adapter.ChatMessageRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.viewModel.FragmentChatViewModel

import com.example.grabthisforme.model.messageContent.domain.MessageContent
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date


class FragmentChat : Fragment(), BottomSheetDialogPhoto.OnPhotosSelectedListener {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private var lastTriggerTime = 0L
    private val debounceThreshold = 100L
    private var isRvHeightInited = false
    private var originalRvHeight = 0
    private lateinit var chatViewModel: FragmentChatViewModel
    private lateinit var chatAdapter: ChatMessageRecyclerViewAdapter
    private lateinit var requestPhotoPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener

    private var photoUri: Uri? = null
    private val messageList = mutableListOf<MessageContent>()
    private var sendTime = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        chatViewModel = ViewModelProvider(this).get( FragmentChatViewModel::class.java)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = chatViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPhotoPermissionLauncher()
        initCameraPermissionLauncher()
        initTakePictureLauncher()
        initRecyclerView()
        setSendButtonClickListener()
        loadTestMessages()
        initObserve()
        initView()
        binding.rvChatMessages.pivotX = binding.rvChatMessages.width / 2f
        binding.rvChatMessages.pivotY = binding.rvChatMessages.height.toFloat()
        globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastTriggerTime < debounceThreshold) {
                    return
                }
                lastTriggerTime = currentTime
                if (!isRvHeightInited) {
                    val rect = Rect()
                    view.getWindowVisibleDisplayFrame(rect)
                    val screenHeight = view.height
                    val keypadHeight = screenHeight - rect.bottom
                    val translationY = if (keypadHeight > 100) -keypadHeight.toFloat() else 0f
                    val toolbarLocation = IntArray(2)
                    val inputLocation = IntArray(2)
                    binding.llTopBar.getLocationOnScreen(toolbarLocation)
                    binding.llInput.getLocationOnScreen(inputLocation)
                    val toolbarBottomY = toolbarLocation[1] + binding.llTopBar.height
                    val inputTopY = inputLocation[1] - translationY.toInt()
                    originalRvHeight = inputTopY - toolbarBottomY
                    if (originalRvHeight <= 0) {
                        originalRvHeight = binding.rvChatMessages.height
                        if (originalRvHeight <= 0) return
                    }
                    val rvInitLayoutParams = binding.rvChatMessages.layoutParams
                    rvInitLayoutParams.height = originalRvHeight
                    binding.rvChatMessages.layoutParams = rvInitLayoutParams
                    isRvHeightInited = true
                }
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        initInsets()
    }
    private fun initInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->

            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())

            if (imeVisible) {
                chatViewModel.turnKeyboardStateToTure()
            } else {
                chatViewModel.turnKeyboardStateToFalse()
            }

            insets
        }


        ViewCompat.setWindowInsetsAnimationCallback(
            binding.root,
            object : WindowInsetsAnimationCompat.Callback(
                WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
            ) {

                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: MutableList<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
                    val imeHeight = imeInsets.bottom
                    binding.llInput.translationY = -imeHeight.toFloat()
                    val targetHeight = if (imeHeight > 0) {
                        chatViewModel.turnKeyboardStateToTure()
                        val target = originalRvHeight - imeHeight
                        target
                    } else {
                        chatViewModel.turnKeyboardStateToFalse()
                        originalRvHeight
                    }
                    val rvLayoutParams = binding.rvChatMessages.layoutParams
                    rvLayoutParams.height = targetHeight
                    binding.rvChatMessages.layoutParams = rvLayoutParams
                    val messageCount = messageList.size
                    Log.d("test11", "$imeHeight")
                    if (messageCount > 0) {
                        binding.rvChatMessages.post {
                            binding.rvChatMessages.scrollToPosition(messageCount - 1)
                        }
                    }
                    return insets
                }
            }
        )
    }
    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).innerBottomBar()
    }
    private fun hideSoftKeyboard() {
        val currentFocus = requireActivity().currentFocus ?: return
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        binding.etMessageInput.clearFocus()
    }


    private fun initObserve(){
        chatViewModel.keyboardStatus.observe(viewLifecycleOwner){
            val messageCount = messageList.size
            Log.d("test11", "initObserve: ")
            if (messageCount > 0) {
                binding.rvChatMessages.post {
                    binding.rvChatMessages.scrollToPosition(messageCount - 1)

                }
            }
        }
    }

    private fun initRecyclerView() {
        chatAdapter = ChatMessageRecyclerViewAdapter(clickListener = {
            val isKeyboardOpen = chatViewModel.keyboardStatus.value ?: false
            if (isKeyboardOpen) {
                hideSoftKeyboard()
                chatViewModel.turnKeyboardStateToFalse()
            }
        },onImageClick = { imageUrl ->
            val previewDialog = PhotoPreviewDialog.newInstance(imageUrl)
            previewDialog.show(childFragmentManager, "PhotoPreviewDialog")
        })

        binding.rvChatMessages.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.rvChatMessages.adapter = chatAdapter

    }
    private fun initView(){
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.ivAlbum.setOnClickListener {
            checkPhotoPermissionAndShowBottomSheet()
        }
        binding.ivTakePhoto.setOnClickListener {
            checkCameraPermissionAndTakePicture()
        }
        binding.etMessageInput.doAfterTextChanged { editable ->
            chatViewModel.onInputChanged(editable?.toString().orEmpty())
        }
    }
    private fun checkPhotoPermissionAndShowBottomSheet() {
        val context = requireContext()
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                showPhotoBottomSheet()
            }
            else -> {
                requestPhotoPermissionLauncher.launch(permission)
            }
        }
    }
    private fun initPhotoPermissionLauncher() {
        requestPhotoPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                showPhotoBottomSheet()
            } else {
                Toast.makeText(requireContext(), "需要照片权限才能选择图片，请在设置中开启", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun initCameraPermissionLauncher() {
        requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                takePicture()
            } else {
                Toast.makeText(requireContext(), "需要相机权限才能拍照，请在设置中开启", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun initTakePictureLauncher() {
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                photoUri?.let { uri ->
                    handleTakenPhoto(uri)
                } ?: run {
                    Toast.makeText(requireContext(), "拍照失败：无法获取照片", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "拍照取消或失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun takePicture() {
        try {
            val photoFile = createImageFile()
            photoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )

            // 启动相机
            takePictureLauncher.launch(photoUri!!)


        } catch (ex: IOException) {
            Toast.makeText(requireContext(), "创建照片文件失败：${ex.message}", Toast.LENGTH_SHORT).show()
            Log.e("FragmentChat", "创建照片文件失败", ex)
        }
    }
    // 新增：处理拍摄的照片
    private fun handleTakenPhoto(photoUri: Uri) {
        val showTimeCheck = System.currentTimeMillis() - sendTime > 2 * 60 * 1000L
        if (showTimeCheck){
            sendTime = System.currentTimeMillis()
        }
        val photoMessage = MessageContent(
            messageId = System.currentTimeMillis().toString(),
            type = MessageContent.MessageType.IMAGE,
            mediaUrl = photoUri.toString(),
            timestamp = System.currentTimeMillis(),
            need_show_time = showTimeCheck,
            isMine = true,
            status = MessageContent.MessageStatus.SENT
        )

        messageList.add(photoMessage)
        chatAdapter.submitList(messageList)
        binding.rvChatMessages.scrollToPosition(messageList.size - 1)
    }
    private fun checkCameraPermissionAndTakePicture() {
        val permission = Manifest.permission.CAMERA

        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                takePicture()
            }
            else -> {
                requestCameraPermissionLauncher.launch(permission)
            }
        }
    }
    // 新增：创建临时照片文件
    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", // 前缀
            ".jpg", // 后缀
            storageDir
        )
    }
    private fun showPhotoBottomSheet() {
        val photoBottomSheet = BottomSheetDialogPhoto.newInstance(BottomSheetDialogPhoto.SELECT_UNLIMIT)
        photoBottomSheet.show(childFragmentManager, "PhotoBottomSheet")
        photoBottomSheet.setOnPhotosSelectedListener(this)
    }

    private fun setSendButtonClickListener() {
        binding.tvSend.setOnClickListener {
            val inputText = binding.etMessageInput.text.toString().trim()
            if (inputText.isNotEmpty()) {
                val newMessage = MessageContent(
                    messageId = System.currentTimeMillis().toString(),
                    type = MessageContent.MessageType.TEXT,
                    content = inputText,
                    timestamp = System.currentTimeMillis(),
                    isMine = true,
                    status = MessageContent.MessageStatus.SENT
                )
                messageList.add(newMessage)
                chatAdapter.submitList(messageList)
                chatViewModel.clearInputState()
                binding.rvChatMessages.scrollToPosition(messageList.size - 1)
            } else {
                Toast.makeText(context, "请输入消息内容", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun loadTestMessages() {
        val testMsg = MessageContent(
            messageId = "test_1",
            type = MessageContent.MessageType.TEXT,
            content = "你好！有什么可以帮你的吗？",
            timestamp = System.currentTimeMillis() - 10000,
            isMine = false,
            status = MessageContent.MessageStatus.READ
        )
        messageList.add(testMsg)
        chatAdapter.submitList(messageList)
    }

    override fun onPhotosSelected(photos: List<Uri>) {
        if (photos.isEmpty()) return
        val showTimeCheck = System.currentTimeMillis() - sendTime > 2 * 60 * 1000L
        if (showTimeCheck) {
            sendTime = System.currentTimeMillis()
        }
        photos.forEachIndexed { index, uri ->
            val photoMessage = MessageContent(
                messageId = "${System.currentTimeMillis()}_$index",
                type = MessageContent.MessageType.IMAGE,
                mediaUrl = uri.toString(),
                timestamp = System.currentTimeMillis(),
                need_show_time = showTimeCheck && index == 0,
                isMine = true,
                status = MessageContent.MessageStatus.SENT
            )
            messageList.add(photoMessage)
        }

        chatAdapter.submitList(messageList)
        binding.rvChatMessages.scrollToPosition(messageList.size - 1)
    }

    override fun onStop() {
        super.onStop()
        val view = binding.root
        if (::globalLayoutListener.isInitialized) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                view.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
            } else {
                @Suppress("DEPRECATION")
                view.viewTreeObserver.removeGlobalOnLayoutListener(globalLayoutListener)
            }
        }
    }
}
