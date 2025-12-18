package com.example.smartshop.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smartshop.auth.LoginScreen
import com.example.smartshop.ui.screens.AddEditProductScreen
import com.example.smartshop.ui.screens.CartScreen
import com.example.smartshop.ui.screens.HomeScreen
import com.example.smartshop.ui.screens.ProductListScreen
import com.example.smartshop.ui.screens.StatisticsScreen
import com.example.smartshop.viewmodel.ProductViewModel
import com.example.smartshop.viewmodel.UserViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    val productViewModel: ProductViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    userViewModel.fetchUserRole()
                    navController.navigate(Routes.ProductList.route) {
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
                onLogoutToLogin = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToAdd = { navController.navigate(Routes.AddProduct.route) },
                onNavigateToStats = { navController.navigate(Routes.Statistics.route) },
                onNavigateToCart = { navController.navigate(Routes.Cart.route) },
                viewModel = productViewModel,
                userViewModel = userViewModel
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

        composable(Routes.Cart.route) {
            CartScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = productViewModel
            )
        }
    }
}
