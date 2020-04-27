precision mediump float;
varying vec2 v_texPosition;
uniform sampler2D sTexture;
void main() {
    gl_FragColor=texture2D(sTexture, v_texPosition);
}
