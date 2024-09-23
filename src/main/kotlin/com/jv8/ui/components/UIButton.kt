package com.jv8.ui.components

import org.lwjgl.opengl.GL11.*
import com.jv8.utils.MouseUtils

class UIButton(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    val label: String,
    val onClick: () -> Unit
) : UIComponent(x, y, width, height) {

    override fun render() {
        if (!isVisible) return

        glBegin(GL_QUADS)
        glVertex2f(x, y)  // Bottom-left corner
        glVertex2f(x + width, y)  // Bottom-right corner
        glVertex2f(x + width, y + height)  // Top-right corner
        glVertex2f(x, y + height)  // Top-left corner
        glEnd()

        glColor3f(1.0f, 1.0f, 1.0f)
    }

    override fun refreshLayout(screenWidth: Int, screenHeight: Int) {
        // will do someday maybe idk
    }

    override fun handleInput() {
        val mouseX = MouseUtils.getMouseX()
        val mouseY = MouseUtils.getMouseY()

        if (containsPoint(mouseX, mouseY) && MouseUtils.isMouseButtonPressed(0)) {
            onClick()
        }
    }
}