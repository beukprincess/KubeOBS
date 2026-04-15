package com.example.kubeobs

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
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
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
){
    val clustersList: List<KubernetesItemsList.Cluster> = listOf(
        KubernetesItemsList.Cluster("softserve-cluster"),
        KubernetesItemsList.Cluster("dyneria-cluster"),
        KubernetesItemsList.Cluster("lanteria-cluster"),
    )
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
                is UIState.Loading ->{
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
                is UIState.Success ->{
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ){
                        Text(
                            text="Success: ${currentState.data}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,)
                    }
                }
                is UIState.Error ->{
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ){
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal=20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(clustersList){cluster->
                                Card(
                                    modifier = Modifier
                                        .height(100.dp)
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(15.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(Colors.kubeColor),
                                        contentColor = Color.White,
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize()
                                    ){
                                        Column(
                                            modifier = Modifier.padding(start=10.dp, top=10.dp)
                                        ){
                                            Text(
                                                text = "Cluster:",
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = UbuntuFamily().ubuntuFamily
                                            )
                                            Text(
                                                text = cluster.name,
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
