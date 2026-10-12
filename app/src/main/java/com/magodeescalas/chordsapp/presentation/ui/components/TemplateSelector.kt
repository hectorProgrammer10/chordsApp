package com.magodeescalas.chordsapp.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magodeescalas.chordsapp.domain.model.TemplateData
import com.magodeescalas.chordsapp.ui.theme.WebPrimary

private val TEMPLATE_NAMES = listOf("A", "B", "C", "D")

/**
 * Segmented template selector [ A | B | C | D ]
 * - Active tab: WebPrimary green rounded background
 * - Inactive tabs: Transparent background with subtle vertical dividers
 * - Displays a small green dot when a template contains saved chords/notes
 */
@Composable
fun TemplateSelector(
    selectedIndex: Int,
    templates: Map<String, TemplateData>,
    onSelectTemplate: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A))
            .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.width(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TEMPLATE_NAMES.forEachIndexed { index, name ->
                val isSelected = (index == selectedIndex)
                val templateData = templates[name]
                val hasContent = (templateData != null && (templateData.chords.isNotEmpty() || templateData.notes.isNotEmpty()))

                TemplateTab(
                    name = name,
                    isSelected = isSelected,
                    hasContent = hasContent,
                    onClick = { onSelectTemplate(index) }
                )

                // Vertical divider between items (except after the last one)
                if (index < TEMPLATE_NAMES.size - 1) {
                    // Only show divider if neither this item nor the next item is selected
                    val nextIsSelected = (index + 1 == selectedIndex)
                    val showDivider = !isSelected && !nextIsSelected
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(22.dp)
                            .background(if (showDivider) Color.White.copy(alpha = 0.35f) else Color.Transparent)
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateTab(
    name: String,
    isSelected: Boolean,
    hasContent: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) WebPrimary else Color.Transparent,
        label = "templateTabBg"
    )

    Box(
        modifier = Modifier
            .width(56.dp)
            .height(38.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = name,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
            )

            // Small indicator dot if template has saved chords/notes
            if (hasContent) {
                Spacer(modifier = Modifier.width(3.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            color = if (isSelected) Color.White else WebPrimary,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}
