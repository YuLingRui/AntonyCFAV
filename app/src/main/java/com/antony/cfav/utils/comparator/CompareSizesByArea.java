package com.antony.cfav.utils.comparator;

import android.util.Size;

import java.util.Comparator;

public class CompareSizesByArea implements Comparator<Size> {
    @Override
    public int compare(Size lhs, Size rhs) {
        // 我们在这里进行了类型转换，以确保乘法不会溢出
        return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                (long) rhs.getWidth() * rhs.getHeight());
    }
}
