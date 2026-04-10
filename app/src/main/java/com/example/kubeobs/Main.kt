package com.example.kubeobs

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class Main() {
}

@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
){
    val state by viewModel.uiState.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when(val currentState = state){
            is UIState.Loading ->{
                CircularProgressIndicator()
            }
            is UIState.Success ->{
                Text(
                    text="Success: ${currentState.data}",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
            }
            is UIState.Error ->{
                Text(
                    text=currentState.e,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
            }
        }
    }
}

class MainViewModel(): ViewModel(){
    private val _uiState = MutableStateFlow<UIState>(UIState.Loading)
    val uiState: StateFlow<UIState> = _uiState.asStateFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            try {
                val response = RetrofitAPI.instance.getAll()
                _uiState.value = UIState.Success(response)
            } catch (e: Exception){
                _uiState.value = UIState.Error(e.toString())
            }
        }
    }
}
