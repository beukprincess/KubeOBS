package com.example.kubeobs.clusters

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
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
import com.example.kubeobs.data.AddClusterRequest
import com.example.kubeobs.data.AddClusterState
import com.example.kubeobs.data.ClusterResponse
import com.example.kubeobs.data.ClustersState
import com.example.kubeobs.data.LogOutState
import com.example.kubeobs.data.RetrofitAPI
import com.example.kubeobs.data.TokenDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClustersScreen(
    viewModel: ClustersViewModel = viewModel(),
    navController: NavController
) {
    val context = LocalContext.current
    val clustersState by viewModel.clustersState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val logOutState by viewModel.LOState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadClusters(context)
    }

    if (showAddDialog) {
        AddClusterDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            onSuccess = {
                showAddDialog = false
                viewModel.loadClusters(context)
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title= {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ){
                        Text(
                            text = "Clusters",
                            fontSize = 42.sp,
                            fontWeight=FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = UbuntuFamily().ubuntuFamily
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.logOut(context, navController) },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(
                        height = 50.dp,
                        width = 120.dp
                    )
                    .padding(end=5.dp)
            ) {
                Text(
                    text = "Log out",
                    fontFamily = UbuntuFamily().ubuntuFamily,
                    fontSize = 20.sp,
                    fontWeight=FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val s = clustersState) {
                is ClustersState.IdleResult -> {}
                is ClustersState.LoadingResult -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                is ClustersState.ErrorResult -> {
                    Text(
                        text = s.e,
                        color = Color.Red,
                        fontFamily = UbuntuFamily().ubuntuFamily
                    )
                    Spacer(
                        modifier = Modifier.height(16.dp)
                    )
                    Button(
                        onClick = { viewModel.loadClusters(context) },
                        colors = ButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            disabledContainerColor = MaterialTheme.colorScheme.primary,
                            disabledContentColor = Color.White
                        )
                    ) {
                        Text("Try again", fontFamily = UbuntuFamily().ubuntuFamily)
                    }
                }
                is ClustersState.SuccessResult -> {
                    if (s.clusters.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No clusters yet.\nTap + to add one.",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontFamily = UbuntuFamily().ubuntuFamily,
                                fontSize = 18.sp
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(s.clusters, key = { it.id }) { cluster ->
                                ClusterCard(
                                    cluster = cluster,
                                    onClick = {
                                        navController.navigate("${Routes.ClusterDetailScreen}/${cluster.id}")
                                    }
                                )
                            }
                        }
                    }
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(top=8.dp)
                    ) {
                        Icon(Icons.Default.Add,
                            contentDescription = "Add cluster",
                            tint = Color.White,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClusterCard(
    cluster: ClusterResponse,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text(
                        text = "Cluster: ",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = UbuntuFamily().ubuntuFamily
                    )
                    Text(
                        modifier = Modifier.padding(start=3.dp),
                        text = cluster.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = UbuntuFamily().ubuntuFamily
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = cluster.endpointUrl,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = UbuntuFamily().ubuntuFamily,
                    maxLines = 1
                )
            }
        }
    }
}
@Composable
fun AddClusterDialog(
    viewModel: ClustersViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val addState by viewModel.addState.collectAsState()

    val name: TextFieldState = rememberTextFieldState()
    val endpointUrl: TextFieldState = rememberTextFieldState()
    val clusterToken: TextFieldState = rememberTextFieldState()

    LaunchedEffect(addState) {
        if (addState is AddClusterState.SuccessResult) {
            viewModel.resetAddState()
            onSuccess()
        }
    }

    AlertDialog(
        onDismissRequest = {
            viewModel.resetAddState()
            onDismiss()
        },
        confirmButton = {
            Button(
                colors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = Color.White
                ),
                onClick = {
                    viewModel.addCluster(
                        context = context,
                        name = name.text.toString(),
                        endpointUrl = endpointUrl.text.toString(),
                        clusterToken = clusterToken.text.toString()
                    )
                },
                enabled = addState !is AddClusterState.LoadingResult
            ) {
                if (addState is AddClusterState.LoadingResult) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Add", fontFamily = UbuntuFamily().ubuntuFamily)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.resetAddState()
                onDismiss()
            }) {
                Text(
                    "Cancel",
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = UbuntuFamily().ubuntuFamily
                )
            }
        },
        title = {
            Text(
                "Add Cluster",
                fontWeight = FontWeight.Bold,
                fontFamily = UbuntuFamily().ubuntuFamily
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (val s = addState) {
                    is AddClusterState.ErrorResult -> {
                        Text(
                            text = s.e,
                            color = Color.Red,
                            fontFamily = UbuntuFamily().ubuntuFamily
                        )
                    }
                    else -> {}
                }
                OutlinedTextField(
                    state = name,
                    lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 1),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    ),
                    label = { Text("Name", fontFamily = UbuntuFamily().ubuntuFamily) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    state = endpointUrl,
                    lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 1),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    ),
                    label = { Text("Endpoint URL", fontFamily = UbuntuFamily().ubuntuFamily) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    state = clusterToken,
                    lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 1),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    ),
                    label = { Text("Cluster token", fontFamily = UbuntuFamily().ubuntuFamily) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    )
}

