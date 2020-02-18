package com.codec.ffmpegmedia;

import androidx.appcompat.app.AppCompatActivity;
import util.FileUtil;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codec.R;
import com.codec.ffcodec.CodecHandler;
import com.codec.res_path.ResPath;

import static com.codec.res_path.ResPath.ENCODE_H264_EXAMPLE;

public class FFEncodeH264ExampleActivity extends AppCompatActivity implements View.OnClickListener {

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
                    progressBar.setVisibility(View.VISIBLE);
                    layoutHandle.setVisibility(View.GONE);
                    break;
                case CodecHandler.MSG_CONTINUE:
                    break;
                case CodecHandler.MSG_END:
                    progressBar.setVisibility(View.GONE);
                    layoutHandle.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffencode_h264_example);
        codecHandler = new CodecHandler(handler);
        progressBar = findViewById(R.id.progress_encode);
        layoutHandle = findViewById(R.id.layout_handle);
        Button btn_encode = findViewById(R.id.btn_encode);
        btn_encode.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_encode:
                String[] commands = {ResPath.ENCODE_H264_EXAMPLE, "libx264"};
                if (!FileUtil.checkFileExist(commands[0])) {
                    Toast.makeText(this, "不存在这个文件！", Toast.LENGTH_LONG).show();
                    return;
                }
                codecHandler.executeFFmpegCmd(commands);
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
