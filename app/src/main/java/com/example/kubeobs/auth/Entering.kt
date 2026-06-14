package com.example.kubeobs.auth

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.kubeobs.data.TokenDataStore
import kotlin.math.roundToInt
import com.example.kubeobs.consts.Routes
import com.example.kubeobs.consts.UbuntuFamily
import com.example.kubeobs.data.LoginRequest
import com.example.kubeobs.data.LoginResultState
import com.example.kubeobs.data.RegResultState
import com.example.kubeobs.data.RegisterRequest
import com.example.kubeobs.data.RetrofitAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun EnteringScreen(
    navController: NavController,
    viewModel: RegisterViewModel = viewModel()
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
        val email: TextFieldState = rememberTextFieldState()
        val password: TextFieldState = rememberTextFieldState()
        val loginViewModel: LoginViewModel = viewModel()
        val loginState by loginViewModel.loginState.collectAsState()
        val context = LocalContext.current

        LaunchedEffect(loginState) {
            if (loginState is LoginResultState.SuccessResult) {
                navController.navigate(Routes.ClustersScreen) {
                    popUpTo(0)
                }
            }
        }
        Column(
            modifier = Modifier
                .offset{
                    offset
                }
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (val s = loginState) {
                is LoginResultState.LoadingResult -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))
                }
                is LoginResultState.ErrorResult -> {
                    Text(text = s.e, color = Color.Red, fontFamily = UbuntuFamily().ubuntuFamily)
                    Spacer(modifier = Modifier.height(10.dp))
                }
                else -> {}
            }
            Text(
                text = "Log In",
                modifier = Modifier
                    .padding(top=50.dp),
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
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
                    state = email,
                    lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 2),
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold),
                    label = {
                        Text(
                            text = "Email",
                            fontFamily = UbuntuFamily().ubuntuFamily
                        )
                            },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.primary,
                        errorBorderColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    state = password,
                    lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 2),
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold),
                    label = {
                        Text(
                            text = "Password",
                            fontFamily = UbuntuFamily().ubuntuFamily
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.primary,
                        errorBorderColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary
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
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.primary,
                        disabledContentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(
                            height = 50.dp,
                            width = 150.dp
                        ),
                    onClick = {
                        loginViewModel.requestForLogin(
                            context = context,
                            email = email.text.toString(),
                            password = password.text.toString()
                        )
                    }
                ) {
                    Text(
                        text = "Log In",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White,
                        fontFamily = UbuntuFamily().ubuntuFamily
                    )
                }
            }
            Text(
                text = "Have no account?",
                color = MaterialTheme.colorScheme.onBackground,
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
                    color = MaterialTheme.colorScheme.primary,
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
        val email: TextFieldState = rememberTextFieldState()
        val password: TextFieldState = rememberTextFieldState()
        val validationPassword: TextFieldState = rememberTextFieldState()
        var adviceText: String by remember { mutableStateOf("Confirm your password") }
        val regState by viewModel.regRequest.collectAsState()
        LaunchedEffect(regState) {
            if (regState is RegResultState.SuccessResult) {
                isSignUpDisplays = false
            }
        }
        Column(
            modifier = Modifier
                .offset{
                    offsetA
                }
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (val currentState = regState) {
                is RegResultState.IdleResult -> { }
                is RegResultState.LoadingResult -> {
                    OnRegLoading()
                }
                is RegResultState.ErrorResult -> {
                    Text(
                        text = currentState.e,
                        color = Color.Red,
                        fontFamily = UbuntuFamily().ubuntuFamily
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                is RegResultState.SuccessResult -> { }
            }
            Text(
                text = "Sign Up",
                modifier = Modifier
                    .padding(top=50.dp),
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
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
                    state = email,
                    lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 2),
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold),
                    label = {
                        Text(
                            text = "Email",
                            fontFamily = UbuntuFamily().ubuntuFamily
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.primary,
                        errorBorderColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    state = password,
                    lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 2),
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold),
                    label = {
                        Text(
                            text = "Password",
                            fontFamily = UbuntuFamily().ubuntuFamily
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.primary,
                        errorBorderColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary
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
                        fontFamily = UbuntuFamily().ubuntuFamily,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                OutlinedTextField(
                    state = validationPassword,
                    lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 2),
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold),
                    label = {
                        Text(
                            text = "Rewrite password",
                            fontFamily = UbuntuFamily().ubuntuFamily
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledBorderColor = MaterialTheme.colorScheme.primary,
                        errorBorderColor = MaterialTheme.colorScheme.primary,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.primary
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
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.primary,
                        disabledContentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(
                            height = 50.dp,
                            width = 150.dp
                        ),
                    onClick = {
                        val validationMsg = signUpValidation(password.text.toString(), validationPassword.text.toString())
                        if (validationMsg == "Confirm your password") {
                            viewModel.requestForReg(
                                email = email.text.toString(),
                                password = password.text.toString()
                            )
                        } else {
                            adviceText = validationMsg
                        }
                    }
                ) {
                    Text(
                        text = "Sign Up",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = UbuntuFamily().ubuntuFamily
                    )
                }
            }
            Text(
                text = "Have an account?",
                color = MaterialTheme.colorScheme.onBackground,
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
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Normal,
                    fontFamily = UbuntuFamily().ubuntuFamily
                )
            }
        }
    }
}

