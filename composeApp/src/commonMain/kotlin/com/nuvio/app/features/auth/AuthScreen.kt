package com.nuvio.app.features.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalAutofillManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nuvio.app.core.auth.AuthRepository
import com.nuvio.app.core.auth.DeviceLinkAuthRepository
import com.nuvio.app.core.auth.DeviceLinkAuthState
import com.nuvio.app.core.build.AppFeaturePolicy
import com.nuvio.app.core.ui.CinemaBlockButton
import com.nuvio.app.core.ui.CinemaColors
import com.nuvio.app.core.ui.NuvioLoadingIndicator
import com.nuvio.app.features.settings.AppBrandWordmark
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.action_back
import nuvio.composeapp.generated.resources.auth_get_help
import nuvio.composeapp.generated.resources.auth_have_account
import nuvio.composeapp.generated.resources.auth_new_to_flowix
import nuvio.composeapp.generated.resources.auth_sign_in_heading
import nuvio.composeapp.generated.resources.auth_sign_in_now
import nuvio.composeapp.generated.resources.auth_sign_up_heading
import nuvio.composeapp.generated.resources.auth_sign_up_now
import nuvio.composeapp.generated.resources.compose_auth_continue_without_account
import nuvio.composeapp.generated.resources.compose_auth_create_account
import nuvio.composeapp.generated.resources.compose_auth_email
import nuvio.composeapp.generated.resources.compose_auth_password
import nuvio.composeapp.generated.resources.compose_auth_sign_in
import nuvio.composeapp.generated.resources.compose_auth_store_locally
import nuvio.composeapp.generated.resources.compose_auth_terms_link
import nuvio.composeapp.generated.resources.compose_auth_terms_prefix
import org.jetbrains.compose.resources.stringResource

internal val AuthTextPrimary = Color.White
internal val AuthTextSecondary = CinemaColors.TextSecondary
private val AuthTextMuted = CinemaColors.TextMuted
private val AuthPrimaryButtonBackground = Color.White
private val AuthPrimaryButtonText = Color.Black
internal val AuthFieldBackground = Color(0xFF1C1718).copy(alpha = 0.92f)
internal val AuthFieldBorder = CinemaColors.FieldBorder
private val AuthSecondaryButtonBackground = Color(0xFF3A3A3A).copy(alpha = 0.8f)
internal val AuthControlShape = RoundedCornerShape(4.dp)

