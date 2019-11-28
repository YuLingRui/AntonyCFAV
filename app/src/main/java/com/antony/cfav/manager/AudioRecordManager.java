package com.antony.cfav.manager;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 使用AudioRecord录制.pcm文件 常用步骤:
 * 构造一个 AudioRecord 对象。
 * 开始采集。
 * 读取采集的数据。
 * 停止采集。
 */
public class AudioRecordManager {
    private AudioRecord mAudioRecord;
    private volatile static AudioRecordManager mInstance;
    //指定音频源 这个和MediaRecorder是相同的 MediaRecorder.AudioSource.MIC指的是麦克风
    private static final int mAudioSource = MediaRecorder.AudioSource.MIC;
    //指定采样率 （MediaRecoder 的采样率通常是8000Hz AAC的通常是44100Hz。 设置采样率为44100，目前为常用的采样率，官方文档表示这个值可以兼容所有的设置）
    private static final int mSampleRateInHz = 44100;
    //指定捕获音频的声道数目。在AudioFormat类中指定用于此的常量
    private static final int mChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO; //单声道
    //指定音频量化位数 ,在AudioFormaat类中指定了以下各种可能的常量。通常我们选择ENCODING_PCM_16BIT和ENCODING_PCM_8BIT PCM代表的是脉冲编码调制，它实际上是原始音频样本。
    //因此可以设置每个样本的分辨率为16位或者8位，16位将占用更多的空间和处理能力,表示的音频也更加接近真实。
    private static final int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //指定缓冲区大小。调用AudioRecord类的getMinBufferSize方法可以获得。
    private int mBufferSizeInBytes;

    //储存AudioRecord录下来的文件
    private File mRecordingFile;
    //文件目录
    private File mFileRoot = null;
    //存放的目录路径名称
    private static final String mPathName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AudiioRecordFile";
    //保存的音频文件名
    private static final String mFileName = "audiorecordtest.pcm";
    //true表示正在录音
    private boolean isRecording = false;
    //缓冲区中数据写入到数据，因为需要使用IO操作，因此读取数据的过程应该在子线程中执行。
    private Thread mThread;
    private DataOutputStream mDataOutputStream;


    /*私有构造函数*/
    private AudioRecordManager() {
        //初始化audiorecord
        initAudioRecord();
    }

    /*单例引用*/
    public AudioRecordManager getInstance() {
        if (mInstance == null) {
            synchronized (AudioRecordManager.this) {
                if (mInstance == null) {
                    mInstance = new AudioRecordManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化 AudioTrack  文件夹目录
     */
    private void initAudioRecord() {
        /*根据采样率，采样精度，单双声道来得到frame的大小。注意，按照数字音频的知识，这个算出来的是一秒钟buffer的大小。*/
        mBufferSizeInBytes = AudioRecord.getMinBufferSize(mSampleRateInHz, mChannelConfig, mAudioFormat);
        //创建AudioRecord
        mAudioRecord = new AudioRecord(mAudioSource, mSampleRateInHz, mChannelConfig, mAudioFormat, mBufferSizeInBytes);

        mFileRoot = new File(mPathName);
        if (!mFileRoot.exists()) {
            //创建文件夹 todo:我们这里创建目录  手机内存/AudiioRecordFile/audiorecordtest.pcm
            boolean isMakdirs = mFileRoot.mkdirs();
        }

    }

    /**
     * 开始录音
     */
    public void startRecord() {
        //AudioRecord.getMinBufferSize的参数是否支持当前的硬件设备
        if (AudioRecord.ERROR_BAD_VALUE == mBufferSizeInBytes || AudioRecord.ERROR == mBufferSizeInBytes) {
            throw new RuntimeException("Unable to getMinBufferSize");
        } else {
            destroyThread();
            isRecording = true;
            if (mThread == null) {
                mThread = new Thread(runnable);
                //开启线程
                mThread.start();
            }
        }
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        isRecording = false;
        //停止录音，回收AudioRecord对象，释放内存
        if (mAudioRecord != null) {
            //getState 判断mAudioRecord是否真正初始化了
            if (mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                mAudioRecord.stop();
                mAudioRecord.release();
            }
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //标记为开始采集状态
            isRecording = true;
            mRecordingFile = new File(mFileRoot, mFileName);
            if (mRecordingFile.exists()) {
                boolean isDeleted = mRecordingFile.delete();
            }
            try {
                boolean isCreated = mRecordingFile.createNewFile();
                mDataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mRecordingFile)));
                byte[] buffer = new byte[mBufferSizeInBytes];
                //判断AudioRecord未初始化，停止录音的时候释放了，状态就为STATE_UNINITIALIZED
                if (mAudioRecord.getState() == mAudioRecord.STATE_UNINITIALIZED) {
                    initAudioRecord();
                }
                //开始录音
                mAudioRecord.startRecording();
                //getRecordingState获取当前AudioReroding是否正在采集数据的状态
                while (isRecording && mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    int bufferReadResult = mAudioRecord.read(buffer, 0, mBufferSizeInBytes);
                    for (int i = 0; i < bufferReadResult; i++) {
                        mDataOutputStream.write(buffer[i]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (mDataOutputStream != null) {
                        mDataOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     * 销毁线程
     */
    private void destroyThread() {
        try {
            isRecording = false;
            if (null != mThread && Thread.State.RUNNABLE == mThread.getState()) {
                try {
                    Thread.sleep(500);
                    mThread.interrupt();
                } catch (Exception e) {
                    mThread = null;
                }
            }
            mThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mThread = null;
        }
    }


}
