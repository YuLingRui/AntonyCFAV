package com.codec.ffmpegmedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import com.codec.R;
import com.codec.ffcodec.NativeEncoder;
import com.codec.view.AutoFitTextureView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Camera2收集裸数据， ffmpeg进行编码
 */
public class CameraV2FFEncodeActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "CameraV2FFEncodeActivity";
    private static final String TAG_PREVIEW = "预览";
    /*Camera 摄像头ID*/
    private String mCameraId;
    /*处理静态图像捕获的{@link ImageReader}。*/
    private ImageReader mImageReader;
    /*对打开的{@link CameraDevice}的引用。当前摄像头*/
    private CameraDevice mCameraDevice;
    /*用于相机预览的{@link CameraCaptureSession}。 */
    private CameraCaptureSession mCaptureSession;
    /*{@link CaptureRequest} 通过 CaptureRequest.Builder生成*/
    private CaptureRequest mPreviewRequest;
    /*{@link CaptureRequest.Builder} 用于相机预览*/
    private CaptureRequest.Builder mPreviewRequestBuilder;
    /*用于开始预览的呈现页面*/
    private AutoFitTextureView autoFitTextureView;
    /*这是我们需要开始预览的输出面 【预览的数据】*/
    private Surface mPreviewSurface;
    /*{@link android.util。相机预览的尺寸}。*/
    private Size mPreviewSize;


    private Button btn_encode_mp4_start, btn_encode_mp4_stop, btn_encode_jpeg;
    private NativeEncoder nativeEncoder;
    private EncodeRunable encodeRunable;
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_encode);
        autoFitTextureView = findViewById(R.id.auto_textureview);
        btn_encode_mp4_start = findViewById(R.id.btn_encode_mp4_start);
        btn_encode_jpeg = findViewById(R.id.btn_encode_jpeg);
        btn_encode_mp4_stop = findViewById(R.id.btn_encode_mp4_stop);
        autoFitTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        btn_encode_mp4_start.setOnClickListener(this);
        btn_encode_mp4_stop.setOnClickListener(this);
        btn_encode_jpeg.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();//开启后台线程
        /*当屏幕关闭并重新打开时，表面结构已经完成将不会调用“onSurfaceTextureAvailable”。那样的话，我们就可以开门了
          一个相机，并开始预览从这里(否则，我们等待，直到表面准备好了SurfaceTextureListener)。*/
        if (autoFitTextureView.isAvailable()) {
            openCamera(); //打开摄像机
        } else {
            autoFitTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();//关闭摄像机
        stopBackgroundThread(); //关闭后台线程
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_encode_mp4_start:

                break;
            case R.id.btn_encode_mp4_stop:

                break;
            case R.id.btn_encode_jpeg:
                break;
        }
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            setUpCamera(width, height);
            configureTransform(width, height);
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    /**
     * 设置与摄像机相关的成员变量。
     *
     * @param width  相机预览可用尺寸的宽度
     * @param height 相机预览可用尺寸的高度
     */
    private void setUpCamera(int width, int height) {
        //相机管理
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (manager == null) {
            return;
        }
        try {
            for (String cameraId : manager.getCameraIdList()) {
                //当前 CameraID摄像头的 相关数据
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                //此例 只是用前置摄像头  LENS_FACING[镜头面对方位]
                //在这个范例中我们不使用前置摄像头。【忽略前置摄像头】
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING); //LENS_FACING[镜头面对方位]
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    //TODO: LENS_FACING_FRONT 前置摄像头
                    continue;
                }
                // 获取StreamConfigurationMap，它是管理摄像头支持的所有输出格式和尺寸
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                //整合 找出适应 当前的预览尺寸【宽高】
                mPreviewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                //屏幕的总体方向。可能是
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    autoFitTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    autoFitTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }
                mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.YUV_420_888, 1);
                // mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);
                mCameraId = cameraId;
                break;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // 选择sizeMap中大于并且最接近width和height的size
    private Size getOptimalSize(Size[] sizeMap, int width, int height) {
        List<Size> sizeList = new ArrayList<>();
        for (Size option : sizeMap) {
            if (width > height) {
                if (option.getWidth() > width && option.getHeight() > height) {
                    sizeList.add(option);
                }
            } else {
                if (option.getWidth() > height && option.getHeight() > width) {
                    sizeList.add(option);
                }
            }
        }
        if (sizeList.size() > 0) {
            return Collections.min(sizeList, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                }
            });
        }
        return sizeMap[0];
    }

    /**
     * 配置必要的{@link android.graphics.Matrix}转换为“mTextureView”。
     * 这个方法应该在 "setupcameraoutput()" 中确定相机预览大小并确定 'mTextureView' 的大小后调用。
     *
     * @param viewWidth  mTextureView 的宽度
     * @param viewHeight mTextureView 的高度
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == autoFitTextureView || null == mPreviewSize) {
            return;
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) viewHeight / mPreviewSize.getHeight(), (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        autoFitTextureView.setTransform(matrix);
    }

    //打开相机
    private void openCamera() {
        //检查权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Must need  Camera  permission");
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(CAMERA_SERVICE);
        if (manager == null) {
            return;
        }
        //打开相机，第一个参数指示打开哪个摄像头，第二个参数stateCallback为相机的状态回调接口，
        // 第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
        try {
            manager.openCamera(mCameraId, stateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //关闭摄像机
    private void closeCamera() {
        if (null != mCaptureSession) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (null != mImageReader) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    /**
     * 当前相机 状态检测回调
     */
    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            //开启预览
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.i(TAG, " CameraDevice.StateCallback  onDisconnected。。。。");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, " CameraDevice.StateCallback  onDisconnected。。。。");
        }
    };

    private void startPreview() {
        //设置ImageReader属性
        //setupImageReader();
        SurfaceTexture mSurfaceTexture = autoFitTextureView.getSurfaceTexture();
        //设置TextureView的缓冲区大小
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        //获取Surface显示预览数据
        mPreviewSurface = new Surface(mSurfaceTexture);
        try {
            getPreviewRequestBuilder();
            //创建相机捕获会话，第一个参数是捕获数据的输出Surface列表，第二个参数是CameraCaptureSession的状态回调接口，当它创建好后会回调onConfigured方法，第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            mCameraDevice.createCaptureSession(Arrays.asList(mPreviewSurface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            mCaptureSession = session;
                            repeatPreview();
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Log.e(TAG, " startPreview onConfigureFailed。。。。");
                        }
                    }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 这里我们准备好   CaptureRequest.Builder
     * 创建预览请求的Builder（TEMPLATE_PREVIEW表示预览请求）
     */
    private void getPreviewRequestBuilder() {
        try {
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        //设置预览的显示界面
        mPreviewRequestBuilder.addTarget(mPreviewSurface);
        MeteringRectangle[] meteringRectangles = mPreviewRequestBuilder.get(CaptureRequest.CONTROL_AF_REGIONS);
        if (meteringRectangles != null && meteringRectangles.length > 0) {
            Log.d(TAG, "PreviewRequestBuilder: AF_REGIONS=" + meteringRectangles[0].getRect().toString());
        }
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_IDLE);
    }

    /**
     * 准备CaptureRequest  做反复捕获数据 一直预览
     */
    private void repeatPreview() {
        mPreviewRequestBuilder.setTag(TAG_PREVIEW);
        mPreviewRequest = mPreviewRequestBuilder.build();
        //设置反复捕获数据的请求，这样预览界面就会一直有数据显示
        try {
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mPreviewCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /*private void setupImageReader() {
        //获取帧数据不能用 ImageFormat.JPEG 格式，否则你会发现预览非常卡的，因为渲染 JPEG 数据量过大，导致掉帧，所以预览帧请使用其他编码格式。
        //前三个参数分别是需要的尺寸和格式，最后一个参数代表每次最多获取几帧数据
        mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(), ImageFormat.NV21, 1);
        //监听ImageReader的事件，当有图像流数据可用时会回调onImageAvailable方法，它的参数就是预览帧数据，可以对这帧数据进行处理
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Log.i(TAG, "Image Available!");
                Image image = reader.acquireLatestImage();
                // 开启线程异步保存图片
                //new Thread(new ImageSaver(image)).start();
            }
        }, null);
    }*/

    private CameraCaptureSession.CaptureCallback mPreviewCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            Log.e(TAG, " onCaptureCompleted session。。。。");

        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            Log.d(TAG, " onCaptureProgressed session。。。。");
        }
    };

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            //FFmpegHandler.getInstance().pushCameraData(yBytes, yBytes.length, uBytes, uBytes.length, vBytes, vBytes.length);
            // Image image = reader.acquireLatestImage();
