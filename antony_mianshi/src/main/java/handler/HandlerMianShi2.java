package handler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * TODO:    使用Handler的postDealy后消息队列会有什么变化？
 * postDelayed()中的方法，里面调用sendMessageDelayed方法。
 * 里面接着调用sendMessageAtTime，参数Message和当前时间 + 延时时间。
 * MessageQueue的next方法。
 * ```
 * for (;;) {
 *     if (nextPollTimeoutMillis != 0) {
 *         Binder.flushPendingCommands();
 *     }
 *
 *     nativePollOnce(ptr, nextPollTimeoutMillis);
 * ···
 *         if (msg != null) {
 *             if (now < msg.when) {
 *                 // Next message is not ready.  Set a timeout to wake up when it is ready.
 *                 nextPollTimeoutMillis = (int) Math.min(msg.when - now, Integer.MAX_VALUE);
 *             } else {
 *                 // Got a message.
 *                 mBlocked = false;
 *                 if (prevMsg != null) {
 *                     prevMsg.next = msg.next;
 *                 } else {
 *                     mMessages = msg.next;
 *                 }
 *                 msg.next = null;
 *                 if (DEBUG) Log.v(TAG, "Returning message: " + msg);
 *                 msg.markInUse();
 *                 return msg;
 *             }
 *         } else {
 *             // No more messages.
 *             nextPollTimeoutMillis = -1;
 *         }
 *     ···
 *     }
 * }
 * ```
 *
 * 如果队列中只有这个消息，那么消息不会被发送，而是计算到时唤醒的时间，先将Looper阻塞，到时间就唤醒它。但如果此时要加入新消息，不是延迟message，或比当前的延迟短，这个消息就会插入头部并且唤起线程来,队头的时间最小、队尾的时间最大。
 */
public class HandlerMianShi2 extends AppCompatActivity {

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler.postDelayed(new ThreadOne(), 3000);
        handler.postDelayed(new ThreadTwo(), 0);
        /**
         * TODO:   Handler.postDelayed(thread, 延迟时间) 是如何做到延迟的
         *    模拟过程：
         *    第一步：handler.postDelayed(线程1， 3秒) 进入消息队列后---》MessageQueue.natviePollonce()阻塞---》loop阻塞
         *
         *    第二步：handler.postDelayed(线程2， 0秒) 进入消息队列
         *            （1）判断 “线程1==[消息1]”时间还没到，正在阻塞， 把“线程2==[消息2]” 插入消息队列的“头”(放到 1 前面) 然后调用natvieWake()方法 唤醒线程
         *            （2）MessageQueue.next() 被唤醒后， 重新开始读取消息链表，“线程2”[消息2]无延迟时，直接返回给looper
         *             (3)looper处理完这个消息 再次调用 MessageQueue.next()方法  MessageQueue继续读取消息链表， “线程1”[消息1]没到时间，计算一下剩余时间(如：剩余9秒)
         *                继续调用nativePollonce（）阻塞
         *             (4)直到阻塞时间到  或者   下一个消息msg进队
         */
    }

    private class ThreadOne implements Runnable {

        @Override
        public void run() {

        }
    }

    private class ThreadTwo implements Runnable {

        @Override
        public void run() {

        }
    }

}
