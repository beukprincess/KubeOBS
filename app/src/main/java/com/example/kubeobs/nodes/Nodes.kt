package com.example.kubeobs.nodes

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kubeobs.consts.Colors
import com.example.kubeobs.consts.Routes
import com.example.kubeobs.consts.UbuntuFamily
import com.example.kubeobs.data.NodesUIState
import com.example.kubeobs.data.TokenDataStore
import com.example.kubeobs.data.WsErrorMessage
import com.example.kubeobs.data.WsMetricsMessage
import com.example.kubeobs.data.base_url
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    clusterId: Int,
    viewModel: MainViewModel = viewModel()
){
    val displayDialog = remember { mutableStateOf(false) }
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        viewModel.connect(context, clusterId)
        onDispose {
            viewModel.disconnect()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ){
                        Text(
                            text = "Nodes",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = UbuntuFamily().ubuntuFamily
                        )
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
                is NodesUIState.LoadingNodes -> {
                    OnLoading()
                }
                is NodesUIState.SuccessNodes -> {
                    OnSuccess(currentState.data, navController, clusterId)
                }
                is NodesUIState.ErrorNodes -> {
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
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        CircularProgressIndicator(
            color = Color(Colors.kubeColor)
        )
    }
}

@Composable
fun OnSuccess(
    liveMetrics: WsMetricsMessage?,
    navController: NavController,
    clusterId: Int
) {
    val nodesList = liveMetrics?.nodesUsage ?: emptyList()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (nodesList.isEmpty()) {
            Text(
                text = "The list is empty or waiting for data...",
                modifier = Modifier.padding(20.dp),
                fontSize = 18.sp,
                fontFamily = UbuntuFamily().ubuntuFamily
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(nodesList) { nodeItem ->

                    val cpuUsageRaw = nodeItem.cpuUsage.removeSuffix("n").toDoubleOrNull() ?: 0.0
                    val cpuCoresTotal = 1.0

                    val cpuUsedPercent = ((cpuUsageRaw / 1_000_000_000.0) / cpuCoresTotal).toFloat().coerceIn(0f, 1f)
                    val cpuFreePercent = 1f - cpuUsedPercent

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { navController.navigate("${Routes.PodScreen}/$clusterId")},
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(Colors.kubeColor),
                            contentColor = Color.White,
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp)
                        ) {
                            Text(
                                text = "Node: ${nodeItem.nodeName}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = UbuntuFamily().ubuntuFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(15.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CPU Usage",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = UbuntuFamily().ubuntuFamily
                                )
                                Text(
                                    text = "${(cpuUsedPercent * 100).toInt()}%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = UbuntuFamily().ubuntuFamily
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White.copy(alpha = 0.3f))
                            ) {
                                if (cpuUsedPercent > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .weight(cpuUsedPercent)
                                            .fillMaxHeight()
                                            .background(Color.White)
                                    )
                                }

                                if (cpuUsedPercent > 0f && cpuFreePercent > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .width(1.5.dp)
                                            .fillMaxHeight()
                                            .background(Color(Colors.kubeColor))
                                    )
                                }

                                if (cpuFreePercent > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .weight(cpuFreePercent)
                                            .fillMaxHeight()
                                            .background(Color.Transparent)
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

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<NodesUIState>(NodesUIState.LoadingNodes)
    val uiState: StateFlow<NodesUIState> = _uiState.asStateFlow()

    private val gson = Gson()
    private val httpClient = OkHttpClient.Builder()
        .pingInterval(15, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var manuallyDisconnected = false

    fun connect(context: Context, clusterId: Int) {
        manuallyDisconnected = false
        viewModelScope.launch {
            _uiState.value = NodesUIState.LoadingNodes
            openSocket(context, clusterId)
        }
    }

    private suspend fun openSocket(context: Context, clusterId: Int) {
        val token = TokenDataStore.getToken(context)
        if (token == null) {
            _uiState.value = NodesUIState.ErrorNodes("Not authorized")
            return
        }

        val wsBaseUrl = base_url
            .replace("https://", "wss://")
            .replace("http://", "ws://")
            .trimEnd('/')

        val url = "$wsBaseUrl/ws/metrics/$clusterId?token=$token"
        Log.d("WS_NODES", "Connecting to $url")

        val request = Request.Builder().url(url).build()

        webSocket = httpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                Log.d("WS_NODES", "Connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    if (text.contains("\"error\"")) {
                        val err = gson.fromJson(text, WsErrorMessage::class.java)
                        _uiState.value = NodesUIState.ErrorNodes(
                            if (err.type == "auth_expired") "Session was closed, reconnect please" else err.error
                        )
                        return
                    }

                    val data = gson.fromJson(text, WsMetricsMessage::class.java)
                    _uiState.value = NodesUIState.SuccessNodes(data)

                } catch (e: Exception) {
                    Log.e("WS_NODES", "Parse error: ${e.message}")
                    _uiState.value = NodesUIState.ErrorNodes("Parse error: ${e.message}")
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WS_NODES", "Closed: $code / $reason")
                if (code == 1008) {
                    _uiState.value = NodesUIState.ErrorNodes("Session was closed, reconnect please")
                } else if (!manuallyDisconnected) {
                    scheduleReconnect(context, clusterId)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                Log.e("WS_NODES", "Failure: ${t.message}")
                if (!manuallyDisconnected) {
                    scheduleReconnect(context, clusterId)
                }
            }
        })
    }

    private fun scheduleReconnect(context: Context, clusterId: Int) {
        reconnectJob?.cancel()
        reconnectJob = viewModelScope.launch {
            delay(5000)
            if (!manuallyDisconnected) {
                Log.d("WS_NODES", "Reconnecting...")
                openSocket(context, clusterId)
            }
        }
    }

    fun disconnect() {
        manuallyDisconnected = true
        reconnectJob?.cancel()
        webSocket?.close(1000, "Navigated away")
        webSocket = null
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}