package fragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.mianshi.R;

/**
 * Fragment 碎片面试
 */
public class FragmentMSActivity extends AppCompatActivity {

    private FrameLayout framentLayout;
    private Button button1, button2;
    FragmentMianShi fragment1;
    FragmentMianShi fragment2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_ms);
        framentLayout = findViewById(R.id.frament_view);
        button1 = findViewById(R.id.fragment_1_btn);
        button2 = findViewById(R.id.fragment_2_btn);

        final FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.frament_view, fragment1).commit(); //把碎片1 默认显示

        //transaction.add(idRes, fragment) 向Activity中添加一个Fragment
        //transaction.remove(fragment); 从Activity中移除一个Fragment,如果被移除的Fragment没有添加到“回退栈”，这个Fragment实例将会被消除

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.frament_view, fragment1).commit();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.frament_view, fragment2).commit();
            }
        });
    }
}
