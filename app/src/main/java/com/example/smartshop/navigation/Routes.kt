package com.example.smartshop.navigation

sealed class Routes(val route: String) {
    object Login : Routes("login")
    object Home : Routes("home")
    object ProductList : Routes("product_list")
    object AddProduct : Routes("add_product")
    object EditProduct : Routes("edit_product/{productId}") {
        fun createRoute(productId: String) = "edit_product/$productId"
    }
    object Statistics : Routes("statistics")
}