package com.example.kubeobs

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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
            composable(Routes.StatScreen){
                StatScreen(navController = navController)
            }
        })
}
