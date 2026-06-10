package com.example.kubeobs.nodes

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kubeobs.consts.Colors
import com.example.kubeobs.consts.Routes
import com.example.kubeobs.consts.UbuntuFamily
import com.example.kubeobs.data.NodesResponse
import com.example.kubeobs.data.NodesUIState
import com.example.kubeobs.data.RetrofitAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
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
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = UbuntuFamily().ubuntuFamily
                    )
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
                is NodesUIState.LoadingNodes ->{
                    OnLoading()
                }
                is NodesUIState.SuccessNodes ->{
                    OnSuccess(currentState.data, navController)
                }
                is NodesUIState.ErrorNodes ->{
                    displayDialog.value = true
                    OnError(currentState.e, displayDialog)
                }
            }
        }
    }
}

@Composable
fun OnLoading(){
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
fun OnSuccess(_podsList: NodesResponse?, navController: NavController) {
    val nodesList = _podsList?.nodes ?: emptyList()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (nodesList.isEmpty()) {
            Text(
                text = "The list is empty",
                modifier = Modifier.padding(20.dp),
                fontSize = 18.sp,
                fontFamily = UbuntuFamily().ubuntuFamily
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(nodesList) { nodeItem ->
                    Card(
                        modifier = Modifier
                            .height(100.dp)
                            .fillMaxWidth(),
                        onClick = {
                            navController.navigate(Routes.PodScreen)
                        },
                        shape = RoundedCornerShape(15.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(Colors.kubeColor),
                            contentColor = Color.White,
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier.padding(start = 10.dp, top = 10.dp)
                            ) {
                                Text(
                                    text = "Node:",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = UbuntuFamily().ubuntuFamily
                                )
                                Text(
                                    text = nodeItem.toString(),
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

@Composable
fun OnError(errorMessage: String, _displayDialog: MutableState<Boolean>){
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

class MainViewModel: ViewModel(){
    private val _uiState = MutableStateFlow<NodesUIState>(NodesUIState.LoadingNodes)
    val uiState: StateFlow<NodesUIState> = _uiState.asStateFlow()

    init {
        fetchData()
    }

    fun fetchData() {
        viewModelScope.launch {
            _uiState.value = NodesUIState.LoadingNodes
            try{
                Log.i("KubeOBS_Network", "Making request...")
                val nodesResponse = RetrofitAPI.instance.getNodes()

                if (nodesResponse.isSuccessful) {
                    val responseData = nodesResponse.body()
                    Log.d("KubeOBS_Network", "Success, Code: ${nodesResponse.code()}")
                    Log.d("KubeOBS_Network", "Resp body: $responseData")
                    Log.d("KubeOBS_Network", "Node body: ${responseData?.nodes}")
                    Log.d("KubeOBS_Network", "Nodes quantity: ${responseData?.nodes?.size}")

                    _uiState.value = NodesUIState.SuccessNodes(responseData)
                } else {
                    Log.e("KubeOBS_Network", "Server err: ${nodesResponse.errorBody()?.string()}")
                    _uiState.value = NodesUIState.ErrorNodes("Error: ${nodesResponse.code()}")
                }
            } catch (e: HttpException) {
                _uiState.value = NodesUIState.ErrorNodes("Net err: ${e.message}")
            } catch (e: IOException) {
                _uiState.value = NodesUIState.ErrorNodes("Con err: ${e.message}")
            }
        }
    }
}


