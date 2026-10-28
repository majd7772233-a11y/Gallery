package com.example

import android.app.LocaleManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SettingsViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {
  private val settingsViewModel: SettingsViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val themeState by settingsViewModel.theme.collectAsState()
      val languageState by settingsViewModel.language.collectAsState()
      
      LaunchedEffect(languageState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = getSystemService(LocaleManager::class.java)
            localeManager.applicationLocales = LocaleList.forLanguageTags(languageState)
        }
      }

      val isDarkTheme = when (themeState) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
      }

      val locale = remember(languageState) { Locale(languageState) }
      Locale.setDefault(locale)

      val configuration = LocalConfiguration.current
      val activityContext = this@MainActivity
      val localizedContext = remember(languageState, configuration) {
        val config = Configuration(configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
        }
        val configContext = activityContext.createConfigurationContext(config)
        object : ContextWrapper(activityContext) {
            override fun getResources(): Resources {
                return configContext.resources
            }
        }
      }

      val layoutDirection = if (languageState == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

      CompositionLocalProvider(
          LocalContext provides localizedContext,
          LocalLayoutDirection provides layoutDirection
      ) {
        MyApplicationTheme(darkTheme = isDarkTheme) {
          MainScreen(settingsViewModel = settingsViewModel)
        }
      }
    }
  }
}
