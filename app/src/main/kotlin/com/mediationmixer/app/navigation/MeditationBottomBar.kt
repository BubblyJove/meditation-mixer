package com.mediationmixer.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.meditationmixer.core.ui.components.NeumorphicButton
import com.meditationmixer.core.ui.theme.MeditationColors

@Composable
fun MeditationBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(MeditationColors.backgroundGradient)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                isSelected = currentRoute == Screen.Home.route,
                onClick = { onNavigate(Screen.Home.route) }
            )
            BottomNavItem(
                icon = Icons.Default.Tune,
                isSelected = currentRoute == Screen.Mixer.route,
                onClick = { onNavigate(Screen.Mixer.route) }
            )
            BottomNavItem(
                icon = Icons.Default.LibraryMusic,
                isSelected = currentRoute == Screen.Library.route,
                onClick = { onNavigate(Screen.Library.route) }
            )
            BottomNavItem(
                icon = Icons.Default.LocalFireDepartment,
                isSelected = currentRoute == Screen.Streaks.route,
                onClick = { onNavigate(Screen.Streaks.route) }
            )
            BottomNavItem(
                icon = Icons.Default.Settings,
                isSelected = currentRoute == Screen.Settings.route,
                onClick = { onNavigate(Screen.Settings.route) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NeumorphicButton(
        onClick = onClick,
        isPressed = isSelected,
        modifier = modifier.size(48.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MeditationColors.accentPrimary else MeditationColors.textMuted,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
