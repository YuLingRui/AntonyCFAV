package com.antony.cfav.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class AnGLSurfaceView extends GLSurfaceView {

    public AnGLSurfaceView(Context context) {
        this(context, null);
    }

    public AnGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //AnRender implements GLSurfaceView.Renderer
        AnRender render = new AnRender(context);
        setRenderer(render);
    }
}
