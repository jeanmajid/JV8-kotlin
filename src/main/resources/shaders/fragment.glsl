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
    // Ambient
    vec3 ambient = ambientColor;
    
    // Diffuse
    vec3 normal = normalize(fragNormal);
    vec3 lightDir = normalize(lightPos - fragPos);
    float diff = max(dot(normal, lightDir), 0.0);
    vec3 diffuse = diffuseColor * diff;
    
    // Specular
    vec3 viewDir = normalize(viewPos - fragPos);
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), shininess);
    vec3 specular = specularColor * spec;
    
    // Combine results
    vec3 result = (ambient + diffuse + specular) * lightColor;
    
    // Apply textures if available
    if (hasDiffuseMap) {
        vec3 texColor = texture(diffuseMap, fragTexCoord).rgb;
        result *= texColor;
    }
    
    fragColor = vec4(result, 1.0);
}