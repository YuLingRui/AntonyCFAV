package bitmap.img_bitamp;

import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.HashMap;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NetworkCallbackImpl extends ConnectivityManager.NetworkCallback {
    private final static String TAG = "NetworkCallbackImpl";
    private HashMap<Object, Object> checkMap = new HashMap<>();

    //网络状态记录
    //private String netType = ;

    @Override
    public void onAvailable(@NonNull Network network) {
        super.onAvailable(network);
        Log.i(TAG, "net connect success! 网络已连接");
    }

    @Override
    public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
        super.onBlockedStatusChanged(network, blocked);
    }

    @Override
    public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities);
    }

    @Override
    public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
        super.onLinkPropertiesChanged(network, linkProperties);
    }

    @Override
    public void onLosing(@NonNull Network network, int maxMsToLive) {
        super.onLosing(network, maxMsToLive);
    }

    @Override
    public void onLost(@NonNull Network network) {
        super.onLost(network);
        Log.i(TAG, "net disconnect! 网络已断开连接");
        //post(NetType.NONE)
    }

    @Override
    public void onUnavailable() {
        super.onUnavailable();
    }
}
