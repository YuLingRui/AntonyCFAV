package com.antony.cfav.manager;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Process;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/***
 * 音频播放声音分为MediaPlayer和AudioTrack两种方案的。
 * MediaPlayer可以播放多种格式的声音文件，例如MP3，WAV，OGG，AAC，MIDI等。
 * 然而AudioTrack只能播放PCM数据流。当然两者之间还是有紧密的联系，
 * MediaPlayer在播放音频时，在framework层还是会创建AudioTrack，把解码后的PCM数流传递给AudioTrack，最后由AudioFlinger进行混音，传递音频给硬件播放出来。利用AudioTrack播放只是跳过Mediaplayer的解码部分而已。
 *
 * AudioTrack作用: AudioTrack是管理和播放单一音频资源的类。AudioTrack仅仅能播放已经解码的PCM流，用于PCM音频流的回放。
 *
 * AudioTrack实现PCM音频播放步骤:[五步走]
 *  1.配置基本参数
 *  2.获取最小缓冲区大小
 *  3.创建AudioTrack对象
 *  4.获取PCM文件，转成DataInputStream
 *  5.开启/停止播放
 */
public class AudioTrackManager {
    private AudioTrack mAudioTrack;
    /*播放文件的数据流*/
    private DataInputStream mDis;
    private Thread mRecordThread;
    private boolean isStart = false;
    private volatile static AudioTrackManager mInstance;

    //音频流类型
    private static final int mStreamType = AudioManager.STREAM_MUSIC;
    //指定采样率 （MediaRecoder 的采样率通常是8000Hz AAC的通常是44100Hz。 设置采样率为44100，目前为常用的采样率，官方文档表示这个值可以兼容所有的设置）
    private static final int mSampleRateInHz = 44100;
    //指定捕获音频的声道数目。在AudioFormat类中指定用于此的常量
    private static final int mChannelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO; //单声道
    /*指定音频量化位数 ,在AudioFormaat类中指定了以下各种可能的常量。通常我们选择ENCODING_PCM_16BIT和ENCODING_PCM_8BIT PCM代表的是脉冲编码调制，它实际上是原始音频样本。
        因此可以设置每个样本的分辨率为16位或者8位，16位将占用更多的空间和处理能力,表示的音频也更加接近真实。*/
    private static final int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //指定缓冲区大小。调用AudioRecord类的getMinBufferSize方法可以获得。
    private int mMinBufferSize;
    /*STREAM的意思是由用户在应用程序通过write方式把数据一次一次得写到audiotrack中。这个和我们在socket中发送数据一样，
        应用层从某个地方获取数据，例如通过编解码得到PCM数据，然后write到audiotrack。*/
    private static int mMode = AudioTrack.MODE_STREAM;

    /*私有构造函数*/
    private AudioTrackManager() {
        //初始化audiotrack
        initTrack();
    }

    /*单例引用*/
    public AudioTrackManager getInstance() {
        if (mInstance == null) {
            synchronized (AudioTrackManager.this) {
                if (mInstance == null) {
                    mInstance = new AudioTrackManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化 AudioTrack
     */
    private void initTrack() {
        /*根据采样率，采样精度，单双声道来得到frame的大小。注意，按照数字音频的知识，这个算出来的是一秒钟buffer的大小。*/
        mMinBufferSize = AudioRecord.getMinBufferSize(mSampleRateInHz, mChannelConfig, mAudioFormat);
        //创建AudioTrack
        mAudioTrack = new AudioTrack(mStreamType, mSampleRateInHz, mChannelConfig, mAudioFormat, mMinBufferSize, mMode);
    }

    /**
     * 播放文件
     *
     * @param src 文件路径
     */
    private void setPath(String src) {
        File file = new File(src);
        try {
            mDis = new DataInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动线程
     */
    private void startThread() {
        destroyThread();
        isStart = true;
        if (mRecordThread == null) {
            mRecordThread = new Thread(recordRunnable);
            mRecordThread.start();
        }
    }

    /**
     * 销毁线程
     */
    private void destroyThread() {
        if (mRecordThread != null && Thread.State.RUNNABLE == mRecordThread.getState()) {
            isStart = false;
            try {
                Thread.sleep(500);
                mRecordThread.isInterrupted();
            } catch (InterruptedException e) {
                e.printStackTrace();
                mRecordThread = null;
            } finally {
                mRecordThread = null;
            }
        }
    }

    /**
     * 播放线程
     */
    private Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                //设置线程的优先级
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                byte[] tempBuffer = new byte[mMinBufferSize];
                int readCount = 0;
                while (mDis.available() > 0) {
                    readCount = mDis.read(tempBuffer);
                    if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
                        continue;
                    }
                    if (readCount != 0 && readCount != -1) {
                        if (mAudioTrack.getState() == mAudioTrack.STATE_UNINITIALIZED) {
                            initTrack();
                        }
                        mAudioTrack.play();
                        mAudioTrack.write(tempBuffer, 0, readCount);
                    }
                }
                stopPlay();//播放完就停止播放
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };


    /**
     * 启动播放
     *
     * @param path
     */
    public void startPlay(String path) {
        try {
//            //AudioTrack未初始化
//            if(mAudioTrack.getState() == AudioTrack.STATE_UNINITIALIZED){
//                throw new RuntimeException("The AudioTrack is not uninitialized");
//            }//AudioRecord.getMinBufferSize的参数是否支持当前的硬件设备
//            else if (AudioTrack.ERROR_BAD_VALUE == mMinBufferSize || AudioTrack.ERROR == mMinBufferSize) {
//                throw new RuntimeException("AudioTrack Unable to getMinBufferSize");
//            }else{
            setPath(path);
            startThread();
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止播放
     */
    public void stopPlay() {
        try {
            destroyThread();//销毁线程
            if (mAudioTrack != null) {
                if (mAudioTrack.getState() == AudioRecord.STATE_INITIALIZED) {//初始化成功
                    mAudioTrack.stop();//停止播放
                }
                if (mAudioTrack != null) {
                    mAudioTrack.release();//释放audioTrack资源
                }
            }
            if (mDis != null) {
                mDis.close();//关闭数据输入流
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
