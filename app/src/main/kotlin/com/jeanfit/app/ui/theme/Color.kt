package com.jeanfit.app.ui.theme

import androidx.compose.ui.graphics.Color

// === JeanFit Ocean Blue Design System ===

// Primär-Palette (Blau-Familie)
val OceanBlue    = Color(0xFF1565C0)  // Primary CTAs, Buttons
val SkyBlue      = Color(0xFF42A5F5)  // Akzente, Chips, Links
val DeepNavy     = Color(0xFF0D2B4E)  // Headlines, dunkler Text
val IceBlue      = Color(0xFFE3F2FD)  // Background (Light Mode)
val MidnightBlue = Color(0xFF0A1929)  // Background (Dark Mode)

// Sekundär
val TealAccent   = Color(0xFF00BCD4)  // Sekundärer Akzent, Charts
val PearlWhite   = Color(0xFFF8FAFF)  // Card-Hintergrund Light
val CoachCardDark = Color(0xFF122136) // Card-Hintergrund Dark

// Food Color System (Ampelsystem — bleibt neutral)
val FoodGreen          = Color(0xFF2ECC71)
val FoodGreenContainer = Color(0xFFE8F5E9)
val FoodYellow         = Color(0xFFF39C12)
val FoodYellowContainer = Color(0xFFFFF8E1)
val FoodOrange         = Color(0xFFE74C3C)
val FoodOrangeContainer = Color(0xFFFFF3E0)

// Gamification
val CoinGold    = Color(0xFFFFD700)
val StreakFire  = Color(0xFFFF6B35)

// Light Theme
val LightPrimary             = OceanBlue
val LightOnPrimary           = Color.White
val LightPrimaryContainer    = Color(0xFFD6E4FF)
val LightOnPrimaryContainer  = Color(0xFF001A41)
val LightSecondary           = TealAccent
val LightOnSecondary         = Color.White
val LightSecondaryContainer  = Color(0xFFB2EBF2)
val LightOnSecondaryContainer = Color(0xFF001F24)
val LightBackground          = IceBlue
val LightOnBackground        = DeepNavy
val LightSurface             = PearlWhite
val LightOnSurface           = DeepNavy
val LightSurfaceVariant      = Color(0xFFDAE2FF)
val LightOnSurfaceVariant    = Color(0xFF44474F)
val LightOutline             = Color(0xFF747780)
val LightError               = Color(0xFFBA1A1A)
val LightOnError             = Color.White

// Dark Theme
val DarkBackground    = MidnightBlue
val DarkSurface       = Color(0xFF122136)
val DarkSurfaceVariant = Color(0xFF1E3A5F)
val DarkOnSurface     = Color(0xFFE2E8F4)
val DarkOnBackground  = Color(0xFFE2E8F4)
val DarkPrimary       = SkyBlue
val DarkOnPrimary     = Color(0xFF00316E)
val DarkPrimaryContainer = Color(0xFF004494)
val DarkOnPrimaryContainer = Color(0xFFD6E4FF)
val DarkSecondary     = TealAccent
val DarkOnSecondary   = Color(0xFF003640)
val DarkSecondaryContainer = Color(0xFF004D5B)
val DarkOnSecondaryContainer = Color(0xFFB2EBF2)
val DarkOutline       = Color(0xFF8E9099)
val DarkError         = Color(0xFFFFB4AB)
val DarkOnError       = Color(0xFF690005)

// Kompatibilitäts-Aliase für bestehende Screens (Ocean Blue Rebranding)
val SunsetOrange   = OceanBlue      // Primäre CTAs → jetzt OceanBlue
val SpringWood     = IceBlue        // Background → jetzt IceBlue
val BlueDianne     = DeepNavy       // Dunkler Text → jetzt DeepNavy
val BlueDianneLight = Color(0xFF1E3A5F)
