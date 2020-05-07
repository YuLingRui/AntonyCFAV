attribute vec4 av_Position;
attribute vec2 af_Position;
varying vec2 v_texPosition;
uniform mat4 u_Matrix;
void main() {
    v_texPosition = af_Position;
    gl_Position = av_Position * u_Matrix;
}
