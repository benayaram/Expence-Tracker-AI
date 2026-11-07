package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Deep Obsidian Dark Aesthetic Palette
val ObsidianBackground = Color(0xFF0B0F19)
val ObsidianSurface = Color(0xFF111827)
val ObsidianCard = Color(0xFF161F30)
val ObsidianCardElevated = Color(0xFF1E293B)
val ObsidianBorder = Color(0xFF22304A)

// High-Contrast Neon Accents
val LuminaIndigo = Color(0xFF6366F1)
val LuminaIndigoLight = Color(0xFF818CF8)
val LuminaIndigoContainer = Color(0xFF1E1B4B)

val LuminaEmerald = Color(0xFF10B981)
val LuminaEmeraldLight = Color(0xFF34D399)
val LuminaEmeraldContainer = Color(0xFF064E3B)

val LuminaCrimson = Color(0xFFF43F5E)
val LuminaCrimsonLight = Color(0xFFFB7185)
val LuminaCrimsonContainer = Color(0xFF4C0519)

val LuminaAmber = Color(0xFFF59E0B)
val LuminaAmberLight = Color(0xFFFBBF24)
val LuminaAmberContainer = Color(0xFF451A03)

val LuminaCyan = Color(0xFF06B6D4)
val LuminaPurple = Color(0xFFA855F7)
val LuminaPink = Color(0xFFEC4899)

// Text Hierarchy
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// Preset Category Swatches
val CategoryColorSwatches = listOf(
    "#6366F1", // Indigo
    "#10B981", // Emerald
    "#F59E0B", // Amber
    "#F43F5E", // Crimson Rose
    "#06B6D4", // Cyan
    "#A855F7", // Purple
    "#EC4899", // Pink
    "#3B82F6", // Blue
    "#14B8A6", // Teal
    "#84CC16"  // Lime
)

fun parseHexColor(hex: String, fallback: Color = LuminaIndigo): Color {
    return try {
        val cleaned = hex.trim().removePrefix("#")
        val longVal = when (cleaned.length) {
            6 -> cleaned.toLong(16) or 0xFF000000L
            8 -> cleaned.toLong(16)
            else -> return fallback
        }
        Color(longVal)
    } catch (_: Exception) {
        fallback
    }
}
