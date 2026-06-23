package com.example.kubeobs.pods

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kubeobs.consts.Colors
import com.example.kubeobs.consts.UbuntuFamily
import com.example.kubeobs.data.PodHealthUIState
import com.example.kubeobs.data.PodsInfoResponse
import com.example.kubeobs.data.RetrofitAPI
import com.example.kubeobs.data.TokenDataStore
import com.example.kubeobs.data.WsErrorMessage
import com.example.kubeobs.data.WsMetricsMessage
import com.example.kubeobs.data.base_url
import com.google.gson.Gson
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.AxisValueOverrider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import retrofit2.HttpException
import java.io.IOException
import java.util.LinkedList
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PodHealthScreen(
    navController: NavController,
    clusterId: Int,
    podIndex: Int,
    viewModel: PodHealthViewModel = viewModel()
){
    val displayDialog = remember {mutableStateOf(false)}
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        viewModel.fetchData(context)
        viewModel.connectWs(context, clusterId)
        onDispose {
            viewModel.stopPolling()
            viewModel.disconnectWs()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title= {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ){
                        Text(
                            text = "Pod health",
                            fontSize = 42.sp,
                            fontWeight=FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = UbuntuFamily().ubuntuFamily
                        )
                    }
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
                is PodHealthUIState.LoadingPodHealth -> { OnPodHealthLoading() }
                is PodHealthUIState.SuccessPodHealth -> {
                    OnPodHealthSuccess(currentState.data, podIndex, context, viewModel)
                }
                is PodHealthUIState.ErrorPodHealth -> {
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
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        CircularProgressIndicator(color = Color(Colors.kubeColor))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnPodHealthSuccess(
    _podsInfo: PodsInfoResponse?,
    podIndex: Int,
    context: Context,
    viewModel: PodHealthViewModel
) {
    val podsList = _podsInfo?.pods ?: emptyList()
    if (podsList.isEmpty() || podIndex >= podsList.size) {
        Text("No data available", modifier = Modifier.padding(20.dp))
        return
    }

    val staticPod = podsList[podIndex]

    val wsStateWrapper by viewModel.wsState.collectAsState()
    val livePodData = wsStateWrapper.data?.pods?.find { it.name == staticPod.name }
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val ageSeconds by viewModel.ageSeconds.collectAsState()


    val currentStatus = livePodData?.status ?: staticPod.status
    val currentRestarts = livePodData?.restarts ?: staticPod.restarts

    val cpuRaw = livePodData?.cpuRaw?.removeSuffix("n")?.toDoubleOrNull() ?: 0.0
    val cpuPercent = (cpuRaw / 1_000_000_000.0) * 100.0
    val ramMb = livePodData?.memoryMb ?: 0.0

    LaunchedEffect(currentStatus) {
        if(currentStatus == "Running" && ageSeconds == 0) {
            viewModel.startPolling(staticPod.ageSeconds)
        } else if (currentStatus != "Running") {
            viewModel.stopPolling()
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.startRefreshing(context) },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(Colors.kubeColor)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ){
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    InfoRow(
                        "Name:",
                        staticPod.name
                    )
                    InfoRow(
                        "Namespace:",
                        staticPod.namespace
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Status: ",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = UbuntuFamily().ubuntuFamily
                        )
                        Text(
                            text = currentStatus,
                            color = if (currentStatus == "Running") Color.Green else Color.Red,
                            fontSize = 22.sp,
                            fontFamily = UbuntuFamily().ubuntuFamily
                        )
                    }

                    InfoRow(
                        "Restarts:",
                        currentRestarts.toString())
                    InfoRow(
                        "Age (sec):",
                        ageSeconds.toString())
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(Colors.kubeColor)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column(modifier = Modifier.padding(vertical = 15.dp)) {
                    PodLiveChart(
                        title = "CPU Usage (%)",
                        percent = String.format("%.2f %%", cpuPercent),
                        currentMetric = cpuPercent,
                        updateTrigger = wsStateWrapper.tick
                    )
                }
            }

            val totalMemoryMb = wsStateWrapper.data?.nodesUsage?.sumOf { it.memoryUsedMb } ?: 0.0
            val otherMemoryMb = (totalMemoryMb - ramMb).coerceAtLeast(0.0)

            val totalForWeight = totalMemoryMb.toFloat().coerceAtLeast(1f)
            val podWeight = (ramMb.toFloat() / totalForWeight).coerceIn(0f, 1f)
            val otherWeight = 1f - podWeight

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(Colors.kubeColor)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 15.dp)
                        .padding(end = 15.dp)
                ) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        modifier = Modifier
                            .padding(start = 10.dp)
                            .fillMaxWidth(),
                    ) {
                        Text(
                            text = "RAM usage (Mb)",
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Normal,
                            fontFamily = UbuntuFamily().ubuntuFamily,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(15.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 10.dp)
                            .padding(bottom = 15.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Cluster Total",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = UbuntuFamily().ubuntuFamily,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Spacer(
                                modifier = Modifier.height(10.dp)
                            )
                            Text(
                                text = String.format("%.2f", totalMemoryMb),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = UbuntuFamily().ubuntuFamily,
                                textAlign = TextAlign.Center
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier.height(40.dp),
                            thickness = 1.dp,
                            color = Color(Colors.kubeColor).copy(alpha = 0.3f)
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(Colors.kubeColor)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "This Pod",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = UbuntuFamily().ubuntuFamily,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = String.format("%.2f", ramMb),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = UbuntuFamily().ubuntuFamily,
                                textAlign = TextAlign.Center
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier.height(40.dp),
                            thickness = 1.dp,
                            color = Color(Colors.kubeColor).copy(alpha = 0.3f)
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.Gray.copy(alpha = 0.5f)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Other",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFamily = UbuntuFamily().ubuntuFamily,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = String.format("%.2f", otherMemoryMb),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = UbuntuFamily().ubuntuFamily,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(5.dp))

                    Row(
                        modifier = Modifier
                            .padding(start = 15.dp)
                            .padding(horizontal = 15.dp)
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.Gray.copy(alpha = 0.2f))
                    ) {
                        if (podWeight > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(podWeight)
                                    .fillMaxHeight()
                                    .background(Color(Colors.kubeColor))
                            )
                        }
                        if (podWeight > 0f && otherWeight > 0f) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .fillMaxHeight()
                                    .background(Color.White)
                            )
                        }
                        if (otherWeight > 0f) {
                            Box(
                                modifier = Modifier
                                    .weight(otherWeight)
                                    .fillMaxHeight()
                                    .background(Color.Gray.copy(alpha = 0.5f))
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = UbuntuFamily().ubuntuFamily
        )
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = UbuntuFamily().ubuntuFamily
        )
    }
}

