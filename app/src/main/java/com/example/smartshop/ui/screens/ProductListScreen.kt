package com.example.smartshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.smartshop.data.model.Product
import com.example.smartshop.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: ProductViewModel = viewModel()
) {
    val products by viewModel.products.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Produits") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
                Spacer(Modifier.width(8.dp))
                Text("Ajouter")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(vertical = 8.dp)
        ) {
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucun produit pour le moment.\nAppuyez sur “Ajouter”.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "Deals of the Day",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products) { product ->
                        ProductCard(
                            product = product,
                            onEdit = { onNavigateToEdit(product.id) },
                            onDelete = { viewModel.deleteProduct(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val cardColors = if (product.isDealOfTheDay) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    } else {
        CardDefaults.cardColors()
    }

    Card(
        modifier = Modifier
            .width(180.dp)
            .height(260.dp),
        colors = cardColors,
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = product.imageUrl.ifBlank { "https://picsum.photos/300/300" },
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "%.2f TND".format(product.price),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(4.dp))

            RatingBar(rating = product.rating)

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onEdit) {
                    Text("Modifier")
                }
                TextButton(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Suppr.")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer le produit") },
            text = { Text("Voulez-vous vraiment supprimer ${product.name} ?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun RatingBar(rating: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { index ->
            val filled = rating >= index + 1
            val starColor = if (filled) Color(0xFFFFC107) else Color(0xFFE0E0E0)
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(starColor, shape = MaterialTheme.shapes.small)
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = "%.1f".format(rating),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
