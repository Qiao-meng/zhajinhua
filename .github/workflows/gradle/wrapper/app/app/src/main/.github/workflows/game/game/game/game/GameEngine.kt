package com.example.zhajinhua.game

import kotlin.random.Random

data class Player(
    val id: Int,
    val name: String,
    val isBot: Boolean,
    val difficulty: Difficulty = Difficulty.NORMAL,
    var chips: Int = 1000,
    var cards: List<PokerCard> = emptyList(),
    var hand: HandResult? = null,
    var hasLooked: Boolean = false,
    var isFolded: Boolean = false,
    var currentRoundBet: Int = 0
)

enum class GamePhase { WAITING, PLAYER_TURN, BOT_TURN, ROUND_END, GAME_OVER }

class GameEngine(
    val botCount: Int,
    val difficulty: Difficulty,
    val initialChips: Int = 1000,
    val baseBet: Int = 10
) {
    var players: MutableList<Player> = mutableListOf()
    var deck: MutableList<PokerCard> = mutableListOf()
    var pot: Int = 0
    var currentBet: Int = baseBet
    var currentPlayerIndex: Int = 0
    var phase: GamePhase = GamePhase.WAITING
    var lastMessage: String = ""
    var round: Int = 0
    var maxRounds: Int = 6

    init {
        setupPlayers()
    }

    private fun setupPlayers() {
        players.clear()
        players.add(Player(0, "你", false, chips = initialChips))
        val names = listOf("阿福", "小美", "老张", "阿强")
        for (i in 1..botCount) {
            players.add(
                Player(i, names[i - 1], true, difficulty, chips = initialChips)
            )
        }
    }

    fun startRound() {
        deck = buildDeck().shuffled().toMutableList()
        pot = 0
        currentBet = baseBet
        round = 0
        players.forEach { p ->
            p.cards = listOf(deck.removeAt(0), deck.removeAt(0), deck.removeAt(0))
            p.hand = evaluateHand(p.cards)
            p.hasLooked = false
            p.isFolded = false
            p.currentRoundBet = 0
            // 扣底注
            val bet = minOf(baseBet, p.chips)
            p.chips -= bet
            p.currentRoundBet += bet
            pot += bet
        }
        currentPlayerIndex = 0
        phase = GamePhase.PLAYER_TURN
        lastMessage = "新一局开始，底注 $baseBet"
    }

    private fun buildDeck(): List<PokerCard> {
        val list = mutableListOf<PokerCard>()
        for (suit in 0..3) for (v in 2..14) list.add(PokerCard(v, suit))
        return list
    }

    fun activePlayers(): List<Player> = players.filter { !it.isFolded }

    fun lookCards() {
        val p = players[currentPlayerIndex]
        p.hasLooked = true
        lastMessage = "${p.name} 看了牌"
    }

    fun call() {
        val p = players[currentPlayerIndex]
        val amount = if (p.hasLooked) currentBet else currentBet / 2
        val actual = minOf(amount, p.chips)
        p.chips -= actual
        p.currentRoundBet += actual
        pot += actual
        lastMessage = "${p.name} 跟注 $actual"
        advanceTurn()
    }

    fun raise() {
        val p = players[currentPlayerIndex]
        currentBet += baseBet
        val amount = if (p.hasLooked) currentBet else currentBet / 2
        val actual = minOf(amount, p.chips)
        p.chips -= actual
        p.currentRoundBet += actual
        pot += actual
        lastMessage = "${p.name} 加注到 $currentBet"
        advanceTurn()
    }

    fun fold() {
        val p = players[currentPlayerIndex]
        p.isFolded = true
        lastMessage = "${p.name} 弃牌"
        advanceTurn()
    }

    /** 主动方与下一个未弃牌玩家比牌 */
    fun compare(): Player? {
        val p = players[currentPlayerIndex]
        val cost = currentBet * 2
        val actual = minOf(cost, p.chips)
        p.chips -= actual
        pot += actual

        val opponents = activePlayers().filter { it.id != p.id }
        if (opponents.isEmpty()) return null
        val target = opponents.random()
        val result = compareHands(p.hand!!, target.hand!!, true)

        return if (result == 1) {
            target.isFolded = true
            lastMessage = "${p.name} 比牌赢了 ${target.name}"
            null
        } else {
            p.isFolded = true
            lastMessage = "${p.name} 比牌输了 ${target.name}"
            p
        }
    }

    fun advanceTurn() {
        val active = activePlayers()
        if (active.size <= 1) {
            phase = GamePhase.ROUND_END
            lastMessage = "${active.firstOrNull()?.name ?: "无人"} 获胜，赢得底池 $pot"
            active.firstOrNull()?.chips = active.firstOrNull()?.chips?.plus(pot) ?: 0
            pot = 0
            return
        }

        // 找下一个未弃牌玩家
        var next = (currentPlayerIndex + 1) % players.size
        var guard = 0
        while (players[next].isFolded && guard < players.size) {
            next = (next + 1) % players.size
            guard++
        }
        if (next == 0) round++
        if (round >= maxRounds) {
            // 强制亮牌
            forceShowdown()
            return
        }
        currentPlayerIndex = next
        phase = if (players[next].isBot) GamePhase.BOT_TURN else GamePhase.PLAYER_TURN
    }

    private fun forceShowdown() {
        val active = activePlayers()
        if (active.isEmpty()) return
        val winner = active.maxByOrNull { it.hand!!.strength } ?: return
        winner.chips += pot
        lastMessage = "强制亮牌，${winner.name} 以 ${winner.hand!!.type} 获胜，赢得 $pot"
        pot = 0
        phase = GamePhase.ROUND_END
    }
}
