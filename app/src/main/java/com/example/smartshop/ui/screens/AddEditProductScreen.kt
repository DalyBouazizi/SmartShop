package com.example.smartshop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartshop.data.model.Product
import com.example.smartshop.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    productId: String?,
    onNavigateBack: () -> Unit,
    viewModel: ProductViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val isEditMode = productId != null

    // Load product if editing
    LaunchedEffect(productId) {
        if (productId != null) {
            isLoading = true
            val product = viewModel.getProductById(productId)
            product?.let {
                name = it.name
                quantity = it.quantity.toString()
                price = it.price.toString()
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Modifier le produit" else "Ajouter un produit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nom du produit") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantité") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Prix (TND)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        // Validation
                        val quantityInt = quantity.toIntOrNull()
                        val priceDouble = price.toDoubleOrNull()

                        when {
                            name.isBlank() -> errorMessage = "Le nom ne peut pas être vide"
                            quantityInt == null || quantityInt <= 0 ->
                                errorMessage = "La quantité doit être > 0"
                            priceDouble == null || priceDouble <= 0 ->
                                errorMessage = "Le prix doit être > 0"
                            else -> {
                                errorMessage = ""
                                isLoading = true

                                if (isEditMode && productId != null) {
                                    val existingProduct = viewModel.getProductById(productId)
                                    existingProduct?.let { product ->
                                        viewModel.updateProduct(
                                            product.copy(
                                                name = name,
                                                quantity = quantityInt,
                                                price = priceDouble
                                            )
                                        )
                                    }
                                } else {
                                    viewModel.addProduct(name, quantityInt, priceDouble)
                                }

                                onNavigateBack()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isEditMode) "Modifier" else "Ajouter")
                }
            }
        }
    }
}
