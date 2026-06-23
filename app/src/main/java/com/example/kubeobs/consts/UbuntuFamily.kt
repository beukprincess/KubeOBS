package com.example.kubeobs.consts

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.kubeobs.R

data class UbuntuFamily(
    val ubuntuFamily: FontFamily = FontFamily(
        Font(R.font.ubuntu_bold, FontWeight.Companion.Bold),
        Font(R.font.ubuntu_regular, FontWeight.Companion.Normal)
    )
)