package com.antony.codec.camera;

import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.TextureView;

import com.antony.codec.FFencodeActivity;

/**
 * @auther antony
 * @data 2020/1/13 13 : 23
 * @function info
 */

public interface ICamera {
    //预览
    void setPreviewView(FFencodeActivity activity, AutoFitTextureView textureView);

    //开始编码
    void encodeStart(String outputPath);

    //结束编码
    void endcodeStop();

    //JPEG 编码
    void encodeJPEG(String jpegPath);

    void onDestroy();


}
