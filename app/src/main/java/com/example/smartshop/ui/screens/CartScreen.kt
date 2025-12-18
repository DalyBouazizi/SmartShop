package com.example.smartshop.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.smartshop.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProductViewModel = viewModel()
) {
    val cart by viewModel.cart.collectAsState()
    var address by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("card") }

    val total = cart.sumOf { it.price * it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panier") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
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
            if (cart.isEmpty()) {
                Text("Votre panier est vide.")
                return@Column
            }

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(cart) { item ->
                    CartItemRow(
                        name = item.name,
                        imageUrl = item.imageUrl,
                        price = item.price,
                        quantity = item.quantity,
                        onIncrease = { viewModel.changeCartQuantity(item.id, +1) },
                        onDecrease = { viewModel.changeCartQuantity(item.id, -1) },
                        onRemove = { viewModel.removeFromCart(item.id) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Total: %.2f TND".format(total),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Adresse de livraison") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Text("Méthode de paiement:", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = paymentMethod == "card",
                    onClick = { paymentMethod = "card" },
                    label = { Text("Carte bancaire") }
                )
                FilterChip(
                    selected = paymentMethod == "cash",
                    onClick = { paymentMethod = "cash" },
                    label = { Text("Paiement à la livraison") }
                )
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (address.isBlank()) return@Button
                    viewModel.confirmOrder(paymentMethod, address)
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Confirmer la commande")
            }
        }
    }
}

@Composable
private fun CartItemRow(
    name: String,
    imageUrl: String,
    price: Double,
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(imageUrl.ifBlank { "https://picsum.photos/100/100" }),
            contentDescription = name,
            modifier = Modifier
                .size(64.dp)
                .padding(end = 8.dp)
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(name, style = MaterialTheme.typography.bodyMedium)
            Text(
                "%.2f TND".format(price * quantity),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onDecrease, enabled = quantity > 1) {
                Text("-")
            }
            Text(
                text = quantity.toString(),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            OutlinedButton(onClick = onIncrease) {
                Text("+")
            }
        }

        Spacer(Modifier.width(8.dp))

        TextButton(onClick = onRemove) {
            Text("Suppr.")
        }
    }
}
