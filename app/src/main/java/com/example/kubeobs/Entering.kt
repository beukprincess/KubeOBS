package com.example.kubeobs

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController


@Composable
fun EnteringScreen(navController: NavController){
    val username: TextFieldState = rememberTextFieldState()
    val password: TextFieldState = rememberTextFieldState()
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Log In",
            modifier = Modifier
                .padding(top=50.dp),
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            OutlinedTextField(
                state = username,
                lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 2),
                textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold),
                label = { Text("Username") },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = Color(Colors.kubeColor),
                    errorBorderColor = Color(Colors.kubeColor),
                    focusedBorderColor = Color(Colors.kubeColor),
                    unfocusedBorderColor = Color(Colors.kubeColor)
                ),
                shape = RoundedCornerShape(10.dp)
            )
            OutlinedTextField(
                state = password,
                lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 2),
                textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold),
                label = { Text("Password") },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = Color(Colors.kubeColor),
                    errorBorderColor = Color(Colors.kubeColor),
                    focusedBorderColor = Color(Colors.kubeColor),
                    unfocusedBorderColor = Color(Colors.kubeColor)
                ),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
            )
            Button(
                colors = ButtonColors(
                    containerColor = Color(Colors.kubeColor),
                    contentColor = Color.White,
                    disabledContainerColor = Color(Colors.kubeColor),
                    disabledContentColor = Color.White
                ),
                modifier = Modifier
                    .size(
                        height = 50.dp,
                        width = 150.dp
                    ),
                onClick = {navController.navigate(Routes.MainScreen)}
            ) {
                Text(
                    text = "Log In",
                    fontSize = 22.sp
                )
            }
        }
        Text(
            text = "Have no account?",
            color = Color.Black,
        )
        TextButton(
            onClick = {},
            modifier = Modifier
                .padding(bottom = 30.dp),
            ) {
            Text(
                text = "Sign up",
                color = Color(Colors.kubeColor),
            )
        }
    }
}


















