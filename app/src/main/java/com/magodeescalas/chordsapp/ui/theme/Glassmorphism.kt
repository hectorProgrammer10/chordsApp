package com.magodeescalas.chordsapp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun GlassBackground(content: @Composable BoxScope.() -> Unit) {
        Box(
                modifier =
                        Modifier.fillMaxSize()
                                .background(Color.White)
                                .background(
                                        brush =
                                                Brush.radialGradient(
                                                        colors =
                                                                listOf(
                                                                        GlassGradient1,
                                                                        Color.Transparent
                                                                ),
                                                        center = Offset(0f, 0f),
                                                        radius =
                                                                with(LocalDensity.current) {
                                                                        1000f
                                                                }
                                                ),
                                        alpha = 1f
                                )
                                .background(
                                        brush =
                                                Brush.radialGradient(
                                                        colors =
                                                                listOf(
                                                                        GlassGradient2,
                                                                        Color.Transparent
                                                                ),
                                                        center = Offset.Unspecified, // Needs size,

                                                        )
                                )
        ) {

                Box(
                        modifier =
                                Modifier.fillMaxSize()
                                        .background(Color.White)
                                        .background(
                                                brush =
                                                        Brush.radialGradient(
                                                                colors =
                                                                        listOf(
                                                                                GlassGradient1,
                                                                                Color.Transparent
                                                                        ),
                                                                center = Offset(0f, 0f),
                                                                radius = 1200f
                                                        )
                                        )
                )


                CanvasWebGradients()

                content()
        }
}

@Composable
fun CanvasWebGradients() {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width

                drawCircle(
                        brush =
                                Brush.radialGradient(
                                        colors = listOf(GlassGradient1, Color.Transparent),
                                        center = Offset(0f, 0f),
                                        radius =
                                                width * 1.2f

                                        ),
                        center = Offset(0f, 0f),
                        radius = width * 1.2f
                )


                drawCircle(
                        brush =
                                Brush.radialGradient(
                                        colors = listOf(GlassGradient2, Color.Transparent),
                                        center = Offset(width / 2, 0f),
                                        radius = width * 0.8f
                                ),
                        center = Offset(width / 2, 0f),
                        radius = width * 0.8f
                )


                drawCircle(
                        brush =
                                Brush.radialGradient(
                                        colors = listOf(GlassGradient3, Color.Transparent),
                                        center = Offset(width, 0f),
                                        radius = width * 1.0f
                                ),
                        center = Offset(width, 0f),
                        radius = width * 1.0f
                )
        }
}


fun Modifier.glass(): Modifier =
        this.background(
                        color = Color(0x59FFFFFF),
                        shape =
                                androidx.compose.foundation.shape.RoundedCornerShape(
                                        12.dp
                                )
                )
                .border(
                        width = 1.dp,
                        color = Color(0x4DFFFFFF),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                )

