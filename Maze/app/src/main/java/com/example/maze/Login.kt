package com.example.login

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.maze.MazeAppGame
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

val DarkBluePrimary = Color(0xFF1A237E)
val DarkBlueSecondary = Color(0xFF2C387E)
val MatrixGreen = Color(0xFF00FF00)
val LightGrayText = Color(0xFFB0BEC5)

class LoginRegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = MatrixGreen,
                    onPrimary = Color.Black,
                    secondary = DarkBlueSecondary,
                    onSecondary = LightGrayText,
                    background = DarkBluePrimary,
                    surface = DarkBlueSecondary,
                    onBackground = LightGrayText,
                    onSurface = LightGrayText,
                    error = Color.Red,
                    onError = Color.White
                )
            ) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") {
                        LoginRegisterScreen(
                            onLoginSuccess = { username ->
                                navController.navigate("game/$username") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("game/{username}") { backStackEntry ->
                        val username = backStackEntry.arguments?.getString("username") ?: ""
                        MazeAppGame(username = username) // Truyền username vào MazeAppGame
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginRegisterScreen(
    onLoginSuccess: (String) -> Unit // Thay đổi để truyền username
) {
    val context = LocalContext.current
    var isLoginScreen by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val database = FirebaseDatabase.getInstance().reference.child("users")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBluePrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .background(DarkBlueSecondary, shape = RoundedCornerShape(16.dp))
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isLoginScreen) "ĐĂNG NHẬP" else "ĐĂNG KÝ",
                style = MaterialTheme.typography.headlineSmall,
                color = MatrixGreen
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    message = ""
                },
                label = { Text("Tên tài khoản", color = LightGrayText) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MatrixGreen,
                    unfocusedBorderColor = LightGrayText,
                    cursorColor = MatrixGreen,
                    focusedLabelColor = MatrixGreen,
                    unfocusedLabelColor = LightGrayText,
                    focusedTextColor = LightGrayText,
                    unfocusedTextColor = LightGrayText
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    message = ""
                },
                label = { Text("Mật khẩu") },
                visualTransformation = PasswordVisualTransformation(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MatrixGreen,
                    unfocusedBorderColor = LightGrayText,
                    cursorColor = MatrixGreen,
                    focusedLabelColor = MatrixGreen,
                    unfocusedLabelColor = LightGrayText,
                    focusedTextColor = LightGrayText,
                    unfocusedTextColor = LightGrayText
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!isLoginScreen) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        message = ""
                    },
                    label = { Text("Nhập lại mật khẩu") },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MatrixGreen,
                        unfocusedBorderColor = LightGrayText,
                        cursorColor = MatrixGreen,
                        focusedLabelColor = MatrixGreen,
                        unfocusedLabelColor = LightGrayText,
                        focusedTextColor = LightGrayText,
                        unfocusedTextColor = LightGrayText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank() || (!isLoginScreen && confirmPassword.isBlank())) {
                        message = "Vui lòng nhập đầy đủ thông tin"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (!isLoginScreen) {
                        if (password != confirmPassword) {
                            message = "Mật khẩu nhập lại không khớp"
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        database.child(username).get().addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                message = "Tên tài khoản đã tồn tại!"
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                Log.d("LoginRegister", "User already exists: $username")
                            } else {
                                database.child(username).child("password").setValue(password)
                                    .addOnSuccessListener {
                                        message = "Đăng ký thành công! Vui lòng đăng nhập."
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        Log.d("LoginRegister", "User registered successfully: $username")
                                        isLoginScreen = true
                                        username = ""
                                        password = ""
                                        confirmPassword = ""
                                    }
                                    .addOnFailureListener { e ->
                                        message = "Lỗi khi lưu dữ liệu: ${e.message}"
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                        Log.e("LoginRegister", "Failed to save user data", e)
                                    }
                            }
                        }.addOnFailureListener { e ->
                            message = "Lỗi kết nối Firebase: ${e.message}"
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            Log.e("LoginRegister", "Firebase get() failed", e)
                        }
                    } else {
                        database.child(username).get().addOnSuccessListener { snapshot ->
                            if (snapshot.exists()) {
                                val storedPassword = snapshot.child("password").getValue(String::class.java)
                                if (storedPassword == password) {
                                    message = "Đăng nhập thành công!"
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    Log.d("LoginRegister", "User logged in: $username")
                                    onLoginSuccess(username) // Truyền username khi đăng nhập thành công
                                } else {
                                    message = "Mật khẩu không đúng!"
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    Log.d("LoginRegister", "Incorrect password for user: $username")
                                }
                            } else {
                                message = "Tên tài khoản không tồn tại!"
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                Log.d("LoginRegister", "User does not exist: $username")
                            }
                        }.addOnFailureListener { e ->
                            message = "Lỗi kết nối Firebase: ${e.message}"
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            Log.e("LoginRegister", "Firebase get() failed", e)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MatrixGreen,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isLoginScreen) "ĐĂNG NHẬP" else "ĐĂNG KÝ")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    isLoginScreen = !isLoginScreen
                    message = ""
                    username = ""
                    password = ""
                    confirmPassword = ""
                }
            ) {
                Text(
                    text = if (isLoginScreen) "Chưa có tài khoản? Đăng ký ngay!" else "Đã có tài khoản? Đăng nhập!",
                    color = MatrixGreen
                )
            }

            if (message.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = message,
                    color = if (message.contains("thành công") || message.contains("Đăng ký thành công")) MatrixGreen else Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}