package client.pegasusclient.app.UI.Fragments.Camera;

import android.content.*;
import android.net.Uri;
import android.os.*;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;
import client.pegasusclient.app.BL.Services.HotspotConnectivityService;
import client.pegasusclient.app.UI.Activities.R;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * class handles all communication with RaspberryPi hotspot, it receives the live video streaming.
 * it connects to an Access point and use Video view to show the streaming
 */
public class PegasusCamera extends Fragment {

    private static PegasusCamera mInstance;
    private static final String TAG = "PegasusCamera";
    private static String PEGASUS_STREAMING_VIDEO_ADDRESS = "http:192.168.42.1:8090/?action=stream";


    private HotspotConnectivityService mHotspotConnectivityService;

    private View mRoot;
    private boolean mIsBoundToService;
    private VideoView mVideoView;
    private MediaController mMediaController;

    private Handler mHandler;

    private MjpegView mMjpegView;


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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_pegasus_camera, container, false);
        //initializeVideoView();
        mMjpegView = (MjpegView)mRoot.findViewById(R.id.pegasus_camera_mjpeg_view);
        new DoRead().execute( "192.168.42.1", "8090");
        return mRoot;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if(mVideoView != null){
//            mVideoView.stopPlayback();
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        if(mHotspotConnectivityService != null){
//
//        }
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
     * initialize video view object
     */
    private void initializeVideoView() {

//        mVideoView = (VideoView)mRoot.findViewById(R.id.video_view_camera_Streaming);
//        Uri vidUri = Uri.parse(PEGASUS_STREAMING_VIDEO_ADDRESS);
//        mVideoView.setVideoURI(vidUri);
//
//        //Add Playback Controls
//        mMediaController = new MediaController(getActivity());
//        mMediaController.setAnchorView(mVideoView);
//        mVideoView.setMediaController(mMediaController);
//        startPlayback();
    }

    /**
     * stop playback
     */
    private void stopPlayback(){
        if(mVideoView != null){
            mVideoView.stopPlayback();
        }
    }

    private void startPlayback(){
        if(mVideoView != null){
            mVideoView.start();
        }
    }


    /**
     * handles incoming messages from Hotspot service
     * @param msg
     */
    private void handleIncomingMessageFromService(Message msg){





    }

    public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
        protected MjpegInputStream doInBackground( String... params){
            Socket socket = null;
            try {
                socket = new Socket( params[0], Integer.valueOf( params[1]));
                return (new MjpegInputStream(socket.getInputStream()));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(MjpegInputStream result) {
            mMjpegView.setSource(result);
            if(result!=null){
                result.setSkip(1);
            }
//            mMjpegView.setDisplayMode(MjpegView.SIZE_STANDARD);
            mMjpegView.setDisplayMode(MjpegView.SIZE_BEST_FIT);
            mMjpegView.showFps(true);
        }
    }



}
