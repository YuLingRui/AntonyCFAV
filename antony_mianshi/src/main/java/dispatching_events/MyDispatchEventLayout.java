package dispatching_events;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class MyDispatchEventLayout extends LinearLayout {


    public MyDispatchEventLayout(Context context) {
        this(context, null);
    }

    public MyDispatchEventLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyDispatchEventLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d("DispatchEvent", "ViewGroup onInterceptTouchEvent!!!");
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d("DispatchEvent", "ViewGroup dispatchTouchEvent!!!");
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("DispatchEvent", "ViewGroup onTouchEvent!!!");
        return super.onTouchEvent(event);
    }
}
