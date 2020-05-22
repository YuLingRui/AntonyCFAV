package bitmap.img_bitamp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.test.R;

import java.util.ArrayList;

/**
 * 测试 Bitmap  LruCache   DiskLruCache 相关内容
 */
public class TestLruCacheActivity extends AppCompatActivity {

    private static boolean mCanLoadForPhoneNet;
    private ImageAdapter imageAdapter;
    ArrayList<String> urls = new ArrayList<>();
    private static boolean mIsGridViewIdle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_lru_cache);
        addData(); //添加数据
        GridView gv_main = (GridView) findViewById(R.id.gv_main);


        // 根据连接网络的情况判断是否加载图片
        if (!NetWorkUtil.isWifi(getApplicationContext())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("首次使用会从手机网络下载图片, 是否确认下载?")
                    .setTitle("友情提示")
                    .setPositiveButton("好的.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mCanLoadForPhoneNet = true;
                            imageAdapter.notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("不行!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getApplicationContext(), "瞅你扣那样!!!", Toast.LENGTH_LONG).show();
                        }
                    }).show();
        } else {
            mCanLoadForPhoneNet = true;
        }

        imageAdapter = new ImageAdapter(getApplicationContext(), urls);
        gv_main.setAdapter(imageAdapter);

        gv_main.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    mIsGridViewIdle = true;
                    // 并触发更新adapter
                    // mageAdapter.notifyDataSetChanged();
                } else {
                    mIsGridViewIdle = false;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    private void addData() {
        for (int i = 0; i < 100; i++) {
            urls.add("http://img02.tooopen.com/images/20160408/tooopen_sy_158723161481.jpg");
            urls.add("http://img02.tooopen.com/images/20160404/tooopen_sy_158262392146.jpg");
            urls.add("http://img02.tooopen.com/images/20160318/tooopen_sy_156339294124.jpg");
            urls.add("http://img06.tooopen.com/images/20160823/tooopen_sy_176393394325.jpg");
            urls.add("http://img06.tooopen.com/images/20160821/tooopen_sy_176144979595.jpg");
            urls.add("http://img06.tooopen.com/images/20160723/tooopen_sy_171462742667.jpg");
            urls.add("http://img05.tooopen.com/images/20150417/tooopen_sy_119014046478.jpg");
            urls.add("http://img02.tooopen.com/images/20150318/tooopen_sy_82853534894.jpg");
            urls.add("http://img05.tooopen.com/images/20150204/tooopen_sy_80359399983.jpg");
            urls.add("http://img01.tooopen.com/Downs/images/2010/4/9/sy_20100409135808693051.jpg");
            urls.add("http://www.yiren001.com/uploads/allimg/150315/114AJ1c-1.jpg");
            urls.add("http://www.yiren001.com/uploads/allimg/150315/114AH0B-0.jpg");
            urls.add("http://www.yiren001.com/uploads/allimg/150315/114AJB9-6.jpg");
            urls.add("http://www.yiren001.com/uploads/allimg/150315/114AG0I-7.jpg");
        }
    }

    private static class ImageAdapter extends BaseAdapter {
        private final ArrayList<String> mUrls;
        private Context mContext;
        private final ImageLoader mImageLoader;

        public ImageAdapter(Context context, ArrayList<String> urls) {
            this.mContext = context;
            this.mUrls = urls;
            this.mImageLoader = ImageLoader.build(context);
        }

        @Override
        public int getCount() {
            return mUrls.size();
        }

        @Override
        public String getItem(int position) {
            return mUrls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.item_photo_wall, null);
                holder = new ViewHolder();
                holder.mImageView = (ImageView) convertView.findViewById(R.id.iv_square);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            // 设置默认图片
            ImageView mImageView = holder.mImageView;
            mImageView.setImageResource(android.R.drawable.screen_background_dark_transparent);

            // 检测是否wifi 和 是否是滑动状态
            if (mCanLoadForPhoneNet) {
//            if (mCanLoadForPhoneNet && mIsGridViewIdle){
                // 加载图片
                mImageLoader.setImageView(mImageView).url(mUrls.get(position));
            }
            return convertView;
        }

        class ViewHolder {
            private ImageView mImageView;
        }
    }
}
