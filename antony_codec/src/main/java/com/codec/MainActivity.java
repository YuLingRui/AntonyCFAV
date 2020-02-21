package com.codec;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.codec.androidmedia.MediaExtractorActivity;
import com.codec.ffmpegmedia.CameraV1FFEncodeActivity;
import com.codec.ffmpegmedia.CameraV2FFEncodeActivity;
import com.codec.ffmpegmedia.FFDecodeH264ExampleActivity;
import com.codec.ffmpegmedia.FFEncodeH264ExampleActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    /*权限请求Code*/
    private final static int PERMISSION_REQUEST_CODE = 1234;
    /*我们需要使用的权限*/
    private String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*SDK>6.0 权限申请*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(permissions[1]) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(permissions[2]) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(permissions[3]) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
        Button naked_data_encode = findViewById(R.id.naked_data_encode);
        Button simulate_data_encode = findViewById(R.id.simulate_data_encode);
        Button multimedia_decdec = findViewById(R.id.multimedia_decdec);
        Button extractor_Muxer = findViewById(R.id.android_extractor_Muxer);
        naked_data_encode.setOnClickListener(this);
        simulate_data_encode.setOnClickListener(this);
        multimedia_decdec.setOnClickListener(this);
        extractor_Muxer.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.naked_data_encode:
                //TODO: 有bug 无法硬编
                intent.setClass(MainActivity.this, CameraV1FFEncodeActivity.class);
                break;
            case R.id.simulate_data_encode:
                //TODO: 现在不能使用 没有x264库
                intent.setClass(MainActivity.this, FFEncodeH264ExampleActivity.class);
                break;
            case R.id.multimedia_decdec:
                //解码H264  生成一些图片
                intent.setClass(MainActivity.this, FFDecodeH264ExampleActivity.class);
                break;
            case R.id.android_extractor_Muxer:
                //MediaExtractor 拆解MP4多媒体  MediaMuxer生成新的MP4
                intent.setClass(MainActivity.this, MediaExtractorActivity.class);
                break;
        }
        startActivity(intent);
    }


    //权限反馈
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                /*PackageManager.PERMISSION_GRANTED  权限被许可*/
                /*PackageManager.PERMISSION_DENIED  没有权限；拒绝访问*/
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showWaringDialog("无法读取内存卡！");
                } else if (grantResults.length > 0 && grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    showWaringDialog("无法读取内存卡！");
                } else if (grantResults.length > 0 && grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                    showWaringDialog("无法使用相机！");
                } else if (grantResults.length > 0 && grantResults[3] != PackageManager.PERMISSION_GRANTED) {
                    showWaringDialog("无法录制音频！");
                }
                break;
        }
    }

    private void showWaringDialog(String msg) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("警告！")
                .setMessage(msg)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 一般情况下如果用户不授权的话，功能是无法运行的，我们暂时做退出处理
                        finish();
                    }
                }).setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 一般情况下如果用户不授权的话，功能是无法运行的，我们暂时做退出处理
                        finish();
                    }
                }).show();
    }
}
