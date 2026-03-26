package com.jeanfit.app.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Speichert alle Chat-Nachrichten zwischen Nutzer und Jean (KI-Coach).
 * Dient gleichzeitig als Gedächtnis-Kontext für zukünftige API-Calls.
 */
@Entity(tableName = "coach_messages")
data class CoachMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val isFromCoach: Boolean,                   // true = Jean, false = Nutzer
    val messageType: String = "chat",           // "chat" | "greeting" | "tip" | "system"
    val contextSnapshot: String? = null,        // JSON: Nutzer-Stats zum Zeitpunkt dieser Nachricht
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

/**
 * Langzeit-Gedächtnis des Coaches: Merkt sich wichtige Fakten über den Nutzer.
 * Wird dem System-Prompt beigefügt → Jean erinnert sich an alles.
 */
@Entity(tableName = "coach_memory")
data class CoachMemory(
    @PrimaryKey val key: String,                // z.B. "favorite_food", "biggest_challenge"
    val value: String,                          // z.B. "Schokolade", "Abendessen"
    val category: String,                       // "preference" | "challenge" | "milestone" | "fact"
    val updatedAt: Long = System.currentTimeMillis()
)
