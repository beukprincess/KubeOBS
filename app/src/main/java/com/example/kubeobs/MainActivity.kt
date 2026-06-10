package com.example.kubeobs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.kubeobs.navigation.Navigation
import com.example.kubeobs.ui.theme.KubeObsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KubeObsTheme {
                Navigation()
            }
        }
    }
}

