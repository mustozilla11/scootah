package com.scootah.app.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EnergySavingsLeaf
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scootah.app.data.model.UserProfile
import com.scootah.app.ui.theme.SafeGreen
import com.scootah.app.ui.theme.ScootahGreen
import com.scootah.app.ui.theme.ScootahGreenDark
import com.scootah.app.ui.theme.StreakAmber
import com.scootah.app.ui.theme.StreakOrange


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onStartRide: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle(
        initialValue = com.scootah.app.data.model.UserProfile()
    )
    val greeting by viewModel.greeting.collectAsStateWithLifecycle(
        initialValue = "Merhaba! 👋"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Greeting header
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Text(
                text = "Bugün de yollara çıkmaya hazır mısın?",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                )
            )

            Spacer(Modifier.height(4.dp))

            // Streak Card
            StreakCard(streakDays = profile.streakDays)

            // Eco Impact Card
            EcoImpactCard(profile = profile)

            // Start Ride FAB
            Spacer(Modifier.height(12.dp))
            ExtendedFloatingActionButton(
                onClick = onStartRide,
                containerColor = ScootahGreen,
                contentColor = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsRun,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.padding(horizontal = 6.dp))
                Text(
                    text = "Sürüşe Başla 🛴",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun StreakCard(streakDays: Int) {
    var progressTarget by remember { mutableFloatStateOf(0f) }
    val progress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 1000),
        label = "streakProgress"
    )

    LaunchedEffect(streakDays) {
        progressTarget = (streakDays % 7) / 7f
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Whatshot,
                    contentDescription = "Streak",
                    tint = StreakOrange,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = "🔥 $streakDays Gündür Yollardasın!",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "${7 - (streakDays % 7)} gün sonra 1 haftalık rozet!",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = StreakAmber
                        )
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50.dp)),
                color = StreakOrange,
                trackColor = StreakOrange.copy(alpha = 0.15f),
                strokeCap = StrokeCap.Round
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(7) { day ->
                    val dayLabel = listOf("Pt", "Sa", "Ça", "Pe", "Cu", "Ct", "Pz")[day]
                    val isActive = day < (streakDays % 7)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = if (isActive) StreakOrange else StreakOrange.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isActive) "🔥" else dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = if (isActive) 14.sp else 10.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EcoImpactCard(profile: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = ScootahGreen.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.EnergySavingsLeaf,
                    contentDescription = null,
                    tint = SafeGreen,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = "Ekolojik Etkini",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EcoStatItem(
                    icon = Icons.Filled.EnergySavingsLeaf,
                    value = "%.1f kg".format(profile.co2SavedKg),
                    label = "CO₂ Tasarrufu",
                    color = SafeGreen,
                    modifier = Modifier.weight(1f)
                )
                EcoStatItem(
                    icon = Icons.Filled.LocalGasStation,
                    value = "%.1f L".format(profile.fuelSavedLiters),
                    label = "Yakıt Tasarrufu",
                    color = StreakAmber,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EcoStatItem(
                    icon = Icons.Filled.DirectionsRun,
                    value = "%.1f km".format(profile.totalKm),
                    label = "Toplam Mesafe",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                EcoStatItem(
                    icon = Icons.Filled.Whatshot,
                    value = "${profile.streakDays} Gün",
                    label = "Aktif Seri",
                    color = StreakOrange,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EcoStatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}
