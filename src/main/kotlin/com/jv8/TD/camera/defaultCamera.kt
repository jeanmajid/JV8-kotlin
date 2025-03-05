package com.jv8.TD.camera

import org.joml.Vector3f
import org.joml.Matrix4f
import org.lwjgl.glfw.GLFW.*

class Camera(
    val position: Vector3f = Vector3f(0f, 0f, 0f),
    target: Vector3f = Vector3f(0f, 0f, 0f)
) {
    // Camera vectors
    private val front = Vector3f(0f, 0f, -1f)
    private val up = Vector3f(0f, 1f, 0f)
    private val right = Vector3f(1f, 0f, 0f)
    
    // Store rotation as a Vector3f (x=pitch, y=yaw, z=roll)
    val rotation = Vector3f(0f, -90f, 0f)
    
    // View matrix calculated from position and rotation
    val viewMatrix = Matrix4f()

    init {
        // Initialize the camera to look at the target
        val direction = Vector3f(target).sub(position)
        if (!direction.equals(Vector3f(0f, 0f, 0f), 0.001f)) {
            lookAt(target)
        }
        updateCameraVectors()
    }

    fun update(window: Long) {
        val moveSpeed = 0.05f
        val rotSpeed = 1f

        // Handle movement (WASD)
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
        
        // Handle rotation (arrow keys)
        if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
            rotation.x -= rotSpeed  // pitch
        }
        if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
            rotation.x += rotSpeed  // pitch
        }
        if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS) {
            rotation.y -= rotSpeed  // yaw
        }
        if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS) {
            rotation.y += rotSpeed  // yaw
        }
        
        // Constrain pitch to avoid gimbal lock
        if (rotation.x > 89f) rotation.x = 89f
        if (rotation.x < -89f) rotation.x = -89f
        
        // Update camera vectors based on rotation
        updateCameraVectors()
    }
    
    private fun updateCameraVectors() {
        // Calculate front vector from Euler angles (pitch, yaw)
        val pitch = Math.toRadians(rotation.x.toDouble())
        val yaw = Math.toRadians(rotation.y.toDouble())
        
        front.x = (Math.cos(pitch) * Math.cos(yaw)).toFloat()
        front.y = Math.sin(pitch).toFloat()
        front.z = (Math.cos(pitch) * Math.sin(yaw)).toFloat()
        front.normalize()
        
        // Recalculate right and up vectors
        right.set(front).cross(Vector3f(0f, 1f, 0f)).normalize()
        up.set(right).cross(front).normalize()
        
        // Update view matrix
        viewMatrix.identity()
        viewMatrix.lookAt(position, Vector3f(position).add(front), up)
    }
    
    fun lookAt(target: Vector3f) {
        // Calculate direction to target
        val direction = Vector3f(target).sub(position).normalize()
        
        // Calculate rotation.y (yaw) from direction
        rotation.y = Math.toDegrees(Math.atan2(direction.z.toDouble(), direction.x.toDouble())).toFloat()
        
        // Calculate rotation.x (pitch) from direction
        val horizontalLength = Math.sqrt((direction.x * direction.x + direction.z * direction.z).toDouble()).toFloat()
        rotation.x = Math.toDegrees(Math.atan2(-direction.y.toDouble(), horizontalLength.toDouble())).toFloat()
        
        updateCameraVectors()
    }
}