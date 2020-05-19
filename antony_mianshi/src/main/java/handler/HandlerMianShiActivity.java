package handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.mianshi.R;

import java.lang.ref.WeakReference;

/**
 * **Message：**需要传递的消息，可以传递数据；
 * <p>
 * **MessageQueue：**消息队列，但是它的内部实现并不是用的队列，实际上是通过一个单链表的数据结构来维护消息列表，因为单链表在插入和删除上比较有优势。主要功能向消息池投递消息(MessageQueue.enqueueMessage)和取走消息池的消息(MessageQueue.next)；
 * <p>
 * **Handler：**消息辅助类，主要功能向消息池发送各种消息事件(Handler.sendMessage)和处理相应消息事件(Handler.handleMessage)；
 * <p>
 * **Looper：**不断循环执行(Looper.loop)，从MessageQueue中读取消息，按分发机制将消息分发给目标处理者。
 */
public class HandlerMianShiActivity extends AppCompatActivity {

    /**
     * TODO: MessageQueue，Handler和Looper三者之间的关系：
     * 每个线程中只能存在“一个Looper”，Looper是保存在ThreadLocal中的。
     * 主线程（UI线程）已经创建了一个Looper，所以在主线程中不需要再创建Looper，但是在其他线程中需要创建Looper。
     * 每个线程中可以有多个Handler，即一个Looper可以处理来自多个Handler的消息。
     * Looper中维护一个MessageQueue，来维护消息队列，消息队列中的Message可以来自不同的Handler。
     */

    /**
     * TODO: ThreadLocal的作用和实现原理
     * ThreadLocal是一个线程内部的数据存储类，通过它可以在指定的线程中存储数据，
     * 数据存储以后，只有在指定的线程中可以获取到存储的数据，对于其他线程来说则无法取到数据。
     * <p>
     * 应用场景：
     * 1.某些数据是以线程为作用域并且不同线程具有不同的数据的副本时，就可以考虑用ThreadLocal。
     * 获取Looper，不同线程具备不同的Looper对象。
     * <p>
     * 2.复杂逻辑下的对象传递，比如监听器的传递，有些时候一个线程中的任务过于复杂，我们又需要监听器能够贯穿整个线程的执行过程。
     * 实现原理：它们所操作的都是当前线程的ThreadLocalMap对象。
     * 在不同线程中，访问同一个ThreadLocal的set和get方法，它们对ThreadLocal的读、写操作仅限于各自线程的内部，从而使ThreadLocal可以在多个线程中互不干扰地存储和修改数据。ThreadLocalMap的key是Thread，value是value。
     */

    /**
     * TODO: 为什么系统不建议在子线程访问UI？
     *
     * Android的UI控件不是线程安全的，如果在多线程中并发 访问可能会导致UI控件处于不可预期的状态。
     */

    /**
     * TODO: Looper死循环为什么不会导致应用卡死？
     * 对于线程即是一段可执行的代码，当可执行代码执行完成后，线程生命周期便该终止了，线程退出。
     * 而对于主线程，我们是绝不希望会被运行一段时间，自己就退出，那么如何保证能一直存活呢？
     * 简单做法就是可执行代码是能一直执行下去的，死循环便能保证不会被退出。
     *
     * 造成ANR的原因：
     * 当前的事件没有机会得到处理，例如UI线程正在响应另一个事件，当前事件由于某种原因被阻塞了。
     * 当前的事件正在处理，但是由于耗时太长没能及时完成。
     *
     * 也并不会占用CPU资源，没有消息时，主线程会释放CPU资源进入休眠状态，直到下个消息到达或者有事务发生，通过往pipe管道写端写入数据来唤醒主线程工作。
     */

