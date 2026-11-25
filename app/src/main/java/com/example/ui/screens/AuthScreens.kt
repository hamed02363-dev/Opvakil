package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.UserRole
import com.example.ui.components.DropdownSelectorField
import com.example.ui.theme.BrassSecondary
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusEmerald
import com.example.ui.viewmodel.AuthViewModel

private enum class AuthSubScreen {
    LOGIN, REGISTER, RECOVER
}

@Composable
fun AuthFlowScreen(
    authViewModel: AuthViewModel
) {
    val state by authViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var subScreen by remember { mutableStateOf(AuthSubScreen.LOGIN) }

    if (subScreen != AuthSubScreen.LOGIN) {
        BackHandler {
            authViewModel.clearMessages()
            subScreen = AuthSubScreen.LOGIN
        }
    }
    if (state.pendingOtpUser != null) {
        BackHandler { authViewModel.cancelOtp() }
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_legal_banner_1795617216388),
                        contentDescription = "نمای دفتر وکالت وکیل‌یار",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        NavyPrimary.copy(alpha = 0.55f),
                                        NavyPrimary.copy(alpha = 0.92f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Gavel,
                                contentDescription = null,
                                tint = BrassSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "وکیل‌یار",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "سامانه جامع و ۱۰۰٪ آفلاین مدیریت پرونده‌های وکالت",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.88f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            color = StatusEmerald.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.OfflinePin,
                                    contentDescription = null,
                                    tint = Color(0xFF81E6B9),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "بدون نیاز به اینترنت • ذخیره‌سازی رمزنگاری‌شده روی گوشی",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFD2FAEA)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (state.errorMessage != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = state.errorMessage!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                )
                            }
                        }
                        if (state.infoMessage != null) {
                            Surface(
                                color = StatusEmerald.copy(alpha = 0.14f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = state.infoMessage!!,
                                    color = StatusEmerald,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                )
                            }
                        }

                        if (state.pendingOtpUser != null) {
                            OtpVerificationContent(
                                generatedOtp = state.generatedOtpCode.orEmpty(),
                                onVerify = { authViewModel.verifyOtp(it) },
                                onCancel = { authViewModel.cancelOtp() }
                            )
                        } else {
                            when (subScreen) {
                                AuthSubScreen.LOGIN -> LoginContent(
                                    isLoading = state.isLoading,
                                    onLogin = { id, pwd -> authViewModel.login(context, id, pwd) },
                                    onGoRegister = {
                                        authViewModel.clearMessages()
                                        subScreen = AuthSubScreen.REGISTER
                                    },
                                    onGoRecover = {
                                        authViewModel.clearMessages()
                                        subScreen = AuthSubScreen.RECOVER
                                    }
                                )

                                AuthSubScreen.REGISTER -> RegisterContent(
                                    isLoading = state.isLoading,
                                    onRegister = { name, id, pwd, role, lic, q, a ->
                                        authViewModel.register(name, id, pwd, role, lic, q, a)
                                    },
                                    onBack = {
                                        authViewModel.clearMessages()
                                        subScreen = AuthSubScreen.LOGIN
                                    }
                                )

                                AuthSubScreen.RECOVER -> RecoverContent(
                                    isLoading = state.isLoading,
                                    onRecover = { id, ans, newPwd ->
                                        authViewModel.recoverPassword(id, ans, newPwd)
                                    },
                                    onBack = {
                                        authViewModel.clearMessages()
                                        subScreen = AuthSubScreen.LOGIN
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun LoginContent(
    isLoading: Boolean,
    onLogin: (String, String) -> Unit,
    onGoRegister: () -> Unit,
    onGoRecover: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Text("ورود به دفتر وکالت", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(
        "با شماره موبایل یا ایمیل و رمز عبور وارد شوید.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    OutlinedTextField(
        value = identifier,
        onValueChange = { identifier = it },
        label = { Text("شماره موبایل یا ایمیل") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("login_identifier_input")
    )

    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("رمز عبور") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(
                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = "نمایش رمز"
                )
            }
        },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("login_password_input")
    )

    Button(
        onClick = { onLogin(identifier, password) },
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("login_button")
    ) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
        else Text("ورود به سامانه")
    }

    // Quick sample account selector for immediate offline evaluation
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(10.dp)
    ) {
        Text(
            "حساب‌های آماده تست آفلاین (رمز: 123456):",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
                onClick = {
                    identifier = "09120000001"
                    password = "123456"
                },
                label = { Text("وکیل اصلی") }
            )
            AssistChip(
                onClick = {
                    identifier = "09120000002"
                    password = "123456"
                },
                label = { Text("وکیل همکار") }
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onGoRecover, modifier = Modifier.testTag("forgot_password_button")) {
            Text("فراموشی رمز عبور")
        }
        TextButton(onClick = onGoRegister, modifier = Modifier.testTag("go_register_button")) {
            Text("ثبت‌نام وکیل جدید")
        }
    }
}

@Composable
private fun OtpVerificationContent(
    generatedOtp: String,
    onVerify: (String) -> Unit,
    onCancel: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    Text("ورود دومرحله‌ای آفلاین (OTP)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(
        "کد ۶ رقمی یک‌بارمصرف در اعلان گوشی صادر شد (کد آفلاین: $generatedOtp).",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    OutlinedTextField(
        value = code,
        onValueChange = { code = it },
        label = { Text("کد ۶ رقمی تأیید") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("otp_code_input")
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
            Text("بازگشت")
        }
        Button(
            onClick = { onVerify(code) },
            modifier = Modifier
                .weight(1f)
                .testTag("verify_otp_button")
        ) {
            Text("تأیید و ورود")
        }
    }
}

@Composable
private fun RegisterContent(
    isLoading: Boolean,
    onRegister: (String, String, String, UserRole, String, String, String) -> Unit,
    onBack: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var barLicense by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.LEAD_LAWYER) }
    var securityQuestion by remember { mutableStateOf("نام شهر محل تولد شما چیست؟") }
    var securityAnswer by remember { mutableStateOf("") }

    Text("ایجاد حساب کاربری جدید", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

    OutlinedTextField(
        value = fullName,
        onValueChange = { fullName = it },
        label = { Text("نام و نام خانوادگی") },
        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = identifier,
        onValueChange = { identifier = it },
        label = { Text("شماره موبایل یا ایمیل") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        modifier = Modifier.fillMaxWidth()
    )
    DropdownSelectorField(
        label = "نقش کاربری",
        options = UserRole.entries,
        selected = role,
        optionLabel = { it.titleFa },
        onSelected = { role = it }
    )
    OutlinedTextField(
        value = barLicense,
        onValueChange = { barLicense = it },
        label = { Text("شماره پروانه وکالت (اختیاری)") },
        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("رمز عبور (حداقل ۴ کاراکتر)") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = securityQuestion,
        onValueChange = { securityQuestion = it },
        label = { Text("سوال امنیتی برای بازیابی رمز") },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = securityAnswer,
        onValueChange = { securityAnswer = it },
        label = { Text("پاسخ سوال امنیتی") },
        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
        modifier = Modifier.fillMaxWidth()
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
            Text("بازگشت")
        }
        Button(
            onClick = { onRegister(fullName, identifier, password, role, barLicense, securityQuestion, securityAnswer) },
            enabled = !isLoading,
            modifier = Modifier.weight(1f)
        ) {
            Text("ثبت‌نام")
        }
    }
}

@Composable
private fun RecoverContent(
    isLoading: Boolean,
    onRecover: (String, String, String) -> Unit,
    onBack: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var securityAnswer by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    Text("بازیابی آفلاین رمز عبور", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(
        "پاسخ سوال امنیتی ثبت‌شده هنگام ساخت حساب را وارد نمایید.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    OutlinedTextField(
        value = identifier,
        onValueChange = { identifier = it },
        label = { Text("شماره موبایل یا ایمیل") },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = securityAnswer,
        onValueChange = { securityAnswer = it },
        label = { Text("پاسخ سوال امنیتی") },
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = newPassword,
        onValueChange = { newPassword = it },
        label = { Text("رمز عبور جدید") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
            Text("بازگشت")
        }
        Button(
            onClick = { onRecover(identifier, securityAnswer, newPassword) },
            enabled = !isLoading,
            modifier = Modifier.weight(1f)
        ) {
            Text("تغییر رمز")
        }
    }
}
