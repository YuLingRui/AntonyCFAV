package com.codec.interfaca;

import com.codec.view.AutoFitTextureView;

public interface ICamera {

    void setPreviewView(AutoFitTextureView surfaceView);

    void onDestroy();

    void encodeStart(String outputPath);

    void encodeStop();

    void encodeJPEG(String jpegPath);
}
