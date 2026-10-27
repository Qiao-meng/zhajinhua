package com.example.zhajinhua.game

enum class HandType {
    HIGH_CARD, PAIR, STRAIGHT, FLUSH, STRAIGHT_FLUSH, THREE_OF_A_KIND, SPECIAL_235
}

data class HandResult(val type: HandType, val sortedValues: List<Int>) {
    val strength: Int
        get() {
            var base = when (type) {
                HandType.HIGH_CARD -> 10
                HandType.PAIR -> 30
                HandType.STRAIGHT -> 50
                HandType.FLUSH -> 60
                HandType.STRAIGHT_FLUSH -> 80
                HandType.THREE_OF_A_KIND -> 95
                HandType.SPECIAL_235 -> 85
            }
            if (type == HandType.HIGH_CARD) {
                base += ((sortedValues[0] - 10) * 0.5).toInt()
            }
            return base.coerceIn(0, 100)
        }
}

fun evaluateHand(cards: List<PokerCard>): HandResult {
    require(cards.size == 3)
    val values = cards.map { it.value }.sortedDescending()
    val suits = cards.map { it.suit }
    val isFlush = suits[0] == suits[1] && suits[1] == suits[2]
    val isStraight = checkStraight(values)
    val isPair = values[0] == values[1] || values[1] == values[2]
    val isTrips = values[0] == values[1] && values[1] == values[2]
    val isSpecial = !isFlush && values[0] == 5 && values[1] == 3 && values[2] == 2

    return when {
        isSpecial -> HandResult(HandType.SPECIAL_235, values)
        isTrips -> HandResult(HandType.THREE_OF_A_KIND, values)
        isFlush && isStraight -> HandResult(HandType.STRAIGHT_FLUSH, values)
        isFlush -> HandResult(HandType.FLUSH, values)
        isStraight -> HandResult(HandType.STRAIGHT, values)
        isPair -> {
            val pairValue = if (values[0] == values[1]) values[0] else values[1]
            val kicker = if (values[0] == values[1]) values[2] else values[0]
            HandResult(HandType.PAIR, listOf(pairValue, pairValue, kicker))
        }
        else -> HandResult(HandType.HIGH_CARD, values)
    }
}

private fun checkStraight(sortedDesc: List<Int>): Boolean {
    if (sortedDesc[0] == 14 && sortedDesc[1] == 3 && sortedDesc[2] == 2) return true // A23
    return sortedDesc[0] - 1 == sortedDesc[1] && sortedDesc[1] - 1 == sortedDesc[2]
}

/** 返回 1: a赢, -1: b赢, 0: 平(主动方输) */
fun compareHands(a: HandResult, b: HandResult, challengerIsA: Boolean): Int {
    if (a.type == HandType.SPECIAL_235 && b.type == HandType.THREE_OF_A_KIND) return 1
    if (b.type == HandType.SPECIAL_235 && a.type == HandType.THREE_OF_A_KIND) return -1

    if (a.type != b.type) return if (a.type.ordinal > b.type.ordinal) 1 else -1

    for (i in 0 until 3) {
        if (a.sortedValues[i] != b.sortedValues[i]) {
            return if (a.sortedValues[i] > b.sortedValues[i]) 1 else -1
        }
    }
    return if (challengerIsA) -1 else 1
}
