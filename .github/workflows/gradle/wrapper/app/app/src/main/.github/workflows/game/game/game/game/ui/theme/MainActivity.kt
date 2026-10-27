package com.example.zhajinhua

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.zhajinhua.game.Difficulty
import com.example.zhajinhua.ui.GameScreen
import com.example.zhajinhua.ui.SetupScreen
import com.example.zhajinhua.ui.theme.ZhajinhuaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZhajinhuaTheme {
                var started by remember { mutableStateOf(false) }
                var botCount by remember { mutableIntStateOf(3) }
                var difficulty by remember { mutableStateOf(Difficulty.NORMAL) }

                if (!started) {
                    SetupScreen(
                        botCount = botCount,
                        difficulty = difficulty,
                        onBotCountChange = { botCount = it },
                        onDifficultyChange = { difficulty = it },
                        onStart = { started = true }
                    )
                } else {
                    GameScreen(
                        botCount = botCount,
                        difficulty = difficulty,
                        onExit = { started = false }
                    )
                }
            }
        }
    }
}
