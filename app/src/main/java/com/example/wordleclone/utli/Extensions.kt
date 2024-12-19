package com.example.wordleclone.utli

inline fun <R : Any> R.applyWhen(
    condition: Boolean,
    block: R.() -> R,
): R = applyChoice(condition = condition, trueBlock = block, falseBlock = { this })

inline fun <R : Any> R.applyChoice(
    condition: Boolean,
    trueBlock: R.() -> R,
    falseBlock: R.() -> R,
): R {
    return if (condition) {
        trueBlock()
    } else {
        falseBlock()
    }
}
