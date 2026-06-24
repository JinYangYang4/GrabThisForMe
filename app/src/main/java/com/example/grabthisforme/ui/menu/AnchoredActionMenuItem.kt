package com.example.grabthisforme.ui.menu

import androidx.annotation.DrawableRes

data class AnchoredActionMenuItem(
    val id: String,
    val title: String,
    @DrawableRes val iconRes: Int,
    @DrawableRes val iconBackgroundRes: Int
)
