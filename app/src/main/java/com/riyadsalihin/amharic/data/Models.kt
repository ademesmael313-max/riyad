package com.riyadsalihin.amharic.data

import kotlinx.serialization.Serializable

/** One book (kitab) of Riyad as-Salihin, as described in assets/content/index.json. */
@Serializable
data class BookSummary(
    val id: Int,
    val titleAr: String,
    val titleEn: String,
    val titleAm: String,
    val hadithCount: Int = 0,
    val translatedCount: Int = 0,
)

@Serializable
data class ContentIndex(
    val schema: Int = 1,
    val totalHadiths: Int = 0,
    val translatedHadiths: Int = 0,
    val books: List<BookSummary> = emptyList(),
)

@Serializable
data class Hadith(
    val number: Int,
    val arabic: String,
    val english: String,
    val amharic: String? = null,
) {
    val hasAmharic: Boolean get() = !amharic.isNullOrBlank()
}

@Serializable
data class BookContent(
    val id: Int,
    val titleAr: String,
    val titleEn: String,
    val titleAm: String,
    val hadiths: List<Hadith> = emptyList(),
)

/** A hadith together with the book it belongs to, used by search and bookmarks. */
data class SearchResult(
    val bookId: Int,
    val bookTitleAm: String,
    val bookTitleEn: String,
    val hadith: Hadith,
)
