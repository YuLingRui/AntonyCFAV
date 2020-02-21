package com.codec.androidmedia;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.codec.R;
import com.codec.res_path.ResPath;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaExtractorActivity extends AppCompatActivity {

    private final String TAG = "MediaExtractorActivity";
    MediaExtractor mMediaExtractor = null;
    MediaMuxer mMediaMuxer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_extractor);
        Button isolate_video_btn = findViewById(R.id.isolate_video);
        Button isolate_audio_btn = findViewById(R.id.isolate_audio);
        isolate_video_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        isolate_audio_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean flag = setExtractorProcess();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();*/
    }

    private boolean setExtractorProcess() throws IOException {
        mMediaExtractor = new MediaExtractor();
        mMediaExtractor.setDataSource(ResPath.Extractor_MP4_RES);
        int mVideoTrackIndex = -1;
        int framerate = 0;
        //遍历源文件通道数
        Log.e(TAG, "轨道数量 = " + mMediaExtractor.getTrackCount());
        for (int i = 0; i < mMediaExtractor.getTrackCount(); i++) {
            //获取指定（index）的通道格式
            MediaFormat format = mMediaExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            Log.e(TAG, i + "编号通道格式 = " + mime);
            if (!mime.startsWith("video/")) {
                continue;
            }
            framerate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
            mMediaExtractor.selectTrack(i);
            mMediaMuxer = new MediaMuxer(ResPath.Extractor_MP4_DST, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            mVideoTrackIndex = mMediaMuxer.addTrack(format);
            mMediaMuxer.start();
        }
        if (mMediaMuxer == null) {
            return false;
        }

        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        info.presentationTimeUs = 0;
        ByteBuffer buffer = ByteBuffer.allocate(500 * 1024);
        int sampleSize = 0;
        //把指定通道中的数据按偏移量读取到ByteBuffer中
        while ((sampleSize = mMediaExtractor.readSampleData(buffer, 0)) > 0) {
            info.offset = 0;
            info.size = sampleSize;
            info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
            info.presentationTimeUs += 1000 * 1000 / framerate;
            mMediaMuxer.writeSampleData(mVideoTrackIndex, buffer, info);
            //读取下一帧数据
            mMediaExtractor.advance();
        }

        mMediaExtractor.release();

        mMediaMuxer.stop();
        mMediaMuxer.release();
        return true;
    }
}
