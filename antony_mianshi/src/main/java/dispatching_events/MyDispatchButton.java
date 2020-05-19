package dispatching_events;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;

import androidx.appcompat.widget.AppCompatButton;

public class MyDispatchButton extends AppCompatButton {


    public MyDispatchButton(Context context) {
        super(context);
    }

    public MyDispatchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyDispatchButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Log.e("DispatchEvent", "Button dispatchTouchEvent***");
        return super.dispatchTouchEvent(event);
        //return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e("DispatchEvent", "Button onTouchEvent***");
        return super.onTouchEvent(event);
    }
}
