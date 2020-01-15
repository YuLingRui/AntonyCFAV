package com.antony.cfav.activity.video;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.antony.cfav.R;
import com.antony.cfav.activity.BaseActivity;
import com.antony.cfav.ffmpeg.FFmpegHandler;
import com.antony.cfav.ffmpeg.FFmpegUtil;
import com.antony.cfav.res_path.ResPath;
import com.antony.cfav.utils.file.FileUtil;

import static com.antony.cfav.ffmpeg.FFmpegHandler.MSG_BEGIN;

public class VideoHandleActivity extends BaseActivity implements View.OnClickListener {

    private ProgressBar progressVideo;
    private LinearLayout layoutVideoHandle;
    private FFmpegHandler ffmpegHandler;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case FFmpegHandler.MSG_BEGIN:
                    progressVideo.setVisibility(View.VISIBLE);
                    layoutVideoHandle.setVisibility(View.GONE);
                    break;
                case FFmpegHandler.MSG_END:
                    progressVideo.setVisibility(View.GONE);
                    layoutVideoHandle.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    public int getLayoutId() {
        return R.layout.activity_video_handle;
    }

    @Override
    public void initView() {
        ffmpegHandler = new FFmpegHandler(mHandler);
        progressVideo = findViewById(R.id.progress_video);
        layoutVideoHandle = findViewById(R.id.layout_video_handle);
        Button btn_video_extract_h264 = findViewById(R.id.btn_video_extract_h264);
        Button btn_video_extract_h264_command = findViewById(R.id.btn_video_extract_h264_by_command);
        Button btn_video_extract_yuv_command = findViewById(R.id.btn_video_extract_yuv_by_command);
        Button btn_video_format_conversion = findViewById(R.id.btn_video_format_conversion);
        Button btn_video_transform = findViewById(R.id.btn_video_transform);
        Button btn_video_cut = findViewById(R.id.btn_video_cut);
        Button btn_video_cut_by_command = findViewById(R.id.btn_video_cut_by_command);
        Button btn_screen_shot = findViewById(R.id.btn_screen_shot);
        Button btn_water_mark = findViewById(R.id.btn_water_mark);
        Button btn_generate_gif = findViewById(R.id.btn_generate_gif);
        Button btn_combine_video = findViewById(R.id.btn_combine_video);
        Button btn_multi_video = findViewById(R.id.btn_multi_video);
        Button btn_reverse_video = findViewById(R.id.btn_reverse_video);
        Button btn_denoise_video = findViewById(R.id.btn_denoise_video);
        Button btn_to_image = findViewById(R.id.btn_to_image);
        Button btn_pip = findViewById(R.id.btn_pip);
        btn_video_extract_h264.setOnClickListener(this);
        btn_video_extract_h264_command.setOnClickListener(this);
        btn_video_extract_yuv_command.setOnClickListener(this);
        btn_video_format_conversion.setOnClickListener(this);
        btn_video_transform.setOnClickListener(this);
        btn_video_cut.setOnClickListener(this);
        btn_video_cut_by_command.setOnClickListener(this);
        btn_screen_shot.setOnClickListener(this);
        btn_water_mark.setOnClickListener(this);
        btn_generate_gif.setOnClickListener(this);
        btn_combine_video.setOnClickListener(this);
        btn_multi_video.setOnClickListener(this);
        btn_reverse_video.setOnClickListener(this);
        btn_denoise_video.setOnClickListener(this);
        btn_to_image.setOnClickListener(this);
        btn_pip.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_video_extract_h264://抽取h264数据
                videoDispose(R.id.btn_video_extract_h264, ResPath.FateCinematicMP4, ResPath.ExtractH264);
                break;
            case R.id.btn_video_extract_h264_by_command://使用命令 抽取h264数据
                videoDispose(R.id.btn_video_extract_h264_by_command, ResPath.FateCinematicMP4, ResPath.ExtractH264ByCommand);
                break;
            case R.id.btn_video_extract_yuv_by_command://使用命令 抽取YUV裸数据
                videoDispose(R.id.btn_video_extract_yuv_by_command, ResPath.FateCinematicMP4, ResPath.ExtractYuv);
                break;
            case R.id.btn_video_format_conversion:// mp4格式 转成  flv格式
                videoDispose(R.id.btn_video_format_conversion, ResPath.FateCinematicMP4, ResPath.ConversionFlv);
                break;
            case R.id.btn_video_cut://截取MP4中的一段
                videoDispose(R.id.btn_video_cut, ResPath.FateCinematicMP4, ResPath.CutMp4);
                break;
            case R.id.btn_video_cut_by_command://截取MP4中的一段, 使用ffmpeg命令
                videoDispose(R.id.btn_video_cut_by_command, ResPath.FateCinematicMP4, ResPath.CutMp4ByCommand);
                break;
            case R.id.btn_video_concat://视频拼接
                videoDispose(R.id.btn_video_concat, ResPath.FateCinematicMP4, ResPath.ConcatVideo);
                break;
            case R.id.btn_video_transform: //视频转码
                videoDispose(R.id.btn_video_transform, ResPath.FateCinematicMP4, ResPath.TransformFlv);
                break;
            case R.id.btn_screen_shot://视频截图
                videoDispose(R.id.btn_screen_shot, ResPath.FateCinematicMP4, ResPath.ScreenShotJpg);
                break;
            case R.id.btn_water_mark://视频添加水印
                videoDispose(R.id.btn_water_mark, ResPath.FateCinematicMP4, ResPath.WaterMark);
                break;
            case R.id.btn_pip: //画中画
                videoDispose(R.id.btn_pip, ResPath.FateCinematicMP4, ResPath.PicInPic);
                break;
            case R.id.btn_generate_gif://视频转成gif图
                videoDispose(R.id.btn_generate_gif, ResPath.FateCinematicMP4, ResPath.PicGif);
                break;
            case R.id.btn_combine_video://图片转视频

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
    private void videoDispose(int type, final String src, final String dst) {
        String[] commands = null;
        if (!FileUtil.checkFileExist(src)) {
            Toast.makeText(this, "不存在这个文件！", Toast.LENGTH_LONG).show();
            return;
        }
        switch (type) {
            case R.id.btn_video_extract_h264: //.mp4文件中抽取 .h264
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        VideoPlayer player = new VideoPlayer();
                        player.extractVideoH264(src, dst);
                    }
                }).start();
                break;
            case R.id.btn_video_extract_h264_by_command: //.mp4文件中抽取 .h264使用ffmpeg命令行
                commands = FFmpegUtil.extractVideoH264(src, dst);
                break;
            case R.id.btn_video_extract_yuv_by_command: //.mp4文件中抽取 .yuv使用ffmpeg命令行
                commands = FFmpegUtil.extractVideoYUV(src, dst);
                break;
            case R.id.btn_video_format_conversion:// mp4格式 转成  flv格式
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        VideoPlayer player = new VideoPlayer();
                        player.formatConversion(src, dst);
                    }
                }).start();
                break;
            case R.id.btn_video_cut://截取MP4中的一段
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        VideoPlayer player = new VideoPlayer();
                        player.cutSection(10, 50, src, dst);
                    }
                }).start();
                break;
            case R.id.btn_video_cut_by_command://截取MP4中的一段, 使用命令
                commands = FFmpegUtil.cutVideo(src, 10, 30, dst);
                break;
            case R.id.btn_video_concat://视频拼接【使用命令行】
                commands = FFmpegUtil.concatVideo(src, ResPath.CinematicMP4, dst);
                break;
            case R.id.btn_video_transform: //视频转码
                commands = FFmpegUtil.transformVideo(src, dst);
                break;
            case R.id.btn_screen_shot://视频截图
                int time = 18;
                String size = "1080X720";
                commands = FFmpegUtil.screenShot(src, time, size, dst);
                break;
            case R.id.btn_water_mark://视频添加水印
                String resolution = "720x1280";
                int bitRate = 1024;
                commands = FFmpegUtil.addWaterMark(src, ResPath.onejpg, resolution, bitRate, dst);
                break;
            case R.id.btn_pip: //画中画
                commands = FFmpegUtil.pinInPicVideo(src, ResPath.CinematicMP4, 200, 150, dst);
                break;
            case R.id.btn_generate_gif://视频转成gif图
                int gifStart = 30;
                int gifDuration = 5;
                String solution = "720x1280";//240x320、480x640、1080x1920
                int frameRate = 10;
                commands = FFmpegUtil.generateGif(src, gifStart, gifDuration, solution, frameRate, dst);
                break;
        }
        if (ffmpegHandler != null) {
            ffmpegHandler.executeFFmpegCmd(commands);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ffmpegHandler != null) {
            ffmpegHandler = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
