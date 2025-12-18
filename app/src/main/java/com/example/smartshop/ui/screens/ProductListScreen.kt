package com.example.smartshop.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.smartshop.data.model.Product
import com.example.smartshop.viewmodel.ProductViewModel
import com.example.smartshop.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onLogoutToLogin: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToCart: () -> Unit,
    viewModel: ProductViewModel,
    userViewModel: UserViewModel
) {
    val products by viewModel.products.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val userRole by userViewModel.userRole.collectAsState()
    val auth = FirebaseAuth.getInstance()

    val isAdmin = userRole == "admin"
    val cartItemCount = cart.sumOf { it.quantity }

    var productToDelete by remember { mutableStateOf<Product?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SmartShop",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = onNavigateToStats) {
                            Icon(
                                imageVector = Icons.Filled.PieChart,
                                contentDescription = "Statistiques",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Cart icon with badge
                    Box {
                        IconButton(onClick = onNavigateToCart) {
                            Icon(
                                imageVector = Icons.Filled.ShoppingCart,
                                contentDescription = "Panier",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Red badge with count
                        if (cartItemCount > 0) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 4.dp, end = 4.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.error
                            ) {
                                Text(
                                    text = if (cartItemCount > 99) "99+" else cartItemCount.toString(),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    IconButton(onClick = {
                        auth.signOut()
                        onLogoutToLogin()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ExitToApp,
                            contentDescription = "Déconnexion",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = onNavigateToAdd,
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Ajouter",
                        tint = Color.White
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🛒",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        text = "Aucun produit disponible",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        ModernProductCard(
                            product = product,
                            isAdmin = isAdmin,
                            onAdminClick = { onNavigateToEdit(product.id) },
                            onUserAddToCart = { viewModel.addItemToCart(product) },
                            onLongClick = {
                                if (isAdmin) {
                                    productToDelete = product
                                    showDeleteDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showDeleteDialog && productToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text("Supprimer le produit") },
                text = { Text("Voulez-vous vraiment supprimer \"${productToDelete?.name}\" ?") },
                confirmButton = {
                    Button(
                        onClick = {
                            productToDelete?.let { viewModel.deleteProduct(it) }
                            showDeleteDialog = false
                            productToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ModernProductCard(
    product: Product,
    isAdmin: Boolean,
    onAdminClick: () -> Unit,
    onUserAddToCart: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .combinedClickable(
                onClick = { if (isAdmin) onAdminClick() },
                onLongClick = if (isAdmin) onLongClick else null
            )
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Image section with gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                ) {
                    AsyncImage(
                        model = product.imageUrl.ifBlank { "https://picsum.photos/400/400" },
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.3f)
                                    )
                                )
                            )
                    )

                    // Stock badge
                    if (product.quantity < 10) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (product.quantity == 0)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.tertiary
                        ) {
                            Text(
                                text = if (product.quantity == 0) "Épuisé" else "Stock bas",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }

                // Product info section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(8.dp))

                    // Price
                    Text(
                        text = "%.2f TND".format(product.price),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(4.dp))

                    // Stock below price
                    Text(
                        text = "En stock: ${product.quantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Add to cart icon button for normal users (bottom right)
            if (!isAdmin && product.quantity > 0) {
                IconButton(
                    onClick = onUserAddToCart,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .shadow(4.dp, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = "Ajouter au panier",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
