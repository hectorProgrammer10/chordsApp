package com.magodeescalas.chordsapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.magodeescalas.chordsapp.ui.theme.ChordsappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = com.magodeescalas.chordsapp.data.repository.MusicRepositoryImpl()
        val viewModelFactory =
                com.magodeescalas.chordsapp.presentation.viewmodel.MainViewModel.Factory(repository)
        val viewModel =
                androidx.lifecycle.ViewModelProvider(this, viewModelFactory)[
                        com.magodeescalas.chordsapp.presentation.viewmodel.MainViewModel::class.java]

        setContent {
            ChordsappTheme {
                com.magodeescalas.chordsapp.ui.theme.GlassBackground {
                    Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            containerColor =
                                    androidx.compose.ui.graphics.Color
                                            .Transparent // Important: make scaffold transparent so
                            // background shows
                            ) { innerPadding ->
                        com.magodeescalas.chordsapp.presentation.ui.MainScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ChordsappTheme { Greeting("Android") }
}
