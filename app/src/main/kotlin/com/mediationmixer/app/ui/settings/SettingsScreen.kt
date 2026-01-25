package com.mediationmixer.app.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.meditationmixer.core.ui.components.NeumorphicButton
import com.meditationmixer.core.ui.components.NeumorphicCard
import com.meditationmixer.core.ui.theme.MeditationColors

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MeditationColors.backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Text(
                text = "SETTINGS",
                color = MeditationColors.textMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Reminder settings
            SettingsSection(title = "Reminders") {
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Daily Reminder",
                    subtitle = "Get reminded to meditate",
                    isEnabled = uiState.reminderEnabled,
                    onToggle = viewModel::toggleReminder
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timer settings
            SettingsSection(title = "Timer") {
                SettingsSliderItem(
                    icon = Icons.Default.Timer,
                    title = "Default Fade Duration",
                    subtitle = "${uiState.fadeDuration} seconds",
                    value = uiState.fadeDuration.toFloat(),
                    range = 10f..60f,
                    onValueChange = { viewModel.setFadeDuration(it.toInt()) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Support
            SettingsSection(title = "Support") {
                SettingsActionItem(
                    icon = Icons.Default.Favorite,
                    title = "Support Development",
                    subtitle = "Help keep the app free and ad-free",
                    onClick = viewModel::onSupportClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Privacy & Legal
            SettingsSection(title = "Privacy & Legal") {
                SettingsInfoItem(
                    icon = Icons.Default.Security,
                    title = "Privacy",
                    content = "Meditation Mixer respects your privacy. No data leaves your device. No analytics, no tracking, no accounts required."
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingsInfoItem(
                    icon = Icons.Default.Info,
                    title = "Disclaimer",
                    content = "This app is for relaxation purposes only and is not intended to diagnose, treat, cure, or prevent any medical condition. Please listen at a comfortable volume."
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Version
            Text(
                text = "Version ${uiState.version}",
                color = MeditationColors.textMuted,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (uiState.showDonationDialog) {
            SupportDevelopmentDialog(
                context = context,
                onDismiss = viewModel::dismissDonationDialog
            )
        }
    }
}

@Composable
private fun SupportDevelopmentDialog(
    context: Context,
    onDismiss: () -> Unit
) {
    val paypalUrl = "https://paypal.me/GigaSneed"
    val btc = "bc1qm3q4hd73yp9hvadd8enpp5eh74va95szalcvvv"
    val eth = "0x2796E06947339E9d802Ed925D2aB57C1BDD7e0E0"
    val sol = "HhoycRkJhTnvPMWXbzW2TMeFmqNcwsRTbvi88GmzB8Mm"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Support Development", color = MeditationColors.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Thanks for supporting Meditation Mixer.",
                    color = MeditationColors.textSecondary,
                    fontSize = 14.sp
                )

                NeumorphicButton(
                    onClick = { openUrl(context, paypalUrl) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MeditationColors.accentGradient)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Open PayPal",
                            color = MeditationColors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                CryptoCopyRow(label = "BTC", value = btc, context = context)
                CryptoCopyRow(label = "ETH", value = eth, context = context)
                CryptoCopyRow(label = "SOL", value = sol, context = context)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close", color = MeditationColors.accentPrimary)
            }
        }
    )
}

@Composable
private fun CryptoCopyRow(
    label: String,
    value: String,
    context: Context,
    modifier: Modifier = Modifier
) {
    NeumorphicCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = MeditationColors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    color = MeditationColors.textMuted,
                    fontSize = 11.sp
                )
            }

            NeumorphicButton(
                onClick = { copyToClipboard(context, label, value) },
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy $label",
                        tint = MeditationColors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

private fun copyToClipboard(context: Context, label: String, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title.uppercase(),
            color = MeditationColors.textMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MeditationColors.accentPrimary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MeditationColors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = MeditationColors.textMuted,
                fontSize = 12.sp
            )
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MeditationColors.accentPrimary,
                checkedTrackColor = MeditationColors.accentDark,
                uncheckedThumbColor = MeditationColors.textMuted,
                uncheckedTrackColor = MeditationColors.surfaceDark
            )
        )
    }
}

@Composable
private fun SettingsSliderItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MeditationColors.accentPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = MeditationColors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = MeditationColors.textMuted,
                    fontSize = 12.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = MeditationColors.accentPrimary,
                activeTrackColor = MeditationColors.accentPrimary,
                inactiveTrackColor = MeditationColors.surfaceDark
            )
        )
    }
}

@Composable
private fun SettingsActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NeumorphicButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MeditationColors.accentGradient)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MeditationColors.textPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    color = MeditationColors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = MeditationColors.textPrimary.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SettingsInfoItem(
    icon: ImageVector,
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MeditationColors.textMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = MeditationColors.textSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            color = MeditationColors.textMuted,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(start = 32.dp)
        )
    }
}
