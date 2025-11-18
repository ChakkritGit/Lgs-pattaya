package com.thanesgroup.lgs.screen.dispense

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.TabRowDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thanesgroup.lgs.R
import com.thanesgroup.lgs.data.viewModel.DispenseViewModel
import com.thanesgroup.lgs.navigation.DispenseRoutes
import com.thanesgroup.lgs.ui.component.BarcodeScanner
import com.thanesgroup.lgs.ui.theme.LgsBlue

@Composable
fun DispenseTurnOnOffLight(
  contentPadding: PaddingValues, dispenseViewModel: DispenseViewModel, context: Context
) {
  val navController = rememberNavController()
  val startDestination = DispenseRoutes.turnOn
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination

  val transitionSpec: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(
      initialOffsetX = { fullWidth -> fullWidth },
      animationSpec = tween(300, easing = FastOutSlowInEasing)
    )
  }

  val popEnterSpec: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideInHorizontally(
      initialOffsetX = { fullWidth -> -fullWidth },
      animationSpec = tween(300, easing = FastOutSlowInEasing)
    )
  }

  val exitSpec: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(
      targetOffsetX = { fullWidth -> -fullWidth },
      animationSpec = tween(300, easing = FastOutSlowInEasing)
    )
  }

  val popExitSpec: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutHorizontally(
      targetOffsetX = { fullWidth -> fullWidth },
      animationSpec = tween(300, easing = FastOutSlowInEasing)
    )
  }

  Scaffold(
    topBar = {
      PrimaryTabRow(
        selectedTabIndex = DispenseRoutes.entries.indexOfFirst { destination ->
          currentDestination?.hierarchy?.any { it.route == destination.route } == true
        }.coerceAtLeast(0), containerColor = MaterialTheme.colorScheme.background, indicator = {
          TabRowDefaults.Indicator(height = 0.dp)
        }, modifier = Modifier
          .padding(contentPadding)
          .drawBehind {
            val strokeWidth = 1.dp.toPx()
            val y = size.height - (strokeWidth / 2)

            drawLine(
              color = Color.Gray.copy(alpha = 0.13f),
              start = Offset(0f, y),
              end = Offset(size.width, y),
              strokeWidth = strokeWidth
            )
          }) {
        DispenseRoutes.entries.forEach { destination ->
          val isSelected =
            currentDestination?.hierarchy?.any { it.route == destination.route } == true
          val tabColor = if (isSelected) LgsBlue else MaterialTheme.colorScheme.onSurfaceVariant

          Tab(
            selected = isSelected,
            onClick = {
              navController.navigate(destination.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                  saveState = true
                }
                launchSingleTop = true
                restoreState = true
              }
            },
            icon = {
              Icon(
                painter = painterResource(if (isSelected) destination.selectedIcon else destination.unselectedIcon),
                contentDescription = destination.label,
                tint = tabColor
              )
            },
            selectedContentColor = LgsBlue,
            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }) {
    NavHost(
      navController,
      startDestination = startDestination.route,
      enterTransition = transitionSpec,
      exitTransition = exitSpec,
      popEnterTransition = popEnterSpec,
      popExitTransition = popExitSpec
    ) {
      DispenseRoutes.entries.forEach { destination ->
        composable(destination.route) {
          when (destination) {
            DispenseRoutes.turnOn -> TurnOnLightScreen(
              dispenseViewModel = dispenseViewModel,
              context = context
            )

            DispenseRoutes.turnOff -> TurnOffLightScreen(
              dispenseViewModel = dispenseViewModel,
              context = context
            )
          }
        }
      }
    }
  }
}

@Composable
private fun TurnOnLightScreen(dispenseViewModel: DispenseViewModel, context: Context) {
  BarcodeScanner { scannedCode ->
    dispenseViewModel.handleDispenseOnManual(scannedCode = scannedCode)
  }

  LaunchedEffect(dispenseViewModel.errorMessage) {
    if (dispenseViewModel.errorMessage.isNotEmpty()) {
      Toast.makeText(context, dispenseViewModel.errorMessage, Toast.LENGTH_SHORT).show()
      dispenseViewModel.errorMessage = ""
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize(), contentAlignment = Alignment.Center
  ) {
    if (dispenseViewModel.isLoading) {
      CircularProgressIndicator(
        modifier = Modifier.size(24.dp), color = LgsBlue, strokeWidth = 2.dp
      )
    } else {
      if (dispenseViewModel.dispenseOnData != null) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row {
            Text(
              text = "BinLo : ",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = dispenseViewModel.dispenseOnData?.location ?: "",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = Color.Red
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = dispenseViewModel.dispenseOnData?.drugName ?: "ไม่พบชื่อยา",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//              text = "จำนวน ${orderLabel?.f_orderqty ?: "0"} ${orderLabel?.f_orderunitdesc ?: ""}",
//              style = MaterialTheme.typography.bodyMedium,
//              color = Color.Gray
//            )
        }
      } else {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          androidx.compose.material3.Text(
            text = "Light Guiding Station",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
          )
          androidx.compose.material3.Text(
            text = "( LGS )",
            fontWeight = FontWeight.Normal,
            style = MaterialTheme.typography.titleLarge
          )
          Spacer(modifier = Modifier.height(22.dp))
          androidx.compose.material3.Text(
            text = "Scan Barcode / QRCode", style = MaterialTheme.typography.bodyLarge
          )
          Spacer(modifier = Modifier.height(42.dp))

          Box(
            modifier = Modifier
              .size(200.dp)
              .clip(CircleShape)
              .padding(4.dp)
              .border(BorderStroke(4.dp, LgsBlue), CircleShape), contentAlignment = Alignment.Center
          ) {
            androidx.compose.material3.Icon(
              painter = painterResource(id = R.drawable.lgs_scan),
              contentDescription = "Scan Barcode or QRCode",
              modifier = Modifier.size(100.dp),
              tint = MaterialTheme.colorScheme.onSurface
            )
          }
          Spacer(modifier = Modifier.height(42.dp))

          androidx.compose.material3.Text(
            text = "สแกน Drug Code",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
          )
        }
      }
    }
  }
}

@Composable
private fun TurnOffLightScreen(dispenseViewModel: DispenseViewModel, context: Context) {
  BarcodeScanner { scannedCode ->
    dispenseViewModel.handleDispenseOffManual(scannedCode = scannedCode)
  }

  LaunchedEffect(dispenseViewModel.errorMessage) {
    if (dispenseViewModel.errorMessage.isNotEmpty()) {
      Toast.makeText(context, dispenseViewModel.errorMessage, Toast.LENGTH_SHORT).show()
      dispenseViewModel.errorMessage = ""
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize(), contentAlignment = Alignment.Center
  ) {
    if (dispenseViewModel.isLoading) {
      CircularProgressIndicator(
        modifier = Modifier.size(24.dp), color = LgsBlue, strokeWidth = 2.dp
      )
    } else {
      if (dispenseViewModel.dispenseOffData != null) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row {
            Text(
              text = "BinLo : ",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = dispenseViewModel.dispenseOffData?.location ?: "",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = Color.Red
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = dispenseViewModel.dispenseOffData?.drugName ?: "ไม่พบชื่อยา",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//              text = "จำนวน ${orderLabel?.f_orderqty ?: "0"} ${orderLabel?.f_orderunitdesc ?: ""}",
//              style = MaterialTheme.typography.bodyMedium,
//              color = Color.Gray
//            )
        }
      } else {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          androidx.compose.material3.Text(
            text = "Light Guiding Station",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
          )
          androidx.compose.material3.Text(
            text = "( LGS )",
            fontWeight = FontWeight.Normal,
            style = MaterialTheme.typography.titleLarge
          )
          Spacer(modifier = Modifier.height(22.dp))
          androidx.compose.material3.Text(
            text = "Scan Barcode / QRCode", style = MaterialTheme.typography.bodyLarge
          )
          Spacer(modifier = Modifier.height(42.dp))

          Box(
            modifier = Modifier
              .size(200.dp)
              .clip(CircleShape)
              .padding(4.dp)
              .border(BorderStroke(4.dp, LgsBlue), CircleShape), contentAlignment = Alignment.Center
          ) {
            androidx.compose.material3.Icon(
              painter = painterResource(id = R.drawable.lgs_scan),
              contentDescription = "Scan Barcode or QRCode",
              modifier = Modifier.size(100.dp),
              tint = MaterialTheme.colorScheme.onSurface
            )
          }
          Spacer(modifier = Modifier.height(42.dp))

          androidx.compose.material3.Text(
            text = "สแกน BinLo",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
          )
        }
      }
    }
  }
}