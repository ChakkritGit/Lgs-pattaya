package com.thanesgroup.lgs.screen.splashScreen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thanesgroup.lgs.ui.theme.LgsBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.thanesgroup.lgs.R

private enum class AnimationState {
  Showing, FadingOut, Typing, Done
}

@Composable
fun SplashScreen(
  innerPadding: PaddingValues
) {
  val fullText = "LGS"
  var displayedTextForTyping by remember { mutableStateOf("") }
  var animationState by remember { mutableStateOf(AnimationState.Showing) }

  val typingColors = remember {
    listOf(
      Color(0xCCFF1000),
      Color(0xCC00FF0C),
      Color(0xCC0028FF)
    )
  }

  val alphaValues = remember {
    listOf(Animatable(1f), Animatable(1f), Animatable(1f))
  }

  val infiniteTransition = rememberInfiniteTransition(label = "cursorTransition")
  val cursorAlpha by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 350, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ), label = "cursorAlpha"
  )

  LaunchedEffect(Unit) {
    delay(700L)
    animationState = AnimationState.FadingOut

    alphaValues.forEachIndexed { _, alpha ->
      launch {
        alpha.animateTo(0f, animationSpec = tween(durationMillis = 250))
      }
      delay(120L)
    }
    delay(350L)
    animationState = AnimationState.Typing

    fullText.forEachIndexed { index, _ ->
      displayedTextForTyping = fullText.take(index + 1)
      delay(250L)
    }
    animationState = AnimationState.Done
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(innerPadding),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      val textToShow = when (animationState) {
        AnimationState.Showing, AnimationState.FadingOut -> {
          buildAnnotatedString {
            fullText.forEachIndexed { index, char ->
              val animatedColor = LgsBlue.copy(alpha = alphaValues[index].value)
              withStyle(
                style = SpanStyle(
                  color = animatedColor,
                  fontSize = 52.sp,
                  fontWeight = FontWeight.Bold
                )
              ) {
                append(char)
              }
            }
          }
        }

        AnimationState.Typing, AnimationState.Done -> {
          buildAnnotatedString {
            displayedTextForTyping.forEachIndexed { index, char ->
              val color = typingColors[index]
              withStyle(
                style = SpanStyle(
                  color = color,
                  fontSize = 52.sp,
                  fontWeight = FontWeight.Bold
                )
              ) {
                append(char)
              }
            }
            if (animationState == AnimationState.Typing) {
              val cursorColor = LgsBlue.copy(alpha = cursorAlpha)
              withStyle(
                style = SpanStyle(
                  color = cursorColor,
                  fontWeight = FontWeight.Normal,
                  fontSize = 28.sp
                )
              ) {
                append("▂")
              }
            }
          }
        }
      }

      Text(
        text = textToShow,
        fontSize = 52.sp,
        fontWeight = FontWeight.Bold
      )

      if (animationState == AnimationState.Done) {
//        Text(text = "Light Guiding Station System", color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        CircularProgressIndicator(
          modifier = Modifier.size(24.dp),
          color = LgsBlue,
          strokeWidth = 2.dp
        )
      }
    }

    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 48.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Image(
        painter = painterResource(id = R.drawable.from_thanes_development_co__ltd),
        contentDescription = "from_thanes_development_co__ltd",
        modifier = Modifier.height(72.dp),
        contentScale = ContentScale.Fit
      )
//      Spacer(modifier = Modifier.height(8.dp))
//      Text(
//        text = "Powered by Your Company",
//        fontSize = 12.sp,
//        color = Color.Gray
//      )
    }
  }
}