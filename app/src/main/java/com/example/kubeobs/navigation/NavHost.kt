package com.example.kubeobs.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kubeobs.auth.EnteringScreen
import com.example.kubeobs.clusters.MetricsScreen
import com.example.kubeobs.consts.Routes
import com.example.kubeobs.nodes.MainScreen
import com.example.kubeobs.pods.PodHealthScreen
import com.example.kubeobs.pods.PodScreen

@Composable
fun Navigation(){
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.EnteringScreen,
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
            composable(Routes.MetricsScreen){
                MetricsScreen(navController = navController)
            }
        })
}
