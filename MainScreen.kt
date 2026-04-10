package com.example.stepbooster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stepbooster.AppState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    state: AppState,
    onToggleService: () -> Unit,
    onMultiplierChange: (Float) -> Unit,
    onRequestPermissions: () -> Unit,
    onBatteryOptimization: () -> Unit,
    onOpenHealthConnectStore: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("StepBooster", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Card Status ──────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Indicator ON/OFF
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(
                                    if (state.isServiceRunning) Color(0xFF4CAF50)
                                    else Color(0xFFF44336)
                                )
                        )
                        Text(
                            text = if (state.isServiceRunning) "ACTIV" else "INACTIV",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (state.isServiceRunning) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }

                    // Buton toggle mare
                    Button(
                        onClick = onToggleService,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = state.healthConnectAvailable && state.hasPermissions,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isServiceRunning)
                                Color(0xFFC62828) else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (state.isServiceRunning)
                                Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = if (state.isServiceRunning) "OPREȘTE StepBooster"
                            else "PORNEȘTE StepBooster",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    // Health Connect status badge
                    Surface(
                        color = if (state.healthConnectAvailable)
                            Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (state.healthConnectAvailable)
                                    Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (state.healthConnectAvailable)
                                    Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (state.healthConnectAvailable)
                                    "Health Connect: Disponibil" else "Health Connect: Lipsă",
                                fontSize = 12.sp,
                                color = if (state.healthConnectAvailable)
                                    Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }
                }
            }

            // ── Card Statistici ──────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Statistici Azi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatBox(
                            label = "Pași Reali",
                            value = state.realStepsToday.toString(),
                            icon = Icons.Default.DirectionsWalk,
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatBox(
                            label = "Pași HC",
                            value = state.boostedStepsToday.toString(),
                            icon = Icons.Default.Favorite,
                            color = Color(0xFFE91E63)
                        )
                        StatBox(
                            label = "Multiplicator",
                            value = "${state.multiplier}x",
                            icon = Icons.Default.TrendingUp,
                            color = Color(0xFFFF9800)
                        )
                    }
                }
            }

            // ── Card Multiplicator ───────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Multiplicator Pași",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    // Valoarea mare centrată
                    Text(
                        text = "${state.multiplier}×",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    // Slider
                    Slider(
                        value = state.multiplier,
                        onValueChange = onMultiplierChange,
                        valueRange = 1.5f..5.0f,
                        steps = 6, // 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1.5×", fontSize = 11.sp, color = Color.Gray)
                        Text("5.0×", fontSize = 11.sp, color = Color.Gray)
                    }

                    // Chips rapide
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1.5f, 2f, 3f, 4f, 5f).forEach { v ->
                            FilterChip(
                                selected = state.multiplier == v,
                                onClick = { onMultiplierChange(v) },
                                label = { Text("${v}×", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Explicație
                    Text(
                        text = "Pentru fiecare pas real, se scriu ${state.multiplier} pași în Health Connect",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ── Card Avertizări & Acțiuni ────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Configurare",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    if (!state.hasPermissions) {
                        OutlinedButton(
                            onClick = onRequestPermissions,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Key, null, modifier = Modifier.padding(end = 8.dp))
                            Text("Solicită Permisiuni Health Connect")
                        }
                    }

                    OutlinedButton(
                        onClick = onBatteryOptimization,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.BatteryFull, null, modifier = Modifier.padding(end = 8.dp))
                        Text("Dezactivează Optimizare Baterie")
                    }

                    if (!state.healthConnectAvailable) {
                        Button(
                            onClick = onOpenHealthConnectStore,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                        ) {
                            Icon(Icons.Default.Download, null, modifier = Modifier.padding(end = 8.dp))
                            Text("Instalează Health Connect")
                        }
                    }

                    // Ghid brand-specific
                    Surface(
                        color = Color(0xFFFFF9C4),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "⚠️ Xiaomi/MIUI: Settings → Manage apps → StepBooster → Autostart → ON",
                                fontSize = 11.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "⚠️ Samsung: Battery → Background usage limits → Never sleeping apps → Adaugă StepBooster",
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun StatBox(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
        Text(label, fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
    }
}
