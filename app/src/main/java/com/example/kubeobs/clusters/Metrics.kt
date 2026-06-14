package com.example.kubeobs.clusters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
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
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
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
import retrofit2.HttpException
import java.io.IOException

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
    LaunchedEffect(Unit) {
        viewModel.fetchData(context)
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
                            text = "Cluster metrics",
                            fontSize = 42.sp,
                            fontWeight=FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = UbuntuFamily().ubuntuFamily
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
                            colors = ButtonColors(
                                containerColor = Color(Colors.kubeColor),
                                contentColor = Color.White,
                                disabledContainerColor = Color(Colors.kubeColor),
                                disabledContentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal=20.dp)
                                .height(50.dp),
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
fun MetricsCard(data: MetricsResponse?){
    val cpuUsage = (data?.clusterMetrics?.firstOrNull()?.cpuUsage?.removeSuffix("n")?.toDoubleOrNull() ?: 0.0)/1_000_000_000.0 * 100
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp)
            .background(MaterialTheme.colorScheme.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ){
            Text(
                text = data?.source.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = UbuntuFamily().ubuntuFamily
            )
        }
        Spacer(
            modifier = Modifier
                .height(15.dp)
                .fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(
                text = "Quantity of nodes:",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = UbuntuFamily().ubuntuFamily
            )
            Text(
                text = data?.nodesCount.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = UbuntuFamily().ubuntuFamily
            )
        }
        Spacer(
            modifier = Modifier
                .height(15.dp)
                .fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(
                text = "CPU usage:",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = UbuntuFamily().ubuntuFamily
            )
            Text(
                text = cpuUsage.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = UbuntuFamily().ubuntuFamily
            )
        }
        Spacer(
            modifier = Modifier
                .height(15.dp)
                .fillMaxWidth()
        )
        MetricLiveChart(
            title = "(%)",
            currentMetric = cpuUsage,
            updateTrigger = data
        )
        Spacer(
            modifier = Modifier
                .height(15.dp)
                .fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(
                text = "RAM usage:",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = UbuntuFamily().ubuntuFamily
            )
            Text(
                text = "${data?.clusterMetrics?.firstOrNull()?.memoryUsedMb?.toString()} Mb",
                fontSize = 22.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = UbuntuFamily().ubuntuFamily
            )
        }
    }
}

@Composable
fun MetricLiveChart(
    title: String,
    currentMetric: Double?,
    updateTrigger: Any?
) {
    val modelProducer = remember { CartesianChartModelProducer() }
    val maxVisiblePoints = 60

    val chartData = remember { mutableListOf<Double>() }
    val stepCounter = remember { intArrayOf(0) }

    LaunchedEffect(updateTrigger) {
        if (currentMetric != null) {
            chartData.add(currentMetric)
            stepCounter[0]++

            if (chartData.size > maxVisiblePoints) {
                chartData.removeAt(0)
            }

            val startX = maxOf(0, stepCounter[0] - maxVisiblePoints)
            val xCoords = List(chartData.size) { startX + it }
            val yCoords = chartData.toList()

            withContext(Dispatchers.Default) {
                modelProducer.runTransaction {
                    lineSeries {
                        series(x = xCoords, y = yCoords)
                    }
                }
            }
        }
    }

    MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(background = Color.Black)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    color = Color.LightGray,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            val chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            fill = LineCartesianLayer.LineFill.single(fill(Color.Blue)),
                            areaFill = LineCartesianLayer.AreaFill.single(fill(Color(Colors.kubeColor).copy(alpha = 0.9f)))
                        )
                    )
                ),
                startAxis = VerticalAxis.rememberStart(
                    itemPlacer = remember { VerticalAxis.ItemPlacer.step({ 20.0 }) }
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    label = null,
                    guideline = null,
                    tick = null,
                    line = null
                )
            )
            CartesianChartHost(
                chart = chart,
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                scrollState = rememberVicoScrollState(scrollEnabled = false),
                animationSpec = null,
                animateIn = false
            )
        }
    }
}