//            encodeRunable = new EncodeRunable(image);
//            mBackgroundHandler.post(encodeRunable);
            Image image = reader.acquireLatestImage();
            //我们可以将这帧数据转成字节数组，类似于Camera1的PreviewCallback回调的预览帧数据
            if (image == null) {
                return;
            }
            final Image.Plane[] planes = image.getPlanes();

            //数据有效宽度，一般的，图片width <= rowStride，这也是导致byte[].length <= capacity的原因
            // 所以我们只取width部分
            int width = image.getWidth();
            int height = image.getHeight();

            //此处用来装填最终的YUV数据，需要1.5倍的图片大小，因为Y U V 比例为 4:1:1
            byte[] yBytes = new byte[width * height];
            //目标数组的装填到的位置
            int dstIndex = 0;

            //临时存储uv数据的
            byte uBytes[] = new byte[width * height / 4];
            byte vBytes[] = new byte[width * height / 4];
            int uIndex = 0;
            int vIndex = 0;

            int pixelsStride, rowStride;
            for (int i = 0; i < planes.length; i++) {
                pixelsStride = planes[i].getPixelStride();
                rowStride = planes[i].getRowStride();

                ByteBuffer buffer = planes[i].getBuffer();

                //如果pixelsStride==2，一般的Y的buffer长度=640*480，UV的长度=640*480/2-1
                //源数据的索引，y的数据是byte中连续的，u的数据是v向左移以为生成的，两者都是偶数位为有效数据
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);

                int srcIndex = 0;
                if (i == 0) {
                    //直接取出来所有Y的有效区域，也可以存储成一个临时的bytes，到下一步再copy
                    for (int j = 0; j < height; j++) {
                        System.arraycopy(bytes, srcIndex, yBytes, dstIndex, width);
                        srcIndex += rowStride;
                        dstIndex += width;
                    }
                } else if (i == 1) {
                    //根据pixelsStride取相应的数据
                    for (int j = 0; j < height / 2; j++) {
                        for (int k = 0; k < width / 2; k++) {
                            uBytes[uIndex++] = bytes[srcIndex];
                            srcIndex += pixelsStride;
                        }
                        if (pixelsStride == 2) {
                            srcIndex += rowStride - width;
                        } else if (pixelsStride == 1) {
                            srcIndex += rowStride - width / 2;
                        }
                    }
                } else if (i == 2) {
                    //根据pixelsStride取相应的数据
                    for (int j = 0; j < height / 2; j++) {
                        for (int k = 0; k < width / 2; k++) {
                            vBytes[vIndex++] = bytes[srcIndex];
                            srcIndex += pixelsStride;
                        }
                        if (pixelsStride == 2) {
                            srcIndex += rowStride - width;
                        } else if (pixelsStride == 1) {
                            srcIndex += rowStride - width / 2;
                        }
                    }
                }
            }
            //FFmpegHandler.getInstance().pushCameraData(yBytes, yBytes.length, uBytes, uBytes.length, vBytes, vBytes.length);
            image.close();
        }
    };

    //TODO: 启动一个后台线程及其{@link Handler}。
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    //TODO: 停止个后台线程及其{@link Handler}。
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class EncodeRunable implements Runnable {
        /*JPEG图像*/
        private Image mImage;

        private EncodeRunable(Image image) {
            mImage = image;
        }

        @Override
        public void run() {
            //我们可以将这帧数据转成字节数组，类似于Camera1的PreviewCallback回调的预览帧数据
            if (mImage == null) {
                return;
            }
            final Image.Plane[] planes = mImage.getPlanes();
            //数据有效宽度，一般的，图片width <= rowStride，这也是导致byte[].length <= capacity的原因
            // 所以我们只取width部分
            int width = mImage.getWidth();
            int height = mImage.getHeight();

            //此处用来装填最终的YUV数据，需要1.5倍的图片大小，因为Y U V 比例为 4:1:1
            byte[] yBytes = new byte[width * height];
            //目标数组的装填到的位置
            int dstIndex = 0;

            //临时存储uv数据的
            byte uBytes[] = new byte[width * height / 4];
            byte vBytes[] = new byte[width * height / 4];
            int uIndex = 0;
            int vIndex = 0;

            int pixelsStride, rowStride;
            for (int i = 0; i < planes.length; i++) {
                pixelsStride = planes[i].getPixelStride();
                rowStride = planes[i].getRowStride();

                ByteBuffer buffer = planes[i].getBuffer();

                //如果pixelsStride==2，一般的Y的buffer长度=640*480，UV的长度=640*480/2-1
                //源数据的索引，y的数据是byte中连续的，u的数据是v向左移以为生成的，两者都是偶数位为有效数据
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);

                int srcIndex = 0;
                if (i == 0) {
                    //直接取出来所有Y的有效区域，也可以存储成一个临时的bytes，到下一步再copy
                    for (int j = 0; j < height; j++) {
                        System.arraycopy(bytes, srcIndex, yBytes, dstIndex, width);
                        srcIndex += rowStride;
                        dstIndex += width;
                    }
                } else if (i == 1) {
                    //根据pixelsStride取相应的数据
                    for (int j = 0; j < height / 2; j++) {
                        for (int k = 0; k < width / 2; k++) {
                            uBytes[uIndex++] = bytes[srcIndex];
                            srcIndex += pixelsStride;
                        }
                        if (pixelsStride == 2) {
                            srcIndex += rowStride - width;
                        } else if (pixelsStride == 1) {
                            srcIndex += rowStride - width / 2;
                        }
                    }
                } else if (i == 2) {
                    //根据pixelsStride取相应的数据
                    for (int j = 0; j < height / 2; j++) {
                        for (int k = 0; k < width / 2; k++) {
                            vBytes[vIndex++] = bytes[srcIndex];
                            srcIndex += pixelsStride;
                        }
                        if (pixelsStride == 2) {
                            srcIndex += rowStride - width;
                        } else if (pixelsStride == 1) {
                            srcIndex += rowStride - width / 2;
                        }
                    }
                }
            }
            //JNI去编码
            //FFmpegHandler.getInstance().pushCameraData(yBytes, yBytes.length, uBytes, uBytes.length, vBytes, vBytes.length);
            mImage.close();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
