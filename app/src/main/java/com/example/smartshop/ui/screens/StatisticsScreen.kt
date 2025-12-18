package com.example.smartshop.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
    val averagePrice = if (totalItems > 0) totalStockValue / totalItems else 0.0

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
                Text("Top 10 produits par stock", style = MaterialTheme.typography.titleMedium)
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
                HorizontalDivider()
            }

            item {
                Spacer(Modifier.height(24.dp))
                Text("Graphique: Top 5 produits (par valeur)", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
            }

            item {
                val top5 = products
                    .sortedByDescending { it.price * it.quantity }
                    .take(5)

                if (top5.isNotEmpty()) {
                    BarChartComposable(
                        data = top5.associate { it.name to (it.price * it.quantity) }
                    )
                } else {
                    Text("Aucune donnée disponible", style = MaterialTheme.typography.bodyMedium)
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                Text("Graphique: Quantité en stock (Top 5)", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
            }

            item {
                val top5Stock = products
                    .sortedByDescending { it.quantity }
                    .take(5)

                if (top5Stock.isNotEmpty()) {
                    BarChartComposable(
                        data = top5Stock.associate { it.name to it.quantity.toDouble() }
                    )
                } else {
                    Text("Aucune donnée disponible", style = MaterialTheme.typography.bodyMedium)
                }
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
private fun BarChartComposable(data: Map<String, Double>) {
    if (data.isEmpty()) return

    val maxValue = data.values.maxOrNull() ?: 1.0
    val barColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Canvas chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barCount = data.size
                val barWidth = (canvasWidth / barCount) * 0.7f
                val spacing = (canvasWidth / barCount) * 0.3f

                data.values.forEachIndexed { index, value ->
                    val barHeight = (value / maxValue * canvasHeight).toFloat()
                    val x = index * (barWidth + spacing) + spacing / 2

                    drawRect(
                        color = barColor,
                        topLeft = Offset(x, canvasHeight - barHeight),
                        size = Size(barWidth, barHeight)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Legend
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                data.entries.forEachIndexed { index, entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${index + 1}. ${entry.key}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "%.2f".format(entry.value),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
