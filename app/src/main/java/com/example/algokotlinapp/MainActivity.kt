package com.example.algokotlinapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

import com.example.algokotlinapp.ui.theme.AlgoKotlinAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlgoKotlinAppTheme {
                var currentScreen by remember { mutableStateOf("MainMenu") }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AnimatedContent(
                        targetState = currentScreen,
                        label = "screen_transition",
                        transitionSpec = {
                            if (targetState != "MainMenu" && initialState == "MainMenu") {
                                slideInHorizontally(animationSpec = tween(400), initialOffsetX = { it }) + fadeIn(tween(400)) togetherWith
                                        slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { -it / 2 }) + fadeOut(tween(400))
                            } else {
                                slideInHorizontally(animationSpec = tween(400), initialOffsetX = { -it / 2 }) + fadeIn(tween(400)) togetherWith
                                        slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { it }) + fadeOut(tween(400))
                            }
                        }
                    ) { targetScreen ->
                        when (targetScreen) {
                            "MainMenu"  -> MainMenuScreen(modifier = Modifier.padding(innerPadding), onNavigate = { currentScreen = it })
                            "Map"       -> CampusMapScreen(Modifier.padding(innerPadding)) { currentScreen = "MainMenu" }
                            "Route"     -> RouteScreen(Modifier.padding(innerPadding)) { currentScreen = "MainMenu" }
                            "Food"      -> FoodScreen(Modifier.padding(innerPadding)) { currentScreen = "MainMenu" }
                            "Coworking" -> CoworkingScreen(Modifier.padding(innerPadding)) { currentScreen = "MainMenu" }
                            "NeuralNet" -> NeuralNetScreen(Modifier.padding(innerPadding)) { currentScreen = "MainMenu" }
                            "KMeans"    -> KMeansScreen(Modifier.padding(innerPadding)) { currentScreen = "MainMenu" }
                            "Tree"      -> DecisionTreeScreen(Modifier.padding(innerPadding)) { currentScreen = "MainMenu" }
                        }
                    }
                }
            }
        }
    }
}
