package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.SetupBackdropGray
import com.example.ui.theme.SplashDarkEnd
import com.example.ui.theme.SplashDarkStart
import com.example.ui.theme.SplashLightEnd
import com.example.ui.theme.SplashLightStart
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    themeMode: AppThemeMode,
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }
    val gradientStart = if (isDark) SplashDarkStart else SplashLightStart
    val gradientEnd = if (isDark) SplashDarkEnd else SplashLightEnd

    LaunchedEffect(Unit) {
        delay(1600L)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // 315° linear gradient: bottom-left (0, height) -> top-right (width, 0)
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(gradientStart, gradientEnd),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, 0f)
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AdminBrandLogo(
                size = 100.dp,
                shape = RoundedCornerShape(26.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "ADMIN",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Initializing...",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    nameInput: String,
    avatarColorIndex: Int,
    onNameChange: (String) -> Unit,
    onCycleAvatarColor: () -> Unit,
    onSkipClick: () -> Unit,
    onGetStartedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    TextButton(
                        onClick = onSkipClick,
                        modifier = Modifier.testTag("profile_skip_button")
                    ) {
                        Text(
                            text = "Skip",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(72.dp))

                RingedProfileAvatar(
                    avatarSize = 78.dp,
                    ringSize = 84.dp,
                    ringWidth = 3.dp,
                    avatarColorIndex = avatarColorIndex,
                    showCameraBadge = true,
                    onClick = onCycleAvatarColor,
                    modifier = Modifier.testTag("profile_avatar_edit")
                )

                Spacer(modifier = Modifier.height(48.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { value ->
                        if (value.length <= 26) onNameChange(value)
                    },
                    placeholder = { Text("Enter your name") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = adminOutlinedFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input")
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onGetStartedClick,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("get_started_button")
                ) {
                    Text(
                        text = "Get Started",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun FirebaseUrlSetupScreen(
    urlInput: String,
    onUrlChange: (String) -> Unit,
    onSaveAndContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tutorialDialogTitle by remember { mutableStateOf<String?>(null) }
    var tutorialDialogBody by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SetupBackdropGray)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card 1: Firebase Tutorial
            AdminElevatedCard(
                cornerRadius = 16.dp,
                elevation = 6.dp,
                contentPadding = 14.dp,
                onClick = {
                    tutorialDialogTitle = "Firebase Tutorial"
                    tutorialDialogBody =
                        "1. Open Firebase Console and create a Realtime Database.\n2. Copy the database URL (e.g. https://your-app-default-rtdb.firebaseio.com).\n3. Paste it below and tap Save & Continue."
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "Firebase Tutorial",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Firebase Tutorial",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Learn How to create Firebase Database URL.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Card 2: Admin & Dialog Tutorial
            AdminElevatedCard(
                cornerRadius = 16.dp,
                elevation = 6.dp,
                contentPadding = 14.dp,
                onClick = {
                    tutorialDialogTitle = "Admin & Dialog Tutorial"
                    tutorialDialogBody =
                        "Use the Home panel to configure Package & Animations, Scheduled & Version info, Dialog Info, Social Links, Notifications, and Active Dialog updates."
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "Admin & Dialog Tutorial",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Admin & Dialog Tutorial",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Learn how to push updates and manage dialog.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Card 3: Main Setup Card
            AdminElevatedCard(
                cornerRadius = 20.dp,
                elevation = 8.dp,
                contentPadding = 16.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SETUP FIREBASE",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Enter Firebase Database URL",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = onUrlChange,
                        placeholder = { Text("Enter Firebase URL") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        shape = RoundedCornerShape(14.dp),
                        colors = adminOutlinedFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("setup_firebase_url_input")
                    )

                    Button(
                        onClick = onSaveAndContinueClick,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_and_continue_button")
                    ) {
                        Text(
                            text = "Save & Continue",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "© 2026 Zenx Modz",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (tutorialDialogTitle != null && tutorialDialogBody != null) {
        AlertDialog(
            onDismissRequest = {
                tutorialDialogTitle = null
                tutorialDialogBody = null
            },
            title = { Text(tutorialDialogTitle!!, fontWeight = FontWeight.Bold) },
            text = { Text(tutorialDialogBody!!) },
            confirmButton = {
                TextButton(onClick = {
                    tutorialDialogTitle = null
                    tutorialDialogBody = null
                }) {
                    Text("Got It")
                }
            }
        )
    }
}
