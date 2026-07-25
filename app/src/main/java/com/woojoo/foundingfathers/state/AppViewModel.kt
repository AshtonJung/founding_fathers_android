package com.woojoo.foundingfathers.state

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.woojoo.foundingfathers.data.AppRepository
import com.woojoo.foundingfathers.data.FoundingFather
import com.woojoo.foundingfathers.data.FounderStats
import com.woojoo.foundingfathers.data.PrefKeys
import com.woojoo.foundingfathers.data.QuizQuestion
import com.woojoo.foundingfathers.data.SampleData
import com.woojoo.foundingfathers.data.WrongAnswer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

data class AppUiState(
    val fathers: List<FoundingFather> = SampleData.fathers,
    val founderStats: Map<String, FounderStats> = emptyMap(),
    val wrongHistory: List<WrongAnswer> = emptyList(),
    val soundEnabled: Boolean = true,
    val backgroundMusicEnabled: Boolean = true,
    val bgmVolume: Float = 0.35f,
    val sfxVolume: Float = 0.9f,
    val dailyGoal: Int = 3,
    val dailyCorrect: Int = 0,
    val dailyDone: Boolean = false,
    val dailyQuestions: List<QuizQuestion> = emptyList(),
    val xp: Int = 120,
    val streak: Int = 3,
    val lastPlayedDate: Long = 0L,
    val bestQuizScore: Int = 0,
    val bestQuoteScore: Int = 0,
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderHour: Int = 19,
    val dailyReminderMinute: Int = 0,
    val hasSeenOnboarding: Boolean = false,
    val coins: Int = 20,
    val lastSpinDate: String = "",
    val loaded: Boolean = false
)

