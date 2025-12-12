package com.example.smartshop.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smartshop.auth.LoginScreen
import com.example.smartshop.ui.screens.*
import com.example.smartshop.viewmodel.ProductViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    // Shared ViewModel across screens
    val productViewModel: ProductViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.Home.route) {
            HomeScreen(
                onNavigateToProducts = {
                    navController.navigate(Routes.ProductList.route)
                },
                onNavigateToStats = {
                    navController.navigate(Routes.Statistics.route)
                },
                onLogout = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ProductList.route) {
            ProductListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAdd = { navController.navigate(Routes.AddProduct.route) },
                onNavigateToEdit = { productId ->
                    navController.navigate(Routes.EditProduct.createRoute(productId))
                },
                viewModel = productViewModel
            )
        }

        composable(Routes.AddProduct.route) {
            AddEditProductScreen(
                productId = null,
                onNavigateBack = { navController.popBackStack() },
                viewModel = productViewModel
            )
        }

        composable(
            route = Routes.EditProduct.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            AddEditProductScreen(
                productId = productId,
                onNavigateBack = { navController.popBackStack() },
                viewModel = productViewModel
            )
        }

        composable(Routes.Statistics.route) {
            StatisticsScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = productViewModel
            )
        }
    }
}