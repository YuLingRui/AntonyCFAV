package com.antony.cfav.activity.audio;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.antony.cfav.R;
import com.antony.cfav.activity.BaseActivity;
import com.antony.cfav.utils.file.FileUtil;

import java.io.File;

/**
 * 音频相关操作页面
 */
public class AudioHandleActivity extends BaseActivity implements View.OnClickListener {
    // 你的答案.mp3
    private String song_path = Environment.getExternalStorageDirectory() + File.separator + "YourAnswer.mp3";
    // dj.mp3
    private String dj_path = Environment.getExternalStorageDirectory() + File.separator + "dj.mp3";
    private String cg = Environment.getExternalStorageDirectory() + File.separator + "cg.mp4";
    private String media_src = Environment.getExternalStorageDirectory() + File.separator + "media.mp4";


    private String my_aac = Environment.getExternalStorageDirectory() + File.separator + "my.aac";
    private ProgressBar progressBar;

    @Override
    public int getLayoutId() {
        return R.layout.activity_audio_handle;
    }

    @Override
    public void initView() {
        Button btn_extract = findViewById(R.id.btn_extract);
        Button btn_transform = findViewById(R.id.btn_transform);
        Button btn_cut = findViewById(R.id.btn_cut);
        Button btn_concat = findViewById(R.id.btn_concat);
        Button btn_mix = findViewById(R.id.btn_mix);
        Button btn_play_audio = findViewById(R.id.btn_play_audio);
        Button btn_play_opensl = findViewById(R.id.btn_play_opensl);
        Button btn_audio_encode = findViewById(R.id.btn_audio_encode);
        Button btn_pcm_concat = findViewById(R.id.btn_pcm_concat);
        btn_extract.setOnClickListener(this);
        btn_transform.setOnClickListener(this);
        btn_cut.setOnClickListener(this);
        btn_concat.setOnClickListener(this);
        btn_mix.setOnClickListener(this);
        btn_play_audio.setOnClickListener(this);
        btn_play_opensl.setOnClickListener(this);
        btn_audio_encode.setOnClickListener(this);
        btn_pcm_concat.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_extract: //0 在.mp4文件中抽取音频生成 .aac
                audioDispose(0, cg, my_aac);
                break;
            case R.id.btn_transform: //0
                break;
            case R.id.btn_cut: //1
                break;
            case R.id.btn_concat: //2
                break;
            case R.id.btn_mix: //3
                break;
            case R.id.btn_play_audio://4 使用ffmpeg解码mp3文件   使用AudioTrack播放
                audioDispose(4, song_path, null);
                break;
            case R.id.btn_play_opensl:
                break;
            case R.id.btn_audio_encode:
                break;
            case R.id.btn_pcm_concat:
                break;
        }
    }

    /**
     * 音频相关处理
     *
     * @param type 处理类型
     * @param src  处理的mp3文件
     */
    private void audioDispose(int type, final String src, final String dst) {
        if (!FileUtil.checkFileExist(src)) {
            Toast.makeText(this, "不存在这个文件！", Toast.LENGTH_LONG).show();
            return;
        }
        if (!FileUtil.isAudio(src)) {
            Toast.makeText(this, "非音频文件,无法进行音频相关操作！", Toast.LENGTH_LONG).show();
            return;
        }
        switch (type) {
            case 0:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AudioPlayer player = new AudioPlayer();
                        player.extractAudio(src, dst);
                    }
                }).start();
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AudioPlayer player = new AudioPlayer();
                        Log.i("AudioPlayer", "path: " + src);
                        player.byAudioTrackPlay(src);
                    }
                }).start();
                return;
            case 5:
                break;
            case 6:
                break;
            case 7:
                break;
        }
    }
}
