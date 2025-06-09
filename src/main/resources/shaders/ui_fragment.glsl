#version 330 core

in vec2 fragTexCoord;
out vec4 fragColor;

uniform sampler2D texSampler;
uniform vec4 color;
uniform bool hasTexture;

void main() {
    if (hasTexture) {
        fragColor = texture(texSampler, fragTexCoord) * color;
    } else {
        fragColor = color;
    }
}