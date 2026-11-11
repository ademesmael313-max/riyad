package com.riyadsalihin.amharic.util

/** Helpers so that search ignores Arabic diacritics and spelling variants. */
object ArabicText {

    private val DIACRITICS =
        Regex("[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")
    private val TATWEEL = Regex("\\u0640")

    fun normalize(text: String): String = text
        .replace(DIACRITICS, "")
        .replace(TATWEEL, "")
        .replace('\u0623', '\u0627') // أ -> ا
        .replace('\u0625', '\u0627') // إ -> ا
        .replace('\u0622', '\u0627') // آ -> ا
        .replace('\u0649', '\u064A') // ى -> ي
        .replace('\u0629', '\u0647') // ة -> ه
        .replace("\u200F", "")
        .replace("\u200E", "")
        .trim()

    /** A short, whitespace-collapsed excerpt for list rows. */
    fun excerpt(text: String, maxLength: Int = 180): String {
        val collapsed = text.replace(Regex("\\s+"), " ").trim()
        if (collapsed.length <= maxLength) return collapsed
        val cut = collapsed.take(maxLength)
        val lastSpace = cut.lastIndexOf(' ')
        return (if (lastSpace > maxLength / 2) cut.substring(0, lastSpace) else cut).trimEnd() + "…"
    }
}
