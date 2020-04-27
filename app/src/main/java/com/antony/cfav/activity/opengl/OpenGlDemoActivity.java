package com.antony.cfav.activity.opengl;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.antony.cfav.R;
import com.antony.cfav.opengl.AnGLSurfaceView;

public class OpenGlDemoActivity extends AppCompatActivity {

    private AnGLSurfaceView anGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_gl_demo);
        anGLSurfaceView = findViewById(R.id.an_gl_view);
    }
}
