package com.example.kubeobs

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

data class UbuntuFamily(
    val ubuntuFamily: FontFamily = FontFamily(
        Font(R.font.ubuntu_bold, FontWeight.Bold),
        Font(R.font.ubuntu_regular, FontWeight.Normal)
    )
)