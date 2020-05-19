package fragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.mianshi.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment 碎片面试
 *          //transaction.add(idRes, fragment) 向Activity中添加一个Fragment
 *         //transaction.remove(fragment); 从Activity中移除一个Fragment,如果被移除的Fragment没有添加到“回退栈”，这个Fragment实例将会被消除
 *         //transaction.replace(int id, fragment); 使用另一个Fragment替换当前的【实际上就是remove() 然后add()】
 *         //transaction.show() 显示之前隐藏的Fragment
 *
 *         //transaction.detach(fragment);会将view从UI中移除,和remove()不同,此时fragment的状态依然由FragmentManager维护
 *         //transaction.attach(fragment) 重建view视图，附加到UI上并显示
 */
public class FragmentMSActivity extends AppCompatActivity {

    private FrameLayout framentLayout;
    private Button button1, button2, button3;
    FragmentOne fragment1;
    FragmentTwo fragment2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_ms);
        framentLayout = findViewById(R.id.frament_view);
        button1 = findViewById(R.id.fragment_1_btn);
        button2 = findViewById(R.id.fragment_2_btn);
        button3 = findViewById(R.id.fragment_3_btn);

        fragment1 = new FragmentOne();
        fragment2 = new FragmentTwo();

        final FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        //transaction.add(R.id.frament_view, fragment1).commit(); //把碎片1 默认显示


        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction1 = manager.beginTransaction();
                transaction1.add(R.id.frament_view, fragment1).commit();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction2 = manager.beginTransaction();
                transaction2.add(R.id.frament_view, fragment2).commit();
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction3 = manager.beginTransaction();
                transaction3.add(R.id.frament_view, fragment1).commit();
            }
        });

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    //TODO: Fragment布局重叠解决方案，方法不唯一，及供参考
    private int mCuffentPos = -1;
    private List<Fragment>  mFragments = new ArrayList<>();
    private void switchFragmentIndex(int position){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(mCuffentPos != -1){
            transaction.hide(mFragments.get(mCuffentPos));
        }
        if(!mFragments.get(position).isAdded()){ //避免Fragment重复添加
            transaction.add(R.id.frament_view, mFragments.get(position));
        }
        transaction.show(mFragments.get(position)).commit();
        mCuffentPos = position; //记录当前Fragment下标

    }
}
//TODO: replace 和  add 之间的区别
/**
 * （1）“添加相同的fragment时”，replace不会有任何变化，add会报IllegalStateException异常；
 *
 * （2）replace先remove掉相同id的所有fragment，然后在add当前的这个fragment，
 *      add是覆盖前一个fragment。所以如果使用add一般会伴随hide()和show()，避免布局重叠；
 **/

/**
 * TODO:  getFragmentManager、getSupportFragmentManager 、getChildFragmentManager之间的区别？
 *
 * 参考回答：
 *     (1) getFragmentManager()所得到的是所在fragment 的父容器的管理器，
 *     (2)getChildFragmentManager()所得到的是在fragment  里面子容器的管理器，
 *     (3)如果是fragment嵌套fragment，那么就需要利用getChildFragmentManager()；
 *        因为Fragment是3.0 Android系统API版本才出现的组件，所以3.0以上系统可以直接调用getFragmentManager()来获取FragmentManager()对象，
 *        而3.0以下则需要调用getSupportFragmentManager() 来间接获取；
 **/

/**
 * TODO:  FragmentPagerAdapter与FragmentStatePagerAdapter的区别与使用场景
 *
 * 参考回答：
 * 相同点 ：二者都继承PagerAdapter
 * 不同点 ：(1)FragmentPagerAdapter的每个Fragment会持久的保存在FragmentManager中，只要用户可以返回到页面中，
 *          它都不会被销毁。因此适用于那些数据相对静态的页，Fragment数量也比较少的那种；
 *
 *          (2)FragmentStatePagerAdapter只保留当前页面，当页面不可见时，该Fragment就会被消除，释放其资源。
 *          因此适用于那些数据动态性较大、占用内存较多，多Fragment的情况；
 **/

/**
 * TODO: AndroidX  Fragment懒加载
 *   https://blog.csdn.net/qq_36486247/article/details/102531304
 **/