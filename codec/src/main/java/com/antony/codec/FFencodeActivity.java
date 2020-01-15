package com.antony.codec;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.antony.codec.camera.AutoFitTextureView;
import com.antony.codec.camera.CameraV2;

/**
 * 使用FFmpeg 软编码
 */
public class FFencodeActivity extends AppCompatActivity implements View.OnClickListener {
    private Button encodeMp4Start, encodeMp4Stop, encodeJpeg;
    private AutoFitTextureView mTextureView;
    private CameraV2 cameraV2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffencode);
        cameraV2 = new CameraV2();
        encodeMp4Start = findViewById(R.id.btn_encode_mp4_start);
        encodeMp4Stop = findViewById(R.id.btn_encode_mp4_stop);
        encodeJpeg = findViewById(R.id.btn_encode_jpeg);
        mTextureView = findViewById(R.id.texture_view);
        encodeMp4Start.setOnClickListener(this);
        encodeMp4Stop.setOnClickListener(this);
        encodeJpeg.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraV2.setPreviewView(this, mTextureView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraV2.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_encode_mp4_start:
                break;
            case R.id.btn_encode_mp4_stop:
                break;
            case R.id.btn_encode_jpeg:
                break;
        }
    }


}
