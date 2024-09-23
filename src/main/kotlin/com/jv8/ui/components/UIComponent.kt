package com.jv8.ui.components

abstract class UIComponent(
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float
) {
    var isVisible: Boolean = true

    abstract fun render()

    abstract fun refreshLayout(screenWidth: Int, screenHeight: Int)

    open fun handleInput() {}

    fun containsPoint(px: Float, py: Float): Boolean {
        return px >= x && px <= x + width && py >= y && py <= y + height
    }
}
