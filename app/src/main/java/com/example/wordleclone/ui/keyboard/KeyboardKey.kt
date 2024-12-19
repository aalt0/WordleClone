package com.example.wordleclone.ui.keyboard

val TOP_ROW = KeyboardKey.entries.filter { it.row == 1 }
val MID_ROW = KeyboardKey.entries.filter { it.row == 2 }
val BOTTOM_ROW = KeyboardKey.entries.filter { it.row == 3 }

enum class KeyboardKey(val row: Int) {
    Q(1), W(1), E(1), R(1), T(1), Y(1), U(1), I(1), O(1), P(1),
    A(2), S(2), D(2), F(2), G(2), H(2), J(2), K(2), L(2),
    ENTER(3), Z(3), X(3), C(3), V(3), B(3), N(3), M(3), DEL(3);
}
