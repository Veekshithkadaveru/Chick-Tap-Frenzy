package app.krafted.chicktapfrenzy.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.chicktapfrenzy.R
import app.krafted.chicktapfrenzy.ui.theme.ChickCream
import app.krafted.chicktapfrenzy.ui.theme.ChickInk
import app.krafted.chicktapfrenzy.ui.theme.ChickInkShadow
import app.krafted.chicktapfrenzy.ui.theme.ChickRedDark
import app.krafted.chicktapfrenzy.ui.theme.ChickYellow
import app.krafted.chicktapfrenzy.ui.theme.ChickYellowBright
import app.krafted.chicktapfrenzy.ui.theme.ChickYellowSoft
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val iconScale = remember { Animatable(0f) }
    val iconAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(24f) }

    LaunchedEffect(Unit) {
        launch {
            iconAlpha.animateTo(1f, tween(400))
        }
        launch {
            iconScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        launch {
            delay(450)
            titleAlpha.animateTo(1f, tween(600))
        }
        launch {
            delay(450)
            titleOffset.animateTo(0f, tween(600))
        }
        delay(2000)
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to ChickInk,
                        0.5f to Color(0xFF2D1A08),
                        1f to ChickInk
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        0f to ChickYellow.copy(alpha = 0.15f),
                        1f to Color.Transparent
                    )
                )
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .graphicsLayer {
                        scaleX = iconScale.value
                        scaleY = iconScale.value
                        alpha = iconAlpha.value
                    }
                    .shadow(elevation = 20.dp, shape = CircleShape, spotColor = Color.Black)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            0f to ChickCream,
                            1f to Color(0xFFF3E6C4)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.chickag6_icon_512),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(156.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha.value
                    translationY = titleOffset.value
                }
            ) {
                Text(
                    text = "CHICK  TAP",
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            colors = listOf(ChickYellowSoft, ChickCream, ChickYellowSoft)
                        ),
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        letterSpacing = 8.sp,
                        shadow = Shadow(
                            color = ChickInkShadow,
                            offset = Offset(0f, 3f),
                            blurRadius = 8f
                        ),
                        textAlign = TextAlign.Center
                    )
                )
                Text(
                    text = "FRENZY",
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            colors = listOf(ChickYellow, ChickYellowBright, ChickYellow)
                        ),
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        fontSize = 58.sp,
                        letterSpacing = 4.sp,
                        shadow = Shadow(
                            color = ChickRedDark,
                            offset = Offset(0f, 6f),
                            blurRadius = 14f
                        ),
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(onFinished = {})
}

