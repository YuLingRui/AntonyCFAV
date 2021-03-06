package 屏幕适配;

public class ScreenAdaptation {
    // 参考地址：

    //1:屏幕尺寸 【手机对角线的物理尺寸(/inch)】
    //2:屏幕分辨率 【手机横向，纵向上的像素点数总和(单位:px)】
         // 1080px * 1920px  【宽度方向上有1080个像素点，高度方向上有1920个像素点】
    //3:屏幕像素密度 【每英寸像素密度点数(单位:dpi)】
            //密度类型	            代表的分辨率（px）	屏幕像素密度（dpi）
            //低密度（ldpi）	    240x320	             120
            //中密度（mdpi）	    320x480	             160
            //高密度（hdpi）	    480x800	             240
            //超高密度（xhdpi）	    720x1280	         320
            //超超高密度（xxhdpi）	1080x1920	         480
    //4. 密度无关像素
            //含义：density-independent pixel，叫dp或dip，与终端上的实际物理像素点无关。
            //单位：dp，可以保证在不同屏幕像素密度的设备上显示相同的效果
    //5. 独立比例像素
            //含义：scale-independent pixel，叫sp或sip
            //单位：sp

    //屏幕适配解决方案：
        //一. "布局" 适配
        //二. "布局组件"  适配
        //三. "图片资源" 适配
}
