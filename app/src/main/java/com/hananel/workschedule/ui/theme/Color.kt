package com.hananel.workschedule.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Brand Colors ────────────────────────────────────────────────────────────
// Change these to instantly update all accent elements app-wide
val AccentIndigo     = Color(0xFF5C6BC0)   // Primary brand — Indigo 400, modern on dark
val AccentIndigoDark = Color(0xFF3949AB)   // Gradient end — Indigo 600

// ─── Semantic Colors ──────────────────────────────────────────────────────────
val BlockedRed  = Color(0xFFEF5350)      // Blocking / Shabbat observer / delete
val CanOnlyBlue = Color(0xFF1976D2)      // "Can only work" restrictions
val Orange      = Color(0xFFFF9800)      // Edit mode / Mitgaber / draft warnings

// ─── Table Colors (replaces hardcoded hex in SimpleTable) ────────────────────
val TableHeaderBg    = Color(0xFF2D3561)   // Deep indigo-navy for table headers
val TableCellBg      = Color(0xFFEEF0F8)   // Pale indigo-tinted white for normal cells
val TableCellBgBlock = Color(0xFFFFEBEE)   // Light red tint for blocking cells
val TableBorderDark  = Color(0xFF1A1E3A)   // Table border color on dark screens
val TableCellText    = Color(0xFF1A1E3A)   // Dark navy text on light cell backgrounds

// ─── Home Screen Card Accent Colors ─────────────────────────────────────────
val CardIndigo      = Color(0xFF5C6BC0)  // New Schedule card (matches brand)
val CardIndigoDark  = Color(0xFF3949AB)  // New Schedule gradient end
val CardSlate       = Color(0xFF546E7A)  // History card — cool blue-grey
val CardSlateDark   = Color(0xFF37474F)  // History card gradient end
val CardAmber       = Color(0xFFF57C00)  // Employees card — warm amber
val CardAmberDark   = Color(0xFFE65100)  // Employees card gradient end
val CardViolet      = Color(0xFF7B1FA2)  // Template settings card
val CardVioletDark  = Color(0xFF6A1B9A)  // Template settings gradient end

// ─── Supporting Colors ────────────────────────────────────────────────────────
val PrimaryGreen     = Color(0xFF43A047)     // Stats positive values / theme tertiary
val PrimaryGreenDark = Color(0xFF1B5E20)     // Gradient end for green buttons/pages
val PrimaryBlue      = Color(0xFF1E88E5)     // Theme secondary
val Yellow           = Color(0xFFFFC107)     // Selection borders

// ─── Gradient Ends ────────────────────────────────────────────────────────────
val OrangeDark     = Color(0xFFE65100)     // Gradient end for orange/amber buttons
val BlockedRedDark = Color(0xFF8B0000)     // Gradient end for red/danger elements
val WhatsAppGreen  = Color(0xFF25D366)     // WhatsApp brand color for share buttons

// ─── Disabled State ───────────────────────────────────────────────────────────
val DisabledGray     = Color(0xFF9E9E9E)   // Disabled card gradient start (no action available)
val DisabledGrayDark = Color(0xFF616161)   // Disabled card gradient end

// ─── Adaptive Text Colors ────────────────────────────────────────────────────
val TextSecondary     = Color(0xFF666666)
val TextSecondaryDark = Color(0xFFB3B3B3)
val TextTertiary      = Color(0xFF999999)
val TextTertiaryDark  = Color(0xFF8A8A8A)
