package com.antony.cfav.activity.audio;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.antony.cfav.ffmpeg.FFmpegHandler;
import com.antony.cfav.ffmpeg.FFmpegUtil;
import com.antony.cfav.R;
import com.antony.cfav.activity.BaseActivity;
import com.antony.cfav.res_path.ResPath;
import com.antony.cfav.utils.file.FileUtil;


/**
 * 音频相关操作页面
 */
public class AudioHandleActivity extends BaseActivity implements View.OnClickListener {

    private FFmpegHandler fmHandler;
    private ProgressBar progressBar;
    private LinearLayout layoutAudioHandle;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case FFmpegHandler.MSG_BEGIN:
                    progressBar.setVisibility(View.VISIBLE);
                    layoutAudioHandle.setVisibility(View.GONE);
                    break;
                case FFmpegHandler.MSG_CONTINUE:
                    break;
                case FFmpegHandler.MSG_END:
                    progressBar.setVisibility(View.GONE);
                    layoutAudioHandle.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    @Override
    public int getLayoutId() {
        return R.layout.activity_audio_handle;
    }

    @Override
    public void initView() {
        fmHandler = new FFmpegHandler(handler);
        progressBar = findViewById(R.id.progress_audio);
        layoutAudioHandle = findViewById(R.id.layout_audio_handle);
        Button btn_extract = findViewById(R.id.btn_extract);
        Button btn_extract_pcm = findViewById(R.id.btn_extract_pcm);
        Button btn_transform = findViewById(R.id.btn_transform);
        Button btn_cut = findViewById(R.id.btn_cut);
        Button btn_concat = findViewById(R.id.btn_concat);
        Button btn_mix = findViewById(R.id.btn_mix);
        Button btn_play_audio = findViewById(R.id.btn_play_audio);
        Button btn_play_opensl = findViewById(R.id.btn_play_opensl);
        Button btn_audio_encode = findViewById(R.id.btn_audio_encode);
        btn_extract.setOnClickListener(this);
        btn_extract_pcm.setOnClickListener(this);
        btn_transform.setOnClickListener(this);
        btn_cut.setOnClickListener(this);
        btn_concat.setOnClickListener(this);
        btn_mix.setOnClickListener(this);
        btn_play_audio.setOnClickListener(this);
        btn_play_opensl.setOnClickListener(this);
        btn_audio_encode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_extract: // 在.mp4文件中抽取音频生成 .aac
                audioDispose(R.id.btn_extract, ResPath.FateCinematicMP4, ResPath.ExtractAac);
                break;
            case R.id.btn_extract_pcm: // 在.mp4文件中抽取音频生成 .pcm
                audioDispose(R.id.btn_extract_pcm, ResPath.FateCinematicMP4, ResPath.ExtractPcm);
                break;
            case R.id.btn_transform: //转换格式{.mp3 ---转---> .wav 或 .aac}
                audioDispose(R.id.btn_transform, ResPath.YourAnswerMP3, ResPath.TransformWav);
                break;
            case R.id.btn_cut: // 裁剪
                audioDispose(R.id.btn_cut, ResPath.YourAnswerMP3, ResPath.CutMP3);
                break;
            case R.id.btn_concat: // 合并
                //audioDispose(R.id.btn_concat, ResPath.YourAnswerMP3, ResPath.ConcatMP3);   todo: mp3可能 不能合并
                break;
            case R.id.btn_mix: // 混音
                audioDispose(R.id.btn_mix, ResPath.YourAnswerMP3, ResPath.MixMp3);
                break;
            case R.id.btn_play_audio:// 使用ffmpeg解码mp3文件   使用AudioTrack播放
                audioDispose(R.id.btn_play_audio, ResPath.YourAnswerMP3, null);
                break;
            case R.id.btn_play_opensl: //
                break;
            case R.id.btn_audio_encode: // 编码{裸数据pcm ---编码---> wav, aac 如果需要编码成MP3,ffmpeg需要重新编译，把MP3库enable}
                audioDispose(R.id.btn_audio_encode, ResPath.ExtractPcm, ResPath.EncodeAac);
                break;
        }
    }

    /**
     * 音频相关处理
     *
     * @param type 处理类型
     * @param src  源文件
     * @param dst  目标文件
     */
    private void audioDispose(int type, final String src, final String dst) {
        String[] commands = null;
        if (!FileUtil.checkFileExist(src)) {
            Toast.makeText(this, "不存在这个文件！", Toast.LENGTH_LONG).show();
            return;
        }
        switch (type) {
            case R.id.btn_extract:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AudioPlayer player = new AudioPlayer();
                        player.extractAudio(src, dst);
                    }
                }).start();
                break;
            case R.id.btn_extract_pcm:
                //pcm数据的采样率，一般采样率为8000、16000、44100
                //pcm数据的声道，单声道为1，立体声道为2
                commands = FFmpegUtil.extractAudioPcm(src, 44100, 2, dst);
                break;
            case R.id.btn_transform:
                commands = FFmpegUtil.transformAudio(src, dst);
                break;
            case R.id.btn_cut:
                commands = FFmpegUtil.cutAudio(src, 20, 100, dst);
                break;
            case R.id.btn_concat:
                //commands = FFmpegUtil.concatAudio(src, ResPath.DjMP3, dst);
                break;
            case R.id.btn_mix:
                commands = FFmpegUtil.mixAudio(src, ResPath.DjMP3, dst);
                break;
            case R.id.btn_play_audio:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AudioPlayer player = new AudioPlayer();
                        Log.i("AudioPlayer", "path: " + src);
                        player.byAudioTrackPlay(src);
                    }
                }).start();
                return;
            case R.id.btn_audio_encode:
                //pcm数据的采样率，一般采样率为8000、16000、44100
                //pcm数据的声道，单声道为1，立体声道为2
                commands = FFmpegUtil.encodeAudio(src, 8000, 1, dst);
                break;
        }
        if (fmHandler != null && commands != null) {
            fmHandler.executeFFmpegCmd(commands);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fmHandler != null) {
            fmHandler = null;
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
