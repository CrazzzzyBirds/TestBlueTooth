package com.heima.testbluetooth.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.heima.testbluetooth.R;

import java.util.List;

public class DeviceListAdapter extends BaseAdapter {
    private List<BluetoothDevice> list;
    private Context mContext;


    public DeviceListAdapter(List<BluetoothDevice> list, Context mContext) {
        this.list = list;
        this.mContext = mContext;
    }

    public void setList(List<BluetoothDevice> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHodler hodler = new ViewHodler();
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_client_list_search, null);
            hodler.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            hodler.tv_adress = (TextView) convertView.findViewById(R.id.tv_adress);
            hodler.tv_state = (TextView) convertView.findViewById(R.id.tv_state);
            convertView.setTag(hodler);
        } else {
            hodler = (ViewHodler) convertView.getTag();
        }
        BluetoothDevice device = (BluetoothDevice) getItem(position);
        hodler.tv_name.setText(device.getName());
        hodler.tv_adress.setText(device.getAddress());
        if (device.getBondState()==BluetoothDevice.BOND_BONDING)
        hodler.tv_state.setText("已配对");
        return convertView;
    }

    class ViewHodler {
        public TextView tv_name, tv_adress, tv_state;

    }
}
