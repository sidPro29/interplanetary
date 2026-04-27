package com.notifiy.interplanetary.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.material3.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.notifiy.interplanetary.data.model.ItvPlan
import com.notifiy.interplanetary.ui.theme.Blue
import com.notifiy.interplanetary.ui.viewmodel.PlansViewModel

@Composable
fun PlansScreen(
    isLoggedIn: Boolean,
    onLoginRequired: () -> Unit,
    onPaymentSuccess: () -> Unit,
    onPaymentError: (String) -> Unit,
    viewModel: PlansViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val paymentSheet = com.stripe.android.paymentsheet.rememberPaymentSheet { paymentResult ->
        when (paymentResult) {
            is com.stripe.android.paymentsheet.PaymentSheetResult.Completed -> {
                viewModel.finalizePurchase("pi_real_success") 
            }
            is com.stripe.android.paymentsheet.PaymentSheetResult.Failed -> {
                onPaymentError(paymentResult.error.message ?: "Payment Failed")
                viewModel.onPaymentSheetCancelled()
            }
            is com.stripe.android.paymentsheet.PaymentSheetResult.Canceled -> {
                viewModel.onPaymentSheetCancelled()
            }
        }
    }

    LaunchedEffect(uiState.clientSecret) {
        uiState.clientSecret?.let { secret ->
            paymentSheet.presentWithPaymentIntent(
                secret,
                com.stripe.android.paymentsheet.PaymentSheet.Configuration(
                    merchantDisplayName = "itv-sandbox",
                    allowsDelayedPaymentMethods = true
                )
            )
        }
    }
    
    LaunchedEffect(uiState.paymentSuccess) {
        if (uiState.paymentSuccess) {
            onPaymentSuccess()
            viewModel.resetPaymentStatus()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            onPaymentError(it)
            viewModel.resetPaymentStatus()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF040404))) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).zIndex(90f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Blue)
            }
        }

        if (uiState.isPaymentProcessing) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)).zIndex(100f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Blue)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Activating Membership...", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                    Text("Securing your access to interplanetary.tv", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 30.dp)
        ) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
            ) {
                Text(
                    text = "Membership Levels",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.8).sp
                )
                Text(
                    text = "Unlock exclusive content and premium features",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Premium Billing Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color(0xFF151515), RoundedCornerShape(35.dp))
                        .padding(6.dp)
                        .border(1.2.dp, Color(0xFF252525), RoundedCornerShape(35.dp))
                ) {
                    BillingOption(
                        text = "Monthly",
                        isSelected = uiState.selectedBillingCycle == "Monthly",
                        onClick = { viewModel.toggleBillingCycle() }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    BillingOption(
                        text = "Yearly",
                        isSelected = uiState.selectedBillingCycle == "Yearly",
                        onClick = { viewModel.toggleBillingCycle() }
                    )
                }
            }

            // Scrollable Content
            val filteredPlans = uiState.availablePlans.filter { 
                it.billingCycle == uiState.selectedBillingCycle || it.category.contains("Ads")
            }
            val categories = filteredPlans.map { it.category }.distinct()

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                categories.forEach { category ->
                    item {
                        Column(modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Box(modifier = Modifier.height(2.dp).width(40.dp).background(Blue).padding(top = 4.dp))
                        }
                    }

                    val categoryPlans = filteredPlans.filter { it.category == category }
                    
                    items(categoryPlans) { plan ->
                        PlanRow(plan = plan, isProcessing = uiState.isPaymentProcessing) {
                            if (!isLoggedIn) {
                                onLoginRequired()
                            } else {
                                viewModel.purchasePlan(plan)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BillingOption(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) Blue else Color.Transparent,
        shape = RoundedCornerShape(25.dp),
        modifier = Modifier.width(130.dp).height(45.dp).clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isSelected) Color.White else Color.Gray,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun PlanRow(plan: ItvPlan, isProcessing: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = !isProcessing) { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
        border = BorderStroke(1.dp, Color(0xFF1A1A1A))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Level Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.4.sp
                )
                Text(
                    text = "€${plan.price} per ${if (plan.billingCycle == "Monthly") "Month" else "Year"}.",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Customers in IT will be charged 20% tax.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            // Select Button
            Button(
                onClick = onClick,
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White)
            ) {
                Text(
                    text = "Select",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
