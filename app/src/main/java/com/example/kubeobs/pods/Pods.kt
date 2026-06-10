package com.example.kubeobs.pods

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.example.kubeobs.consts.Colors
import com.example.kubeobs.consts.Routes
import com.example.kubeobs.consts.UbuntuFamily
import com.example.kubeobs.data.PodsRefState
import com.example.kubeobs.data.PodsResponse
import com.example.kubeobs.data.PodsUIState
import com.example.kubeobs.data.RetrofitAPI
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodScreen(
    navController: NavController,
    viewModel: PodViewModel = viewModel()
){
    var displayDialog = remember {mutableStateOf(false)}
    val state by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title= {
                    Text(
                        text = "Your pods",
                        fontSize = 42.sp,
                        fontWeight=FontWeight.Bold,
                        color = Color.Black,
                        fontFamily = UbuntuFamily().ubuntuFamily
                    )
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
                                .padding(end = 20.dp, bottom = 20.dp)
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
                is PodsUIState.LoadingPods ->{
                    OnPodsLoading()
                }
                is PodsUIState.SuccessPods ->{
                    OnPodsSuccess(currentState.data, navController)
                }
                is PodsUIState.ErrorPods ->{
                    displayDialog.value = true
                    OnPodsError(currentState.e, displayDialog)
                }
            }
        }
    }
}

@Composable
fun OnPodsLoading(){
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

@SuppressLint("UnrememberedMutableState")
@Composable
fun OnPodsSuccess(
    _podsList: PodsResponse?,
    navController: NavController,
    viewModel: PodViewModel = viewModel()
) {
    val podsList = _podsList?.pods ?: emptyList()
    val refState by viewModel.refState.collectAsState()
    val isRefing = refState is PodsRefState.LoadingPodsRef
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (podsList.isEmpty()) {
            Text(
                text = "The list is empty",
                modifier = Modifier.padding(20.dp),
                fontSize = 18.sp,
                fontFamily = UbuntuFamily().ubuntuFamily
            )
        } else {
            PullToRefreshBox(
                isRefreshing = isRefing,
                onRefresh = { viewModel.startRefreshing() }
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(podsList) { podItem ->
                        Card(
                            modifier = Modifier
                                .height(100.dp)
                                .fillMaxWidth(),
                            onClick = {
                                navController.navigate("${Routes.PodHealthScreen}/${podsList.indexOf(podItem)}")
                            },
                            shape = RoundedCornerShape(15.dp),
                            border = BorderStroke(2.dp, Color(Colors.kubeColor)),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White,
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Column(
                                    modifier = Modifier.padding(start = 10.dp, top = 10.dp)
                                ) {
                                    Text(
                                        text = "Pod:",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = UbuntuFamily().ubuntuFamily
                                    )
                                    Text(
                                        text = podItem,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = UbuntuFamily().ubuntuFamily
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnPodsError(errorMessage: String, _displayDialog: MutableState<Boolean>){
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

class PodViewModel: ViewModel(){
    private val _refState = MutableStateFlow<PodsRefState>(PodsRefState.IdlePodsRef)
    val refState: StateFlow<PodsRefState> = _refState.asStateFlow()
    private val _uiState = MutableStateFlow<PodsUIState>(PodsUIState.LoadingPods)
    val uiState: StateFlow<PodsUIState> = _uiState.asStateFlow()
    private var refreshingJob: Job? = null

    fun startRefreshing(){
        if(refreshingJob?.isActive == true) return
        refreshingJob = viewModelScope.launch {
            refreshData()
        }
    }

    private suspend fun refreshData(){
        _refState.value = PodsRefState.LoadingPodsRef
        try{
            val refResponse = RetrofitAPI.instance.getPods()
            if (refResponse.isSuccessful){
                val responseData = refResponse.body()
                _uiState.value = PodsUIState.SuccessPods(responseData)
                _refState.value = PodsRefState.SuccessPodsRef(responseData)
            } else{
                _refState.value = PodsRefState.ErrorPodsRef("Error code: ${refResponse.code()}")
            }
        } catch(e: Exception) {
            Log.e("ERROR","Network error")
        }
    }

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            _uiState.value = PodsUIState.LoadingPods
            try {
                Log.i("KubeOBS_Network", "Making request...")
                val podsResponse = RetrofitAPI.instance.getPods()

                if (podsResponse.isSuccessful) {
                    val responseData = podsResponse.body()

                    Log.d("KubeOBS_Network", "Success, Code: ${podsResponse.code()}")
                    Log.d("KubeOBS_Network", "Resp body: $responseData")
                    Log.d("KubeOBS_Network", "Pod body: ${responseData?.pods}")
                    Log.d("KubeOBS_Network", "Pods quantity: ${responseData?.pods?.size}")

                    _uiState.value = PodsUIState.SuccessPods(responseData)
                } else {
                    Log.e("KubeOBS_Network", "Server err: ${podsResponse.errorBody()?.string()}")
                    _uiState.value = PodsUIState.ErrorPods("Error: ${podsResponse.code()}")
                }
            } catch (e: HttpException) {
                _uiState.value = PodsUIState.ErrorPods("Net err: ${e.message}")
            } catch (e: IOException) {
                _uiState.value = PodsUIState.ErrorPods("Con err: ${e.message}")
            }
        }
    }
}
