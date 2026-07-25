package com.woojoo.foundingfathers.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

val Context.dataStore by preferencesDataStore(name = "founding_fathers_prefs")

object PrefKeys {
    val WRONG_HISTORY = stringPreferencesKey("wrong_history_v1")
    val FOUNDER_STATS = stringPreferencesKey("founder_stats_v1")
    val LAST_DAILY_DATE = stringPreferencesKey("last_daily_date")
    val DAILY_GOAL = intPreferencesKey("daily_goal")
    val DAILY_CORRECT = intPreferencesKey("daily_correct")
    val DAILY_DONE = booleanPreferencesKey("daily_done")
    val DAILY_QUESTION_IDS = stringPreferencesKey("daily_question_ids")
    val XP = intPreferencesKey("xp")
    val STREAK = intPreferencesKey("streak")
    val LAST_PLAYED_DATE = longPreferencesKey("last_played_date")
    val BGM_ENABLED = booleanPreferencesKey("bgm_enabled")
    val SFX_ENABLED = booleanPreferencesKey("sfx_enabled")
    val BGM_VOLUME = floatPreferencesKey("bgm_volume")
    val SFX_VOLUME = floatPreferencesKey("sfx_volume")
    val BEST_QUIZ_SCORE = intPreferencesKey("best_quiz_score")
    val BEST_QUOTE_SCORE = intPreferencesKey("best_quote_score")
    val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
    val DAILY_REMINDER_HOUR = intPreferencesKey("daily_reminder_hour")
    val DAILY_REMINDER_MINUTE = intPreferencesKey("daily_reminder_minute")
    val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding_v1")
    val COINS = intPreferencesKey("coins")
    val LAST_SPIN_DATE = stringPreferencesKey("last_spin_date")
}

class AppRepository(private val context: Context) {

    val prefsFlow: Flow<androidx.datastore.preferences.core.Preferences> = context.dataStore.data

    suspend fun <T> read(key: androidx.datastore.preferences.core.Preferences.Key<T>, default: T): T {
        return context.dataStore.data.map { it[key] ?: default }.first()
    }

    suspend fun <T> write(key: androidx.datastore.preferences.core.Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }

    // --- Wrong answer history (JSON array of objects) ---
    fun encodeWrongHistory(list: List<WrongAnswer>): String {
        val arr = JSONArray()
        for (w in list) {
            val o = JSONObject()
            o.put("id", w.id)
            o.put("date", w.date)
            o.put("questionId", w.questionId)
            o.put("question", w.question)
            o.put("chosen", w.chosen)
            o.put("correct", w.correct)
            o.put("founderId", w.founderId)
            arr.put(o)
        }
        return arr.toString()
    }

    fun decodeWrongHistory(json: String): List<WrongAnswer> {
        if (json.isBlank()) return emptyList()
        val out = mutableListOf<WrongAnswer>()
        val arr = JSONArray(json)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                WrongAnswer(
                    id = o.getString("id"),
                    date = o.getLong("date"),
                    questionId = o.getString("questionId"),
                    question = o.getString("question"),
                    chosen = o.getString("chosen"),
                    correct = o.getString("correct"),
                    founderId = o.getString("founderId")
                )
            )
        }
        return out
    }

    // --- Founder stats (JSON object keyed by founderId) ---
    fun encodeFounderStats(map: Map<String, FounderStats>): String {
        val root = JSONObject()
        for ((founderId, stats) in map) {
            val o = JSONObject()
            o.put("uniqueSeen", JSONArray(stats.uniqueSeen.toList()))
            o.put("correct", stats.correct)
            o.put("total", stats.total)
            root.put(founderId, o)
        }
        return root.toString()
    }

    fun decodeFounderStats(json: String): Map<String, FounderStats> {
        if (json.isBlank()) return emptyMap()
        val root = JSONObject(json)
        val out = mutableMapOf<String, FounderStats>()
        val keys = root.keys()
        while (keys.hasNext()) {
            val founderId = keys.next()
            val o = root.getJSONObject(founderId)
            val seenArr = o.getJSONArray("uniqueSeen")
            val seenSet = (0 until seenArr.length()).map { seenArr.getString(it) }.toSet()
            out[founderId] = FounderStats(
                uniqueSeen = seenSet,
                correct = o.getInt("correct"),
                total = o.getInt("total")
            )
        }
        return out
    }

    fun encodeIdList(ids: List<String>): String = JSONArray(ids).toString()

    fun decodeIdList(json: String): List<String> {
        if (json.isBlank()) return emptyList()
        val arr = JSONArray(json)
        return (0 until arr.length()).map { arr.getString(it) }
    }
}
