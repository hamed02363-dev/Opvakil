package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.VakilTopBar
import com.example.ui.theme.StatusEmerald
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.WorkspaceUiState
import com.example.util.PersianDateUtils

@Composable
fun SettingsScreen(
    state: WorkspaceUiState,
    viewModel: MainViewModel,
    onMenuClick: () -> Unit
) {
    val user = state.currentUser ?: return
    var showChangePassword by remember { mutableStateOf(false) }
    var securityQuestion by remember(user.id) { mutableStateOf(user.securityQuestion) }
    var newSecurityAnswer by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            VakilTopBar(
                title = "تنظیمات و امنیت حساب",
                subtitle = user.fullName,
                onMenuClick = onMenuClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Profile Summary
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("مشخصات کاربری", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        HorizontalDivider()
                        Text("نام: ${user.fullName}")
                        Text("شناسه: ${user.identifier}")
                        Text("نقش: ${user.role.titleFa}")
                        if (user.barLicenseNumber.isNotBlank()) {
                            Text("پروانه: ${user.barLicenseNumber}")
                        }
                        Text(
                            "آخرین ورود: ${PersianDateUtils.formatShamsiDateTime(user.lastLoginAt)}",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Appearance
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("حالت تیره (Dark Mode)", fontWeight = FontWeight.SemiBold)
                                Text("تغییر تم روشن و تیره برنامه", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = state.isDarkTheme,
                            onCheckedChange = { viewModel.toggleDarkTheme(it) },
                            modifier = Modifier.testTag("dark_theme_switch")
                        )
                    }
                }
            }

            // Security & Offline Encryption
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EnhancedEncryption, contentDescription = null, tint = StatusEmerald)
                            Spacer(Modifier.width(8.dp))
                            Text("امنیت و رمزگذاری آفلاین", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "کلیه رمزهای عبور با الگوریتم SHA-256 نمک‌دار و یادداشت‌های محرمانه پرونده‌ها با رمزنگاری AES-256 روی حافظه دستگاه محافظت می‌شوند.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text("ورود دومرحله‌ای آفلاین (OTP)", fontWeight = FontWeight.SemiBold)
                                    Text("صدور کد ۶ رقمی یک‌بارمصرف هنگام ورود", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = user.twoFactorEnabled,
                                onCheckedChange = { enabled ->
                                    viewModel.updateSecuritySettings(enabled, securityQuestion, null)
                                }
                            )
                        }

                        OutlinedTextField(
                            value = securityQuestion,
                            onValueChange = { securityQuestion = it },
                            label = { Text("سوال امنیتی بازیابی رمز") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newSecurityAnswer,
                            onValueChange = { newSecurityAnswer = it },
                            label = { Text("پاسخ جدید سوال امنیتی (در صورت تغییر)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                viewModel.updateSecuritySettings(
                                    user.twoFactorEnabled,
                                    securityQuestion,
                                    newSecurityAnswer.takeIf { it.isNotBlank() }
                                )
                                newSecurityAnswer = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("به‌روزرسانی سوال امنیتی")
                        }

                        OutlinedButton(
                            onClick = { showChangePassword = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.LockReset, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("تغییر رمز عبور")
                        }
                    }
                }
            }

            // Logout
            item {
                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("logout_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("خروج از حساب کاربری")
                }
            }
        }
    }

    if (showChangePassword) {
        var currentPwd by remember { mutableStateOf("") }
        var newPwd by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showChangePassword = false }) {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("تغییر رمز عبور", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = currentPwd,
                        onValueChange = { currentPwd = it },
                        label = { Text("رمز عبور فعلی") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPwd,
                        onValueChange = { newPwd = it },
                        label = { Text("رمز عبور جدید (حداقل ۴ کاراکتر)") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showChangePassword = false }) { Text("انصراف") }
                        Button(
                            enabled = currentPwd.isNotBlank() && newPwd.length >= 4,
                            onClick = {
                                viewModel.changePassword(currentPwd, newPwd)
                                showChangePassword = false
                            }
                        ) { Text("ذخیره") }
                    }
                }
            }
        }
    }
}
