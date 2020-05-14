package com.antony.cfav.camera2_opengl.filter;

import android.content.Context;

import com.antony.cfav.R;

public class ScreenFilter extends AbsFboFilter {

    public ScreenFilter(Context context, int mVertexShaderId, int mFragShaderId) {
        super(context, R.raw.screen_vert, R.raw.screen_frag);
    }
}
