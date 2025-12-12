package com.example.smartshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.smartshop.navigation.NavGraph
import com.example.smartshop.navigation.Routes
import com.example.smartshop.ui.theme.SmartShopTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartShopTheme {
                val navController = rememberNavController()
                val auth = FirebaseAuth.getInstance()

                // Check if user is already logged in
                val startDestination = if (auth.currentUser != null) {
                    Routes.Home.route
                } else {
                    Routes.Login.route
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}

























//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import com.example.smartshop.auth.LoginScreen
//import com.example.smartshop.ui.theme.SmartShopTheme
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            SmartShopTheme {
//                var isLoggedIn by remember { mutableStateOf(false) }
//
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    if (isLoggedIn) {
//                        HomeScreen()
//                    } else {
//                        LoginScreen(onLoginSuccess = { isLoggedIn = true })
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun HomeScreen() {
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Text("Bienvenue dans SmartShop!", style = MaterialTheme.typography.headlineMedium)
//    }
//}