class ClustersViewModel : ViewModel() {
    private val _LOState = MutableStateFlow<LogOutState>(LogOutState.IdleLogOut)
    val LOState: StateFlow<LogOutState> = _LOState
    private val _clustersState = MutableStateFlow<ClustersState>(ClustersState.IdleResult)
    val clustersState: StateFlow<ClustersState> = _clustersState

    private val _addState = MutableStateFlow<AddClusterState>(AddClusterState.IdleResult)
    val addState: StateFlow<AddClusterState> = _addState

    fun logOut(context: Context, navController: NavController){
        viewModelScope.launch{
            _LOState.value = LogOutState.LoadingLogOut
            val token = TokenDataStore.getToken(context)
            Log.d("LOGOUT", "Token first try: $token")
            TokenDataStore.clearToken(context)
            if(token==null){
                Log.d("LOGOUT", "Token second try: $token")
                _LOState.value = LogOutState.SuccessLogOut
                navController.navigate(Routes.EnteringScreen)
            }
            else{
                TokenDataStore.clearToken(context)
                TokenDataStore.clearToken(context)
                TokenDataStore.clearToken(context)
                TokenDataStore.clearToken(context)
                TokenDataStore.clearToken(context)
                TokenDataStore.clearToken(context)
                TokenDataStore.clearToken(context)
                Log.d("LOGOUT", "Token second try: $token")
            }
        }
    }
    fun loadClusters(context: Context) {
        viewModelScope.launch {
            try {
                _clustersState.value = ClustersState.LoadingResult
                val token = TokenDataStore.getToken(context)
                if (token == null) {
                    _clustersState.value = ClustersState.ErrorResult("Not authorized")
                    return@launch
                }
                val response = RetrofitAPI.instance.getClusters("Bearer $token")
                Log.d("CLUSTERS", "Code: ${response.code()}")
                Log.d("ADD_CLUSTER", "Error body: ${response.errorBody()?.string()}")
                if (response.isSuccessful) {
                    _clustersState.value = ClustersState.SuccessResult(response.body() ?: emptyList())
                } else {
                    _clustersState.value = ClustersState.ErrorResult("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("CLUSTERS", "Exception: ${e.message}")
                _clustersState.value = ClustersState.ErrorResult("Network error: ${e.message}")
            }
        }
    }

    fun addCluster(context: Context, name: String, endpointUrl: String, clusterToken: String) {
        viewModelScope.launch {
            try {
                _addState.value = AddClusterState.LoadingResult
                val token = TokenDataStore.getToken(context)
                if (token == null) {
                    _addState.value = AddClusterState.ErrorResult("Not authorized")
                    return@launch
                }
                val response = RetrofitAPI.instance.addCluster(
                    token = "Bearer $token",
                    body = AddClusterRequest(name, endpointUrl, clusterToken)
                )
                Log.d("ADD_CLUSTER", "Code: ${response.code()}")
                Log.d("ADD_CLUSTER", "Error body: ${response.errorBody()?.string()}")
                if (response.isSuccessful && response.body() != null) {
                    _addState.value = AddClusterState.SuccessResult(response.body()!!)
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "Invalid cluster data"
                        401 -> "Session expired, please log in again"
                        409 -> "Cluster with this name already exists"
                        else -> "Error: ${response.code()}"
                    }
                    _addState.value = AddClusterState.ErrorResult(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("ADD_CLUSTER", "Exception: ${e.message}")
                _addState.value = AddClusterState.ErrorResult("Network error: ${e.message}")
            }
        }
    }

    fun resetAddState() {
        _addState.value = AddClusterState.IdleResult
    }
}