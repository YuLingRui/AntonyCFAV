package com.antony.cfav.camera2_opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class CaTwoSurfaceView extends GLSurfaceView {

    private CaTwoRender render;

    public CaTwoSurfaceView(Context context) {
        this(context, null);
    }

    public CaTwoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        render = new CaTwoRender(context, this);
        setEGLContextClientVersion(2);//设置EGL版本
        setRenderer(render);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);//手动渲染模式
    }
}
