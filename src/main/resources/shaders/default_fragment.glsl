#version 330 core

in vec3 fragPos;
in vec3 fragNormal;
in vec2 fragTexCoord;

out vec4 fragColor;

// Material properties
uniform vec3 ambientColor;
uniform vec3 diffuseColor;
uniform vec3 specularColor;
uniform float shininess;

// Light properties
uniform vec3 lightPos;
uniform vec3 lightColor;
uniform vec3 viewPos;

// Texture uniforms
uniform sampler2D diffuseMap;
uniform sampler2D normalMap;
uniform sampler2D specularMap;

// Flags
uniform bool hasDiffuseMap;
uniform bool hasNormalMap;
uniform bool hasSpecularMap;

void main() {
    // Calculate normals from geometry using screen-space derivatives
    vec3 dFdxPos = dFdx(fragPos);
    vec3 dFdyPos = dFdy(fragPos);
    vec3 calculatedNormal = normalize(cross(dFdxPos, dFdyPos));
    
    // Use calculated normals for lighting
    vec3 normal = calculatedNormal;
    vec3 lightDir = normalize(lightPos - fragPos);
    
    // Simple diffuse lighting
    float NdotL = max(dot(normal, lightDir), 0.0);
    
    // Wrapped lighting to avoid pure black areas
    float diffuse = NdotL * 0.6 + 0.4;
    
    // Base color with lighting
    vec3 baseColor = vec3(0.7, 0.7, 0.8);
    vec3 result = baseColor * diffuse * lightColor;
    
    fragColor = vec4(result, 1.0);
}
