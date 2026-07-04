package com.example.grabthisforme.ui.menu

import androidx.annotation.DrawableRes

data class AnchoredActionMenuItem(
    val id: String,
    val title: String,
    @param:DrawableRes val iconRes: Int,
    @param:DrawableRes val iconBackgroundRes: Int
)
