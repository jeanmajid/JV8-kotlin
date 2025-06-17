#version 330 core

in vec3 fragPos;
in vec2 fragTexCoord;
in vec3 fragNormal;
in mat3 TBN;

out vec4 fragColor;

// Material structure
struct Material {
    vec3 diffuse;
    vec3 ambient;
    vec3 specular;
    float shininess;
    float alpha;
    
    int hasDiffuseMap;
    int hasNormalMap;
    int hasSpecularMap;
    
    sampler2D diffuseMap;
    sampler2D normalMap;
    sampler2D specularMap;
};

// Light structure
struct Light {
    vec3 position;
    vec3 color;
    float intensity;
};

uniform Material material;
uniform Light lights[4]; // Array of lights
uniform int numLights;  // Actual number of lights being used
uniform vec3 viewPos;   // Camera position for specular calculation

void main()
{
    // Get material properties
    vec3 albedo;
    vec3 ambient;
    vec3 specularStrength;
    float alpha;
    
    // Use texture if available, otherwise use material color
    if (material.hasDiffuseMap == 1) {
        vec4 texColor = texture(material.diffuseMap, fragTexCoord);
        albedo = texColor.rgb;
        alpha = texColor.a * material.alpha;
    } else {
        albedo = material.diffuse;
        alpha = material.alpha;
    }
    
    // Get ambient color
    ambient = material.ambient * albedo;
    
    // Get specular strength
    if (material.hasSpecularMap == 1) {
        specularStrength = texture(material.specularMap, fragTexCoord).rgb;
    } else {
        specularStrength = material.specular;
    }
    
    // Prepare normal for lighting calculation
    vec3 norm;
    if (material.hasNormalMap == 1) {
        // Sample normal from texture and transform to range [-1, 1]
        norm = texture(material.normalMap, fragTexCoord).rgb;
        norm = norm * 2.0 - 1.0;
        // Transform normal vector to world space using TBN matrix
        norm = normalize(TBN * norm);
    } else {
        norm = normalize(fragNormal);
    }
    
    // Calculate view direction
    vec3 viewDir = normalize(viewPos - fragPos);
    
    // Calculate lighting
    vec3 lighting = ambient; // Start with ambient light
    
    // Process all active lights
    for (int i = 0; i < numLights; i++) {
        if (i >= 4) break; // Safety check
        
        Light light = lights[i];
        
        // Calculate light direction and distance
        vec3 lightDir = normalize(light.position - fragPos);
        float distance = length(light.position - fragPos);
        float attenuation = 1.0 / (1.0 + 0.09 * distance + 0.032 * distance * distance);
        
        // Diffuse shading
        float diff = max(dot(norm, lightDir), 0.0);
        vec3 diffuse = diff * albedo * light.color * light.intensity;
        
        // Specular shading - Blinn-Phong
        vec3 halfwayDir = normalize(lightDir + viewDir);
        float spec = pow(max(dot(norm, halfwayDir), 0.0), material.shininess);
        vec3 specular = spec * specularStrength * light.color * light.intensity;
        
        // Apply attenuation to diffuse and specular
        lighting += (diffuse + specular) * attenuation;
    }    // Final color
    fragColor = vec4(lighting, alpha);
    
    // Only discard fragments with extremely low alpha (for performance)
    if (alpha < 0.01)
        discard;
}