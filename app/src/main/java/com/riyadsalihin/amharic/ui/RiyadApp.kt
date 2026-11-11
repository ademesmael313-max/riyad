package com.riyadsalihin.amharic.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.riyadsalihin.amharic.AppContainer
import com.riyadsalihin.amharic.R
import com.riyadsalihin.amharic.data.AppSettings
import com.riyadsalihin.amharic.ui.about.AboutScreen
import com.riyadsalihin.amharic.ui.bookmarks.BookmarksScreen
import com.riyadsalihin.amharic.ui.home.HomeScreen
import com.riyadsalihin.amharic.ui.reader.ReaderScreen
import com.riyadsalihin.amharic.ui.search.SearchScreen
import com.riyadsalihin.amharic.ui.settings.SettingsScreen
import com.riyadsalihin.amharic.ui.theme.RiyadTheme

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val BOOKMARKS = "bookmarks"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val READER = "reader/{bookId}?hadith={hadith}"

    fun reader(bookId: Int, hadithNumber: Int? = null): String =
        "reader/$bookId?hadith=${hadithNumber ?: 0}"
}

private data class Tab(
    val route: String,
    @StringRes val label: Int,
    val icon: ImageVector,
)

private val Tabs = listOf(
    Tab(Routes.HOME, R.string.tab_books, Icons.Outlined.MenuBook),
    Tab(Routes.SEARCH, R.string.tab_search, Icons.Outlined.Search),
    Tab(Routes.BOOKMARKS, R.string.tab_bookmarks, Icons.Outlined.BookmarkBorder),
    Tab(Routes.SETTINGS, R.string.tab_settings, Icons.Outlined.Settings),
)

@Composable
fun RiyadApp(container: AppContainer) {
    val settings by container.settings.settings.collectAsStateWithLifecycle(initialValue = AppSettings())

    RiyadTheme(themeMode = settings.theme) {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val showBottomBar = Tabs.any { it.route == currentRoute }

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    RiyadBottomBar(currentRoute = currentRoute, navController = navController)
                }
            },
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.padding(padding),
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        container = container,
                        onOpenSearch = { navController.navigate(Routes.SEARCH) },
                        onOpenBook = { bookId, hadithNumber ->
                            navController.navigate(Routes.reader(bookId, hadithNumber))
                        },
                    )
                }
                composable(Routes.SEARCH) {
                    SearchScreen(
                        container = container,
                        onOpenResult = { bookId, hadithNumber ->
                            navController.navigate(Routes.reader(bookId, hadithNumber))
                        },
                    )
                }
                composable(Routes.BOOKMARKS) {
                    BookmarksScreen(
                        container = container,
                        onOpenResult = { bookId, hadithNumber ->
                            navController.navigate(Routes.reader(bookId, hadithNumber))
                        },
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        container = container,
                        onOpenAbout = { navController.navigate(Routes.ABOUT) },
                    )
                }
                composable(
                    route = Routes.READER,
                    arguments = listOf(
                        navArgument("bookId") { type = NavType.IntType },
                        navArgument("hadith") {
                            type = NavType.IntType
                            defaultValue = 0
                        },
                    ),
                ) { entry ->
                    val bookId = entry.arguments?.getInt("bookId") ?: 1
                    val hadithNumber = entry.arguments?.getInt("hadith") ?: 0
                    ReaderScreen(
                        container = container,
                        bookId = bookId,
                        initialHadith = hadithNumber.takeIf { it > 0 },
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(Routes.ABOUT) {
                    AboutScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

@Composable
private fun RiyadBottomBar(currentRoute: String?, navController: NavHostController) {
    NavigationBar {
        Tabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    if (currentRoute != tab.route) {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                label = { Text(stringResource(tab.label)) },
                colors = NavigationBarItemDefaults.colors(),
            )
        }
    }
}
