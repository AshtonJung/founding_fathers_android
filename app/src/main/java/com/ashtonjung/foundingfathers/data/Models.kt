package com.ashtonjung.foundingfathers.data

data class TimelineEvent(
    val year: Int,
    val title: String,
    val detail: String
)

data class FoundingFather(
    val id: String,
    val name: String,
    val shortBio: String,
    val portraitRes: Int,
    val timeline: List<TimelineEvent>,
    val quotes: List<String>
)

data class QuizQuestion(
    val id: String,
    val founderId: String,
    val prompt: String,
    val choices: List<String>,
    val correctIndex: Int,
    val explain: String,
    /** Optional primary-source excerpt shown above the prompt, styled as a quoted
     *  source card. Null for standard recall questions (the vast majority). */
    val stimulus: String? = null
)

data class SourceItem(
    val title: String,
    val url: String
)

data class WrongAnswer(
    val id: String,
    val date: Long,
    val questionId: String,
    val question: String,
    val chosen: String,
    val correct: String,
    val founderId: String
)

data class FounderStats(
    val uniqueSeen: Set<String> = emptySet(),
    val correct: Int = 0,
    val total: Int = 0
)

enum class Tier { NONE, BRONZE, SILVER, GOLD }

fun tierFor(progress: Double): Tier = when {
    progress >= 0.90 -> Tier.GOLD
    progress >= 0.75 -> Tier.SILVER
    progress >= 0.60 -> Tier.BRONZE
    else -> Tier.NONE
}
