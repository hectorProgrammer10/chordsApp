package com.magodeescalas.chordsapp.presentation.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.magodeescalas.chordsapp.domain.model.GuitarConstants
import com.magodeescalas.chordsapp.domain.model.GuitarString
import com.magodeescalas.chordsapp.domain.model.TuningStatus
import com.magodeescalas.chordsapp.presentation.util.AppStrings
import com.magodeescalas.chordsapp.presentation.viewmodel.MainUiState
import com.magodeescalas.chordsapp.ui.theme.WebPrimary
import com.magodeescalas.chordsapp.ui.theme.WebSecondary
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuitarTunerSheet(
    uiState: MainUiState,
    strings: AppStrings,
    onClose: () -> Unit,
    onReset: () -> Unit,
    onPermissionResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        val currentPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        onPermissionResult(currentPermission)
        if (!currentPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A).copy(alpha = 0.96f), // Glass dark slate
        scrimColor = Color.Black.copy(alpha = 0.55f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.35f), RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Title & Close "X" Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = strings.tunerTitle,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = strings.tunerSubtitle,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }

                // "X" exit button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.12f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar afinador",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!uiState.hasAudioPermission) {
                // Permission Request Card
                PermissionRationaleCard(
                    strings = strings,
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )
            } else {
                // Celebration alert when all strings are tuned
                AnimatedVisibility(
                    visible = uiState.allStringsTuned,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    AllStringsTunedBanner(
                        strings = strings,
                        onReset = onReset
                    )
                }

                if (uiState.allStringsTuned) {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Tension Action Badge: Tensar (↑) / Aflojar (↓) / Afinada (✓)
                TensionActionIndicator(
                    tuningStatus = uiState.tuningStatus,
                    strings = strings,
                    targetString = uiState.targetString,
                    cents = uiState.detectedCents
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Tuning Gauge & Frequency Meter
                TuningGauge(
                    cents = uiState.detectedCents,
                    frequency = uiState.detectedPitch,
                    targetString = uiState.targetString,
                    tuningStatus = uiState.tuningStatus
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 6 Guitar Strings Status Grid (with green circles when in-tune)
                StringsStatusRow(
                    guitarStrings = GuitarConstants.STANDARD_GUITAR_STRINGS,
                    tunedIndices = uiState.tunedStrings,
                    activeStringIndex = uiState.detectedStringIndex
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tuned Strings Counter & Reset Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Afinadas: ${uiState.tunedStrings.size} / 6",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (uiState.allStringsTuned) WebPrimary else Color.White.copy(alpha = 0.7f)
                    )

                    TextButton(
                        onClick = onReset,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White.copy(alpha = 0.75f)
                        )
                    ) {
                        Text(
                            text = strings.tunerResetButton,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Visual badge indicating whether to Tighten (Tensar ↑), Loosen (Aflojar ↓), or In-Tune (✓).
 */
@Composable
fun TensionActionIndicator(
    tuningStatus: TuningStatus,
    strings: AppStrings,
    targetString: GuitarString?,
    cents: Float
) {
    val bgColor by animateColorAsState(
        targetValue = when (tuningStatus) {
            TuningStatus.IN_TUNE -> WebPrimary.copy(alpha = 0.25f)
            TuningStatus.TIGHTEN -> Color(0xFFF59E0B).copy(alpha = 0.22f) // Amber
            TuningStatus.LOOSEN -> Color(0xFFEF4444).copy(alpha = 0.22f)  // Coral Red
            TuningStatus.IDLE -> Color.White.copy(alpha = 0.08f)
        },
        label = "bgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = when (tuningStatus) {
            TuningStatus.IN_TUNE -> WebPrimary
            TuningStatus.TIGHTEN -> Color(0xFFF59E0B)
            TuningStatus.LOOSEN -> Color(0xFFEF4444)
            TuningStatus.IDLE -> Color.White.copy(alpha = 0.15f)
        },
        label = "borderColor"
    )

    val textColor by animateColorAsState(
        targetValue = when (tuningStatus) {
            TuningStatus.IN_TUNE -> WebPrimary
            TuningStatus.TIGHTEN -> Color(0xFFFBBF24)
            TuningStatus.LOOSEN -> Color(0xFFF87171)
            TuningStatus.IDLE -> Color.White.copy(alpha = 0.6f)
        },
        label = "textColor"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            when (tuningStatus) {
                TuningStatus.TIGHTEN -> {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = strings.tunerTighten,
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = strings.tunerTighten,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        targetString?.let {
                            Text(
                                text = "Gira la clavija para tensar (${cents.toInt()} cents)",
                                fontSize = 12.sp,
                                color = textColor.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                TuningStatus.LOOSEN -> {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = strings.tunerLoosen,
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = strings.tunerLoosen,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        targetString?.let {
                            Text(
                                text = "Gira la clavija para aflojar (+${cents.toInt()} cents)",
                                fontSize = 12.sp,
                                color = textColor.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                TuningStatus.IN_TUNE -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Afinada",
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = strings.tunerInTune,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )
                        targetString?.let {
                            Text(
                                text = "Cuerda ${it.fullLabel} en el tono correcto",
                                fontSize = 12.sp,
                                color = textColor.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                TuningStatus.IDLE -> {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.White.copy(alpha = 0.4f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.tunerListening,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}

/**
 * Visual arc and needle gauge showing cents deviation (-50 to +50 cents).
 */
@Composable
fun TuningGauge(
    cents: Float,
    frequency: Float,
    targetString: GuitarString?,
    tuningStatus: TuningStatus
) {
    val clampedCents = cents.coerceIn(-50f, 50f)
    val animatedCents by animateFloatAsState(
        targetValue = clampedCents,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f),
        label = "animatedCents"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val centerY = height * 0.85f
            val radius = height * 0.65f

            // Background arc (-50 to +50 cents -> 140 to 40 degrees)
            val startAngle = 180f + 25f
            val sweepAngle = 130f

            drawArc(
                color = Color.White.copy(alpha = 0.15f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )

            // In-tune center target zone (around 0 cents)
            val centerAngle = startAngle + (sweepAngle / 2f)
            val inTuneSweep = sweepAngle * (GuitarConstants.IN_TUNE_TOLERANCE_CENTS * 2f / 100f)
            drawArc(
                color = WebPrimary,
                startAngle = centerAngle - (inTuneSweep / 2f),
                sweepAngle = inTuneSweep,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            )

            // Tick marks for -40, -20, 0, +20, +40 cents
            val ticks = listOf(-40, -20, 0, 20, 40)
            ticks.forEach { tickCents ->
                val ratio = (tickCents + 50f) / 100f
                val angleRad = Math.toRadians((startAngle + sweepAngle * ratio).toDouble())
                val innerR = radius - 14.dp.toPx()
                val outerR = radius - 4.dp.toPx()

                val x1 = centerX + innerR * cos(angleRad).toFloat()
                val y1 = centerY + innerR * sin(angleRad).toFloat()
                val x2 = centerX + outerR * cos(angleRad).toFloat()
                val y2 = centerY + outerR * sin(angleRad).toFloat()

                val tickColor = if (tickCents == 0) WebPrimary else Color.White.copy(alpha = 0.4f)
                val strokeWidth = if (tickCents == 0) 3.dp.toPx() else 1.5.dp.toPx()

                drawLine(
                    color = tickColor,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }

            // Needle indicating cents
            val needleRatio = (animatedCents + 50f) / 100f
            val needleAngleRad = Math.toRadians((startAngle + sweepAngle * needleRatio).toDouble())
            val needleLength = radius - 6.dp.toPx()

            val needleColor = when (tuningStatus) {
                TuningStatus.IN_TUNE -> WebPrimary
                TuningStatus.TIGHTEN -> Color(0xFFF59E0B)
                TuningStatus.LOOSEN -> Color(0xFFEF4444)
                TuningStatus.IDLE -> Color.White.copy(alpha = 0.5f)
            }

            val needleEnd = Offset(
                centerX + needleLength * cos(needleAngleRad).toFloat(),
                centerY + needleLength * sin(needleAngleRad).toFloat()
            )

            drawLine(
                color = needleColor,
                start = Offset(centerX, centerY),
                end = needleEnd,
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Needle pivot circle
            drawCircle(
                color = needleColor,
                radius = 7.dp.toPx(),
                center = Offset(centerX, centerY)
            )
            drawCircle(
                color = Color(0xFF0F172A),
                radius = 3.dp.toPx(),
                center = Offset(centerX, centerY)
            )
        }

        // Center Note & Frequency Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = (-16).dp)
        ) {
            val noteText = targetString?.displayName ?: "--"
            Text(
                text = noteText,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = when (tuningStatus) {
                    TuningStatus.IN_TUNE -> WebPrimary
                    TuningStatus.TIGHTEN -> Color(0xFFFBBF24)
                    TuningStatus.LOOSEN -> Color(0xFFF87171)
                    TuningStatus.IDLE -> Color.White
                }
            )

            val freqText = if (frequency > 0f) {
                "%.1f Hz".format(frequency)
            } else {
                targetString?.let { "Obj: %.1f Hz".format(it.targetFrequency) } ?: "-- Hz"
            }

            Text(
                text = freqText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * 6 Guitar Strings Indicators with green circle when tuned.
 */
@Composable
fun StringsStatusRow(
    guitarStrings: List<GuitarString>,
    tunedIndices: Set<Int>,
    activeStringIndex: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        guitarStrings.forEachIndexed { index, guitarString ->
            val isTuned = index in tunedIndices
            val isActive = index == activeStringIndex

            StringItemCard(
                guitarString = guitarString,
                isTuned = isTuned,
                isActive = isActive,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StringItemCard(
    guitarString: GuitarString,
    isTuned: Boolean,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isTuned -> WebPrimary
        isActive -> WebSecondary
        else -> Color.White.copy(alpha = 0.15f)
    }

    val cardBg = when {
        isTuned -> WebPrimary.copy(alpha = 0.15f)
        isActive -> WebSecondary.copy(alpha = 0.15f)
        else -> Color.White.copy(alpha = 0.05f)
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // String Number
        Text(
            text = "${guitarString.number}ª",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.6f)
        )

        // Note Name
        Text(
            text = guitarString.noteName,
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            color = if (isTuned) WebPrimary else Color.White
        )

        // Target Frequency
        Text(
            text = "${guitarString.targetFrequency.toInt()}Hz",
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Green Circle Indicator when tuned (Requirement: "cuando una cuerda este afinada se marcara con un circulo verde")
        Box(
            modifier = Modifier
                .size(20.dp)
                .then(
                    if (isTuned) {
                        Modifier
                            .background(WebPrimary, CircleShape)
                            .shadow(4.dp, CircleShape, spotColor = WebPrimary)
                    } else {
                        Modifier
                            .background(Color.Transparent, CircleShape)
                            .border(1.5.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isTuned) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Afinada",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

/**
 * Banner shown when all 6 strings have been successfully tuned.
 */
@Composable
fun AllStringsTunedBanner(
    strings: AppStrings,
    onReset: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF065F46), // Dark emerald
                        Color(0xFF047857),
                        Color(0xFF0F766E)
                    )
                )
            )
            .border(1.5.dp, WebPrimary, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Celebration,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.tunerAllTunedTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = strings.tunerAllTunedMessage,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Permission prompt card if RECORD_AUDIO is not granted.
 */
@Composable
fun PermissionRationaleCard(
    strings: AppStrings,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(WebPrimary, WebSecondary))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = WebPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.tunerPermissionTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = strings.tunerPermissionDesc,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = WebPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = strings.tunerGrantPermission,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
