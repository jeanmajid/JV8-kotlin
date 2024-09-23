package com.jv8.utils

object MathUtils {
    fun addVectors(x1: Float, y1: Float, x2: Float, y2: Float): Pair<Float, Float> {
        return Pair(x1 + x2, y1 + y2)
    }
}
