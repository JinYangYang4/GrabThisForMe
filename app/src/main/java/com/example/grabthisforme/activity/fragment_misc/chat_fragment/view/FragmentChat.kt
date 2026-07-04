package com.example.grabthisforme.activity.fragment_misc.chat_fragment.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
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
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.grabthisforme.R
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.adapter.ChatMessageRecyclerViewAdapter
import com.example.grabthisforme.activity.fragment_misc.chat_fragment.viewModel.FragmentChatViewModel
import com.example.grabthisforme.activity.mainactivity.view.MainActivity
import com.example.grabthisforme.databinding.FragmentChatBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentChat : Fragment(), BottomSheetDialogPhoto.OnPhotosSelectedListener {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val chatViewModel: FragmentChatViewModel by viewModels()
    private val args: FragmentChatArgs by navArgs()

    private lateinit var chatAdapter: ChatMessageRecyclerViewAdapter
    private lateinit var requestPhotoPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener

    private var lastTriggerTime = 0L
    private val debounceThreshold = 100L
    private var isRvHeightInited = false
    private var originalRvHeight = 0
    private var photoUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
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
        loadMessages()
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
                    val layoutParams = binding.rvChatMessages.layoutParams
                    layoutParams.height = originalRvHeight
                    binding.rvChatMessages.layoutParams = layoutParams
                    isRvHeightInited = true
                }
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        initInsets()
    }

    private fun initInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
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
                    val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                    binding.llInput.translationY = -imeHeight.toFloat()
                    val targetHeight = if (imeHeight > 0) {
                        chatViewModel.turnKeyboardStateToTure()
                        originalRvHeight - imeHeight
                    } else {
                        chatViewModel.turnKeyboardStateToFalse()
                        originalRvHeight
                    }
                    val layoutParams = binding.rvChatMessages.layoutParams
                    layoutParams.height = targetHeight
                    binding.rvChatMessages.layoutParams = layoutParams
                    val messageCount = chatAdapter.currentList.size
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

    private fun initObserve() {
        chatViewModel.keyboardStatus.observe(viewLifecycleOwner) {
            val messageCount = chatAdapter.currentList.size
            if (messageCount > 0) {
                binding.rvChatMessages.post {
                    binding.rvChatMessages.scrollToPosition(messageCount - 1)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    chatViewModel.messages.collect { messages ->
                        chatAdapter.submitList(messages)
                        if (messages.isNotEmpty()) {
                            binding.rvChatMessages.post {
                                binding.rvChatMessages.scrollToPosition(messages.size - 1)
                            }
                        }
                    }
                }
                launch {
                    chatViewModel.conversationUiModel.collect { uiModel ->
                        binding.tvName.text = uiModel?.title ?: "鑱婂ぉ"
                        binding.tvChatSubtitle.text = uiModel?.subtitle ?: "鐐瑰嚮澶村儚鏌ョ湅璇︾粏璧勬枡"
                        Glide.with(this@FragmentChat)
                            .load(uiModel?.avatarUrl)
                            .placeholder(R.drawable.ic_back_charactor2)
                            .error(R.drawable.ic_back_charactor2)
                            .into(binding.ivTopAvatar)
                    }
                }
            }
        }
        chatViewModel.openUserDetailId.observe(viewLifecycleOwner) { userId ->
            if (userId == null || userId <= 0L) return@observe
            val action = FragmentChatDirections.actionFragmentChatToUserDetailFragment(userId)
            findNavController().navigate(action)
            chatViewModel.onUserDetailNavigationConsumed()
        }
        chatViewModel.openGroupDetailId.observe(viewLifecycleOwner) { groupId ->
            if (groupId == null || groupId <= 0L) return@observe
            val action = FragmentChatDirections.actionFragmentChatToGroupDetailFragment(groupId)
            findNavController().navigate(action)
            chatViewModel.onGroupDetailNavigationConsumed()
        }
    }

    private fun initRecyclerView() {
        chatAdapter = ChatMessageRecyclerViewAdapter(
            clickListener = {
                val isKeyboardOpen = chatViewModel.keyboardStatus.value ?: false
                if (isKeyboardOpen) {
                    hideSoftKeyboard()
                    chatViewModel.turnKeyboardStateToFalse()
                }
            },
            onImageClick = { imageMessage ->
                val imageMessages = chatAdapter.currentList
                    .filter { it.type == com.example.grabthisforme.model.message.domain.Message.MessageType.IMAGE }
                    .filter { !it.mediaUrl.isNullOrBlank() }
                val imageUrls = imageMessages.mapNotNull { it.mediaUrl }
                val initialIndex = imageMessages.indexOfFirst { it.clientMsgId == imageMessage.clientMsgId }
                    .takeIf { it >= 0 }
                    ?: 0
                val previewDialog = PhotoPreviewDialog.newInstance(imageUrls, initialIndex)
                previewDialog.show(childFragmentManager, "PhotoPreviewDialog")
            },
            onPeerAvatarClick = { senderId ->
                chatViewModel.onPeerAvatarClick(senderId)
            }
        )

        binding.rvChatMessages.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvChatMessages.adapter = chatAdapter
        binding.rvChatMessages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy >= 0) return
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                if (layoutManager.findFirstVisibleItemPosition() <= 2) {
                    chatViewModel.loadOlderMessagesIfNeeded()
                }
            }
        })
    }

    private fun initView() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.llHead.setOnClickListener {
            chatViewModel.onTopAvatarClick()
        }
        binding.ivTopAvatar.setOnClickListener {
            chatViewModel.onTopAvatarClick()
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
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED -> {
                showPhotoBottomSheet()
            }

            else -> {
                requestPhotoPermissionLauncher.launch(permission)
            }
        }
    }

    private fun initPhotoPermissionLauncher() {
        requestPhotoPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    showPhotoBottomSheet()
                } else {
                    Toast.makeText(requireContext(), "需要相册权限后才能选择图片，请在设置中开启", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun initCameraPermissionLauncher() {
        requestCameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    takePicture()
                } else {
                    Toast.makeText(requireContext(), "需要相机权限后才能拍照，请在设置中开启", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun initTakePictureLauncher() {
        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
                if (isSuccess) {
                    photoUri?.let { uri ->
                        handleTakenPhoto(uri)
                    } ?: run {
                        Toast.makeText(requireContext(), "拍照失败：无法获取照片", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "鎷嶇収宸插彇娑堟垨澶辫触", Toast.LENGTH_SHORT).show()
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
            takePictureLauncher.launch(photoUri!!)
        } catch (exception: IOException) {
            Toast.makeText(requireContext(), "鍒涘缓鐓х墖鏂囦欢澶辫触锛?{exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleTakenPhoto(photoUri: Uri) {
        chatViewModel.sendImageMessage(args.conversationId, photoUri.toString())
    }

    private fun checkCameraPermissionAndTakePicture() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED -> {
                takePicture()
            }

            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
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
                chatViewModel.sendTextMessage(args.conversationId, inputText)
                chatViewModel.clearInputState()
            } else {
                Toast.makeText(context, "请输入消息内容", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMessages() {
        chatViewModel.loadMessages(args.conversationId)
    }

    override fun onPhotosSelected(photos: List<Uri>) {
        if (photos.isEmpty()) return
        photos.forEach { uri ->
            chatViewModel.sendImageMessage(args.conversationId, uri.toString())
        }
    }

    override fun onStop() {
        super.onStop()
        chatViewModel.onChatPageStopped()
        val rootView = binding.root
        if (::globalLayoutListener.isInitialized) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                rootView.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
            } else {
                @Suppress("DEPRECATION")
                rootView.viewTreeObserver.removeGlobalOnLayoutListener(globalLayoutListener)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
