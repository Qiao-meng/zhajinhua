package com.example.zhajinhua.game

import kotlin.random.Random

enum class Difficulty { EASY, NORMAL, HARD }

enum class BotAction { CALL, RAISE, FOLD, COMPARE }

fun botDecision(
    hand: HandResult,
    difficulty: Difficulty,
    currentBet: Int,
    myChips: Int,
    pot: Int
): BotAction {
    val strength = hand.strength
    val rnd = Random.nextDouble()

    return when (difficulty) {
        Difficulty.EASY -> when {
            strength < 25 -> if (rnd < 0.7) BotAction.FOLD else BotAction.CALL
            strength > 70 && rnd < 0.3 -> BotAction.RAISE
            else -> BotAction.CALL
        }
        Difficulty.NORMAL -> when {
            strength < 20 -> if (rnd < 0.5) BotAction.FOLD else BotAction.CALL
            strength > 60 -> if (rnd < 0.4 && currentBet * 2 <= myChips) BotAction.RAISE else BotAction.CALL
            rnd < 0.1 && currentBet * 2 <= myChips -> BotAction.RAISE
            else -> BotAction.CALL
        }
        Difficulty.HARD -> {
            val potOdds = currentBet.toDouble() / (pot + currentBet).coerceAtLeast(1)
            when {
                strength < 15 -> BotAction.FOLD
                strength > 55 -> when {
                    rnd < 0.5 && currentBet * 2 <= myChips -> BotAction.RAISE
                    rnd < 0.7 && currentBet * 2 <= myChips -> BotAction.COMPARE
                    else -> BotAction.CALL
                }
                potOdds < 0.1 && strength > 15 -> BotAction.CALL
                rnd < 0.15 && currentBet * 2 <= myChips -> BotAction.RAISE
                else -> BotAction.CALL
            }
        }
    }
}
