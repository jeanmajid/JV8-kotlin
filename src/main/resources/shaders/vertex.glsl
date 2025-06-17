#version 330 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 texCoord;
layout (location = 2) in vec3 normal;
layout (location = 3) in vec3 tangent;

out vec3 fragPos;
out vec2 fragTexCoord;
out vec3 fragNormal;
out mat3 TBN;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main()
{
    fragPos = vec3(model * vec4(position, 1.0));
    fragTexCoord = texCoord;
    
    // Calculate normal matrix (transpose of inverse of model matrix) for correct normal transformation
    mat3 normalMatrix = transpose(inverse(mat3(model)));
    fragNormal = normalMatrix * normal;
    
    // Calculate TBN matrix for normal mapping
    vec3 T = normalize(normalMatrix * tangent);
    // Re-orthogonalize T with respect to N
    vec3 N = normalize(fragNormal);
    T = normalize(T - dot(T, N) * N);
    // Calculate bitangent
    vec3 B = cross(N, T);
    
    TBN = mat3(T, B, N);
    
    gl_Position = projection * view * vec4(fragPos, 1.0);
}