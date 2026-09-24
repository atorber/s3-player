package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AetherSurfaceTier2
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TextHighContrast
import com.example.ui.theme.TextLowContrast

@Composable
fun AetherStatusChip(
    label: String,
    modifier: Modifier = Modifier,
    indicatorColor: Color? = null,
    borderColor: Color = ElectricCyan.copy(alpha = 0.3f),
    backgroundColor: Color = AetherSurfaceTier2,
    textColor: Color = TextHighContrast
) {
    Row(
        modifier = modifier
            .height(22.dp)
            .clip(RoundedCornerShape(9999.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(9999.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (indicatorColor != null) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
        }
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp,
            color = textColor
        )
    }
}
