package com.mindgambit.app.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mindgambit.app.presentation.board.BoardScreen
import com.mindgambit.app.presentation.home.HomeScreen
import com.mindgambit.app.presentation.ladder.LadderScreen
import com.mindgambit.app.presentation.onboarding.OnboardingScreen
import com.mindgambit.app.presentation.openings.OpeningDetailScreen
import com.mindgambit.app.presentation.openings.OpeningLessonScreen
import com.mindgambit.app.presentation.openings.OpeningsScreen
import com.mindgambit.app.presentation.review.ReviewScreen
import com.mindgambit.app.presentation.tactics.TacticsScreen

sealed class Screen(val route: String) {
    data object Onboarding    : Screen("onboarding")
    data object Home          : Screen("home")
    data object Board         : Screen("board/{gameMode}") {
        fun createRoute(gameMode: String = "RAPID") = "board/$gameMode"
    }
    data object Tactics       : Screen("tactics")
    data object Openings      : Screen("openings")
    data object OpeningDetail : Screen("opening/{openingId}") {
        fun createRoute(id: String) = "opening/$id"
    }
    data object OpeningLesson : Screen("opening/{openingId}/lesson/{lessonId}") {
        fun createRoute(openingId: String, lessonId: String) = "opening/$openingId/lesson/$lessonId"
    }
    data object Ladder        : Screen("ladder")
    data object Review        : Screen("review/{gameId}") {
        fun createRoute(id: Long) = "review/$id"
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NavGraph(startDestination: String = Screen.Home.route) {
    val navController = rememberNavController()

    NavHost(
        navController    = navController,
        startDestination = startDestination,
        enterTransition  = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(280)) + fadeIn(tween(280))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(280)) + fadeOut(tween(180))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(280)) + fadeIn(tween(280))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(280)) + fadeOut(tween(180))
        }
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onComplete = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToBoard    = { mode -> navController.navigate(Screen.Board.createRoute(mode)) },
                onNavigateToTactics  = { navController.navigate(Screen.Tactics.route) },
                onNavigateToOpenings = { navController.navigate(Screen.Openings.route) },
                onNavigateToLadder   = { navController.navigate(Screen.Ladder.route) }
            )
        }

        composable(
            route     = Screen.Board.route,
            arguments = listOf(navArgument("gameMode") { type = NavType.StringType })
        ) { backStack ->
            BoardScreen(
                gameMode       = backStack.arguments?.getString("gameMode") ?: "RAPID",
                onNavigateUp   = { navController.popBackStack() },
                onGameComplete = { gameId ->
                    navController.navigate(Screen.Review.createRoute(gameId)) { popUpTo(Screen.Home.route) }
                }
            )
        }

        composable(Screen.Tactics.route) {
            TacticsScreen(onNavigateUp = { navController.popBackStack() })
        }

        composable(Screen.Openings.route) {
            OpeningsScreen(
                onNavigateUp      = { navController.popBackStack() },
                onOpeningSelected = { id -> navController.navigate(Screen.OpeningDetail.createRoute(id)) }
            )
        }

        composable(
            route     = Screen.OpeningDetail.route,
            arguments = listOf(navArgument("openingId") { type = NavType.StringType })
        ) { backStack ->
            val openingId = backStack.arguments?.getString("openingId") ?: return@composable
            OpeningDetailScreen(
                openingId    = openingId,
                onNavigateUp = { navController.popBackStack() },
                onLessonClick = { lessonId ->
                    navController.navigate(Screen.OpeningLesson.createRoute(openingId, lessonId))
                }
            )
        }

        composable(
            route     = Screen.OpeningLesson.route,
            arguments = listOf(
                navArgument("openingId") { type = NavType.StringType },
                navArgument("lessonId")  { type = NavType.StringType }
            )
        ) { backStack ->
            val openingId = backStack.arguments?.getString("openingId") ?: return@composable
            val lessonId  = backStack.arguments?.getString("lessonId")  ?: return@composable
            OpeningLessonScreen(
                openingId    = openingId,
                lessonId     = lessonId,
                onComplete   = { navController.popBackStack() },
                onNavigateUp = { navController.popBackStack() }
            )
        }

        composable(Screen.Ladder.route) {
            LadderScreen(onNavigateUp = { navController.popBackStack() })
        }

        composable(
            route     = Screen.Review.route,
            arguments = listOf(navArgument("gameId") { type = NavType.LongType })
        ) { backStack ->
            ReviewScreen(
                gameId       = backStack.arguments?.getLong("gameId") ?: 0L,
                onNavigateUp = { navController.popBackStack() }
            )
        }
    }
}
