package com.codec.ffmpegmedia;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.codec.R;
import com.codec.ffcodec.CodecHandler;
import com.codec.res_path.ResPath;

/**
 * 模拟FFmpeg 解码
 */
public class FFDecodeH264ExampleActivity extends AppCompatActivity implements View.OnClickListener {

    private CodecHandler codecHandler;
    private ProgressBar progressBar;
    private LinearLayout layoutHandle;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CodecHandler.MSG_BEGIN:
                    Log.i("FFcodecNative", "......开始。。。");
                    progressBar.setVisibility(View.VISIBLE);
                    layoutHandle.setVisibility(View.GONE);
                    break;
                case CodecHandler.MSG_CONTINUE:
                    Log.i("FFcodecNative", "");
                    break;
                case CodecHandler.MSG_END:
                    Log.i("FFcodecNative", "......结束。。。");
                    progressBar.setVisibility(View.GONE);
                    layoutHandle.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffdecode_h264_example);
        codecHandler = new CodecHandler(handler);
        progressBar = findViewById(R.id.progress_encode);
        layoutHandle = findViewById(R.id.layout_handle);
        Button btn_decode = findViewById(R.id.btn_decode);
        btn_decode.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_decode:
                String[] commands = {ResPath.DECODE_H264_RES, ResPath.VIDEO_PATH};
                /*if (!FileUtil.checkFileExist(commands[0])) {
                    Toast.makeText(this, "不存在这个文件！", Toast.LENGTH_LONG).show();
                    return;
                }*/
                codecHandler.executeFFCmdDecode(commands);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (codecHandler != null) {
            codecHandler = null;
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
