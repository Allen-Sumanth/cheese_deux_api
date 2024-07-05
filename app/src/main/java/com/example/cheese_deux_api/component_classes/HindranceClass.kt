package com.example.cheese_deux_api.component_classes

data class HindranceClass(
    val type: HindranceType = HindranceType.NONE,
    val amount: Int = 0,
)

enum class HindranceType {
    NONE,
    SPEEDUP,
    AUTOJUMP,
    CAT_CLOSER
}