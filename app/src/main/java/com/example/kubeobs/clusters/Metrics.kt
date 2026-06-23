package com.example.kubeobs.clusters

import android.annotation.SuppressLint
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.example.kubeobs.consts.Colors
import com.example.kubeobs.consts.Routes
import com.example.kubeobs.consts.UbuntuFamily
import com.example.kubeobs.data.AddClusterRequest
import com.example.kubeobs.data.AddClusterState
import com.example.kubeobs.data.Metrics
import com.example.kubeobs.data.MetricsDataState
import com.example.kubeobs.data.MetricsResponse
import com.example.kubeobs.data.MetricsUIState
import com.example.kubeobs.data.RetrofitAPI
import com.example.kubeobs.data.TokenDataStore
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.kubeobs.data.WsMetricsMessage
import com.example.kubeobs.data.WsErrorMessage
import com.example.kubeobs.data.base_url
import com.google.gson.Gson
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.shape.rounded
import com.patrykandpatrick.vico.core.cartesian.data.AxisValueOverrider
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer.ColumnProvider.Companion.series
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import java.util.LinkedList
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricsScreen(
    navController: NavController,
    clusterId: Int,
    viewModel: MetricsInitViewModel = viewModel()
){
    val context = LocalContext.current
    var displayDialog = remember {mutableStateOf(false)}
    val state by viewModel.uiState.collectAsState()
    val metricsState by viewModel.metricsState.collectAsState()
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
                    ) {
                        val clusterName = (metricsState as? MetricsDataState.SuccessMetricsData)?.data?.clusterName

                        val titleText = if (!clusterName.isNullOrEmpty()) {
                            "$clusterName metrics"
                        } else {
                            "Cluster metrics"
                        }

                        Text(
                            text = titleText,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = UbuntuFamily().ubuntuFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonColors(
                                containerColor = Color(Colors.kubeColor),
                                contentColor = Color.White,
                                disabledContainerColor = Color(Colors.kubeColor),
                                disabledContentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .height(50.dp),
                            onClick = {
                                navController.navigate("${Routes.MainScreen}/$clusterId")
                            }
                        ) {
                            Text(
                                text = "Nodes",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
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
                .background(MaterialTheme.colorScheme.background),
        ){
            when(val currentState = state){
                is MetricsUIState.LoadingMetrics ->{
                    OnLoadingMetrics()
                }
                is MetricsUIState.SuccessMetrics ->{
                    OnSuccessMetrics(currentState.data, navController, metricsState)
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

@SuppressLint("UnrememberedMutableState")
@Composable
fun MetricsCard(data: WsMetricsMessage?, updateTick: Long) {
    val nodes = data?.nodesUsage.orEmpty()
    val totalCpuCoresUsed = nodes.sumOf {
        it.cpuUsage.removeSuffix("n").toDoubleOrNull() ?: 0.0
    } / 1_000_000_000.0
    val CORES_PER_NODE = 1.0
    val totalCpuLimit = nodes.size * CORES_PER_NODE
    val cpuPercentage = if (totalCpuLimit > 0) (totalCpuCoresUsed / totalCpuLimit) * 100.0 else 0.0
    val finalCpuPercent = cpuPercentage.coerceIn(0.0, 100.0)

    val totalMemoryMb = data?.nodesUsage?.sumOf { it.memoryUsedMb } ?: 0.0
    val podsMemoryMb = data?.pods?.sumOf { it.memoryMb } ?: 0.0
    val otherMemoryMb = (totalMemoryMb - podsMemoryMb).coerceAtLeast(0.0)

    val totalForWeight = totalMemoryMb.toFloat().coerceAtLeast(1f)
    val podsWeight = (podsMemoryMb.toFloat() / totalForWeight).coerceIn(0f, 1f)
    val otherWeight = 1f - podsWeight

    Column(
        modifier = Modifier
            .background(Color.Transparent)
            .fillMaxSize(),
    ) {
        Spacer(
            modifier = Modifier
                .height(10.dp)
                .fillMaxWidth()
        )
        Card(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .height(250.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color(Colors.kubeColor)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background,
            )
        ) {
            Spacer(
                modifier = Modifier
                    .height(20.dp)
                    .fillMaxWidth()
            )
            MetricLiveChart(
                title = "CPU usage(%)",
                percent = String.format("%.1f %%", finalCpuPercent),
                currentMetric = finalCpuPercent,
                updateTrigger = updateTick
            )
        }
        Row(
            modifier = Modifier
                .padding(top = 20.dp)
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Card(
                modifier = Modifier
                    .size(
                        width = 175.dp,
                        height = 90.dp
                    ),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(Colors.kubeColor)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Quantity of nodes:",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = UbuntuFamily().ubuntuFamily,
                        textAlign = TextAlign.Center
                    )
                    Spacer(
                        modifier = Modifier
                            .height(15.dp)
                            .fillMaxWidth()
                    )
                    Text(
                        text = nodes.size.toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = UbuntuFamily().ubuntuFamily
                    )
                }
            }

            Card(
                modifier = Modifier
                    .size(
                        width = 175.dp,
                        height = 90.dp
                    ),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(Colors.kubeColor)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Quantity of pods:",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = UbuntuFamily().ubuntuFamily,
                        textAlign = TextAlign.Center
                    )
                    Spacer(
                        modifier = Modifier
                            .height(15.dp)
                            .fillMaxWidth()
                    )
                    Text(
                        text = data?.pods?.size.toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = UbuntuFamily().ubuntuFamily
                    )
                }
            }
        }
        Spacer(
            modifier = Modifier
                .height(20.dp)
                .fillMaxWidth()
        )
        Card(
            modifier = Modifier
                .padding(horizontal = 20.dp)
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
                Spacer(
                    modifier = Modifier.height(5.dp)
                )
                Row(
                    modifier = Modifier
                        .padding(start=10.dp)
                        .fillMaxWidth(),
                ) {
                    Text(
                        text = "RAM usage",
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Normal,
                        fontFamily = UbuntuFamily().ubuntuFamily,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Spacer(
                    modifier = Modifier.height(15.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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
                                text = "Total (Mb)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = UbuntuFamily().ubuntuFamily,
                                maxLines = 1
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
                                text = "Pods (Mb)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = UbuntuFamily().ubuntuFamily,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = String.format("%.2f", podsMemoryMb),
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
                                text = "Other (Mb)",
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
                Spacer(
                    modifier = Modifier.height(5.dp)
                )
                Row(
                    modifier = Modifier
                        .padding(start = 15.dp)
                        .padding(horizontal = 15.dp)
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Gray.copy(alpha = 0.2f))
                ) {
                    if (podsWeight > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(podsWeight)
                                .fillMaxHeight()
                                .background(Color(Colors.kubeColor))
                        )
                    }
                    if (podsWeight > 0f && otherWeight > 0f) {
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
                Spacer(
                    modifier = Modifier.height(10.dp)
                )
            }
        }
    }
}

@Composable
fun MetricLiveChart(
    title: String,
    percent: String,
    currentMetric: Double?,
    updateTrigger: Any?
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val maxDataPoints = 15

    val dataWindow = remember {
        LinkedList<Double>().apply {
            repeat(maxDataPoints) { add(0.0) }
        }
    }

    LaunchedEffect(updateTrigger) {
        if (currentMetric != null) {
            dataWindow.add(currentMetric)

            if (dataWindow.size > maxDataPoints) {
                dataWindow.removeFirst()
            }

            withContext(Dispatchers.Default) {
                modelProducer.runTransaction {
                    columnSeries {
                        series(dataWindow.toList())
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(start=5.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Normal,
                fontFamily = UbuntuFamily().ubuntuFamily,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = percent,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Normal,
                fontFamily = UbuntuFamily().ubuntuFamily,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberColumnCartesianLayer(
                        columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                            rememberLineComponent(
                                color = Color(Colors.kubeColor),
                                thickness = 120.dp
                            )
                        ),
                        axisValueOverrider = AxisValueOverrider.fixed(minY = 0.0, maxY = 100.0)
                    ),
                    startAxis = rememberStartAxis(
                        itemPlacer = remember { VerticalAxis.ItemPlacer.step({ 20.0 }) }
                    ),
                    bottomAxis = rememberBottomAxis(label = null)
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(8.dp))
                    .padding(16.dp)
                    .height(200.dp),

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
}

@Composable
fun OnSuccessMetrics(
    _metricsObject: WsMetricsMessage?,
    navController: NavController,
    metricsState: MetricsDataState
) {
    when(val currentState = metricsState){
        is MetricsDataState.LoadingMetricsData -> {}
        is MetricsDataState.SuccessMetricsData ->{
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MetricsCard(
                    data = currentState.data,
                    updateTick = currentState.receivedAt
                )
            }
        }
        is MetricsDataState.ErrorMetricsData ->{
            Text(text = "Updating error: ${currentState.e}", color = MaterialTheme.colorScheme.error)
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

class MetricsInitViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MetricsUIState>(MetricsUIState.LoadingMetrics)
    val uiState: StateFlow<MetricsUIState> = _uiState.asStateFlow()

    private val _metricsState = MutableStateFlow<MetricsDataState>(MetricsDataState.LoadingMetricsData)
    val metricsState: StateFlow<MetricsDataState> = _metricsState.asStateFlow()

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
            _uiState.value = MetricsUIState.LoadingMetrics
            openSocket(context, clusterId)
        }
    }

    private suspend fun openSocket(context: Context, clusterId: Int) {
        val token = TokenDataStore.getToken(context)
        if (token == null) {
            _uiState.value = MetricsUIState.ErrorMetrics("Not authorized")
            return
        }

        val wsBaseUrl = base_url
            .replace("https://", "wss://")
            .replace("http://", "ws://")
            .trimEnd('/')

        val url = "$wsBaseUrl/ws/metrics/$clusterId?token=$token"
        Log.d("WS_METRICS", "Connecting to $url")

        val request = Request.Builder().url(url).build()

        webSocket = httpClient.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                Log.d("WS_METRICS", "Connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WS_METRICS", "Message received at ${System.currentTimeMillis()}")
                handleMessage(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WS_METRICS", "Closed: $code / $reason")
                if (code == 1008) {
                    setError("Session was closed, reconnect please")
                } else if (!manuallyDisconnected) {
                    scheduleReconnect(context, clusterId)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                Log.e("WS_METRICS", "Failure: ${t.message}")
                setError("Connection error: ${t.message}")
                if (!manuallyDisconnected) {
                    scheduleReconnect(context, clusterId)
                }
            }
        })
    }

    private fun handleMessage(text: String) {
        try {
            if (text.contains("\"error\"")) {
                val err = gson.fromJson(text, WsErrorMessage::class.java)
                setError(if (err.type == "auth_expired") "Session was closed, reconnect please" else err.error)
                return
            }

            val data = gson.fromJson(text, WsMetricsMessage::class.java)
            _metricsState.value = MetricsDataState.SuccessMetricsData(data)

            if (_uiState.value !is MetricsUIState.SuccessMetrics) {
                _uiState.value = MetricsUIState.SuccessMetrics(data)
            }
        } catch (e: Exception) {
            Log.e("WS_METRICS", "Parse error: ${e.message}")
            _metricsState.value = MetricsDataState.ErrorMetricsData("Parse error: ${e.message}")
        }
    }

    private fun setError(message: String) {
        _metricsState.value = MetricsDataState.ErrorMetricsData(message)
        if (_uiState.value is MetricsUIState.LoadingMetrics) {
            _uiState.value = MetricsUIState.ErrorMetrics(message)
        }
    }

    private fun scheduleReconnect(context: Context, clusterId: Int) {
        reconnectJob?.cancel()
        reconnectJob = viewModelScope.launch {
            delay(5000)
            if (!manuallyDisconnected) {
                Log.d("WS_METRICS", "Reconnecting...")
                openSocket(context, clusterId)
            }
        }
    }

    fun disconnect() {
        manuallyDisconnected = true
        reconnectJob?.cancel()
        webSocket?.close(1000, "ViewModel cleared")
        webSocket = null
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}
