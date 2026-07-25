package com.woojoo.foundingfathers.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.woojoo.foundingfathers.audio.SoundManager
import com.woojoo.foundingfathers.audio.BgmGroup
import com.woojoo.foundingfathers.screens.FounderDetailScreen
import com.woojoo.foundingfathers.screens.HistoryScreen
import com.woojoo.foundingfathers.screens.HomeScreen
import com.woojoo.foundingfathers.screens.OnboardingScreen
import com.woojoo.foundingfathers.screens.QuizScreen
import com.woojoo.foundingfathers.screens.QuoteMatchScreen
import com.woojoo.foundingfathers.screens.SettingsScreen
import com.woojoo.foundingfathers.screens.TimelineDetailScreen
import com.woojoo.foundingfathers.screens.TimelineListScreen
import com.woojoo.foundingfathers.state.AppViewModel

private data class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val tabs = listOf(
    Tab("home", "Home", Icons.Filled.Home),
    Tab("timeline", "Timeline", Icons.Filled.Schedule),
    Tab("quiz", "Quiz", Icons.Filled.Star),
    Tab("quotes", "Quotes", Icons.Filled.FormatQuote),
    Tab("settings", "Settings", Icons.Filled.Settings)
)

@Composable
fun AppRoot(viewModel: AppViewModel, soundManager: SoundManager) {
    val state by viewModel.state.collectAsState()

    if (!state.hasSeenOnboarding) {
        OnboardingScreen(onDone = { viewModel.completeOnboarding() })
        return
    }

    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            when (tab.route) {
                                "home" -> soundManager.playBgm(BgmGroup.HOME)
                                "quiz" -> soundManager.playBgm(BgmGroup.QUIZ)
                                "timeline" -> soundManager.playBgm(BgmGroup.EXPLORE)
                                "quotes" -> soundManager.playBgm(BgmGroup.QUOTES)
                                "settings" -> soundManager.playBgm(BgmGroup.SETTINGS)
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 6 } },
            exitTransition = { fadeOut(tween(180)) },
            popEnterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { -it / 6 } },
            popExitTransition = { fadeOut(tween(180)) }
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    soundManager = soundManager,
                    onOpenFounder = { id -> navController.navigate("founder_detail/$id") },
                    onOpenHistory = { navController.navigate("history") },
                    onStartQuiz = {
                        navController.navigate("quiz") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable("timeline") {
                TimelineListScreen(
                    viewModel = viewModel,
                    onOpenFounder = { id -> navController.navigate("timeline_detail/$id") }
                )
            }
            composable(
                "timeline_detail/{founderId}",
                arguments = listOf(navArgument("founderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val founderId = backStackEntry.arguments?.getString("founderId") ?: return@composable
                val founder = viewModel.founderById(founderId) ?: return@composable
                TimelineDetailScreen(father = founder, onBack = { navController.popBackStack() })
            }
            composable(
                "founder_detail/{founderId}",
                arguments = listOf(navArgument("founderId") { type = NavType.StringType })
            ) { backStackEntry ->
                val founderId = backStackEntry.arguments?.getString("founderId") ?: return@composable
                val founder = viewModel.founderById(founderId) ?: return@composable
                FounderDetailScreen(father = founder, onBack = { navController.popBackStack() })
            }
            composable("quiz") {
                QuizScreen(viewModel = viewModel, soundManager = soundManager)
            }
            composable("quotes") {
                QuoteMatchScreen(viewModel = viewModel, soundManager = soundManager)
            }
            composable("settings") {
                SettingsScreen(viewModel = viewModel, soundManager = soundManager)
            }
            composable("history") {
                HistoryScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
        }
    }
}
