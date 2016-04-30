package client.pegasusclient.app.UI.autonomous.adapters;

import android.graphics.Color;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 4/30/2016.
 */
public class VehicleLog {


    private static final String JSON_KEY_WHEN = "when";
    private static final String JSON_KEY_WHAT = "what";
    private static final String JSON_KEY_TYPE = "type";

    private static final String MESSAGE_TYPE_INFO = "INFO";
    private static final String MESSAGE_TYPE_SETTINGS = "SETTINGS";
    private static final String MESSAGE_TYPE_ERROR = "ERROR";
    private static final int TYPE_INFO = 0;
    private static final int TYPE_SETTINGS = 1;
    private static final int TYPE_ERROR = 2;

    private String mWhen;
    private String mWhat;
    private int mType;
    private String mMessageType;
    private int mColor;

    public VehicleLog(JSONObject aJsonObject) throws JSONException {
        setWhen(aJsonObject.getString(JSON_KEY_WHEN));
        setWhat(aJsonObject.getString(JSON_KEY_WHAT));
        setType(aJsonObject.getInt(JSON_KEY_TYPE));
    }


    private void setWhen(String aWhen){
        mWhen = aWhen;
    }

    private void setWhat(String aWhat){
        mWhat = aWhat;
    }

    private void setType(int aType){
        mType = aType;
        switch(mType){
            case TYPE_INFO:
                mMessageType = MESSAGE_TYPE_INFO;
                mColor = Color.GREEN;
                break;
            case TYPE_SETTINGS:
                mMessageType = MESSAGE_TYPE_SETTINGS;
                mColor = Color.BLUE;
                break;
            case TYPE_ERROR:
                mMessageType = MESSAGE_TYPE_ERROR;
                mColor = Color.RED;
                break;
        }
    }

    public String getWhen(){
        return mWhen;
    }

    public String getWhat(){
        return mWhat;
    }

    public String getType(){
        return mMessageType;
    }

    public int getColor(){
        return mColor;
    }

    @Override
    public String toString() {
        return String.format("%s/[%s] : %s",getType(),getWhen(),getWhat());
    }
}
