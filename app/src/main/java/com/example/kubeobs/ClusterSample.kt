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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

@Composable
fun ClusterSample(
    navController: NavController,
    viewModel: ClusterSampleViewModel = viewModel()
){
    val kubernetesItemsList: List<KubernetesItemsList> = listOf(
        KubernetesItemsList.Cluster.NamespaceHeader("development"),
        KubernetesItemsList.Cluster.Node("development-node", 0.17f, 2.03f),
        KubernetesItemsList.Cluster.Pod("development-pod1", "Ok", 3),
        KubernetesItemsList.Cluster.Node("features-node", 1.29f, 4.11f),
        KubernetesItemsList.Cluster.Pod("feature-pod1", "Ok", 0),
        KubernetesItemsList.Cluster.Pod("feature-pod2", "Ok", 0),
        KubernetesItemsList.Cluster.NamespaceHeader("testing"),
        KubernetesItemsList.Cluster.Node("testing-node", 0.01f, 0.03f),
        KubernetesItemsList.Cluster.Pod("testing-pod", "Err", 4),
        KubernetesItemsList.Cluster.NamespaceHeader("production"),
        KubernetesItemsList.Cluster.Node("production-node", 17.98f, 21.01f),
        KubernetesItemsList.Cluster.Pod("production-pod1", "Ok", 5),
        KubernetesItemsList.Cluster.Node("server-node", 53.32f, 45.88f),
        KubernetesItemsList.Cluster.Pod("server-pod1", "Ok", 0),
        KubernetesItemsList.Cluster.Pod("server-pod2", "Warn", 0),
        KubernetesItemsList.Cluster.Node("feedback-node", 0.02f, 0.10f),
        KubernetesItemsList.Cluster.Pod("feedback-pod1", "Warn", 8),
    )
    val state by viewModel.uiState.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .height(122.dp)
                .padding(start=20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = "KubeOBS",
                fontSize = 42.sp,
                fontWeight=FontWeight.Bold,
                color = Color.Black,
                fontFamily = UbuntuFamily().ubuntuFamily
            )
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal=20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(kubernetesItemsList) {item->
                when(val currentItem = item){
                    is KubernetesItemsList.Cluster.NamespaceHeader ->{
                        Card(
                            modifier = Modifier
                                .height(100.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(15.dp),
                            border = BorderStroke(2.dp, Color(Colors.kubeColor)),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White,
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize()
                            ){
                                Column(
                                    modifier = Modifier.padding(start=10.dp, top=10.dp)
                                ){
                                    Text(
                                        text = "Namespace header:",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = UbuntuFamily().ubuntuFamily
                                    )
                                    Text(
                                        text = item.name,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = UbuntuFamily().ubuntuFamily
                                    )
                                }
                            }
                        }
                    }
                    is KubernetesItemsList.Cluster.Node ->{
                        Card(
                            modifier = Modifier
                                .height(100.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(15.dp),
                            border = BorderStroke(2.dp, Color(Colors.kubeColor)),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White,
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize()
                            ){
                                Column(
                                    modifier = Modifier.padding(start=10.dp, top=10.dp)
                                ){
                                    Text(
                                        text = "Node:",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = UbuntuFamily().ubuntuFamily
                                    )
                                    Text(
                                        text = item.name,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = UbuntuFamily().ubuntuFamily
                                    )
                                }
                            }
                        }
                    }
                    is KubernetesItemsList.Cluster.Pod ->{
                        Card(
                            modifier = Modifier
                                .height(100.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(15.dp),
                            border = BorderStroke(2.dp, Color(Colors.kubeColor)),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White,
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize()
                            ){
                                Column(
                                    modifier = Modifier.padding(start=10.dp, top=10.dp)
                                ){
                                    Text(
                                        text = "Pod:",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = UbuntuFamily().ubuntuFamily
                                    )
                                    Text(
                                        text = item.name,
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
        Row(
            modifier = Modifier
                .height(100.dp)
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
//        when(val currentState = state){
//            is UIState.Loading ->{
//                CircularProgressIndicator(
//                    color = Color(Colors.kubeColor)
//                )
//            }
//            is UIState.Success ->{
//                Text(
//                    text="Success: ${currentState.data}",
//                    fontSize = 26.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black,
//                )
//            }
//            is UIState.Error ->{
//                Text(
//                    text=currentState.e,
//                    fontSize = 26.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.Black,
//                )
//            }
//        }
    }
}

class ClusterSampleViewModel(): ViewModel(){
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
