package com.riyadsalihin.amharic

import com.riyadsalihin.amharic.util.ArabicText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArabicTextTest {

    @Test
    fun `diacritics are ignored when searching`() {
        val withHarakat = "الْحَمْدُ لِلَّهِ"
        val withoutHarakat = "الحمد لله"
        assertEquals(ArabicText.normalize(withoutHarakat), ArabicText.normalize(withHarakat))
    }

    @Test
    fun `alef and ta marbuta variants are unified`() {
        assertEquals(
            ArabicText.normalize("احمد"),
            ArabicText.normalize("أحمد"),
        )
        assertEquals(
            ArabicText.normalize("صلاة"),
            ArabicText.normalize("صلاه"),
        )
    }

    @Test
    fun `excerpt keeps short text unchanged`() {
        val text = "إنما الأعمال بالنيات"
        assertEquals(text, ArabicText.excerpt(text, 100))
    }

    @Test
    fun `excerpt truncates long text on a word boundary`() {
        val text = "كلمة ".repeat(60)
        val excerpt = ArabicText.excerpt(text, 40)
        assertTrue(excerpt.length <= 41)
        assertTrue(excerpt.endsWith("…"))
        assertTrue(!excerpt.contains("  "))
    }
}