private enum class AuthStep { Welcome, SignIn, SignUp }

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
) {
    val authError by AuthRepository.error.collectAsStateWithLifecycle()
    val deviceLinkAuthState by DeviceLinkAuthRepository.state.collectAsStateWithLifecycle()
    val serverConnectionState by ServerConnectionController.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var step by rememberSaveable { mutableStateOf(AuthStep.Welcome) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var emailFieldBounds by remember { mutableStateOf<Rect?>(null) }
    var passwordFieldBounds by remember { mutableStateOf<Rect?>(null) }
    var showServerSheet by rememberSaveable { mutableStateOf(false) }
    var showOfficialServerDialog by rememberSaveable { mutableStateOf(false) }
    val isSignUp = step == AuthStep.SignUp

    fun submitAuth() {
        if (email.isBlank() || password.length < 6 || isLoading) return
        DeviceLinkAuthRepository.cancel()
        isLoading = true
        focusManager.clearFocus(force = true)
        scope.launch {
            if (isSignUp) AuthRepository.signUpWithEmail(email, password)
            else AuthRepository.signInWithEmail(email, password)
            isLoading = false
        }
    }

    fun openStep(target: AuthStep) {
        DeviceLinkAuthRepository.cancel()
        AuthRepository.clearError()
        focusManager.clearFocus(force = true)
        step = target
    }

    fun startDeviceLink() {
        if (isLoading) return
        focusManager.clearFocus(force = true)
        AuthRepository.clearError()
        DeviceLinkAuthRepository.start()
    }

    LaunchedEffect(serverConnectionState.activeServer.backendUrl) {
        DeviceLinkAuthRepository.cancel()
        if (!serverConnectionState.activeServer.isCustom) {
            showOfficialServerDialog = false
        }
    }

    DisposableEffect(Unit) {
        onDispose(DeviceLinkAuthRepository::cancel)
    }

    val serverMenu: @Composable () -> Unit = {
        if (AppFeaturePolicy.customServerConnectionsEnabled) {
            ServerConnectionMenu(
                activeServer = serverConnectionState.activeServer,
                onUseOfficial = {
                    ServerConnectionController.resetDiscovery()
                    showOfficialServerDialog = true
                },
                onConnectCustom = {
                    ServerConnectionController.resetDiscovery()
                    showServerSheet = true
                },
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(emailFieldBounds, passwordFieldBounds) {
                awaitEachGesture {
                    val down = awaitFirstDown(
                        requireUnconsumed = false,
                        pass = PointerEventPass.Initial,
                    )
                    val tappedTextField = listOfNotNull(emailFieldBounds, passwordFieldBounds)
                        .any { bounds -> bounds.contains(down.position) }
                    if (!tappedTextField) {
                        focusManager.clearFocus(force = true)
                    }
                }
            },
    ) {
        AnimatedContent(
            targetState = step == AuthStep.Welcome,
            transitionSpec = {
                if (targetState) {
                    (slideInHorizontally { -it / 4 } + fadeIn()) togetherWith (slideOutHorizontally { it / 4 } + fadeOut())
                } else {
                    (slideInHorizontally { it / 4 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 4 } + fadeOut())
                }
            },
            label = "auth_step",
        ) { welcome ->
            if (welcome) {
                AuthWelcomeScreen(
                    onGetStarted = { openStep(AuthStep.SignUp) },
                    onSignIn = { openStep(AuthStep.SignIn) },
                    menu = serverMenu,
                )
            } else {
                AuthFormScreen(
                    isSignUp = isSignUp,
                    email = email,
                    password = password,
                    passwordVisible = passwordVisible,
                    isLoading = isLoading,
                    authError = authError,
                    deviceLinkAuthState = deviceLinkAuthState,
                    deviceLinkEnabled = serverConnectionState.activeServer.capabilities.tvLogin,
                    onBack = { openStep(AuthStep.Welcome) },
                    onEmailChange = {
                        email = it
                        AuthRepository.clearError()
                    },
                    onPasswordChange = {
                        password = it
                        AuthRepository.clearError()
                    },
                    onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                    onSubmit = ::submitAuth,
                    onToggleAuthMode = { openStep(if (isSignUp) AuthStep.SignIn else AuthStep.SignUp) },
                    onContinueWithoutAccount = {
                        focusManager.clearFocus(force = true)
                        DeviceLinkAuthRepository.cancel()
                        AuthRepository.signInAnonymously()
                    },
                    onStartDeviceLink = ::startDeviceLink,
                    onCancelDeviceLink = DeviceLinkAuthRepository::cancel,
                    onEmailBoundsChange = { emailFieldBounds = it },
                    onPasswordBoundsChange = { passwordFieldBounds = it },
                    menu = serverMenu,
                )
            }
        }
    }

    if (
        AppFeaturePolicy.customServerConnectionsEnabled &&
        showServerSheet &&
        serverConnectionState.discoveredServer == null
    ) {
        ServerConnectionSheet(
            state = serverConnectionState,
            onDiscover = ServerConnectionController::discover,
            onDismiss = {
                showServerSheet = false
                ServerConnectionController.resetDiscovery()
            },
        )
    }

    serverConnectionState.discoveredServer?.let { server ->
        if (AppFeaturePolicy.customServerConnectionsEnabled) {
            ServerTrustDialog(
                server = server,
                isSwitching = serverConnectionState.isSwitching,
                switchFailure = serverConnectionState.switchFailure,
                onConfirm = ServerConnectionController::connectDiscovered,
                onDismiss = {
                    showServerSheet = false
                    ServerConnectionController.resetDiscovery()
                },
            )
        }
    }

    if (AppFeaturePolicy.customServerConnectionsEnabled && showOfficialServerDialog) {
        OfficialServerDialog(
            isSwitching = serverConnectionState.isSwitching,
            switchFailure = serverConnectionState.switchFailure,
            onConfirm = ServerConnectionController::useOfficial,
            onDismiss = {
                showOfficialServerDialog = false
                ServerConnectionController.resetDiscovery()
            },
        )
    }
}

/**
 * "Enter your info to sign in": back arrow and wordmark, a large heading, two fields, one red
 * action. Device-link sign-in and continuing without an account fold away under "Get help".
 */
@Composable
private fun AuthFormScreen(
    isSignUp: Boolean,
    email: String,
    password: String,
    passwordVisible: Boolean,
    isLoading: Boolean,
    authError: String?,
    deviceLinkAuthState: DeviceLinkAuthState,
    deviceLinkEnabled: Boolean,
    onBack: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onSubmit: () -> Unit,
    onToggleAuthMode: () -> Unit,
    onContinueWithoutAccount: () -> Unit,
    onStartDeviceLink: () -> Unit,
    onCancelDeviceLink: () -> Unit,
    onEmailBoundsChange: (Rect) -> Unit,
    onPasswordBoundsChange: (Rect) -> Unit,
    menu: @Composable () -> Unit,
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val uriHandler = LocalUriHandler.current
    val autofillManager = LocalAutofillManager.current
    val submitWithAutofillCommit: () -> Unit = {
        autofillManager?.commit()
        onSubmit()
    }
    var helpExpanded by rememberSaveable { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(if (helpExpanded) 180f else 0f, tween(200), label = "auth_help_chevron")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFF331316),
                    0.32f to Color(0xFF170B0C),
                    0.7f to Color(0xFF060404),
                    1f to Color.Black,
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = statusBarTop, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(start = 12.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(role = Role.Button, onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                        contentDescription = stringResource(Res.string.action_back),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Spacer(Modifier.width(20.dp))
                AppBrandWordmark(modifier = Modifier.height(32.dp))
                Spacer(Modifier.weight(1f))
                menu()
            }

            Column(
                modifier = Modifier
                    .widthIn(max = 460.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(Modifier.height(56.dp))
                AnimatedContent(
                    targetState = isSignUp,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "auth_heading",
                ) { signUp ->
                    Text(
                        text = stringResource(
                            if (signUp) Res.string.auth_sign_up_heading else Res.string.auth_sign_in_heading,
                        ),
                        color = Color.White,
                        fontSize = 30.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() },
                    )
                }
                Spacer(Modifier.height(26.dp))

                AuthTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = stringResource(Res.string.compose_auth_email),
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        onEmailBoundsChange(coordinates.boundsInRoot())
                    },
                    autofillContentType = ContentType.EmailAddress + ContentType.Username,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                )
                Spacer(Modifier.height(12.dp))
                AuthTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = stringResource(Res.string.compose_auth_password),
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityToggle = onPasswordVisibilityToggle,
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        onPasswordBoundsChange(coordinates.boundsInRoot())
                    },
                    autofillContentType = if (isSignUp) ContentType.NewPassword else ContentType.Password,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    onImeAction = submitWithAutofillCommit,
                )

                authError?.let { errorText ->
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = errorText,
                        color = Color(0xFFFF6B6B),
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                    )
                }

                if (isSignUp) {
                    Spacer(Modifier.height(14.dp))
                    AuthTermsAcknowledgement(
                        onTermsClick = { uriHandler.openUri("https://nuvio.tv/terms") },
                    )
                }

                Spacer(Modifier.height(16.dp))
                CinemaBlockButton(
                    text = stringResource(
                        if (isSignUp) Res.string.compose_auth_create_account else Res.string.compose_auth_sign_in,
                    ),
                    onClick = submitWithAutofillCommit,
                    loading = isLoading,
                    enabled = !isLoading,
                )

                Spacer(Modifier.height(22.dp))
                AuthModeToggle(isSignUp = isSignUp, onToggleAuthMode = onToggleAuthMode)

                Spacer(Modifier.height(36.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(role = Role.Button) { helpExpanded = !helpExpanded }
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.auth_get_help),
                        color = Color.White,
                        fontSize = 19.sp,
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(26.dp)
                            .rotate(chevronRotation),
                    )
                }

                AnimatedVisibility(
                    visible = helpExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Column(Modifier.padding(top = 16.dp)) {
                        if (!isSignUp && deviceLinkEnabled) {
                            DeviceLinkAuthSection(
                                state = deviceLinkAuthState,
                                enabled = !isLoading,
                                height = 52.dp,
                                scale = 1f,
                                onStart = onStartDeviceLink,
                                onCancel = onCancelDeviceLink,
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        AuthSecondaryButton(
                            text = stringResource(Res.string.compose_auth_continue_without_account),
                            enabled = !isLoading,
                            height = 52.dp,
                            scale = 1f,
                            onClick = onContinueWithoutAccount,
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = stringResource(Res.string.compose_auth_store_locally),
                            color = AuthTextMuted,
                            fontSize = 14.sp,
                            lineHeight = 19.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthTermsAcknowledgement(
    onTermsClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.compose_auth_terms_prefix),
            color = AuthTextSecondary,
            fontSize = 15.sp,
            lineHeight = 21.sp,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = stringResource(Res.string.compose_auth_terms_link),
            modifier = Modifier.clickable(role = Role.Button, onClick = onTermsClick),
            color = Color.White,
            fontSize = 15.sp,
            lineHeight = 21.sp,
            textDecoration = TextDecoration.Underline,
        )
    }
}

/**
 * Dark field with a hairline grey edge whose label sits inside as a placeholder, then rises to a
 * small caption above the typed value once the field has focus or text.
 */
@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityToggle: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onImeAction: (() -> Unit)? = null,
    autofillContentType: ContentType? = null,
) {
    val state = rememberTextFieldState(initialText = value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(state) {
        snapshotFlow { state.text.toString() }.collect { text -> currentOnValueChange(text) }
    }
    LaunchedEffect(value) {
        if (state.text.toString() != value) state.setTextAndPlaceCursorAtEnd(value)
    }

    val raised = focused || state.text.isNotEmpty()
    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        color = Color.White,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal,
    )
    val fieldModifier = Modifier
        .fillMaxWidth()
        .onFocusChanged { focused = it.isFocused }
        .then(
            if (autofillContentType != null) {
                Modifier.semantics { contentType = autofillContentType }
            } else {
                Modifier
            },
        )
    val keyboardActionHandler = onImeAction?.let { action -> KeyboardActionHandler { action() } }
    val decorator = TextFieldDecorator { innerTextField ->
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label,
                color = if (raised) AuthTextSecondary else AuthTextSecondary.copy(alpha = 0.9f),
                fontSize = if (raised) 13.sp else 18.sp,
                lineHeight = if (raised) 16.sp else 24.sp,
            )
            if (raised) {
                Spacer(Modifier.height(2.dp))
                innerTextField()
            } else {
                Box(Modifier.height(0.dp)) { innerTextField() }
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(AuthFieldBackground, AuthControlShape)
            .border(1.dp, if (focused) Color.White else AuthFieldBorder, AuthControlShape)
            .padding(start = 18.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f).fillMaxHeight()) {
            if (isPassword) {
                BasicSecureTextField(
                    state = state,
                    modifier = fieldModifier.fillMaxHeight(),
                    textStyle = textStyle,
                    keyboardOptions = keyboardOptions,
                    onKeyboardAction = keyboardActionHandler,
                    cursorBrush = SolidColor(Color.White),
                    decorator = decorator,
                    textObfuscationMode = if (passwordVisible) {
                        TextObfuscationMode.Visible
                    } else {
                        TextObfuscationMode.RevealLastTyped
                    },
                )
            } else {
                BasicTextField(
                    state = state,
                    modifier = fieldModifier.fillMaxHeight(),
                    textStyle = textStyle,
                    keyboardOptions = keyboardOptions,
                    onKeyboardAction = keyboardActionHandler,
                    lineLimits = TextFieldLineLimits.SingleLine,
                    cursorBrush = SolidColor(Color.White),
                    decorator = decorator,
                )
            }
        }
        if (isPassword) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = onPasswordVisibilityToggle,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = AuthTextSecondary,
                )
            }
        }
    }
}

