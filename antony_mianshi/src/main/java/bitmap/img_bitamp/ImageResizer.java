package bitmap.img_bitamp;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileDescriptor;

/**
 * 完成图片压缩功能
 */
public class ImageResizer {
    private static final String TAG = "ImageResizer";


    /**
     * 对一个Resources的资源文件进行指定长宽来加载进内存, 并把这个bitmap对象返回
     *
     * @param res   资源文件对象
     * @param resId 要操作的图片id
     * @param reqWidth 最终想要得到bitmap的宽度
     * @param reqHeight 最终想要得到bitmap的高度
     * @return 返回采样之后的bitmap对象
     */
    public static Bitmap decodeSampledBitampFormResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // 首先先指定加载的模式 为只是获取资源文件的大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; //first decode with inJustDecodeBounds=true  to  checkdimensions
        BitmapFactory.decodeResource(res, resId, options);
        //Calculate Size  计算要设置的采样率 并把值设置到option上
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 关闭只加载属性模式, 并重新加载的时候传入自定义的options对象
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * 对一个文件描述符所关联的文件进行指定长宽来加载进内存, 并把这个bitmap对象返回
     *
     * @param fd    要进行操作文件的描述对象
     * @param reqWidth 最终想要得到bitmap的宽度
     * @param reqHeight 最终想要得到bitmap的高度
     * @return 返回采样之后的bitmap对象
     */
    public static Bitmap decodeSampledBitmapFormFileDescriptor(FileDescriptor fd, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; //first decode with inJustDecodeBounds=true  to  checkdimensions
        BitmapFactory.decodeFileDescriptor(fd, null, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fd, null, options);
    }


    //计算出采样率 inSampleSize
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        if(reqHeight == 0 || reqWidth==0){
            return 1;
        }
        final int height = options.outHeight;//图片的原始高度
        final int width = options.outWidth;//图片的原始宽度
        int inSampleSize = 1;
        // 如果想要实现的宽高比原始图片的宽高小那么就可以计算出采样率, 否则不需要改变采样率
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // 判断原始长宽的一半是否比目标大小小, 如果小那么增大采样率2倍, 直到出现修改后原始值会比目标值大的时候
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= halfWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
