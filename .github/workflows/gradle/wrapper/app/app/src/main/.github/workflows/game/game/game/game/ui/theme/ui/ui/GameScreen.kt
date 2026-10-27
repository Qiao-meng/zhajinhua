package com.example.zhajinhua.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zhajinhua.game.*
import com.example.zhajinhua.ui.theme.CardBrown
import com.example.zhajinhua.ui.theme.Cream
import com.example.zhajinhua.ui.theme.TableGreen
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    botCount: Int,
    difficulty: Difficulty,
    onExit: () -> Unit
) {
    val engine = remember { GameEngine(botCount, difficulty) }
    var tick by remember { mutableIntStateOf(0) }
    var showResult by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }

    // 开局
    LaunchedEffect(Unit) {
        engine.startRound()
        tick++
    }

    // 机器人回合自动行动
    LaunchedEffect(tick, engine.phase, engine.currentPlayerIndex) {
        val p = engine.players.getOrNull(engine.currentPlayerIndex)
        if (engine.phase == GamePhase.BOT_TURN && p != null && p.isBot && !p.isFolded) {
            delay(800L + (0..700).random())
            val action = botDecision(
                p.hand!!, p.difficulty, engine.currentBet, p.chips, engine.pot
            )
            when (action) {
                BotAction.CALL -> engine.call()
                BotAction.RAISE -> engine.raise()
                BotAction.FOLD -> engine.fold()
                BotAction.COMPARE -> {
                    val loser = engine.compare()
                    if (loser == null) engine.advanceTurn()
                }
            }
            tick++
        }
    }

    // 结算检测
    LaunchedEffect(tick, engine.phase) {
        if (engine.phase == GamePhase.ROUND_END) {
            resultText = engine.lastMessage
            showResult = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(TableGreen, Color(0xFF1B5E20)))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部状态
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("底池: ${engine.pot}", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                Text(
                    "注: ${engine.currentBet} · 轮 ${engine.round + 1}/${engine.maxRounds}",
                    color = Color.White,
                    fontSize = 13.sp
                )
                TextButton(onClick = onExit) { Text("退出", color = Color.White) }
            }

            Spacer(Modifier.height(8.dp))

            // 机器人区
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                engine.players.filter { it.isBot }.forEach { bot ->
                    QBotAvatar(
                        player = bot,
                        isActive = engine.currentPlayerIndex == bot.id &&
                            engine.phase == GamePhase.BOT_TURN
                    )
                }
            }

            Spacer(Modifier.weight(0.3f))

            // 牌桌中央信息
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    engine.lastMessage,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            Spacer(Modifier.weight(0.5f))

            // 玩家手牌
            val me = engine.players[0]
            Text(
                "${me.name} · 筹码 ${me.chips}",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy((-12).dp)) {
                me.cards.forEachIndexed { idx, card ->
                    PokeCardView(
                        card = card,
                        faceUp = me.hasLooked || engine.phase == GamePhase.ROUND_END,
                        rotation = (idx - 1) * 5f
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 操作按钮
            if (engine.phase == GamePhase.PLAYER_TURN) {
                val canAct = !me.isFolded
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ActionButton("看牌", Color(0xFFFF9800), enabled = !me.hasLooked && canAct) {
                        engine.lookCards(); tick++
                    }
                    ActionButton("跟注", Color(0xFF4CAF50), enabled = canAct) {
                        engine.call(); tick++
                    }
                    ActionButton("加注", Color(0xFFF44336), enabled = canAct) {
                        engine.raise(); tick++
                    }
                    ActionButton("比牌", Color(0xFF9C27B0), enabled = canAct && engine.activePlayers().size > 1) {
                        val loser = engine.compare()
                        if (loser == null) engine.advanceTurn()
                        tick++
                    }
                    ActionButton("弃牌", Color(0xFF757575), enabled = canAct) {
                        engine.fold(); tick++
                    }
                }
            } else {
                Text(
                    if (engine.phase == GamePhase.BOT_TURN) "机器人思考中…" else "",
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.height(56.dp)
                )
            }
        }

        // 结算弹窗
        if (showResult) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("本局结束") },
                text = { Text(resultText) },
                confirmButton = {
                    TextButton(onClick = {
                        showResult = false
                        engine.startRound()
                        tick++
                    }) { Text("再来一局") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showResult = false
                        onExit()
                    }) { Text("返回") }
                }
            )
        }
    }
}

@Composable
private fun QBotAvatar(player: Player, isActive: Boolean) {
    val scale by animateFloatAsState(if (isActive) 1.1f else 1f, label = "scale")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size((56 * scale).dp)
                .clip(CircleShape)
                .background(if (isActive) Color(0xFFFFD700) else Color(0xFFBDBDBD))
                .border(3.dp, CardBrown, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                player.name.take(1),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(player.name, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Text("${player.chips}", fontSize = 11.sp, color = Color(0xFFFFD54F))
        if (player.isFolded) {
            Text("已弃牌", fontSize = 10.sp, color = Color(0xFFEF9A9A))
        }
    }
}

@Composable
private fun PokeCardView(card: PokerCard, faceUp: Boolean, rotation: Float) {
    Box(
        modifier = Modifier
            .size(width = 64.dp, height = 90.dp)
            .rotate(rotation)
            .clip(RoundedCornerShape(8.dp))
            .background(if (faceUp) Cream else Color(0xFFB71C1C))
            .border(2.dp, CardBrown, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.TopStart
    ) {
        if (faceUp) {
            Column(modifier = Modifier.padding(6.dp)) {
                Text(
                    card.displayValue,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (card.isRed) Color(0xFFD32F2F) else Color(0xFF212121)
                )
                Text(
                    card.displaySuit,
                    fontSize = 18.sp,
                    color = if (card.isRed) Color(0xFFD32F2F) else Color(0xFF212121)
                )
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("🂠", fontSize = 36.sp, color = Color(0xFFFFCDD2))
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .weight(1f)
            .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = color.copy(alpha = 0.3f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
