package com.thanesgroup.lgs.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.thanesgroup.lgs.R

val ibmpiexsansthailooped = FontFamily(
  Font(R.font.ibmpiexsansthailooped_light, FontWeight.Light),
  Font(R.font.ibmpiexsansthailooped_regular, FontWeight.Normal),
  Font(R.font.ibmpiexsansthailooped_medium, FontWeight.Medium),
  Font(R.font.ibmpiexsansthailooped_bold, FontWeight.Bold)
)

val anuphanFamily = FontFamily(
  Font(R.font.anuphan_light, FontWeight.Light),
  Font(R.font.anuphan_regular, FontWeight.Normal),
  Font(R.font.anuphan_medium, FontWeight.Medium),
  Font(R.font.anuphan_bold, FontWeight.Bold)
)

val Typography = Typography(
  bodyLarge = TextStyle(
    fontFamily = ibmpiexsansthailooped,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp
  ),
  titleLarge = TextStyle(
    fontFamily = ibmpiexsansthailooped,
    fontWeight = FontWeight.Normal,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp
  ),
  labelSmall = TextStyle(
    fontFamily = ibmpiexsansthailooped,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp
  )
)