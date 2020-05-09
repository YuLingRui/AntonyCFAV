package view;

import android.content.Context;
import android.util.AttributeSet;

import base.BaseSurfaceView;

public class OneGLSurfaceView extends BaseSurfaceView {

    private OneRender oneRender;

    public OneGLSurfaceView(Context context) {
        this(context, null);
    }

    public OneGLSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OneGLSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        oneRender = new OneRender(context);
        setRender(oneRender);
    }

    public void setTextureId(int textureId, int index) {
        if (oneRender != null) {
            oneRender.setTextureId(textureId, index);
        }
    }
}
