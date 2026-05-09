package com.sarvix.app.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sarvix.app.ui.components.FloatingPillTopAppBar
import com.sarvix.app.ui.navigation.Screen
import com.sarvix.app.ui.screens.chat.ChatsScreen
import com.sarvix.app.ui.screens.match.MatchesScreen
import com.sarvix.app.ui.screens.post.SarvixReadsScreen
import com.sarvix.app.ui.screens.profile.ProfileScreen
import com.sarvix.app.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset

// Navigation item data class
private data class NavigationItem(
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

@Composable
fun MainScreen(
    navController: NavController
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val mainNavController = rememberNavController()
    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Chats.route

    val navigationItems = listOf(
        NavigationItem("Chats", Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat, Screen.Chats.route),
        NavigationItem("Matches", Icons.Filled.People, Icons.Outlined.People, Screen.Matches.route),
        NavigationItem("Sarvix Reads", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, Screen.SarvixReads.route),
        NavigationItem("Profile", Icons.Filled.Person, Icons.Outlined.Person, Screen.Profile.route)
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = Surface
            ) {
                // FIXED: Thin animated gradient line on RIGHT EDGE only
                Box(modifier = Modifier.fillMaxHeight()) {
                    Column(modifier = Modifier.fillMaxHeight()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        // App Logo and Title
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Logo placeholder - will be replaced with actual logo
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "S",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = OnPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Sarvix",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = OnSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Communication Clarity",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = DividerColor
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Navigation Items
                        navigationItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationDrawerItem(
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title,
                                        tint = if (selected) AccentCyan else OnSurfaceVariant
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        color = if (selected) OnSurface else OnSurfaceVariant,
                                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                },
                                selected = selected,
                                onClick = {
                                    scope.launch { drawerState.close() }
                                    mainNavController.navigate(item.route) {
                                        popUpTo(mainNavController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 12.dp),
                                colors = NavigationDrawerItemDefaults.colors(
                                    selectedContainerColor = NavActiveBg,
                                    selectedIconColor = AccentCyan,
                                    selectedTextColor = OnSurface,
                                    unselectedIconColor = OnSurfaceVariant,
                                    unselectedTextColor = OnSurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = DividerColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Settings at bottom
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    Icons.Outlined.Settings,
                                    contentDescription = "Settings",
                                    tint = OnSurfaceVariant
                                )
                            },
                            label = { Text("Settings", color = OnSurfaceVariant) },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(Screen.Settings.route)
                            },
                            modifier = Modifier.padding(horizontal = 12.dp),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // FIXED: Thin animated gradient line on RIGHT EDGE only
                    val infiniteTransition = rememberInfiniteTransition(label = "drawerEdge")
                    val offset by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(4000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "edgeOffset"
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(2.dp)
                            .align(Alignment.CenterEnd)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(GradientPurple, AccentPink, AccentCyan, AccentOrange, GradientPurple),
                                    start = Offset(0f, offset * 2000f),
                                    end = Offset(0f, offset * 2000f + 1000f),
                                    tileMode = TileMode.Mirror
                                )
                            )
                    )
                }
            }
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            containerColor = Background,
            topBar = {
                FloatingPillTopAppBar(
                    title = when (currentRoute) {
                        Screen.Chats.route -> "Chats"
                        Screen.Matches.route -> "Matches"
                        Screen.SarvixReads.route -> "Sarvix Reads"
                        Screen.Profile.route -> "Profile"
                        else -> "Sarvix"
                    },
                    onMenuClick = { scope.launch { drawerState.open() } },
                    actions = {
                        if (currentRoute == Screen.SarvixReads.route) {
                            IconButton(onClick = { navController.navigate(Screen.NewPost.route) }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "New Post",
                                    tint = AccentCyan
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = mainNavController,
                startDestination = Screen.Chats.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Chats.route) {
                    ChatsScreen(
                        navController = navController,
                        onFindPeople = {
                            mainNavController.navigate(Screen.Matches.route) {
                                popUpTo(mainNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(Screen.Matches.route) {
                    MatchesScreen(navController = navController)
                }
                composable(Screen.SarvixReads.route) {
                    SarvixReadsScreen(navController = navController)
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(navController = navController)
                }
            }
        }
    }
}
