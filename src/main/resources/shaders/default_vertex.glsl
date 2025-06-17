#version 330 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 texCoord;
layout (location = 2) in vec3 normal;
layout (location = 3) in vec3 tangent;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform float time;

out vec3 fragPos;
out vec3 fragNormal;
out vec2 fragTexCoord;
out mat3 TBN;  // Tangent-Bitangent-Normal matrix for normal mapping

void main() {
    vec4 worldPos = model * vec4(position, 1.0);
    fragPos = worldPos.xyz;
    
    // Proper normal transformation using normal matrix
    mat3 normalMatrix = mat3(transpose(inverse(model)));
    fragNormal = normalize(normalMatrix * normal);
    
    fragTexCoord = texCoord;
    
    // Calculate TBN matrix for normal mapping (if needed)
    vec3 T = normalize(normalMatrix * tangent);
    vec3 N = fragNormal;
    vec3 B = cross(N, T);
    TBN = mat3(T, B, N);
    
    gl_Position = projection * view * worldPos;
}
