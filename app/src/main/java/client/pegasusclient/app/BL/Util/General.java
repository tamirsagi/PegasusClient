package client.pegasusclient.app.BL.Util;

import android.os.Bundle;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 4/30/2016.
 */
public class General {


    public static JSONObject convertBundleToJson(Bundle aBundle) throws JSONException {
        JSONObject json = new JSONObject();
        for(String key : aBundle.keySet()){
            json.put(key,aBundle.get(key));
        }

        return json;

    }

}
