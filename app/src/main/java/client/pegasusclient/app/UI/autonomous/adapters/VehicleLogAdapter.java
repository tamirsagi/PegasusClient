package client.pegasusclient.app.UI.autonomous.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import client.pegasusclient.app.BL.Bluetooth.BluetoothDeviceInfo;
import client.pegasusclient.app.UI.Activities.R;
import client.pegasusclient.app.UI.Fragments.settings.Connections.Bluetooth.BluetoothDevicesListListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tamir Sagi
 *         This class manages the rcycler list's  adapter inside records fragment.
 */

public class VehicleLogAdapter extends ArrayAdapter<VehicleLogAdapter.RecordHolder> {

    private Context context;
    private List<VehicleLog> mVehicleLogsList;
    private int mLayoutResourceId;

    public VehicleLogAdapter(Context context, int layoutResourceId) {
        super(context, layoutResourceId, new ArrayList());
        this.context = context;
        mVehicleLogsList = new ArrayList<VehicleLog>();
        mLayoutResourceId = layoutResourceId;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(mLayoutResourceId, parent, false);
            RecordHolder holder = new RecordHolder(convertView);
            VehicleLog msg = mVehicleLogsList.get(position);
            holder.tv_message.setText(msg.toString());
            holder.tv_message.setTextColor(msg.getColor());
        }

        return convertView;
    }


    @Override
    public int getCount() {
        return mVehicleLogsList.size();
    }


    public void refreshList() {
        notifyDataSetChanged();
    }


    public void addItem(int position, VehicleLog data) {
        mVehicleLogsList.add(position, data);
        notifyDataSetInvalidated();
    }

    public void removeItem(int position) {
        mVehicleLogsList.remove(position);
        notifyDataSetInvalidated();
    }

    /**
     * this class represents an item within the recycler list.
     */
    class RecordHolder{
        TextView tv_message;

        public RecordHolder(View itemView) {
            tv_message = (TextView) itemView.findViewById(R.id.vehicle_log_field_message);
        }
    }


}