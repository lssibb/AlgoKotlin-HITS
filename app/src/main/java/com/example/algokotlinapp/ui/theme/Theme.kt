package com.example.algokotlinapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.algokotlinapp.R


val GolosFontFamily = FontFamily(
    Font(resId = R.font.golostextregular, weight = FontWeight.Normal),
    Font(resId = R.font.golostextmedium, weight = FontWeight.Medium),
    Font(resId = R.font.golostextbold, weight = FontWeight.Bold)
)

private val DarkColorScheme = darkColorScheme(
    primary = TsuBluePrimary,
    secondary = TsuBlueSecondary,
    tertiary = TsuBluePrimary
)

private val LightColorScheme = lightColorScheme(
    primary = TsuBluePrimary,
    secondary = TsuBlueSecondary,
    tertiary = TsuBluePrimary,
    background = TsuBackgroundLight,
    surface = PureWhite,
    onPrimary = PureWhite,
    onBackground = TsuTextDark,
    onSurface = TsuTextDark
)

@Composable
fun AlgoKotlinAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}