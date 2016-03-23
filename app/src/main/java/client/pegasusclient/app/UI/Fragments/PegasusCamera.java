package client.pegasusclient.app.UI.Fragments;

import android.content.*;
import android.media.MediaPlayer;
import android.os.*;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.webkit.WebView;
import client.pegasusclient.app.BL.Services.HotspotConnectivityService;
import client.pegasusclient.app.UI.Activities.R;


/**
 * class handles all communication with RaspberryPi hotspot, it receives the live video streaming.
 * it connects to an Access point and use Video view to show the streaming
 */
public class PegasusCamera extends Fragment{

    private static PegasusCamera mInstance;
    private static final String TAG = "PegasusCamera";
    private static String PEGASUS_STREAMING_VIDEO_ADDRESS = "http:192.168.42.1:8090/?action=stream";


    private HotspotConnectivityService mHotspotConnectivityService;

    private View mRoot;
    private boolean mIsBoundToService;

    private Handler mHandler;
    private WebView mWebview;
    private MediaPlayer mediaPlayer;
    private SurfaceHolder vidHolder;
    private SurfaceView vidSurface;


    public static PegasusCamera getInstance(){
        if(mInstance == null)
            mInstance = new PegasusCamera();
        return mInstance;
    }



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
        mWebview = (WebView) mRoot.findViewById(R.id.pegasus_camera);
        mWebview.loadUrl(PEGASUS_STREAMING_VIDEO_ADDRESS);
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
     * Bind to Hotspot Connection Manager Service
     */
    private void createBinnedHotspotConnectivityServiceService() {
        Intent intent = new Intent(getActivity(), HotspotConnectivityService.class);
        getActivity().getApplicationContext().bindService(intent, hotspotConnectivityService, Context.BIND_AUTO_CREATE);
        mIsBoundToService = true;
    }

    /**
     * Create the service instance
     */
    private ServiceConnection hotspotConnectivityService = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            HotspotConnectivityService.MyLocalBinder hotspotConnectivityService = (HotspotConnectivityService.MyLocalBinder) service;
            mHotspotConnectivityService = hotspotConnectivityService.gerService();
            try{
                mHotspotConnectivityService.registerHandler(mHandler);
                //connect to AP
             //   mHotspotConnectivityService.connectToPegasusAP();
            }catch (Exception e){
                Log.e(TAG,e.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mIsBoundToService) {
                getActivity().getApplicationContext().unbindService(hotspotConnectivityService);
                mIsBoundToService = false;
            }
        }
    };




    /**
     * handles incoming messages from Hotspot service
     * @param msg
     */
    private void handleIncomingMessageFromService(Message msg){





    }

}