    private TextView textView;
    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };

    private MyHandler myHandler;

    private static class MyHandler extends Handler {

        WeakReference<HandlerMianShiActivity> weakReference;

        public MyHandler(WeakReference<HandlerMianShiActivity> weakReference) {
            this.weakReference = weakReference;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            weakReference.get().textView.setText("Handler 持有 外部Activity的弱引用");
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myHandler = new MyHandler(new WeakReference<HandlerMianShiActivity>(this));

        setContentView(R.layout.activity_handler_mian_shi);
        textView = findViewById(R.id.my_handler_mianshi);

        new Thread(new Runnable() {
            @Override
            public void run() {
                //...............耗时操作
                Message message = Message.obtain();
                message.what = 1;
                handler.sendMessage(message);
                /**
                 * TODO:  Message可以如何创建？哪种效果更好，为什么？
                 * 参考回答：可以通过三种方法创建：
                 *      直接生成实例Message m = new Message
                 *      通过Message m = Message.obtain
                 *      通过Message m = mHandler.obtainMessage()
                 *      后两者效果更好，因为Android默认的消息池中消息数量是10，而后两者是直接在消息池中取出一个Message实例，这样做就可以避免多生成Message实例。
                 */
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                /**
                 * TODO: Handler 内存泄漏场景展现 （及如何解决内存泄露问题）
                 * 原因：延时发送消息时，关闭了Activity，这个泄露是因为Message会持有Handler，而又因为内部类会持有外部类，
                 *      使得Activity会被Handler持有，这样最终就导致Activity泄露。
                 *
                 * 解决方案：将Handler定义成静态的内部类，在内部持有Activity的弱引用，
                 *           并在Acitivity的onDestroy()中调用handler.removeCallbacksAndMessages(null)及时移除所有消息。
                 */
                Message message = Message.obtain();
                message.what = 1;
                myHandler.sendMessageDelayed(message, 3000);
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myHandler.removeMessages(1);
        myHandler.removeCallbacksAndMessages(null);
    }
    /**
     * TODO: 为什么loop这个死循环会在主线程执行，不会ANR么【为什么不会卡死主线程】？
     *
     * 答：最开始Android的入口ActivityThread里面的main方法，里面有一个巨大的Handler，然后会创建一个主线程的looper对象，这也是为什么直接在主线程拿Handler就有Looper的原因，在其他线程是要自己Looper.prepare()的。
     * 其实整个Android就是在一个Looper的loop循环的，整个Android的一切都是以Handler机制进行的，即只要有代码执行都是通过Handler来执行的，而所谓ANR便是Looper.loop没有得到及时处理，一旦没有消息，Linux的epoll机制则会通过管道写文件描述符的方式来对主线程进行唤醒与沉睡，Android里调用了linux层的代码实现在适当时会睡眠主线程。
     *
     * 真正会卡死主线程的操作是在回调方法onCreate/onStart/onResume等操作时间过长，会导致掉帧，甚至发生ANR，looper.loop本身不会导致应用卡死。
     *
     * ActivityThread的main方法主要作用就是做消息循环，一旦退出消息循环，主线程运行完毕，那么你的应用也就退出了。
     *
     * Android是事件驱动的，在Loop.loop()中不断接收事件、处理事件，而Activity的生命周期都依靠于主线程的Loop.loop()来调度，所以可想而知它的存活周期和Activity也是一致的。当没有事件需要处理时，主线程就会阻塞；当子线程往消息队列发送消息，并且往管道文件写数据时，主线程就被唤醒。
     *
     * 主线程在没有事件需要处理的时候就是处于阻塞的状态。想让主线程活动起来一般有两种方式：
     * 第一种是系统唤醒主线程，并且将点击事件传递给主线程；
     * 第二种是其他线程使用Handler向MessageQueue中存放了一条消息，导致loop被唤醒继续执行。
     *
     * 主线程Looper从消息队列读取消息，当读完所有消息时，主线程阻塞。子线程往消息队列发送消息，并且往管道文件写数据，主线程即被唤醒，从管道文件读取数据，主线程被唤醒只是为了读取消息，当消息读取完毕，再次睡眠。因此loop的循环并不会对CPU性能有过多的消耗。
     *
     * 总结： Looer.loop()方法可能会引起主线程的阻塞，但只要它的消息循环没有被阻塞，能一直处理事件就不会产生ANR异常。
     */
}
