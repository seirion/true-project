package com.trueedu.project

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.project.data.ScreenControl
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.AuthRemote
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.ranking.VolumeRankingFragment
import com.trueedu.project.ui.theme.TrueProjectTheme
import com.trueedu.project.ui.topbar.TopBar
import com.trueedu.project.ui.view.SettingFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var local: Local
    @Inject
    lateinit var screen: ScreenControl
    @Inject
    lateinit var authRemote: AuthRemote

    private val vm by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm.init()
        enableEdgeToEdge()
        setContent {
            TrueProjectTheme(
                n = screen.theme.intValue,
                forceDark = screen.forceDark.value
            ) {
                Scaffold(
                    topBar = {
                        TopBar(::onSetting)
                    },
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        HomeItem(
                            name = "거래량 순위",
                            ::gotoVolumeRanking
                        )
                    }
                }
            }
        }
    }

    private fun onSetting() {
        SettingFragment.show(supportFragmentManager)
    }

    private fun gotoVolumeRanking() {
        VolumeRankingFragment.show(supportFragmentManager)
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeItem(
    name: String = "종목 보기",
    onClick: () -> Unit = {}
) {
    BasicText(
        s = name,
        fontSize = 18,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp)
    )
    HorizontalDivider()
}
