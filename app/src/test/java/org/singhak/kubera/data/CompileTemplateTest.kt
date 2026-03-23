package org.singhak.kubera.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource

class CompileTemplateTest {
    @ParameterizedTest
    @CsvFileSource(resources = ["compile_template_cases.csv"], numLinesToSkip = 1)
    fun `templates produce expected matches`(template: String, input: String, expectedAmount: String?) {
        val match = compileTemplate(template).find(input)
        assertEquals(expectedAmount, match?.groupValues?.get(1))
    }

    @Test
    fun `unknown placeholder throws error`() {
        assertThrows(IllegalStateException::class.java) {
            compileTemplate("Hello {unknown} world")
        }
    }
}
