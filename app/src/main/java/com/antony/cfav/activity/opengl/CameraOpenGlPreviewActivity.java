package com.antony.cfav.activity.opengl;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.antony.cfav.R;
import com.antony.cfav.activity.BaseActivity;
import com.antony.cfav.camera2_opengl.CaTwoSurfaceView;
import com.antony.cfav.opengl.AnGLSurfaceView;

public class CameraOpenGlPreviewActivity extends BaseActivity {

    private CaTwoSurfaceView caTwoSurfaceView;


    @Override
    public int getLayoutId() {
        return R.layout.activity_camera_open_gl_preview;
    }

    @Override
    public void initView() {
        caTwoSurfaceView = findViewById(R.id.camera_opengl_preview);
    }
}