@Composable
internal fun AuthPrimaryButton(
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    height: Dp,
    scale: Float,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        enabled = enabled,
        shape = AuthControlShape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AuthPrimaryButtonBackground,
            contentColor = AuthPrimaryButtonText,
            disabledContainerColor = if (isLoading) {
                AuthPrimaryButtonBackground
            } else {
                AuthPrimaryButtonBackground.copy(alpha = 0.45f)
            },
            disabledContentColor = AuthPrimaryButtonText.copy(alpha = 0.55f),
        ),
    ) {
        if (isLoading) {
            NuvioLoadingIndicator(
                modifier = Modifier.size(20.dp * scale),
                color = AuthPrimaryButtonText,
            )
        } else {
            Text(
                text = text,
                fontSize = (17f * scale).sp,
                lineHeight = (22f * scale).sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun AuthModeToggle(
    isSignUp: Boolean,
    onToggleAuthMode: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(if (isSignUp) Res.string.auth_have_account else Res.string.auth_new_to_flowix),
            color = AuthTextSecondary,
            fontSize = 16.sp,
            lineHeight = 22.sp,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(if (isSignUp) Res.string.auth_sign_in_now else Res.string.auth_sign_up_now),
            modifier = Modifier.clickable(role = Role.Button, onClick = onToggleAuthMode),
            color = Color.White,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
internal fun AuthSecondaryButton(
    text: String,
    enabled: Boolean,
    height: Dp,
    scale: Float,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        enabled = enabled,
        shape = AuthControlShape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AuthSecondaryButtonBackground,
            contentColor = AuthTextPrimary,
            disabledContainerColor = AuthSecondaryButtonBackground.copy(alpha = 0.45f),
            disabledContentColor = AuthTextPrimary.copy(alpha = 0.55f),
        ),
    ) {
        Text(
            text = text,
            fontSize = (16f * scale).sp,
            lineHeight = (21f * scale).sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

private fun LayoutCoordinates.boundsInRoot(): Rect {
    val position = positionInRoot()
    return Rect(
        left = position.x,
        top = position.y,
        right = position.x + size.width,
        bottom = position.y + size.height,
    )
}
