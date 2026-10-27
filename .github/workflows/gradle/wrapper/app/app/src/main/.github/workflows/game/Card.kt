package com.example.zhajinhua.game

data class PokerCard(val value: Int, val suit: Int) {
    // value: 2-14 (14=A), suit: 0♠ 1♥ 2♣ 3♦
    val displayValue: String
        get() = when {
            value <= 10 -> value.toString()
            else -> listOf("J", "Q", "K", "A")[value - 11]
        }

    val displaySuit: String
        get() = listOf("♠", "♥", "♣", "♦")[suit]

    val isRed: Boolean
        get() = suit == 1 || suit == 3
}
