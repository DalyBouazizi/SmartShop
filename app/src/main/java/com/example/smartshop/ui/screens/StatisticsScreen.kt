package com.example.smartshop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smartshop.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProductViewModel
) {
    val products by viewModel.products.collectAsState()

    val totalProducts = products.size
    val totalStockValue = products.sumOf { it.price * it.quantity }
    val totalItems = products.sumOf { it.quantity }
    val averagePrice = if (totalProducts > 0) totalStockValue / totalItems else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistiques") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Résumé du stock", style = MaterialTheme.typography.titleLarge)
            }

            item {
                StatCard("Nombre de produits", totalProducts.toString())
            }

            item {
                StatCard("Valeur totale du stock", "%.2f TND".format(totalStockValue))
            }

            item {
                StatCard("Nombre total d'articles", totalItems.toString())
            }

            item {
                StatCard("Prix moyen par article", "%.2f TND".format(averagePrice))
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text("Produits par stock", style = MaterialTheme.typography.titleMedium)
            }

            items(products.sortedByDescending { it.quantity }.take(10)) { product ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(product.name, modifier = Modifier.weight(1f))
                    Text("${product.quantity} unités", color = MaterialTheme.colorScheme.primary)
                }
                Divider()
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text("Graphique: Top 5 produits (par valeur)", style = MaterialTheme.typography.titleMedium)
            }

            item {
                val top5 = products
                    .sortedByDescending { it.price * it.quantity }
                    .take(5)

                SimpleBarChart(
                    data = top5.associate { it.name to (it.price * it.quantity) }
                )
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun SimpleBarChart(data: Map<String, Double>) {
    val maxValue = data.values.maxOrNull() ?: 1.0

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        data.forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = label,
                    modifier = Modifier.width(100.dp),
                    style = MaterialTheme.typography.bodySmall
                )
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .fillMaxWidth((value / maxValue).toFloat())
                        .padding(vertical = 4.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.primary
                    ) {}
                }
            }
        }
    }
}
