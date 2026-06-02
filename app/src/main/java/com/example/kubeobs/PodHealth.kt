package com.example.kubeobs

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodHealthScreen(
    navController: NavController,
    viewModel: PodHealthViewModel = viewModel(),
    podIndex: Int
){
    var displayDialog = remember {mutableStateOf(false)}
    val state by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title= {
                    Text(
                        text = "KubeOBS",
                        fontSize = 42.sp,
                        fontWeight=FontWeight.Bold,
                        color = Color.Black,
                        fontFamily = UbuntuFamily().ubuntuFamily
                    )
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ){
            when(val currentState = state){
                is PodHealthUIState.LoadingPodHealth ->{
                    OnPodHealthLoading()
                }
                is PodHealthUIState.SuccessPodHealth ->{
                    OnPodHealthSuccess(currentState.data, podIndex)
                }
                is PodHealthUIState.ErrorPodHealth ->{
                    displayDialog.value = true
                    OnPodHealthError(currentState.e, displayDialog)
                }
            }
        }
    }
}

@Composable
fun OnPodHealthLoading(){
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        CircularProgressIndicator(
            color = Color(Colors.kubeColor)
        )
    }
}

@Composable
fun OnPodHealthSuccess(_podsInfo: PodsInfoResponse?, podIndex: Int) {
    val podsList = _podsInfo?.pods ?: emptyList()
    val currentPod = podsList[podIndex]
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxSize(),
    ) {
        if (podsList.isEmpty()) {
            Text(
                text = "The list is empty",
                modifier = Modifier.padding(20.dp),
                fontSize = 18.sp,
                fontFamily = UbuntuFamily().ubuntuFamily
            )
        } else{
            Text(
                text = currentPod.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = UbuntuFamily().ubuntuFamily
            )
        }
    }
}

@Composable
fun OnPodHealthError(errorMessage: String, _displayDialog: MutableState<Boolean>){
    if(_displayDialog.value){
        AlertDialog(
            title = {
                Text(
                    text = "Error:",
                    fontWeight = FontWeight.Bold,
                    fontFamily = UbuntuFamily().ubuntuFamily
                )
            },
            text = {
                Text(
                    text = errorMessage,
                    fontWeight = FontWeight.Normal,
                    fontFamily = UbuntuFamily().ubuntuFamily
                )
            },
            onDismissRequest = {},
            confirmButton = {
                TextButton(
                    onClick = {
                        _displayDialog.value = false
                    }
                ) {
                    Text("Ok")
                }
            }
        )
    }
}

class PodHealthViewModel(): ViewModel(){
    private val _uiState = MutableStateFlow<PodHealthUIState>(PodHealthUIState.LoadingPodHealth)
    val uiState: StateFlow<PodHealthUIState> = _uiState.asStateFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            _uiState.value = PodHealthUIState.LoadingPodHealth
            try {
                Log.i("KubeOBS_Network", "Making request...")
                val podsInfoResponse = RetrofitAPI.instance.getPodsInfo()

                if (podsInfoResponse.isSuccessful) {
                    val responseData = podsInfoResponse.body()

                    Log.d("KubeOBS_Network", "Success, Code: ${podsInfoResponse.code()}")
                    Log.d("KubeOBS_Network", "Resp body: $responseData")

                    _uiState.value = PodHealthUIState.SuccessPodHealth(responseData)
                } else {
                    Log.e("KubeOBS_Network", "Server err: ${podsInfoResponse.errorBody()?.string()}")
                    _uiState.value = PodHealthUIState.ErrorPodHealth("Error: ${podsInfoResponse.code()}")
                }
            } catch (e: HttpException) {
                _uiState.value = PodHealthUIState.ErrorPodHealth("Net err: ${e.message}")
            } catch (e: IOException) {
                _uiState.value = PodHealthUIState.ErrorPodHealth("Con err: ${e.message}")
            }
        }
    }
}