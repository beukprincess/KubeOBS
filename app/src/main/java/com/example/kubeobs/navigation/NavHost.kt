package com.example.kubeobs.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kubeobs.auth.EnteringScreen
import com.example.kubeobs.clusters.ClustersScreen
import com.example.kubeobs.clusters.MetricsScreen
import com.example.kubeobs.data.TokenDataStore
import com.example.kubeobs.consts.Routes
import com.example.kubeobs.nodes.MainScreen
import com.example.kubeobs.pods.PodHealthScreen
import com.example.kubeobs.pods.PodScreen
import kotlinx.coroutines.runBlocking

@Composable
fun Navigation(){
    val navController = rememberNavController()
    val context = LocalContext.current

    val startDestination = remember {
        val token = runBlocking { TokenDataStore.getToken(context) }
        if (token != null) Routes.ClustersScreen else Routes.EnteringScreen
    }
    NavHost(
        navController = navController,
        startDestination = startDestination,
        builder = {
            composable(Routes.EnteringScreen){
                EnteringScreen(navController = navController)
            }
            composable(Routes.MainScreen){
                MainScreen(navController = navController)
            }
            composable(Routes.PodScreen){
                PodScreen(navController = navController)
            }
            composable(
                route = "${Routes.PodHealthScreen}/{podIndex}",
                arguments = listOf(
                    navArgument("podIndex") {
                        type = NavType.IntType
                        nullable = false
                    }
                )
            ) { backStackEntry ->
                val passedPodIndex = backStackEntry.arguments?.getInt("podIndex")
                    ?: throw IllegalArgumentException("podIndex is required")

                PodHealthScreen(
                    navController = navController,
                    podIndex = passedPodIndex
                )
            }
            composable(
                route = "${Routes.ClusterDetailScreen}/{clusterId}",
                arguments = listOf(
                    navArgument("clusterId") {
                        type = NavType.IntType
                        nullable = false
                    }
                )
            ) { backStackEntry ->
                val clusterId = backStackEntry.arguments?.getInt("clusterId")
                    ?: throw IllegalArgumentException("clusterId is required")
                MetricsScreen(navController = navController, clusterId = clusterId)
            }
            composable(Routes.ClustersScreen) {
                ClustersScreen(navController = navController)
            }
        })
}
