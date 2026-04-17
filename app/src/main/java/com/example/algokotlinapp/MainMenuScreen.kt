package com.example.algokotlinapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.algokotlinapp.ui.theme.TsuBluePrimary

@Composable
fun MainMenuScreen(modifier: Modifier = Modifier, onNavigate: (String) -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(0.dp))
        Image(painter = painterResource(id = R.drawable.tsulogo11), contentDescription = null, modifier = Modifier.size(350.dp))
        Text(stringResource(R.string.app_title), fontSize = 40.sp, fontWeight = FontWeight.Bold, color = TsuBluePrimary, modifier = Modifier.padding(bottom = 1.dp))
        Button(onClick = { onNavigate("Map") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text(stringResource(R.string.menu_map), fontSize = 16.sp) }
        Button(onClick = { onNavigate("Route") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text(stringResource(R.string.menu_route), fontSize = 16.sp) }
        Button(onClick = { onNavigate("Food") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text(stringResource(R.string.menu_food), fontSize = 16.sp) }
        Button(onClick = { onNavigate("KMeans") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text(stringResource(R.string.menu_kmeans), fontSize = 16.sp) }
        Button(onClick = { onNavigate("Coworking") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text(stringResource(R.string.menu_coworking), fontSize = 16.sp) }
        Button(onClick = { onNavigate("Tree") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text(stringResource(R.string.menu_tree), fontSize = 16.sp) }
        Button(onClick = { onNavigate("NeuralNet") }, modifier = Modifier.fillMaxWidth(0.8f).padding(4.dp)) { Text(stringResource(R.string.menu_neuralnet), fontSize = 16.sp) }
    }
}