@Composable
fun OnRegLoading(){
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}
fun signUpValidation(pass: String, valPass: String): String {
    var upperQuantity = 0
    var lowerQuantity = 0
    var digitQuantity = 0
    for (x in pass.indices){
        if(pass[x].isUpperCase()){upperQuantity++}
        if(pass[x].isLowerCase()){lowerQuantity++}
        if(pass[x].isDigit()){digitQuantity++}
    }

    return when {
        pass != valPass -> "Passwords mismatch"
        pass.length < 8 -> "Password has to contain at least 8 symbols"
        upperQuantity == 0 || lowerQuantity == 0 || digitQuantity == 0 -> "Password has to contain uppercase, lowercase and numbers"
        else -> "Confirm your password"
    }
}

class RegisterViewModel : ViewModel() {
    private val _regRequest = MutableStateFlow<RegResultState>(RegResultState.IdleResult)
    val regRequest: StateFlow<RegResultState> = _regRequest

    fun requestForReg(email: String, password: String) {
        viewModelScope.launch {
            try {
                _regRequest.value = RegResultState.LoadingResult
                val response = RetrofitAPI.instance.registerUser(RegisterRequest(email, password))
                Log.d("REGISTER", "Code: ${response.code()}")
                Log.d("REGISTER", "Headers: ${response.headers()}")
                Log.d("REGISTER", "Error body: ${response.errorBody()?.string()}")
                Log.d("REGISTER", "Body: ${response.body()}")
                if (response.isSuccessful) {
                    _regRequest.value = RegResultState.SuccessResult(response.body())
                    Log.d("REGISTER", "Success: ${response.body()?.email}")
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> "Email has been already used"
                        else -> "Error: ${response.code()}"
                    }
                    _regRequest.value = RegResultState.ErrorResult(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("REGISTER", "Exception type: ${e::class.java.simpleName}")
                Log.e("REGISTER", "Message: ${e.message}")
                Log.e("REGISTER", "Cause: ${e.cause}")
                _regRequest.value = RegResultState.ErrorResult("Network error: ${e.message}")
            }
        }
    }
}

class LoginViewModel : ViewModel() {
    private val _loginState = MutableStateFlow<LoginResultState>(LoginResultState.IdleResult)
    val loginState: StateFlow<LoginResultState> = _loginState

    fun requestForLogin(context: Context, email: String, password: String) {
        viewModelScope.launch {
            try {
                _loginState.value = LoginResultState.LoadingResult
                val response = RetrofitAPI.instance.loginUser(LoginRequest(email, password))
                Log.d("LOGIN", "Code: ${response.code()}")
                Log.d("LOGIN", "Error body: ${response.errorBody()?.string()}")
                Log.d("LOGIN", "Body: ${response.body()}")
                if (response.isSuccessful) {
                    val token = response.body()?.accessToken
                    if (token != null) {
                        TokenDataStore.saveToken(context, token)
                        _loginState.value = LoginResultState.SuccessResult(token)
                        Log.d("LOGIN", "Token saved!")
                    } else {
                        _loginState.value = LoginResultState.ErrorResult("Token is empty")
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Wrong email or password"
                        else -> "Error: ${response.code()}"
                    }
                    _loginState.value = LoginResultState.ErrorResult(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("LOGIN", "Exception: ${e::class.java.simpleName}")
                Log.e("LOGIN", "Message: ${e.message}")
                _loginState.value = LoginResultState.ErrorResult("Network error: ${e.message}")
            }
        }
    }
}












