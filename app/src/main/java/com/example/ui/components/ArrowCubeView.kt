package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.config.GameConfig
import com.example.model.ArrowDirection
import com.example.model.CubeCell
import kotlin.math.roundToInt

@Composable
fun ArrowCubeView(
    cube: CubeCell,
    sizeDp: Dp,
    themeColors: GameConfig.ThemeColors,
    isHinted: Boolean,
    isShaking: Boolean,
    isFlying: Boolean,
    onAnimationDone: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shakeOffset = remember { Animatable(0f) }
    val flyProgress = remember { Animatable(0f) }
    val alphaAnim = remember { Animatable(1f) }

    // Physical tap press interaction
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile 3D depression depth: block presses downward into the board when touched
    val pressDepression by animateDpAsState(
        targetValue = if (isPressed && !isFlying) 3.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "press_depression"
    )

    // Smooth physics-based damped harmonic shake when blocked
    LaunchedEffect(isShaking) {
        if (isShaking) {
            shakeOffset.animateTo(12f, tween(35, easing = LinearEasing))
            shakeOffset.animateTo(-9f, tween(35, easing = LinearEasing))
            shakeOffset.animateTo(6f, tween(30, easing = LinearEasing))
            shakeOffset.animateTo(-3f, tween(30, easing = LinearEasing))
            shakeOffset.animateTo(0f, tween(25, easing = LinearEasing))
            onAnimationDone()
        }
    }

    // High-speed leap & fly-off physics animation with aerodynamic stretch
    LaunchedEffect(isFlying) {
        if (isFlying) {
            flyProgress.animateTo(1f, tween(210, easing = FastOutLinearInEasing))
            alphaAnim.animateTo(0f, tween(40))
            onAnimationDone()
        }
    }

    // Hint pulse radiant breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "hint_pulse")
    val hintPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isHinted) 1.10f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hint_scale"
    )

    // Vibrant directional arrow color
    val arrowColor = when (cube.direction) {
        ArrowDirection.UP -> themeColors.arrowUpColor
        ArrowDirection.RIGHT -> themeColors.arrowRightColor
        ArrowDirection.DOWN -> themeColors.arrowDownColor
        ArrowDirection.LEFT -> themeColors.arrowLeftColor
    }

    // Fly translation distance
    val flyDistancePx = 950f
    val flyOffsetX = (cube.direction.deltaCol * flyDistancePx * flyProgress.value).roundToInt()
    val flyOffsetY = (cube.direction.deltaRow * flyDistancePx * flyProgress.value).roundToInt()

    // Aerodynamic squash & stretch in flight direction
    val scaleX = when {
        isHinted -> hintPulseScale
        isFlying && (cube.direction == ArrowDirection.LEFT || cube.direction == ArrowDirection.RIGHT) -> 1.16f
        isFlying -> 0.92f
        else -> 1f
    }
    val scaleY = when {
        isHinted -> hintPulseScale
        isFlying && (cube.direction == ArrowDirection.UP || cube.direction == ArrowDirection.DOWN) -> 1.16f
        isFlying -> 0.92f
        else -> 1f
    }

    // Shake translation along collision normal
    val shakeX = if (cube.direction == ArrowDirection.UP || cube.direction == ArrowDirection.DOWN) {
        shakeOffset.value.roundToInt()
    } else 0
    val shakeY = if (cube.direction == ArrowDirection.LEFT || cube.direction == ArrowDirection.RIGHT) {
        shakeOffset.value.roundToInt()
    } else 0

    val baseCornerRadius = 12.dp
    val totalExtrusionDepth = 5.dp

    Box(
        modifier = modifier
            .size(sizeDp, sizeDp + totalExtrusionDepth)
            .offset { IntOffset(flyOffsetX + shakeX, flyOffsetY + shakeY) }
            .alpha(alphaAnim.value)
            .scale(scaleX, scaleY)
            .testTag("cube_${cube.row}_${cube.col}")
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isFlying,
                onClick = onClick
            ),
        contentAlignment = Alignment.TopStart
    ) {
        // 1. Solid flat 3D base extrusion (Physical bottom shadow)
        Box(
            modifier = Modifier
                .size(sizeDp)
                .offset(y = totalExtrusionDepth)
                .clip(RoundedCornerShape(baseCornerRadius))
                .background(themeColors.cubeShadow)
                .border(2.dp, Color(0xFF0F172A), RoundedCornerShape(baseCornerRadius))
        )

        // Cube Face Border: Highlighted gold if hinted, vivid red if shaking
        val faceBorder = when {
            isHinted -> themeColors.accentGold
            isShaking -> themeColors.accentRed
            else -> Color(0xFF0F172A)
        }
        val borderWidth = if (isHinted || isShaking) 3.dp else 2.dp

        // 2. Main Cube Face (Physical tile resting above the base, depressing when pressed)
        Box(
            modifier = Modifier
                .size(sizeDp)
                .offset { IntOffset(0, pressDepression.roundToPx()) }
                .clip(RoundedCornerShape(baseCornerRadius))
                .background(Color.White)
                .border(borderWidth, faceBorder, RoundedCornerShape(baseCornerRadius))
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            // Draw real physical embossed arrow and 3D chamfer specular highlights
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Top chamfer specular reflection (pure white glossy rim)
                drawLine(
                    color = Color.White,
                    start = Offset(8f, 2.5f),
                    end = Offset(w - 8f, 2.5f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )

                // Right edge subtle specular chamfer
                drawLine(
                    color = Color(0xFFF1F5F9),
                    start = Offset(w - 3f, 8f),
                    end = Offset(w - 3f, h - 8f),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )

                // Embossed arrow drop shadow (gives tactile 3D indented realism)
                drawRealDirectionalArrow(
                    direction = cube.direction,
                    color = Color(0xFFCBD5E1),
                    strokeWidth = w * 0.17f,
                    offset = Offset(2f, 2.5f)
                )

                // Main vibrant directional arrow
                drawRealDirectionalArrow(
                    direction = cube.direction,
                    color = arrowColor,
                    strokeWidth = w * 0.17f,
                    offset = Offset.Zero
                )
            }
        }
    }
}

