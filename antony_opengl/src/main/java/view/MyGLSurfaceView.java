package view;

import android.content.Context;
import android.util.AttributeSet;

import base.BaseSurfaceView;
import base.MyCustomRender;

public class MyGLSurfaceView extends BaseSurfaceView {

    private MyCustomRender myCustomRender;

    public MyGLSurfaceView(Context context) {
        this(context, null);
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        myCustomRender = new MyCustomRender(context);
        setRender(myCustomRender);
    }

    public MyCustomRender getMyCustomRender() {
        return myCustomRender;
    }
}
