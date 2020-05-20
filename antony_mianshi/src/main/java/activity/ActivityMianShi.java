package activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mianshi.R;

import java.util.List;

public class ActivityMianShi extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mian_shi);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    /**
     * TODO: 如何知道 某个Activity是否在前台显示
     */
    /**
     * 判断某个界面是否在前台
     * <p>
     * manager.getRunningTasks(int) 这个方法还要求设置android.permission.GET_TASKS权限。
     * 谷歌不推荐使用这种方式
     *
     * @param context   Context
     * @param className 界面的类名
     * @return 是否在前台显示
     */
    private boolean isForground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = manager.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName()))
                return true;
        }
        return false;
    }

    /**
     * 返回当前的应用是否处于前台显示状态
     *
     * @param packageName
     * @return
     */
    private boolean isTopActivity(Context context, String packageName) {
        //_context是一个保存的上下文
        ActivityManager manager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = manager.getRunningAppProcesses();
        if (list.size() == 0) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo processInfo : list) {
            Log.d("TAG", Integer.toString(processInfo.importance));
            Log.d("TAG", processInfo.processName);
            /**
             * 在6.0/7.0等新版本中 可能还有另外几种状态:
             * 1.RunningAppProcessInfo.IMPORTANCE_TOP_SLEEPING(应用在前台时锁屏幕)
             * 2.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE(应用开启了服务,然后锁屏幕,此时服务还是在前台运行)
             */
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    processInfo.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

}