@Composable
fun OnSuccessMetrics(
    _metricsObject: MetricsResponse?,
    navController: NavController,
    metricsState: MetricsDataState
) {
    when(val currentState = metricsState){
        is MetricsDataState.LoadingMetricsData ->{
        }
        is MetricsDataState.SuccessMetricsData ->{
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxSize(),
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(2.dp, Color(Colors.kubeColor)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    )
                ){
                    MetricsCard(data = currentState.data)
                }
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

class MetricsInitViewModel: ViewModel(){
    private val _uiState = MutableStateFlow<MetricsUIState>(MetricsUIState.LoadingMetrics)
    val uiState: StateFlow<MetricsUIState> = _uiState.asStateFlow()
    private val _metricsState = MutableStateFlow<MetricsDataState>(MetricsDataState.LoadingMetricsData)
    val metricsState: StateFlow<MetricsDataState> = _metricsState.asStateFlow()
    private var pollingJob: Job? = null

    fun startPolling(context: Context) {
        Log.d("POLLING", "startPolling called, job active: ${pollingJob?.isActive}")
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (isActive) {
                updateData(context)
                delay(1000)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
    }

    private suspend fun updateData(context: Context) {
        Log.d("POLLING", "updateData called at ${System.currentTimeMillis()}")
        try {
            Log.d("REQ","Making request...")
            val token = TokenDataStore.getToken(context)
            if (token == null) {
                _metricsState.value = MetricsDataState.ErrorMetricsData("Not authorized")
            }
            val response = RetrofitAPI.instance.getMetrics("Bearer $token")
            if (response.isSuccessful && response.body()!=null) {
                val responseData = response.body()
                Log.d("POLLING", "Setting metricsState: ${response.body()}")
                _metricsState.value = MetricsDataState.SuccessMetricsData(responseData)
            } else {
                val errorMsg = when (response.code()) {
                    400 -> "Invalid cluster data"
                    401 -> "Session expired, please log in again"
                    409 -> "Cluster with this name already exists"
                    else -> "Error: ${response.code()}"
                }
                _metricsState.value = MetricsDataState.ErrorMetricsData("Error: ${response.code()}")
            }
        } catch (e: HttpException) {
            _metricsState.value = MetricsDataState.ErrorMetricsData("Net err: ${e.message}")
        } catch (e: IOException) {
            _metricsState.value = MetricsDataState.ErrorMetricsData("Con err: ${e.message}")
        }
    }

    fun fetchData(context: Context) {
        viewModelScope.launch {
            _uiState.value = MetricsUIState.LoadingMetrics
            try {
                Log.i("KubeOBS_Network", "Making request...")
                val token = TokenDataStore.getToken(context)
                if (token == null) {
                    _uiState.value = MetricsUIState.ErrorMetrics("Not authorized")
                    return@launch
                }
                val response = RetrofitAPI.instance.getMetrics(
                    token = "Bearer $token",
                )
                Log.d("ADD_CLUSTER", "Code: ${response.code()}")
                Log.d("ADD_CLUSTER", "Error body: ${response.errorBody()?.string()}")
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = MetricsUIState.SuccessMetrics(response.body()!!)
                    delay(500)
                    startPolling(context)
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "Invalid cluster data"
                        401 -> "Session expired, please log in again"
                        409 -> "Cluster with this name already exists"
                        else -> "Error: ${response.code()}"
                    }
                    _uiState.value = MetricsUIState.ErrorMetrics(errorMsg)
                }
            } catch (e: HttpException) {
                _uiState.value = MetricsUIState.ErrorMetrics("Net err: ${e.message}")
            } catch (e: IOException) {
                _uiState.value = MetricsUIState.ErrorMetrics("Con err: ${e.message}")
            }
        }
    }
}
