package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.repository.VakilRepository
import com.example.reminder.ReminderReceiver
import com.example.util.SecurityUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val pendingOtpUser: UserEntity? = null,
    val generatedOtpCode: String? = null
)

class AuthViewModel(
    private val repository: VakilRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, infoMessage = null)
    }

    fun login(context: Context, identifier: String, password: String) {
        if (identifier.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "لطفاً شماره موبایل/ایمیل و رمز عبور را وارد کنید.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, infoMessage = null)
            val result = repository.authenticate(identifier, password)
            result.onSuccess { user ->
                if (user.twoFactorEnabled) {
                    val otp = SecurityUtils.generateOtpCode()
                    // Show offline local notification with OTP so user can verify without internet
                    runCatching {
                        ReminderReceiver.showNotification(
                            context = context,
                            id = 9001,
                            title = "کد ورود دومرحلهای وکیل‌یار",
                            message = "کد تأیید آفلاین شما: $otp"
                        )
                    }
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        pendingOtpUser = user,
                        generatedOtpCode = otp,
                        infoMessage = "کد تأیید دومرحله‌ای تولید شد (در اعلان گوشی و کادر زیر قابل مشاهده است)."
                    )
                } else {
                    repository.completeLogin(user)
                    _uiState.value = AuthUiState(isLoading = false)
                }
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "خطا در ورود به حساب کاربری"
                )
            }
        }
    }

    fun verifyOtp(enteredCode: String) {
        val state = _uiState.value
        val pending = state.pendingOtpUser ?: return
        if (enteredCode.trim() == state.generatedOtpCode) {
            viewModelScope.launch {
                repository.completeLogin(pending)
                _uiState.value = AuthUiState()
            }
        } else {
            _uiState.value = state.copy(errorMessage = "کد تأیید واردشده صحیح نیست.")
        }
    }

    fun cancelOtp() {
        _uiState.value = AuthUiState()
    }

    fun register(
        fullName: String,
        identifier: String,
        password: String,
        role: UserRole,
        barLicense: String,
        securityQuestion: String,
        securityAnswer: String,
        autoLogin: Boolean = true
    ) {
        if (fullName.isBlank() || identifier.isBlank() || password.length < 4 || securityAnswer.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "تمامی فیلدهای ضروری و پاسخ سوال امنیتی (رمز حداقل ۴ کاراکتر) را کامل کنید."
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.registerUser(
                fullName = fullName,
                identifier = identifier,
                password = password,
                role = role,
                barLicense = barLicense,
                securityQuestion = securityQuestion,
                securityAnswer = securityAnswer
            )
            result.onSuccess { user ->
                if (autoLogin) {
                    repository.completeLogin(user)
                }
                _uiState.value = AuthUiState(
                    isLoading = false,
                    infoMessage = "حساب کاربری با موفقیت ایجاد شد."
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "خطا در ثبت‌نام"
                )
            }
        }
    }

    fun recoverPassword(identifier: String, securityAnswer: String, newPassword: String) {
        if (identifier.isBlank() || securityAnswer.isBlank() || newPassword.length < 4) {
            _uiState.value = _uiState.value.copy(errorMessage = "اطلاعات بازیابی و رمز عبور جدید (حداقل ۴ کاراکتر) را وارد کنید.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val res = repository.recoverPassword(identifier, securityAnswer, newPassword)
            res.onSuccess {
                _uiState.value = AuthUiState(
                    isLoading = false,
                    infoMessage = "رمز عبور شما با موفقیت تغییر یافت. اکنون می‌توانید وارد شوید."
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "خطا در بازیابی رمز عبور"
                )
            }
        }
    }
}
