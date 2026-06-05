package com.example.kubeobs

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import kotlin.text.toDouble

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricsScreen(
    navController: NavController,
    viewModel: MetricsInitViewModel = viewModel()
){
    var displayDialog = remember {mutableStateOf(false)}
    val state by viewModel.uiState.collectAsState()
    val metricsState by viewModel.metricsState.collectAsState()
    DisposableEffect(Unit) {
        viewModel.startPolling()
        onDispose {
            viewModel.stopPolling()
        }
    }
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
                text = "CPU usage:",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = UbuntuFamily().ubuntuFamily
            )
            Text(
                text = data?.metrics?.cpuPercentage.toString(),
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
        MetricLiveChart(
            title = "CPU usage (%)",
            currentMetric = data?.metrics?.cpuPercentage?.toDouble(),
            updateTrigger = data
        )
        Text(
            text = "RAM usage: ",
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
                text = data?.metrics?.ram?.totalGB.toString(),
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
                text = data?.metrics?.ram?.usedGB.toString(),
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
                text = data?.metrics?.ram?.percentage.toString(),
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
        MetricLiveChart(
            title = "RAM usage (%)",
            currentMetric = data?.metrics?.ram?.percentage?.toDouble(),
            updateTrigger = data
        )
        Text(
            text = "Disk usage:",
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
                text = data?.metrics?.disk?.totalGB.toString(),
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
                text = data?.metrics?.disk?.freeGB.toString(),
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
                text = data?.metrics?.disk?.percentage.toString(),
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
        MetricLiveChart(
            title = "Disk usage (%)",
            currentMetric = data?.metrics?.disk?.percentage?.toDouble(),
            updateTrigger = data
        )
    }
}

@Composable
fun MetricLiveChart(
    title: String,
    currentMetric: Double?,
    updateTrigger: Any? // Додаємо тригер для LaunchedEffect
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

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
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
                .padding(vertical = 8.dp) // Додамо невеликий відступ між графіками
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title, // Використовуємо переданий заголовок
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
                    .height(80.dp),
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
                        containerColor = Color.White,
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

    init {
        fetchData()
    }

    fun startPolling() {
        if (pollingJob?.isActive == true) return

        pollingJob = viewModelScope.launch {
            while (isActive) {
                updateData()
                delay(500)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
    }

    private suspend fun updateData() {
        try {
            val metricsResponse = RetrofitAPI.instance.getMetrics()

            if (metricsResponse.isSuccessful) {
                val responseData = metricsResponse.body()
                _metricsState.value = MetricsDataState.SuccessMetricsData(responseData)
            } else {
                _metricsState.value = MetricsDataState.ErrorMetricsData("Error: ${metricsResponse.code()}")
            }
        } catch (e: HttpException) {
            _metricsState.value = MetricsDataState.ErrorMetricsData("Net err: ${e.message}")
        } catch (e: IOException) {
            _metricsState.value = MetricsDataState.ErrorMetricsData("Con err: ${e.message}")
        }
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
                    startPolling()
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
