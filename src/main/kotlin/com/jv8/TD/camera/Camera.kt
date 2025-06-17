package com.jv8.TD.camera

import org.joml.Vector3f
import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW.*
import com.jv8.utils.MouseUtils

class Camera(
    val position: Vector3f = Vector3f(0f, 0f, 0f),
    target: Vector3f = Vector3f(0f, 0f, 0f)
) {
    private val front = Vector3f(0f, 0f, -1f)
    private val up = Vector3f(0f, 1f, 0f)
    private val right = Vector3f(1f, 0f, 0f)
    
    val rotation = Vector3f(0f, -90f, 0f)
    
    val viewMatrix = Matrix4f()
    
    private var firstMouse = true
    private var lastX = 0.0
    private var lastY = 0.0
    private val mouseSensitivity = 0.1f
    private var cursorLocked = false
    
    init {
        val direction = Vector3f(target).sub(position)
        if (!direction.equals(Vector3f(0f, 0f, 0f), 0.001f)) {
            lookAt(target)
        }
        updateCameraVectors()
    }

    fun update(window: Long) {
        val moveSpeed = 0.5f
        
        if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            cursorLocked = false
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
        }
        
        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS && !cursorLocked) {
            cursorLocked = true
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
            firstMouse = true
        }
        
        if (cursorLocked) {
            val mousePos = MouseUtils.getMouseXY()
            
            if (firstMouse) {
                lastX = mousePos.x
                lastY = mousePos.y
                firstMouse = false
            }
            
            val xOffset = (mousePos.x - lastX) * mouseSensitivity
            val yOffset = (lastY - mousePos.y) * mouseSensitivity
            
            lastX = mousePos.x
            lastY = mousePos.y
            
            rotation.y += xOffset.toFloat()
            rotation.x += yOffset.toFloat()
            
            if (rotation.x > 89f) rotation.x = 89f
            if (rotation.x < -89f) rotation.x = -89f
        }

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            position.add(Vector3f(front).mul(moveSpeed))
        }
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            position.sub(Vector3f(front).mul(moveSpeed))
        }
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            position.sub(Vector3f(right).mul(moveSpeed))
        }
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            position.add(Vector3f(right).mul(moveSpeed))
        }
        
        if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
            position.add(Vector3f(0f, moveSpeed, 0f))
        }
        if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
            position.sub(Vector3f(0f, moveSpeed, 0f))
        }
        
        updateCameraVectors()
    }
    
    private fun updateCameraVectors() {
        val pitch = Math.toRadians(rotation.x.toDouble())
        val yaw = Math.toRadians(rotation.y.toDouble())
        
        front.x = (Math.cos(pitch) * Math.cos(yaw)).toFloat()
        front.y = Math.sin(pitch).toFloat()
        front.z = (Math.cos(pitch) * Math.sin(yaw)).toFloat()
        front.normalize()
        
        right.set(front).cross(Vector3f(0f, 1f, 0f)).normalize()
        up.set(right).cross(front).normalize()
        
        viewMatrix.identity()
        viewMatrix.lookAt(position, Vector3f(position).add(front), up)
    }
    
    fun lookAt(target: Vector3f) {
        val direction = Vector3f(target).sub(position).normalize()
        
        rotation.y = Math.toDegrees(Math.atan2(direction.z.toDouble(), direction.x.toDouble())).toFloat()
        
        val horizontalLength = Math.sqrt((direction.x * direction.x + direction.z * direction.z).toDouble()).toFloat()
        rotation.x = Math.toDegrees(Math.atan2(-direction.y.toDouble(), horizontalLength.toDouble())).toFloat()
        
        updateCameraVectors()
    }
    
    fun initMouseCapture(window: Long) {
        cursorLocked = true
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
    }
}