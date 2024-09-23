package com.jv8.ui.components

import com.jv8.constants.FontPaths
import com.jv8.renderers.FontRenderer
import com.jv8.utils.Resource
import org.json.JSONObject
import java.io.File

object UIParser {
    fun loadUI(uiFileIdentifier: String): List<UIComponent> {
        val uiComponents = mutableListOf<UIComponent>()
        val content = Resource.readFile("ui/$uiFileIdentifier");
        val jsonObj = JSONObject(content)

        val uiArray = jsonObj.getJSONArray("ui")
        for (i in 0 until uiArray.length()) {
            val componentData = uiArray.getJSONObject(i)
            val type = componentData.getString("type")

            when (type) {
                "button" -> {
                    val x = componentData.getFloat("x")
                    val y = componentData.getFloat("y")
                    val width = componentData.getFloat("width")
                    val height = componentData.getFloat("height")
                    val label = componentData.getString("label")
                    val action = componentData.getString("action")

                    val button = UIButton(x, y, width, height, label) {
                        println("$action was triggered!")
                    }

                    uiComponents.add(button)
                }
                "label" -> {
                    val x = componentData.getFloat("x")
                    val y = componentData.getFloat("y")
                    val text = componentData.getString("text")
                    val font = componentData.getString("font")
                    val label = UILabel(x, y, text, font)

                    uiComponents.add(label)
                }
            }
        }
        return uiComponents
    }
}
