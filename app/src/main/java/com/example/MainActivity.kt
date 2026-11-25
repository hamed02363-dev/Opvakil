package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.VakilDatabase
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.VakilRepository
import com.example.ui.theme.VakilYarTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = VakilDatabase.getInstance(applicationContext)
        val prefs = PreferencesRepository(applicationContext)
        val repository = VakilRepository(db, prefs)

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(repository) as T
                    modelClass.isAssignableFrom(MainViewModel::class.java) -> MainViewModel(repository, prefs) as T
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }

        setContent {
            val authViewModel: AuthViewModel = viewModel(factory = factory)
            val mainViewModel: MainViewModel = viewModel(factory = factory)
            val isDarkTheme by mainViewModel.isDarkTheme.collectAsStateWithLifecycle()

            VakilYarTheme(darkTheme = isDarkTheme) {
                VakilApp(
                    authViewModel = authViewModel,
                    mainViewModel = mainViewModel
                )
            }
        }
    }
}
