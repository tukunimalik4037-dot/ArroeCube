package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var size: Float,
    var alpha: Float,
    var rotation: Float,
    val rotationSpeed: Float,
    val decay: Float
)

class ParticleEmitter {
    val particles = mutableStateListOf<Particle>()

    fun explode(startX: Float, startY: Float, cubeColor: Color = Color(0xFF38BDF8)) {
        val colors = listOf(
            cubeColor,
            Color(0xFFFBBF24), // Gold
            Color(0xFF38BDF8), // Blue
            Color(0xFFEC4899), // Pink
            Color(0xFF4ADE80), // Green
            Color(0xFFA855F7)  // Purple
        )
        for (i in 0 until 40) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
            val speed = Random.nextFloat() * 14f + 5f
            particles.add(
                Particle(
                    x = startX,
                    y = startY,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 4f, // upward lift
                    color = colors.random(),
                    size = Random.nextFloat() * 12f + 6f,
                    alpha = 1f,
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = (Random.nextFloat() - 0.5f) * 20f,
                    decay = Random.nextFloat() * 0.02f + 0.012f
                )
            )
        }
    }

    fun update() {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx
            p.y += p.vy
            p.vy += 0.45f // gravity
            p.vx *= 0.95f // drag
            p.alpha -= p.decay
            p.rotation += p.rotationSpeed
            p.size *= 0.98f
            if (p.alpha <= 0f || p.size <= 1f) {
                iterator.remove()
            }
        }
    }
}

@Composable
fun ParticleEffectCanvas(
    emitter: ParticleEmitter,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(emitter) {
        while (true) {
            delay(16L) // ~60 FPS
            emitter.update()
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        for (p in emitter.particles) {
            rotate(p.rotation, pivot = Offset(p.x, p.y)) {
                drawRect(
                    color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                    topLeft = Offset(p.x - p.size / 2, p.y - p.size / 2),
                    size = Size(p.size, p.size * 1.5f)
                )
            }
        }
    }
}