@Composable
fun PodLiveChart(title: String, percent: String, currentMetric: Double, updateTrigger: Any?) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val maxDataPoints = 15

    val dataWindow = remember {
        LinkedList<Double>().apply { repeat(maxDataPoints) { add(0.0) } }
    }

    LaunchedEffect(updateTrigger) {
        dataWindow.add(currentMetric)
        if (dataWindow.size > maxDataPoints) dataWindow.removeFirst()

        withContext(Dispatchers.Default) {
            modelProducer.runTransaction {
                columnSeries { series(dataWindow.toList()) }
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(start = 5.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 22.sp,
                fontFamily = UbuntuFamily().ubuntuFamily
            )
            Text(
                text = percent,
                fontSize = 22.sp,
                fontFamily = UbuntuFamily().ubuntuFamily
            )
        }

        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(color = Color(Colors.kubeColor), thickness = 120.dp)
                    ),

                ),
                startAxis = rememberStartAxis(itemPlacer = remember { VerticalAxis.ItemPlacer.step({ 20.0 }) }),
                bottomAxis = rememberBottomAxis(label = null)
            ),
            modelProducer = modelProducer,
            modifier = Modifier.padding(horizontal = 16.dp).height(150.dp),

            scrollState = com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState(
                scrollEnabled = true,
                initialScroll = com.patrykandpatrick.vico.core.cartesian.Scroll.Absolute.End
            ),
            zoomState = com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState(
                zoomEnabled = false,
                initialZoom = com.patrykandpatrick.vico.core.cartesian.Zoom.static(value = 0.1f)
            ),

            runInitialAnimation = false
        )
    }
}

