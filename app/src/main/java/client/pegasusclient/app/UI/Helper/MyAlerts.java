package client.pegasusclient.app.UI.Helper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.view.ContextThemeWrapper;
import client.pegasusclient.app.UI.Activities.PegasusSettings;
import client.pegasusclient.app.UI.Activities.R;

/**
 * Created by Tamir on 4/28/2016.
 */
public class MyAlerts {


    /**
     * show alert message when client is not connected to vehicle and  needs to re-direct to settings
     */
    public static void showAlertDialog(final Context aContext) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(aContext, android.R.style.Theme_Dialog));
        builder.setTitle("Connect To Vehicle");
        builder.setMessage("You are not connected to Pegasus Vehicle, please connect");
        builder.setIcon(R.drawable.bluetooth_disable_icon);
        final Intent MainPegasusClientIntent = new Intent(aContext, PegasusSettings.class);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                aContext.getApplicationContext().startActivity(MainPegasusClientIntent);
            }
        });
        AlertDialog alertdialog = builder.create();
        alertdialog.show();
    }
}
