package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.DrawerGradientEnd
import com.example.ui.theme.DrawerGradientMid
import com.example.ui.theme.DrawerGradientStart
import com.example.ui.theme.HoloRedLight
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShellScreen(
    uiState: AdminPanelUiState,
    onSelectSection: (DrawerSection) -> Unit,
    onRefreshAppClick: () -> Unit,
    onExitAppClick: () -> Unit,
    onUpdateHomeForm: ((HomeFormState) -> HomeFormState) -> Unit,
    onFetchVersionClick: () -> Unit,
    onPushChangeClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEditDashboardApp: (DashboardAppItem) -> Unit,
    onDeleteDashboardApp: (String) -> Unit,
    onManageLinkInputChange: (String) -> Unit,
    onSaveLinkClick: () -> Unit,
    onSelectActiveLink: (String) -> Unit,
    onCopyLink: (String) -> Unit,
    onDeleteLink: (String) -> Unit,
    onEditProfileName: (String) -> Unit,
    onThemeModeSelected: (AppThemeMode) -> Unit,
    onLogoutClick: () -> Unit,
    onUiNotice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showOverflowMenu by remember { mutableStateOf(false) }

    BackHandler(enabled = drawerState.isOpen || uiState.currentSection != DrawerSection.HOME) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            onSelectSection(DrawerSection.HOME)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        modifier = modifier,
        drawerContent = {
            ModalDrawerSheet(
                drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 0.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                Column(modifier = Modifier.fillMaxHeight()) {
                    // Drawer Header: 135° linear gradient (#73E8E0 -> #86C2EE -> #9B9AFD)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topEnd = 32.dp))
                            .drawBehind {
                                drawRect(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            DrawerGradientStart,
                                            DrawerGradientMid,
                                            DrawerGradientEnd
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, size.height)
                                    )
                                )
                            }
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 22.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            AdminBrandLogo(
                                size = 64.dp,
                                shape = CircleShape
                            )
                            Text(
                                text = "ADMIN PANEL",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

                    Spacer(modifier = Modifier.height(10.dp))

                    // Single-select Menu Items (5 items)
                    val menuItems = listOf(
                        Triple(DrawerSection.HOME, "Home", Icons.Rounded.Home),
                        Triple(DrawerSection.DASHBOARD, "Dashboard", Icons.Rounded.Dashboard),
                        Triple(DrawerSection.MANAGE_LINKS, "Manage Links", Icons.Rounded.Link),
                        Triple(DrawerSection.SETTINGS, "Settings", Icons.Rounded.Settings)
                    )

                    menuItems.forEach { (section, label, icon) ->
                        val selected = uiState.currentSection == section
                        NavigationDrawerItem(
                            icon = { Icon(icon, contentDescription = null) },
                            label = {
                                Text(
                                    text = label,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            selected = selected,
                            onClick = {
                                onSelectSection(section)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 2.dp)
                                .testTag("drawer_item_${section.name.lowercase()}")
                        )
                    }

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Rounded.Refresh, contentDescription = null) },
                        label = { Text("Refresh App") },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onRefreshAppClick()
                        },
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .testTag("drawer_item_refresh")
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Drawer Footer
                    HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable {
                                scope.launch { drawerState.close() }
                                onExitAppClick()
                            }
                            .padding(horizontal = 20.dp)
                            .testTag("drawer_exit_app"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                            contentDescription = "Exit App",
                            tint = HoloRedLight,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Exit App",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Version 1.0",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(top = 4.dp, bottom = 14.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = uiState.currentSection.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("drawer_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "Open navigation drawer"
                            )
                        }
                    },
                    actions = {
                        Box {
                            IconButton(
                                onClick = { showOverflowMenu = true },
                                modifier = Modifier.testTag("toolbar_overflow_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "More options"
                                )
                            }
                            DropdownMenu(
                                expanded = showOverflowMenu,
                                onDismissRequest = { showOverflowMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    onClick = {
                                        showOverflowMenu = false
                                        onSelectSection(DrawerSection.SETTINGS)
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (uiState.currentSection) {
                    DrawerSection.HOME -> HomeFragment(
                        profileName = uiState.profileName,
                        avatarColorIndex = uiState.avatarColorIndex,
                        form = uiState.homeForm,
                        onUpdateForm = onUpdateHomeForm,
                        onFetchVersionClick = onFetchVersionClick,
                        onPushChangeClick = onPushChangeClick
                    )

                    DrawerSection.DASHBOARD -> DashboardFragment(
                        searchQuery = uiState.searchQuery,
                        apps = uiState.apps,
                        onSearchQueryChange = onSearchQueryChange,
                        onEditApp = onEditDashboardApp,
                        onDeleteApp = onDeleteDashboardApp
                    )

                    DrawerSection.MANAGE_LINKS -> ManageLinksFragment(
                        urlInput = uiState.newLinkInput,
                        savedLinks = uiState.savedLinks,
                        onUrlInputChange = onManageLinkInputChange,
                        onSaveLinkClick = onSaveLinkClick,
                        onSelectActiveLink = onSelectActiveLink,
                        onCopyLink = onCopyLink,
                        onDeleteLink = onDeleteLink
                    )

                    DrawerSection.SETTINGS -> SettingsFragment(
                        profileName = uiState.profileName,
                        avatarColorIndex = uiState.avatarColorIndex,
                        themeMode = uiState.themeMode,
                        onEditProfileName = onEditProfileName,
                        onThemeModeSelected = onThemeModeSelected,
                        onLogoutClick = onLogoutClick,
                        onUiNotice = onUiNotice
                    )
                }
            }
        }
    }
}
