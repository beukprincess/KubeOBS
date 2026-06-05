package com.example.kubeobs

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
fun MetricsScreen(
    navController: NavController,
    viewModel: MetricsViewModel = viewModel()
){
    var displayDialog = remember {mutableStateOf(false)}
    val state by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title= {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ){
                        Text(
                            text = "KubeOBS",
                            fontSize = 42.sp,
                            fontWeight=FontWeight.Bold,
                            color = Color.Black,
                            fontFamily = UbuntuFamily().ubuntuFamily
                        )
                        Button(
                            colors = ButtonColors(
                                containerColor = Color(Colors.kubeColor),
                                contentColor = Color.White,
                                disabledContainerColor = Color(Colors.kubeColor),
                                disabledContentColor = Color.White
                            ),
                            modifier = Modifier
                                .padding(end=20.dp, bottom=20.dp)
                                .size(
                                    height = 40.dp,
                                    width = 100.dp
                                ),
                            onClick = {
                                navController.navigate(Routes.MainScreen)
                            }
                        ) {
                            Text(
                                text = "Nodes",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = UbuntuFamily().ubuntuFamily
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                actions = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            colors = ButtonColors(
                                containerColor = Color(Colors.kubeColor),
                                contentColor = Color.White,
                                disabledContainerColor = Color(Colors.kubeColor),
                                disabledContentColor = Color.White
                            ),
                            modifier = Modifier
                                .padding(end=20.dp, bottom=20.dp)
                                .size(
                                    height = 40.dp,
                                    width = 150.dp
                                ),
                            onClick = {}
                        ) {
                            Text(
                                text = "Add cluster",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = UbuntuFamily().ubuntuFamily
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ){
            when(val currentState = state){
                is MetricsUIState.LoadingMetrics ->{
                    OnLoadingMetrics()
                }
                is MetricsUIState.SuccessMetrics ->{
                    OnSuccessMetrics(currentState.data, navController)
                }
                is MetricsUIState.ErrorMetrics ->{
                    displayDialog.value = true
                    OnErrorMetrics(currentState.e, displayDialog)
                }
            }
        }
    }
}

@Composable
fun OnLoadingMetrics(){
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
fun OnSuccessMetrics(_metricsObject: MetricsResponse?, navController: NavController) {
    Card(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxSize(),
        shape = RoundedCornerShape(15.dp),
        border = BorderStroke(2.dp, Color(Colors.kubeColor)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        )
    ){
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = "CPU loading:",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = UbuntuFamily().ubuntuFamily
                )
                Text(
                    text = _metricsObject?.metrics?.cpuPercentage.toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = UbuntuFamily().ubuntuFamily
                )
            }
            Spacer(
                modifier = Modifier
                    .height(10.dp)
                    .fillMaxWidth()
            )
            Text(
                text = "RAM loading: ",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = UbuntuFamily().ubuntuFamily
            )
            Row(
                modifier = Modifier
                    .padding(start=10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = "total volume: ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = UbuntuFamily().ubuntuFamily
                )
                Text(
                    text = _metricsObject?.metrics?.ram?.totalGB.toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = UbuntuFamily().ubuntuFamily
                )
            }
            Row(
                modifier = Modifier
                    .padding(start=10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = "used volume: ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = UbuntuFamily().ubuntuFamily
                )
                Text(
                    text = _metricsObject?.metrics?.ram?.usedGB.toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = UbuntuFamily().ubuntuFamily
                )
            }
            Row(
                modifier = Modifier
                    .padding(start=10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = "percentage: ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = UbuntuFamily().ubuntuFamily
                )
                Text(
                    text = _metricsObject?.metrics?.ram?.percentage.toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = UbuntuFamily().ubuntuFamily
                )
            }
            Spacer(
                modifier = Modifier
                    .height(10.dp)
                    .fillMaxWidth()
            )
            Text(
                text = "Disk:",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = UbuntuFamily().ubuntuFamily
            )
            Row(
                modifier = Modifier
                    .padding(start=10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = "total volume: ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = UbuntuFamily().ubuntuFamily
                )
                Text(
                    text = _metricsObject?.metrics?.disk?.totalGB.toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = UbuntuFamily().ubuntuFamily
                )
            }
            Row(
                modifier = Modifier
                    .padding(start=10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = "free volume: ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = UbuntuFamily().ubuntuFamily
                )
                Text(
                    text = _metricsObject?.metrics?.disk?.freeGB.toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = UbuntuFamily().ubuntuFamily
                )
            }
            Row(
                modifier = Modifier
                    .padding(start=10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ){
                Text(
                    text = "percentage: ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = UbuntuFamily().ubuntuFamily
                )
                Text(
                    text = _metricsObject?.metrics?.disk?.percentage.toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = UbuntuFamily().ubuntuFamily
                )
            }
            Spacer(
                modifier = Modifier
                    .height(10.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun OnErrorMetrics(errorMessage: String, _displayDialog: MutableState<Boolean>){
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

class MetricsViewModel(): ViewModel(){
    private val _uiState = MutableStateFlow<MetricsUIState>(MetricsUIState.LoadingMetrics)
    val uiState: StateFlow<MetricsUIState> = _uiState.asStateFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            _uiState.value = MetricsUIState.LoadingMetrics
            try {
                Log.i("KubeOBS_Network", "Making request...")
                val metricsResponse = RetrofitAPI.instance.getMetrics()

                if (metricsResponse.isSuccessful) {
                    val responseData = metricsResponse.body()

                    Log.d("KubeOBS_Network", "Success, Code: ${metricsResponse.code()}")
                    Log.d("KubeOBS_Network", "Resp body: $responseData")

                    _uiState.value = MetricsUIState.SuccessMetrics(responseData)
                } else {
                    Log.e("KubeOBS_Network", "Server err: ${metricsResponse.errorBody()?.string()}")
                    _uiState.value = MetricsUIState.ErrorMetrics("Error: ${metricsResponse.code()}")
                }
            } catch (e: HttpException) {
                _uiState.value = MetricsUIState.ErrorMetrics("Net err: ${e.message}")
            } catch (e: IOException) {
                _uiState.value = MetricsUIState.ErrorMetrics("Con err: ${e.message}")
            }
        }
    }
}
