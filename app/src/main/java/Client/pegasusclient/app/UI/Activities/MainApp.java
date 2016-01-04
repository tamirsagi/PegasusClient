package client.pegasusclient.app.UI.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainApp extends AppCompatActivity {

    public  static final String KEY_SHARED_PREFERNCES_NAME = "Pegasus Client Preferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_app);
    }






    public void onButtonLoginClick(View view){
        //Todo - Check user details

        Intent MainPegasusClientIntent = new Intent(this, MainPegasusClient.class);
        startActivity(MainPegasusClientIntent);

    }


}