@Composable
fun OnPodHealthError(errorMessage: String, _displayDialog: MutableState<Boolean>){
    if(_displayDialog.value){
        AlertDialog(
            title = { Text("Error:", fontWeight = FontWeight.Bold, fontFamily = UbuntuFamily().ubuntuFamily) },
            text = { Text(errorMessage, fontFamily = UbuntuFamily().ubuntuFamily) },
            onDismissRequest = {},
            confirmButton = { TextButton(onClick = { _displayDialog.value = false }) { Text("Ok") } }
        )
    }
}

data class PodWsState(
    val data: WsMetricsMessage? = null,
    val tick: Long = 0L
)
class PodHealthViewModel: ViewModel(){
    private val _ageSeconds = MutableStateFlow(0)
    val ageSeconds: StateFlow<Int> = _ageSeconds.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _uiState = MutableStateFlow<PodHealthUIState>(PodHealthUIState.LoadingPodHealth)
    val uiState: StateFlow<PodHealthUIState> = _uiState.asStateFlow()

    private val _wsState = MutableStateFlow(PodWsState())
    val wsState: StateFlow<PodWsState> = _wsState.asStateFlow()

    private var pollingJob: Job? = null
    private var refreshingJob: Job? = null

    private val gson = Gson()
    private val httpClient = OkHttpClient.Builder().pingInterval(15, TimeUnit.SECONDS).build()
    private var webSocket: WebSocket? = null
    private var wsReconnectJob: Job? = null
    private var manuallyDisconnected = false

    fun startPolling(initialAge: Int){
        if(pollingJob?.isActive == true) return
        _ageSeconds.value = initialAge
        pollingJob = viewModelScope.launch{
            while(isActive){
                delay(1000)
                _ageSeconds.value++
            }
        }
    }
    fun stopPolling() { pollingJob?.cancel() }

    fun startRefreshing(context: Context){
        if(refreshingJob?.isActive == true) return
        refreshingJob = viewModelScope.launch { fetchDataInternal(context, isRefresh = true) }
    }

    fun fetchData(context: Context) {
        viewModelScope.launch { fetchDataInternal(context, isRefresh = false) }
    }

    private suspend fun fetchDataInternal(context: Context, isRefresh: Boolean) {
        if (isRefresh) _isRefreshing.value = true else _uiState.value = PodHealthUIState.LoadingPodHealth
        try {
            val token = TokenDataStore.getToken(context)
            if (token == null) {
                _uiState.value = PodHealthUIState.ErrorPodHealth("Not authorized")
                return
            }
            val response = RetrofitAPI.instance.getPodsInfo("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                _uiState.value = PodHealthUIState.SuccessPodHealth(response.body())
            } else {
                _uiState.value = PodHealthUIState.ErrorPodHealth("Error: ${response.code()}")
            }
        } catch (e: Exception) {
            _uiState.value = PodHealthUIState.ErrorPodHealth("Net err: ${e.message}")
        } finally {
            if (isRefresh) _isRefreshing.value = false
        }
    }

    fun connectWs(context: Context, clusterId: Int) {
        manuallyDisconnected = false
        viewModelScope.launch { openSocket(context, clusterId) }
    }

    private suspend fun openSocket(context: Context, clusterId: Int) {
        val token = TokenDataStore.getToken(context) ?: return
        val wsBaseUrl = base_url.replace("https://", "wss://").replace("http://", "ws://").trimEnd('/')
        val url = "$wsBaseUrl/ws/metrics/$clusterId?token=$token"

        val request = Request.Builder().url(url).build()
        webSocket = httpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    if (!text.contains("\"error\"")) {
                        val data = gson.fromJson(text, WsMetricsMessage::class.java)
                        _wsState.value = PodWsState(data = data, tick = System.currentTimeMillis())
                    }
                } catch (e: Exception) { Log.e("WS_POD", "Parse err: ${e.message}") }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (!manuallyDisconnected) scheduleWsReconnect(context, clusterId)
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                if (!manuallyDisconnected) scheduleWsReconnect(context, clusterId)
            }
        })
    }

    private fun scheduleWsReconnect(context: Context, clusterId: Int) {
        wsReconnectJob?.cancel()
        wsReconnectJob = viewModelScope.launch {
            delay(5000)
            if (!manuallyDisconnected) openSocket(context, clusterId)
        }
    }

    fun disconnectWs() {
        manuallyDisconnected = true
        wsReconnectJob?.cancel()
        webSocket?.close(1000, "Leaving pod screen")
        webSocket = null
        _wsState.value = PodWsState()
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
        disconnectWs()
    }
}