package com.dollyplastic.invoiceapp.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF4F8FF),
        Color(0xFFEAF2FF),
        Color(0xFFFFFFFF)
    )
)


val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

object AppColors {
    val Background = Color(0xFFFFFFFF)
    val FieldBackground = Color(0xFFF9FAFB)   // subtle grey
    val Border = Color(0xFFE5E7EB)
    val TextPrimary = Color(0xFF000000)
    val TextSecondary = Color(0xFF6B7280)
    val Icon = Color(0xFF9CA3AF)
    val PrimaryBlue = Color(red=47, green=112, blue=183)
    val textFieldGrey=Color(0xFFF3F4F6)

    // Base
    val Foreground = Color(0xFF000000)

    // Primary
    val Primary = Color(0xFF000000)
    val PrimaryForeground = Color(0xFFFFFFFF)

    // Secondary / Muted
    val Secondary = Color(0xFFF5F5F5)
    val Muted = Color(0xFFF5F5F5)
    val MutedForeground = Color(0xFF6B7280)

    // Accent
    val Accent = Color(0xFFC4FF0D)
    val AccentForeground = Color(0xFF000000)

    // Inputs
    val InputBackground = Color(0xFFF9FAFB)

    // Destructive
    val Destructive = Color(0xFFEF4444)
    val DestructiveForeground = Color(0xFFFFFFFF)

    val Card = Color(0xFFFFFFFF)
    val CardForeground = Color(0xFF000000)

    // Primary / Secondary


    val SecondaryForeground = Color(0xFF000000)




    val SwitchBackground = Color(0xFFCBCED4)


    // Ring / focus
    val Ring = Accent

    // Charts colors
    val Chart1 = Color(0xFFED8936)
    val Chart2 = Color(0xFF38B2AC)
    val Chart3 = Color(0xFF4A5568)
    val Chart4 = Color(0xFFF6E05E)
    val Chart5 = Color(0xFFF6AD55)

    // Sidebar colors
    val Sidebar = Color(0xFFF9FAFB)
    val SidebarForeground = Color(0xFF000000)
    val SidebarPrimary = Color(0xFF030213)

    // Bill To (Buyer) - Green Theme
    val BillToBackground = Color(0xFF32A852).copy(alpha = 0.1f) // Light Green
    val BillToIcon = Color(0xFF32A852)       // Green

    // Ship To (Consignee) - Pink/Purple Theme
    val ShipToBackground = Color(0xFFFDF2F8) // Light Pink
    val ShipToIcon = Color(0xFFDB2777)       // Pink/Magenta
}



