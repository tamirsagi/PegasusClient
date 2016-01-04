package client.pegasusclient.app.UI.Fragments.settings.Connections.Bluetooth;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import client.pegasusclient.app.BL.Bluetooth.BluetoothDeviceInfo;
import client.pegasusclient.app.UI.Activities.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tamir Sagi
 *         This class manages the rcycler list's  adapter inside records fragment.
 */

public class BluetoothDevicesRecyclerAdapter extends ArrayAdapter<BluetoothDevicesRecyclerAdapter.RecordHolder> {

    private Context context;
    private List<BluetoothDeviceInfo> meBluetoothDevicesInfo;
    private BluetoothDevicesListListener mBluetoothDevicesListener;
    private int mLayoutResourceId;

    public BluetoothDevicesRecyclerAdapter(Context context, int layoutResourceId, BluetoothDevicesListListener listener) {
        super(context, layoutResourceId, new ArrayList());
        this.context = context;
        meBluetoothDevicesInfo = new ArrayList<BluetoothDeviceInfo>();
        mLayoutResourceId = layoutResourceId;
        mBluetoothDevicesListener = listener;
    }


//    @Override
//    public RecordHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
//        View itemView = inflater.inflate(R.layout.bluetooth_dialog_device_details, parent, false);
//        return new RecordHolder(itemView);          //return custom item
//    }
//
//    @Override
//    public void onBindViewHolder(RecordHolder holder, int position) {
//        if (meBluetoothDevicesInfo != null && meBluetoothDevicesInfo.size() > 0) {
//            BluetoothDeviceInfo record = meBluetoothDevicesInfo.get(position);
//            holder.tv_Name.setText(record.getName());
//            holder.tv_Address.setText(record.getAddress());
//            holder.tv_Signal.setText(record.getSignal());
//        }
//    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(mLayoutResourceId, parent, false);
            RecordHolder holder = new RecordHolder(convertView);
            BluetoothDeviceInfo bt = meBluetoothDevicesInfo.get(position);
            holder.tv_Name.setText(bt.getName());
            holder.tv_Address.setText(bt.getAddress());
            holder.tv_Signal.setText(bt.getSignal());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String address = meBluetoothDevicesInfo.get(position).getAddress();
                    mBluetoothDevicesListener.getBluetoothDeviceToConnect(v, address);       //send the press address to listener
                }
            });
        }

        return convertView;
    }


    @Override
    public int getCount() {
        return meBluetoothDevicesInfo.size();
    }


    public void refreshList() {
        notifyDataSetChanged();
    }


    public void addItem(int position, BluetoothDeviceInfo data) {
        meBluetoothDevicesInfo.add(position, data);
        notifyDataSetInvalidated();
    }

    public void removeItem(int position) {
        meBluetoothDevicesInfo.remove(position);
        notifyDataSetInvalidated();
    }

    /**
     * this class represents an item within the recycler list.
     */
    class RecordHolder{
        TextView tv_Name;
        TextView tv_Address;
        TextView tv_Signal;

        public RecordHolder(View itemView) {
            tv_Name = (TextView) itemView.findViewById(R.id.bluetooth_dialog_device_name);
            tv_Address = (TextView) itemView.findViewById(R.id.bluetooth_dialog_device_address);
            tv_Signal = (TextView) itemView.findViewById(R.id.bluetooth_dialog_device_signal);
        }
    }


}