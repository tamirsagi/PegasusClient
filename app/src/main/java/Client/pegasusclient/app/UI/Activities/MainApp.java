package client.pegasusclient.app.UI.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainApp extends AppCompatActivity {

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
