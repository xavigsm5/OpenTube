package com.opentube.util

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateFormatter {
    
    /**
     * Formatea una fecha ISO a formato legible sin el sufijo T03:00Z
     * Ejemplo: "2024-11-06T03:00Z" -> "6 nov 2024"
     */
    fun formatUploadDate(isoDate: String?): String {
        if (isoDate.isNullOrEmpty()) return ""
        
        return try {
            val dateTime = OffsetDateTime.parse(isoDate)
            val now = OffsetDateTime.now()
            
            val daysBetween = ChronoUnit.DAYS.between(dateTime, now)
            
            when {
                daysBetween == 0L -> "Hoy"
                daysBetween == 1L -> "Ayer"
                daysBetween < 7 -> "Hace $daysBetween días"
                daysBetween < 30 -> "Hace ${daysBetween / 7} semanas"
                daysBetween < 365 -> "Hace ${daysBetween / 30} meses"
                else -> {
                    val years = daysBetween / 365
                    "Hace $years ${if (years == 1L) "año" else "años"}"
                }
            }
        } catch (e: Exception) {
            // Si falla el parsing, intentar mostrar solo la fecha sin la hora
            try {
                isoDate.substringBefore("T")
            } catch (e: Exception) {
                isoDate
            }
        }
    }
}
