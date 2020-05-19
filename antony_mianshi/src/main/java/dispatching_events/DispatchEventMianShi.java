package dispatching_events;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mianshi.R;

/**
 * 点击事件产生后，会直接调用dispatchTouchEvent（）方法
 * public boolean dispatchTouchEvent(MotionEvent ev) {
 *
 *     //代表是否消耗事件
 *     boolean consume = false;
 *
 *     if (onInterceptTouchEvent(ev)) {
 *              如果onInterceptTouchEvent()返回true则代表当前ViewGroup拦截了点击事件
 *              则该点击事件则会交给当前ViewGroup进行处理, 即调用ViewGroup.onTouchEvent (）方法去处理点击事件
 *          consume = father.onTouchEvent (ev) ;
 *     } else {
 *              如果onInterceptTouchEvent()返回false则代表当前ViewGroup不拦截点击事件
 *              则该点击事件则会继续传递给它的子元素
 *              子元素的dispatchTouchEvent（）就会被调用，重复上述过程
 *              直到点击事件被最终处理为止
 *       consume = child.dispatchTouchEvent (ev) ;
 *     }
 *    return consume;
 * }
 */

public class DispatchEventMianShi extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dispatch_event_mianshi);
        MyDispatchButton  button = findViewById(R.id.dispatch_event_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("DispatchEvent", "Activity 点击事件...");
            }
        });
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.i("DispatchEvent", "Activity dispatchTouchEvent...");
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("DispatchEvent", "Activity onTouchEvent...");
        return true;
    }
}
