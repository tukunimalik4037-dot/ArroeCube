package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Modern flat 3D cubical button with animated physical press, solid outlines,
 * and bottom depth. Strictly NO gradients.
 */
@Composable
fun CubicalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF0284C7),
    bottomShadowColor: Color = Color(0xFF0369A1),
    borderColor: Color = Color(0xFF0F172A),
    textColor: Color = Color.White,
    icon: (@Composable () -> Unit)? = null,
    fontSize: Int = 16,
    height: Dp = 54.dp,
    enabled: Boolean = true,
    testTag: String = "cubical_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val depth = 5.dp
    val currentOffset by animateDpAsState(
        targetValue = if (isPressed && enabled) depth else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "button_press_offset"
    )

    val actualBg = if (enabled) backgroundColor else Color(0xFFE2E8F0)
    val actualShadow = if (enabled) bottomShadowColor else Color(0xFFCBD5E1)
    val actualBorder = if (enabled) borderColor else Color(0xFF94A3B8)
    val actualTextColor = if (enabled) textColor else Color(0xFF94A3B8)

    Box(
        modifier = modifier
            .height(height + depth)
            .testTag(testTag)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.TopStart
    ) {
        // Bottom 3D solid shadow base
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = depth)
                .clip(RoundedCornerShape(14.dp))
                .background(actualShadow)
                .border(2.5.dp, actualBorder, RoundedCornerShape(14.dp))
        )

        // Top animated button face
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset { IntOffset(0, currentOffset.roundToPx()) }
                .clip(RoundedCornerShape(14.dp))
                .background(actualBg)
                .border(2.5.dp, actualBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    icon()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    color = actualTextColor,
                    fontWeight = FontWeight.Black,
                    fontSize = fontSize.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

/**
 * Flat 3D Cubical Card with flat offset shadow and solid outline.
 */
@Composable
fun CubicalCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = Color(0xFFCBD5E1),
    shadowColor: Color = Color(0xFFE2E8F0),
    shadowDepth: Dp = 4.dp,
    cornerRadius: Dp = 14.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
    ) {
        // Flat shadow layer behind
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowDepth, y = shadowDepth)
                .clip(RoundedCornerShape(cornerRadius))
                .background(shadowColor)
        )

        // Top card surface
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(cornerRadius))
                .background(backgroundColor)
                .border(2.dp, borderColor, RoundedCornerShape(cornerRadius))
        ) {
            content()
        }
    }
}

/**
 * Flat 3D Icon button (square cubical format)
 */
@Composable
fun CubicalIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    backgroundColor: Color = Color.White,
    bottomShadowColor: Color = Color(0xFFCBD5E1),
    borderColor: Color = Color(0xFF0F172A),
    enabled: Boolean = true,
    testTag: String = "cubical_icon_button",
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val depth = 4.dp
    val currentOffset by animateDpAsState(
        targetValue = if (isPressed && enabled) depth else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "icon_btn_press"
    )

    Box(
        modifier = modifier
            .size(size, size + depth)
            .testTag(testTag)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.TopStart
    ) {
        // Shadow
        Box(
            modifier = Modifier
                .size(size)
                .offset(y = depth)
                .clip(RoundedCornerShape(12.dp))
                .background(bottomShadowColor)
                .border(2.dp, borderColor, RoundedCornerShape(12.dp))
        )

        // Button Face
        Box(
            modifier = Modifier
                .size(size)
                .offset { IntOffset(0, currentOffset.roundToPx()) }
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