private val isoDayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    timeZone = TimeZone.getDefault()
}

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppRepository(app.applicationContext)

    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val wrongHistory = repo.decodeWrongHistory(repo.read(PrefKeys.WRONG_HISTORY, ""))
            val founderStats = repo.decodeFounderStats(repo.read(PrefKeys.FOUNDER_STATS, ""))
            val lastDailyDate = repo.read(PrefKeys.LAST_DAILY_DATE, "")
            val dailyGoal = repo.read(PrefKeys.DAILY_GOAL, 3)
            val dailyCorrect = repo.read(PrefKeys.DAILY_CORRECT, 0)
            val dailyDone = repo.read(PrefKeys.DAILY_DONE, false)
            val dailyIds = repo.decodeIdList(repo.read(PrefKeys.DAILY_QUESTION_IDS, ""))
            val xp = repo.read(PrefKeys.XP, 120)
            val streak = repo.read(PrefKeys.STREAK, 3)
            val lastPlayed = repo.read(PrefKeys.LAST_PLAYED_DATE, 0L)
            val bgmEnabled = repo.read(PrefKeys.BGM_ENABLED, true)
            val sfxEnabled = repo.read(PrefKeys.SFX_ENABLED, true)
            val bgmVolume = repo.read(PrefKeys.BGM_VOLUME, 0.35f)
            val sfxVolume = repo.read(PrefKeys.SFX_VOLUME, 0.9f)
            val bestQuiz = repo.read(PrefKeys.BEST_QUIZ_SCORE, 0)
            val bestQuote = repo.read(PrefKeys.BEST_QUOTE_SCORE, 0)
            val reminderEnabled = repo.read(PrefKeys.DAILY_REMINDER_ENABLED, false)
            val reminderHour = repo.read(PrefKeys.DAILY_REMINDER_HOUR, 19)
            val reminderMinute = repo.read(PrefKeys.DAILY_REMINDER_MINUTE, 0)
            val seenOnboarding = repo.read(PrefKeys.HAS_SEEN_ONBOARDING, false)
            val coins = repo.read(PrefKeys.COINS, 20)
            val lastSpinDate = repo.read(PrefKeys.LAST_SPIN_DATE, "")

            val today = isoDayFormat.format(Date())
            val freshToday = lastDailyDate == today
            val dailyQuestions = if (freshToday && dailyIds.isNotEmpty()) {
                dailyIds.mapNotNull { id -> SampleData.quiz.find { it.id == id } }
            } else {
                emptyList()
            }

            _state.value = _state.value.copy(
                wrongHistory = wrongHistory,
                founderStats = founderStats,
                dailyGoal = dailyGoal,
                dailyCorrect = if (freshToday) dailyCorrect else 0,
                dailyDone = if (freshToday) dailyDone else false,
                dailyQuestions = dailyQuestions,
                xp = xp,
                streak = streak,
                lastPlayedDate = lastPlayed,
                backgroundMusicEnabled = bgmEnabled,
                soundEnabled = sfxEnabled,
                bgmVolume = bgmVolume,
                sfxVolume = sfxVolume,
                bestQuizScore = bestQuiz,
                bestQuoteScore = bestQuote,
                dailyReminderEnabled = reminderEnabled,
                dailyReminderHour = reminderHour,
                dailyReminderMinute = reminderMinute,
                hasSeenOnboarding = seenOnboarding,
                coins = coins,
                lastSpinDate = lastSpinDate,
                loaded = true
            )

            if (!freshToday) {
                regenerateDaily(persistDate = true)
            }
        }
    }

    private fun persistWrongHistory(list: List<WrongAnswer>) = viewModelScope.launch {
        repo.write(PrefKeys.WRONG_HISTORY, repo.encodeWrongHistory(list))
    }

    private fun persistFounderStats(map: Map<String, FounderStats>) = viewModelScope.launch {
        repo.write(PrefKeys.FOUNDER_STATS, repo.encodeFounderStats(map))
    }

    fun questionsFor(founder: FoundingFather): List<QuizQuestion> =
        SampleData.quiz.filter { it.founderId == founder.id }

    fun masteryProgress(founder: FoundingFather): Double {
        val s = _state.value.founderStats[founder.id] ?: FounderStats()
        val pool = questionsFor(founder)
        val totalPool = maxOf(1, pool.size)
        val coverage = s.uniqueSeen.size.toDouble() / totalPool.toDouble()
        val accuracy = if (s.total > 0) s.correct.toDouble() / s.total.toDouble() else 0.0
        val score = 0.1 + 0.6 * accuracy + 0.3 * coverage
        return score.coerceIn(0.0, 1.0)
    }

    fun wrongAnswersFor(founder: FoundingFather?): List<WrongAnswer> {
        val all = _state.value.wrongHistory
        return if (founder == null) all else all.filter { it.founderId == founder.id }
    }

    fun recordAnswer(question: QuizQuestion, chosenIndex: Int) {
        val isCorrect = chosenIndex == question.correctIndex
        val current = _state.value

        var newHistory = current.wrongHistory
        if (!isCorrect) {
            val chosenText = question.choices.getOrNull(chosenIndex) ?: ""
            val correctText = question.choices.getOrNull(question.correctIndex) ?: ""
            val item = WrongAnswer(
                id = UUID.randomUUID().toString(),
                date = System.currentTimeMillis(),
                questionId = question.id,
                question = question.prompt,
                chosen = chosenText,
                correct = correctText,
                founderId = question.founderId
            )
            newHistory = listOf(item) + current.wrongHistory
        }

        val stats = current.founderStats[question.founderId] ?: FounderStats()
        val updatedSeen = stats.uniqueSeen + question.id
        val updatedStats = stats.copy(
            uniqueSeen = updatedSeen,
            total = stats.total + 1,
            correct = stats.correct + if (isCorrect) 1 else 0
        )
        val newStatsMap = current.founderStats + (question.founderId to updatedStats)

        _state.value = current.copy(wrongHistory = newHistory, founderStats = newStatsMap)
        persistWrongHistory(newHistory)
        persistFounderStats(newStatsMap)
    }

    fun deleteWrongAnswer(item: WrongAnswer) {
        val newHistory = _state.value.wrongHistory.filterNot { it.id == item.id }
        _state.value = _state.value.copy(wrongHistory = newHistory)
        persistWrongHistory(newHistory)
    }

    fun clearWrongAnswers(founder: FoundingFather? = null) {
        val newHistory = if (founder == null) {
            emptyList()
        } else {
            _state.value.wrongHistory.filterNot { it.founderId == founder.id }
        }
        _state.value = _state.value.copy(wrongHistory = newHistory)
        persistWrongHistory(newHistory)
    }

    fun adaptiveGlobalQuestions(count: Int): List<QuizQuestion> {
        val all = SampleData.quiz
        val wrongSet = _state.value.wrongHistory.map { it.questionId }.toSet()
        val seenSet = _state.value.founderStats.values.flatMap { it.uniqueSeen }.toSet()

        fun weight(q: QuizQuestion): Int = when {
            wrongSet.contains(q.id) -> 3
            !seenSet.contains(q.id) -> 2
            else -> 1
        }
        val weighted = all.sortedWith(compareByDescending { weight(it) }).let {
            // shuffle within equal-weight groups for variety
            it.groupBy { q -> weight(q) }.toSortedMap(compareByDescending { w -> w })
                .values.flatMap { group -> group.shuffled() }
        }
        val n = count.coerceIn(0, weighted.size)
        return weighted.take(n)
    }

    fun beginQuizSession(count: Int = 10): List<QuizQuestion> = adaptiveGlobalQuestions(count).ifEmpty {
        SampleData.quiz.shuffled().take(count)
    }

    // --- Daily challenge ---
    fun ensureDailyFresh() {
        val today = isoDayFormat.format(Date())
        viewModelScope.launch {
            val savedDate = repo.read(PrefKeys.LAST_DAILY_DATE, "")
            if (savedDate != today) {
                regenerateDaily(persistDate = true)
            } else if (_state.value.dailyQuestions.size != _state.value.dailyGoal) {
                regenerateDaily(persistDate = false)
            }
        }
    }

    private fun regenerateDaily(persistDate: Boolean) {
        val required = maxOf(1, _state.value.dailyGoal)
        var picks = adaptiveGlobalQuestions(required)
        if (picks.size < required) {
            picks = picks + SampleData.quiz.take(required - picks.size)
        }
        _state.value = _state.value.copy(dailyQuestions = picks, dailyCorrect = 0, dailyDone = false)
        viewModelScope.launch {
            repo.write(PrefKeys.DAILY_QUESTION_IDS, repo.encodeIdList(picks.map { it.id }))
            repo.write(PrefKeys.DAILY_CORRECT, 0)
            repo.write(PrefKeys.DAILY_DONE, false)
            if (persistDate) {
                repo.write(PrefKeys.LAST_DAILY_DATE, isoDayFormat.format(Date()))
            }
        }
    }

    fun resetDaily() = regenerateDaily(persistDate = true)

    fun applyDailyAnswer(isCorrect: Boolean) {
        val current = _state.value
        val newCorrect = if (isCorrect) current.dailyCorrect + 1 else current.dailyCorrect
        val required = maxOf(1, current.dailyGoal)
        val done = newCorrect >= required
        _state.value = current.copy(dailyCorrect = newCorrect, dailyDone = done)
        viewModelScope.launch {
            repo.write(PrefKeys.DAILY_CORRECT, newCorrect)
            repo.write(PrefKeys.DAILY_DONE, done)
            repo.write(PrefKeys.LAST_DAILY_DATE, isoDayFormat.format(Date()))
        }
    }

    // --- Home screen play/streak tracking ---
    fun markPlayed() {
        val now = System.currentTimeMillis()
        val wasToday = isSameDay(_state.value.lastPlayedDate, now)
        val newStreak = if (!wasToday) _state.value.streak + 1 else _state.value.streak
        _state.value = _state.value.copy(lastPlayedDate = now, streak = newStreak)
        viewModelScope.launch {
            repo.write(PrefKeys.LAST_PLAYED_DATE, now)
            repo.write(PrefKeys.STREAK, newStreak)
        }
    }

    private fun isSameDay(a: Long, b: Long): Boolean {
        if (a <= 0L) return false
        val fmt = isoDayFormat
        return fmt.format(Date(a)) == fmt.format(Date(b))
    }

    fun addXp(amount: Int) {
        val newXp = _state.value.xp + amount
        _state.value = _state.value.copy(xp = newXp)
        viewModelScope.launch { repo.write(PrefKeys.XP, newXp) }
    }

    fun setBestQuizScore(score: Int) {
        if (score <= _state.value.bestQuizScore) return
        _state.value = _state.value.copy(bestQuizScore = score)
        viewModelScope.launch { repo.write(PrefKeys.BEST_QUIZ_SCORE, score) }
    }

    fun setBestQuoteScore(score: Int) {
        if (score <= _state.value.bestQuoteScore) return
        _state.value = _state.value.copy(bestQuoteScore = score)
        viewModelScope.launch { repo.write(PrefKeys.BEST_QUOTE_SCORE, score) }
    }

    // --- Settings ---
    fun setBgmEnabled(on: Boolean) {
        _state.value = _state.value.copy(backgroundMusicEnabled = on)
        viewModelScope.launch { repo.write(PrefKeys.BGM_ENABLED, on) }
    }

    fun setSfxEnabled(on: Boolean) {
        _state.value = _state.value.copy(soundEnabled = on)
        viewModelScope.launch { repo.write(PrefKeys.SFX_ENABLED, on) }
    }

    fun setBgmVolume(v: Float) {
        _state.value = _state.value.copy(bgmVolume = v)
        viewModelScope.launch { repo.write(PrefKeys.BGM_VOLUME, v) }
    }

    fun setSfxVolume(v: Float) {
        _state.value = _state.value.copy(sfxVolume = v)
        viewModelScope.launch { repo.write(PrefKeys.SFX_VOLUME, v) }
    }

    fun setDailyReminder(enabled: Boolean, hour: Int, minute: Int) {
        _state.value = _state.value.copy(
            dailyReminderEnabled = enabled,
            dailyReminderHour = hour,
            dailyReminderMinute = minute
        )
        viewModelScope.launch {
            repo.write(PrefKeys.DAILY_REMINDER_ENABLED, enabled)
            repo.write(PrefKeys.DAILY_REMINDER_HOUR, hour)
            repo.write(PrefKeys.DAILY_REMINDER_MINUTE, minute)
        }
    }

    fun completeOnboarding() {
        _state.value = _state.value.copy(hasSeenOnboarding = true)
        viewModelScope.launch { repo.write(PrefKeys.HAS_SEEN_ONBOARDING, true) }
    }

    fun replayOnboarding() {
        _state.value = _state.value.copy(hasSeenOnboarding = false)
        viewModelScope.launch { repo.write(PrefKeys.HAS_SEEN_ONBOARDING, false) }
    }

    fun founderById(id: String): FoundingFather? = _state.value.fathers.find { it.id == id }

    // --- Coins & rewards ---
    fun addCoins(amount: Int) {
        val newCoins = _state.value.coins + amount
        _state.value = _state.value.copy(coins = newCoins)
        viewModelScope.launch { repo.write(PrefKeys.COINS, newCoins) }
    }

    /** Returns true and deducts coins if the player can afford [amount]; false (no change) otherwise. */
    fun spendCoins(amount: Int): Boolean {
        val current = _state.value.coins
        if (current < amount) return false
        val newCoins = current - amount
        _state.value = _state.value.copy(coins = newCoins)
        viewModelScope.launch { repo.write(PrefKeys.COINS, newCoins) }
        return true
    }

    fun canSpinToday(): Boolean = _state.value.lastSpinDate != isoDayFormat.format(Date())

    /** Marks today's spin as used and grants the reward. Returns false if already spun today. */
    fun claimDailySpin(rewardCoins: Int, rewardXp: Int): Boolean {
        if (!canSpinToday()) return false
        val today = isoDayFormat.format(Date())
        _state.value = _state.value.copy(
            lastSpinDate = today,
            coins = _state.value.coins + rewardCoins,
            xp = _state.value.xp + rewardXp
        )
        viewModelScope.launch {
            repo.write(PrefKeys.LAST_SPIN_DATE, today)
            repo.write(PrefKeys.COINS, _state.value.coins)
            repo.write(PrefKeys.XP, _state.value.xp)
        }
        return true
    }
}
