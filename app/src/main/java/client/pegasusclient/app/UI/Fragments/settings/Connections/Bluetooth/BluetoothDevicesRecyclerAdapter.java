package client.pegasusclient.app.UI.Fragments.settings.Connections.Bluetooth;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import client.pegasusclient.app.BL.Bluetooth.BluetoothDeviceInfo;
import client.pegasusclient.app.UI.Activities.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tamir Sagi
 *         This class manages the rcycler list's  adapter inside records fragment.
 */

public class BluetoothDevicesRecyclerAdapter extends RecyclerView.Adapter<BluetoothDevicesRecyclerAdapter.RecordHolder> {

    private Context context;
    private List<BluetoothDeviceInfo> meBluetoothDevicesInfo;


    public BluetoothDevicesRecyclerAdapter(Context context) {
        this.context = context;
    }


    @Override
    public RecordHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.bluetooth_dialog_device_details, parent, false);
        meBluetoothDevicesInfo = new ArrayList<BluetoothDeviceInfo>();
        return new RecordHolder(itemView);          //return custom item
    }

    @Override
    public void onBindViewHolder(RecordHolder holder, int position) {
        if(meBluetoothDevicesInfo != null && meBluetoothDevicesInfo.size() > 0 && meBluetoothDevicesInfo.size() < position) {
            BluetoothDeviceInfo record = meBluetoothDevicesInfo.get(position);
            holder.tv_Name.setText(record.getName());
            holder.tv_Address.setText(record.getAddress());
            holder.tv_Signal.setText(record.getSignal());
        }
    }


    @Override
    public int getItemCount() {
        return meBluetoothDevicesInfo.size();
    }


    public void refreshList() {
        notifyDataSetChanged();
    }


    public void addItem(int position, BluetoothDeviceInfo data) {
        meBluetoothDevicesInfo.add(position, data);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        meBluetoothDevicesInfo.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * this class represents an item within the recycler list.
     */
    class RecordHolder extends RecyclerView.ViewHolder{
        TextView tv_Name;
        TextView tv_Address;
        TextView tv_Signal;

        public RecordHolder(View itemView) {
            super(itemView);
            tv_Name = (TextView) itemView.findViewById(R.id.bluetooth_dialog_device_name);
            tv_Address = (TextView) itemView.findViewById(R.id.bluetooth_dialog_device_address);
            tv_Signal = (TextView) itemView.findViewById(R.id.bluetooth_dialog_device_signal);
        }

    }


}