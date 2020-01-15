package com.antony.cfav.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.antony.cfav.R;
import com.antony.cfav.activity.audio.AudioHandleActivity;
import com.antony.cfav.activity.video.VideoHandleActivity;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        Button btn_audio = findViewById(R.id.btn_audio);
        Button btn_video = findViewById(R.id.btn_video);
        Button btn_media = findViewById(R.id.btn_media);
        Button btn_play = findViewById(R.id.btn_play);
        Button btn_push = findViewById(R.id.btn_push);
        Button btn_live = findViewById(R.id.btn_live);
        Button btn_filter = findViewById(R.id.btn_filter);
        Button btn_reverse = findViewById(R.id.btn_reverse);
        Button btn_preview = findViewById(R.id.btn_preview);
        btn_audio.setOnClickListener(this);
        btn_video.setOnClickListener(this);
        btn_media.setOnClickListener(this);
        btn_play.setOnClickListener(this);
        btn_push.setOnClickListener(this);
        btn_live.setOnClickListener(this);
        btn_filter.setOnClickListener(this);
        btn_reverse.setOnClickListener(this);
        btn_preview.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.btn_audio://音频处理
                intent.setClass(MainActivity.this, AudioHandleActivity.class);
                break;
            case R.id.btn_video://视频处理
                intent.setClass(MainActivity.this, VideoHandleActivity.class);
                break;
            case R.id.btn_codec: //编解码(H264  AAC)

                break;
            case R.id.btn_media://音视频处理
                //intent.setClass(MainActivity.this, MediaHandleActivity.class);
                break;
            case R.id.btn_play://音视频播放
                //intent.setClass(MainActivity.this, MediaPlayerActivity.class);
                break;
            case R.id.btn_push://FFmpeg推流
                //intent.setClass(MainActivity.this, PushActivity.class);
                break;
            case R.id.btn_live://实时推流直播:AAC音频编码、H264视频编码、RTMP推流
                //intent.setClass(MainActivity.this, LiveActivity.class);
                break;
            case R.id.btn_filter://滤镜特效
                //intent.setClass(MainActivity.this, FilterActivity.class);
                break;
            case R.id.btn_reverse://视频倒播
                //intent.setClass(MainActivity.this, VideoReverseActivity.class);
                break;
            case R.id.btn_preview://视频拖动实时预览
                //intent.setClass(MainActivity.this, VideoPreviewActivity.class);
                break;
            default:
                break;
        }
        startActivity(intent);
    }
}
