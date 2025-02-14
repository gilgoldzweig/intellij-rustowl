package jp.s6n.idea.rustowl.configuration

import java.awt.Color

enum class DecorationType(val displayText: String, val defaultColor: Color) {
    LIFETIME("Lifetime", Color(0xCC47EA54.toInt(), true)),
    IMM_BORROW("Immutable Borrow", Color(0xCC4762EA.toInt(), true)),
    MUT_BORROW("Mutable Borrow", Color(0xCCEA47EA.toInt(), true)),
    CALL("Function Call", Color(0xCCEAA647.toInt(), true)),
    MOVE("Move", Color(0xCCEAA647.toInt(), true)), // Shares color with CALL
    OUTLIVE("Outlives Relationship", Color(0xCCEA4747.toInt(), true));

    companion object {
        fun fromString(value: String): DecorationType? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
