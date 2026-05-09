package com.example.grabthisforme.activity.fragment_misc.setfragment.model.data

import com.example.grabthisforme.model.user.domain.UserProfile

data class EditPersonalFormState(
    val userId: Long = 0L,
    val accountName: String = "",
    val displayName: String = "",
    val phone: String = "",
    val email: String = "",
    val signature: String = "",
    val gender: Int = UserProfile.GENDER_UNKNOWN,
    val avatarUrl: String = ""
)