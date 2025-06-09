package com.jv8.ui.components

import com.jv8.TD.shader.Shader
import com.jv8.renderers.FontRenderer
import org.joml.Vector4f

class UILabel(
    x: Float,
    y: Float,
    var text: String,
    val fontPath: String,
    val textColor: Vector4f = Vector4f(1f, 1f, 1f, 1f)
) : UIComponent(x, y, 0f, 0f) {
    private var fontRenderer: FontRenderer? = null
    private lateinit var shader: Shader

    override fun render() {
        if (!isVisible) return

        if (fontRenderer == null) {
            fontRenderer = FontRenderer()
            fontRenderer?.loadFontConfig(fontPath)
        }

        fontRenderer?.renderText(x, y, text, shader, textColor)
    }

    override fun refreshLayout(screenWidth: Int, screenHeight: Int) {
        // Implement if needed
    }
    
    fun setShader(shader: Shader) {
        this.shader = shader
    }
    
    fun cleanup() {
        fontRenderer?.cleanup()
    }
}