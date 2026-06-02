package com.example.kubeobs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Composable
fun EnteringScreen(
    navController: NavController,
){
    var isSignUpDisplays by remember { mutableStateOf(false) }
    val slideDur: Int = 500
    val visibilityDur: Int = 1000
    val pxToMove = with(LocalDensity.current) {
        1000.dp.toPx().roundToInt()
    }
    val offset by animateIntOffsetAsState(
        targetValue = if (isSignUpDisplays) {
            IntOffset(0, pxToMove)
        } else {
            IntOffset.Zero
        },
        animationSpec = tween(durationMillis = slideDur),
        label = "offset"
    )
    val offsetA by animateIntOffsetAsState(
        targetValue = if (!isSignUpDisplays) {
            IntOffset(0, pxToMove)
        } else {
            IntOffset.Zero
        },
        animationSpec = tween(durationMillis = slideDur),
        label = "offsetA"
    )
    AnimatedVisibility(
        visible = !isSignUpDisplays,
        enter = fadeIn(
            animationSpec = tween(durationMillis = visibilityDur)
        ) + expandVertically(
            animationSpec = tween(durationMillis = visibilityDur)
        ),
        exit = fadeOut(
            animationSpec = tween(durationMillis = visibilityDur)
        ) + shrinkVertically(
            animationSpec = tween(durationMillis = visibilityDur)
        )
    ) {
        val username: TextFieldState = rememberTextFieldState()
        val password: TextFieldState = rememberTextFieldState()
        Column(
            modifier = Modifier
                .offset{
                    offset
                }
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Log In",
                modifier = Modifier
                    .padding(top=50.dp),
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontFamily = UbuntuFamily().ubuntuFamily
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
                    label = {
                        Text(
                            text = "Username",
                            fontFamily = UbuntuFamily().ubuntuFamily
                        )
                            },
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
                    label = {
                        Text(
                            text = "Password",
                            fontFamily = UbuntuFamily().ubuntuFamily
                        )
                    },
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
                    onClick = {
                        navController.navigate(Routes.MainScreen)
                    }
                ) {
                    Text(
                        text = "Log In",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = UbuntuFamily().ubuntuFamily
                    )
                }
            }
            Text(
                text = "Have no account?",
                color = Color.Black,
                fontWeight = FontWeight.Normal,
                fontFamily = UbuntuFamily().ubuntuFamily
            )
            TextButton(
                onClick = {isSignUpDisplays=!isSignUpDisplays},
                modifier = Modifier
                    .padding(bottom = 30.dp),
            ) {
                Text(
                    text = "Sign up",
                    color = Color(Colors.kubeColor),
                    fontWeight = FontWeight.Normal,
                    fontFamily = UbuntuFamily().ubuntuFamily
                )
            }
        }
    }
    AnimatedVisibility(
        visible = isSignUpDisplays,
        enter = fadeIn(
            animationSpec = tween(durationMillis = visibilityDur)
        ) + expandVertically(
            animationSpec = tween(durationMillis = visibilityDur)
        ),
        exit = fadeOut(
            animationSpec = tween(durationMillis = visibilityDur)
        ) + shrinkVertically(
            animationSpec = tween(durationMillis = visibilityDur)
        )
    ) {
        val username: TextFieldState = rememberTextFieldState()
        val password: TextFieldState = rememberTextFieldState()
        val validationPassword: TextFieldState = rememberTextFieldState()
        var adviceText: String by remember { mutableStateOf("Confirm your password") }
        Column(
            modifier = Modifier
                .offset{
                    offsetA
                }
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Sign Up",
                modifier = Modifier
                    .padding(top=50.dp),
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontFamily = UbuntuFamily().ubuntuFamily
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
                    label = {
                        Text(
                            text = "Username",
                            fontFamily = UbuntuFamily().ubuntuFamily
                        )
                    },
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
                    label = {
                        Text(
                            text = "Password",
                            fontFamily = UbuntuFamily().ubuntuFamily
                        )
                    },
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
                        .height(5.dp)
                )
                Row(
                    modifier = Modifier.width(OutlinedTextFieldDefaults.MinWidth)
                ){
                    Text(
                        text = adviceText,
                        fontWeight = FontWeight.Normal,
                        fontFamily = UbuntuFamily().ubuntuFamily
                    )
                }
                OutlinedTextField(
                    state = validationPassword,
                    lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 2),
                    textStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold),
                    label = {
                        Text(
                            text = "Rewrite password",
                            fontFamily = UbuntuFamily().ubuntuFamily
                        )
                    },
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
                    onClick = {
                        if(signUpValidation(password.text.toString(), validationPassword.text.toString())=="Confirm your password"){
                            navController.navigate(Routes.MainScreen)
                        }
                        else{
                            adviceText=signUpValidation(password.text.toString(), validationPassword.text.toString())
                        }
                    }
                ) {
                    Text(
                        text = "Sign Up",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = UbuntuFamily().ubuntuFamily
                    )
                }
            }
            Text(
                text = "Have an account?",
                color = Color.Black,
                fontWeight = FontWeight.Normal,
                fontFamily = UbuntuFamily().ubuntuFamily
            )
            TextButton(
                onClick = {isSignUpDisplays=!isSignUpDisplays},
                modifier = Modifier
                    .padding(bottom = 30.dp),
            ) {
                Text(
                    text = "Log In",
                    color = Color(Colors.kubeColor),
                    fontWeight = FontWeight.Normal,
                    fontFamily = UbuntuFamily().ubuntuFamily
                )
            }
        }
    }
}

fun signUpValidation(pass: String, valPass: String): String = runBlocking{
    var upperQuantity: Int = 0
    var lowerQuantity: Int = 0
    var digitQuantity: Int = 0
    for (x in 0 until pass.length){
        if(pass[x].isUpperCase()){upperQuantity++}
        if(pass[x].isLowerCase()){lowerQuantity++}
        if(pass[x].isDigit()){digitQuantity++}
    }
    if (pass != valPass){
        return@runBlocking "Passwords mismatch"
    }
    else if(pass.length<8){
        return@runBlocking "Password has to contain at least 8 symbols"
    }
    else if(upperQuantity==0 || lowerQuantity==0 || digitQuantity==0){
        return@runBlocking "Password has to contain uppercase, lowercase and numbers"
    }
    else{
        return@runBlocking "Confirm your password"
    }
}
















