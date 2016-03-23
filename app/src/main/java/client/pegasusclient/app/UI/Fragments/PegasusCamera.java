package client.pegasusclient.app.UI.Fragments;

import android.os.*;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.*;
import android.webkit.WebView;
import client.pegasusclient.app.UI.Activities.R;


/**
 * class handles all communication with RaspberryPi hotspot, it receives the live video streaming.
 * it connects to an Access point and use Video view to show the streaming
 */
public class PegasusCamera extends Fragment{

    private static PegasusCamera mInstance;
    private static final String TAG = "PegasusCamera";
    private static String PEGASUS_STREAMING_VIDEO_ADDRESS = "http:192.168.42.1:8090/?action=stream";


    private View mRoot;
    private WebView mWebView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mHandler = new Handler(new Handler.Callback() {
//            @Override
//            public boolean handleMessage(Message msg) {
//                handleIncomingMessageFromService(msg);
//                return true;
//            }
//        });
        //createBinnedHotspotConnectivityServiceService();
        //sample public cam

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_pegasus_camera, container, false);
        //initializeVideoView();
        mWebView = (WebView) mRoot.findViewById(R.id.pegasus_camera);
        mWebView.loadUrl(PEGASUS_STREAMING_VIDEO_ADDRESS);
        return mRoot;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onResume() {
        super.onResume();
    }






    /**
     * handles incoming messages from Hotspot service
     * @param msg
     */
    private void handleIncomingMessageFromService(Message msg){





    }

}
