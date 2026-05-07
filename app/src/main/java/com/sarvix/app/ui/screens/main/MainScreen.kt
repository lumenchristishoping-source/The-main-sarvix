package com.sarvix.app.ui.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sarvix.app.ui.navigation.Screen
import com.sarvix.app.ui.screens.chat.ChatsScreen
import com.sarvix.app.ui.screens.match.MatchesScreen
import com.sarvix.app.ui.screens.post.SarvixReadsScreen
import com.sarvix.app.ui.screens.profile.ProfileScreen
import com.sarvix.app.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

data class NavigationItem(
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
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
        NavigationItem(
            title = "Chats",
            selectedIcon = Icons.AutoMirrored.Filled.Chat,
            unselectedIcon = Icons.AutoMirrored.Outlined.Chat,
            route = Screen.Chats.route
        ),
        NavigationItem(
            title = "Matches",
            selectedIcon = Icons.Filled.People,
            unselectedIcon = Icons.Outlined.People,
            route = Screen.Matches.route
        ),
        NavigationItem(
            title = "Sarvix Reads",
            selectedIcon = Icons.Filled.MenuBook,
            unselectedIcon = Icons.Outlined.MenuBook,
            route = Screen.SarvixReads.route
        ),
        NavigationItem(
            title = "Profile",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
            route = Screen.Profile.route
        )
    )
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(24.dp))
                
                // App Logo/Title
                Text(
                    text = "Sarvix",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Clarity in Communication",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Navigation Items
                navigationItems.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
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
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Settings
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate(Screen.Settings.route)
                    },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            when (currentRoute) {
                                Screen.Chats.route -> "Chats"
                                Screen.Matches.route -> "Matches"
                                Screen.SarvixReads.route -> "Sarvix Reads"
                                Screen.Profile.route -> "Profile"
                                else -> "Sarvix"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    actions = {
                        if (currentRoute == Screen.SarvixReads.route) {
                            IconButton(onClick = { navController.navigate(Screen.NewPost.route) }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "New Post"
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
                    ChatsScreen(navController = navController)
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