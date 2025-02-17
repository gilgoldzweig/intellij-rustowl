package jp.s6n.idea.rustowl.configuration

import com.intellij.ui.JBColor
import java.awt.Color

private const val COLOR_PREFIX = "rustowl.color."

enum class DecorationType(
    val displayText: String,
    private val defaultLightRgb: Int,
    private val defaultDarkRgb: Int
) {
    LIFETIME("Lifetime", 0x47EA54, 0x47EA54),
    IMM_BORROW("Immutable Borrow", 0x4762EA, 0x4762EA),
    MUT_BORROW("Mutable Borrow", 0xEA47EA, 0xEA47EA),
    CALL("Function Call", 0xEAA647, 0xEAA647),
    MOVE("Move", 0xEAA647, 0xEAA647),
    OUTLIVE("Outlives Relationship", 0xEA4747, 0xEA4747);

    val colorName = "$COLOR_PREFIX$name"
    val defaultColor: JBColor = JBColor.namedColor(colorName, defaultLightRgb, defaultDarkRgb)

    companion object {
        fun fromString(value: String): DecorationType? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }

    }
}


