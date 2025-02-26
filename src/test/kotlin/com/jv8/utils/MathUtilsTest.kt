package com.jv8.utils

import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class MathUtilsTest {
    @Test
    fun addVectors() {
        val result = MathUtils.addVectors(1f, 1f, 2f, 2f)
        kotlin.test.assertEquals(result.first, 3f)
        kotlin.test.assertEquals(result.second, 3f)
    }
}