/**
 * Draws a crisp, geometric tactile arrow with smooth rounded tip and thick stem.
 */
private fun DrawScope.drawRealDirectionalArrow(
    direction: ArrowDirection,
    color: Color,
    strokeWidth: Float,
    offset: Offset
) {
    val w = size.width
    val h = size.height
    val cx = (w / 2f) + offset.x
    val cy = (h / 2f) + offset.y

    val arrowHeadPath = Path()

    when (direction) {
        ArrowDirection.UP -> {
            // Stem
            drawLine(
                color = color,
                start = Offset(cx, cy + h * 0.30f),
                end = Offset(cx, cy - h * 0.10f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            // Arrow Head
            arrowHeadPath.moveTo(cx - w * 0.30f, cy - h * 0.03f)
            arrowHeadPath.lineTo(cx, cy - h * 0.35f)
            arrowHeadPath.lineTo(cx + w * 0.30f, cy - h * 0.03f)
            arrowHeadPath.close()
        }
        ArrowDirection.DOWN -> {
            // Stem
            drawLine(
                color = color,
                start = Offset(cx, cy - h * 0.30f),
                end = Offset(cx, cy + h * 0.10f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            // Arrow Head
            arrowHeadPath.moveTo(cx - w * 0.30f, cy + h * 0.03f)
            arrowHeadPath.lineTo(cx, cy + h * 0.35f)
            arrowHeadPath.lineTo(cx + w * 0.30f, cy + h * 0.03f)
            arrowHeadPath.close()
        }
        ArrowDirection.LEFT -> {
            // Stem
            drawLine(
                color = color,
                start = Offset(cx + w * 0.30f, cy),
                end = Offset(cx - w * 0.10f, cy),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            // Arrow Head
            arrowHeadPath.moveTo(cx - w * 0.03f, cy - h * 0.30f)
            arrowHeadPath.lineTo(cx - w * 0.35f, cy)
            arrowHeadPath.lineTo(cx - w * 0.03f, cy + h * 0.30f)
            arrowHeadPath.close()
        }
        ArrowDirection.RIGHT -> {
            // Stem
            drawLine(
                color = color,
                start = Offset(cx - w * 0.30f, cy),
                end = Offset(cx + w * 0.10f, cy),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            // Arrow Head
            arrowHeadPath.moveTo(cx + w * 0.03f, cy - h * 0.30f)
            arrowHeadPath.lineTo(cx + w * 0.35f, cy)
            arrowHeadPath.lineTo(cx + w * 0.03f, cy + h * 0.30f)
            arrowHeadPath.close()
        }
    }

    drawPath(
        path = arrowHeadPath,
        color = color,
        style = Fill
    )

    // Outline for crisp edge definition
    drawPath(
        path = arrowHeadPath,
        color = color,
        style = Stroke(
            width = strokeWidth * 0.35f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

