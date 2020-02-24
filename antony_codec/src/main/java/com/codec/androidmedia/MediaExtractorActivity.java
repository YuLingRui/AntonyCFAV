package com.codec.androidmedia;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;
import android.text.TextUtils;
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
    public final static String AUDIO_MIME = "audio";
    public final static String VIDEO_MIME = "video";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_extractor);
        Button isolate_video_btn = findViewById(R.id.isolate_video);
        Button isolate_audio_btn = findViewById(R.id.isolate_audio);
        isolate_video_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        divideMedia(ResPath.Extractor_MP4_RES, VIDEO_MIME);
                    }
                }).start();
            }
        });
        isolate_audio_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        divideMedia(ResPath.Extractor_MP4_RES, AUDIO_MIME);
                    }
                }).start();
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

    public void divideMedia(String sourceMediaPath, String divideMime) {
        mMediaExtractor = new MediaExtractor();
        try {
            mMediaExtractor.setDataSource(sourceMediaPath);
            int count = mMediaExtractor.getTrackCount();
            for (int i = 0; i < count; i++) {
                MediaFormat mediaFormat = mMediaExtractor.getTrackFormat(i);
                String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
                if (!TextUtils.isEmpty(mime) && !mime.startsWith(divideMime)) {
                    continue;
                }
                int inputSize = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                ByteBuffer byteBuffer = ByteBuffer.allocate(inputSize);
                if (divideMime.equals(AUDIO_MIME)) {
                    mMediaMuxer = new MediaMuxer(ResPath.Extractor_MP3_DST, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                    int audioTrack = mMediaMuxer.addTrack(mediaFormat);
                    mMediaMuxer.start();
                    divideToOutputAudio(mMediaExtractor, mMediaMuxer, byteBuffer, mediaFormat, audioTrack, i);
                    finish(mMediaExtractor, mMediaMuxer);
                    break;
                } else if (divideMime.equals(VIDEO_MIME)) {
                    mMediaMuxer = new MediaMuxer(ResPath.Extractor_MP4_DST, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                    int videoTrack = mMediaMuxer.addTrack(mediaFormat);
                    mMediaMuxer.start();
                    divideToOutputVideo(mMediaExtractor, mMediaMuxer, byteBuffer, mediaFormat, videoTrack, i);
                    finish(mMediaExtractor, mMediaMuxer);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void divideToOutputVideo(MediaExtractor mediaExtractor, MediaMuxer mediaMuxer, ByteBuffer byteBuffer, MediaFormat mediaFormat, int videoTrack, int videoTrackIndex) {
        long videoDuration = mediaFormat.getLong(MediaFormat.KEY_DURATION);
        mediaExtractor.selectTrack(videoTrackIndex);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        bufferInfo.presentationTimeUs = 0;
        long videoFrameTimes;
        mediaExtractor.readSampleData(byteBuffer, 0);
        if (mediaExtractor.getSampleFlags() != MediaExtractor.SAMPLE_FLAG_SYNC) {
            mediaExtractor.advance();
        }
        mediaExtractor.readSampleData(byteBuffer, 0);
        mediaExtractor.advance();
        long firstFrame = mediaExtractor.getSampleTime();
        mediaExtractor.advance();
        mediaExtractor.readSampleData(byteBuffer, 0);
        long secondFrame = mediaExtractor.getSampleTime();

        videoFrameTimes = Math.abs(secondFrame - firstFrame);
        mediaExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
        int sampleSize;
        while ((sampleSize = mediaExtractor.readSampleData(byteBuffer, 0)) != -1) {
            long presentTime = bufferInfo.presentationTimeUs;
            if (presentTime >= videoDuration) {
                mediaExtractor.unselectTrack(videoTrackIndex);
                break;
            }
            mediaExtractor.advance();
            bufferInfo.offset = 0;
            bufferInfo.flags = mediaExtractor.getSampleFlags();
            bufferInfo.size = sampleSize;
            mediaMuxer.writeSampleData(videoTrack, byteBuffer, bufferInfo);
            bufferInfo.presentationTimeUs += videoFrameTimes;
        }
        mediaExtractor.unselectTrack(videoTrackIndex);
    }

    private void divideToOutputAudio(MediaExtractor mediaExtractor, MediaMuxer mediaMuxer, ByteBuffer byteBuffer, MediaFormat format, int audioTrack, int audioTrackIndex) {
        long audioSampleSize;
        int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        int channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        long audioDuration = format.getLong(MediaFormat.KEY_DURATION);
        mediaExtractor.selectTrack(audioTrackIndex);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        bufferInfo.presentationTimeUs = 0;
        mediaExtractor.readSampleData(byteBuffer, 0);
        if (mediaExtractor.getSampleTime() == 0) {
            mediaExtractor.advance();
        }
        mediaExtractor.readSampleData(byteBuffer, 0);
        long firstRateSample = mediaExtractor.getSampleTime();
        mediaExtractor.advance();
        mediaExtractor.readSampleData(byteBuffer, 0);
        long secondRateSample = mediaExtractor.getSampleTime();
        audioSampleSize = Math.abs(secondRateSample - firstRateSample);
        mediaExtractor.seekTo(0, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        int sampleSize;
        while ((sampleSize = mediaExtractor.readSampleData(byteBuffer, 0)) != -1) {
            int trackIndex = mediaExtractor.getSampleTrackIndex();
            long presentationTimeUs = bufferInfo.presentationTimeUs;
            Log.d(TAG, "trackIndex:" + trackIndex + ",presentationTimeUs:" + presentationTimeUs);
            if (presentationTimeUs >= audioDuration) {
                mediaExtractor.unselectTrack(audioTrackIndex);
                break;
            }
            mediaExtractor.advance();
            bufferInfo.offset = 0;
            bufferInfo.size = sampleSize;
            mediaMuxer.writeSampleData(audioTrack, byteBuffer, bufferInfo);//audioTrack为通过mediaMuxer.add()获取到的
            bufferInfo.presentationTimeUs += audioSampleSize;
        }
        mediaExtractor.unselectTrack(audioTrackIndex);
    }

    private void finish(MediaExtractor mediaExtractor, MediaMuxer mediaMuxer) {
        mediaMuxer.stop();
        mediaMuxer.release();
        mediaMuxer = null;
        mediaExtractor.release();
        mediaExtractor = null;
    }
}
