package main;

import androidx.appcompat.app.AppCompatActivity;
import app.antony.antony_opengl.R;
import base.MyCustomRender;
import view.MyGLSurfaceView;
import view.OneGLSurfaceView;

import android.os.Bundle;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    private MyGLSurfaceView myGLSurfaceView;
    private LinearLayout left_top_layout;
    private LinearLayout right_bottom_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myGLSurfaceView = findViewById(R.id.my_surface_view);
        left_top_layout = findViewById(R.id.left_top_layout);
        right_bottom_layout = findViewById(R.id.right_bottom_layout);

        myGLSurfaceView.getMyCustomRender().setOnRenderCreateListener(new MyCustomRender.OnRenderCreateListener() {
            @Override
            public void onCreate(final int textid) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        OneGLSurfaceView one = new OneGLSurfaceView(MainActivity.this);
                        one.setTextureId(textid, 0);
                        one.setSurfaceAndEglContext(null, myGLSurfaceView.getEglContext());
                        left_top_layout.addView(one);
                    }
                });
            }
        });

       /* myGLSurfaceView.getMyCustomRender().setOnRenderCreateListener(new MyCustomRender.OnRenderCreateListener() {
            @Override
            public void onCreate(final int textid) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        OneGLSurfaceView two = new OneGLSurfaceView(MainActivity.this);
                        two.setTextureId(textid, 1);
                        two.setSurfaceAndEglContext(null, myGLSurfaceView.getEglContext());
                        right_bottom_layout.addView(two);
                    }
                });
            }
        });*/
    }
}
