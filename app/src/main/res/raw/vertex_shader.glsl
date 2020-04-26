attribute vec4 av_Position; //定义顶点坐标 向量vec4  代表x(横坐标) y(纵坐标) z(z坐标) w(焦距)
attribute vec2 af_Position; //定义纹理坐标 向量vec2
varying vec2 v_texPosition;
void main() {
    v_texPosition = af_Position;
    gl_Position = av_Position;  //gl_Position是OpenGL中提供的变量，装载顶点坐标
}

//attribute 只能在Vertex(顶点)中使用
//varying 用于Vertex(顶点)和Fragment(纹理)之间传递值