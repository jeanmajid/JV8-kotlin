package com.jv8.ui.components

import org.lwjgl.opengl.GL11.*
import com.jv8.renderers.FontRenderer

class UILabel(
    x: Float,
    y: Float,
    var text: String,
    val fontPath: String
) : UIComponent(x, y, 0f, 0f) {
    private var fontRenderer: FontRenderer? = null

    override fun render() {
        if (!isVisible) return

        glColor3f(1.0f, 1.0f, 1.0f)

        if (fontRenderer == null) {
            fontRenderer = FontRenderer()
            fontRenderer?.loadFontConfig(fontPath)
        }

        fontRenderer?.renderText(x, y, text)
    }

    override fun refreshLayout(screenWidth: Int, screenHeight: Int) {
        // Implement if crazy
    }
}
