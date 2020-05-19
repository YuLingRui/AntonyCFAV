package fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mianshi.R;

public class FragmentTwo extends Fragment {


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("Fragment", "FragmentTwo onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e("Fragment", "FragmentTwo onCreateView");
        return inflater.inflate(R.layout.fragment_two, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e("Fragment", "FragmentTwo onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("Fragment", "FragmentTwo onDestroy");
    }
}
