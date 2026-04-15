package com.example.grabthisforme.activity.fragment_misc.chat_fragment.modle

import android.net.Uri

data class PhotoItem(
    val uri: Uri,
    val isSelected: Boolean = false // 记录选中状态
)