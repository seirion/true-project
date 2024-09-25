package com.trueedu.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.AuthRemote
import com.trueedu.project.ui.theme.TrueProjectTheme
import com.trueedu.project.ui.topbar.TopBar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var local: Local
    @Inject
    lateinit var authRemote: AuthRemote

    private val vm by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm.init()
        enableEdgeToEdge()
        setContent {
            TrueProjectTheme {
                Scaffold(
                    topBar = {
                        TopBar()
                    },
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TrueProjectTheme {
        Greeting("Android")
    }
}