package com.example.kubeobs.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.kubeobs.consts.Colors

private val LightColorScheme = lightColorScheme(
    primary = Color(Colors.kubeColor),
    background = Color(Colors.bgColor),
    surface = Color(Colors.bgColor),
    onPrimary = Color.White,
    onBackground = Color(Colors.textColor),
    onSurface = Color(Colors.textColor),
    error = Color(Colors.errorColor)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(Colors.kubeColorDark),
    background = Color(Colors.bgColorDark),
    surface = Color(Colors.bgColorDark),
    onPrimary = Color.White,
    onBackground = Color(Colors.textColorDark),
    onSurface = Color(Colors.textColorDark),
    error = Color(Colors.errorColorDark)
)

@Composable
fun KubeObsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}