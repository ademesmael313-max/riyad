package com.riyadsalihin.amharic

import com.riyadsalihin.amharic.data.BookContent
import com.riyadsalihin.amharic.data.ContentIndex
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `index and books deserialize with optional fields`() {
        val index = json.decodeFromString(
            ContentIndex.serializer(),
            """
            {"schema":1,"totalHadiths":2,"translatedHadiths":1,
             "books":[{"id":1,"titleAr":"كتاب المقدمات","titleEn":"The Book of Miscellany",
                       "titleAm":"የተለያዩ ምክሮች","hadithCount":2,"translatedCount":1}]}
            """.trimIndent(),
        )
        assertEquals(2, index.totalHadiths)
        assertEquals(1, index.books.size)

        val book = json.decodeFromString(
            BookContent.serializer(),
            """
            {"id":1,"titleAr":"كتاب المقدمات","titleEn":"The Book of Miscellany",
             "titleAm":"የተለያዩ ምክሮች","hadiths":[
               {"number":1,"arabic":"إنما الأعمال بالنيات","english":"Actions are by intentions",
                "amharic":"ሥራዎች በኒያ ናቸው"},
               {"number":2,"arabic":"لا هجرة بعد الفتح","english":"There is no emigration"}
             ]}
            """.trimIndent(),
        )
        assertEquals(2, book.hadiths.size)
        assertTrue(book.hadiths[0].hasAmharic)
        assertFalse(book.hadiths[1].hasAmharic)
    }
}
