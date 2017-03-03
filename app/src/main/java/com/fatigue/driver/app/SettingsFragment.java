package com.fatigue.driver.app;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * Created by Eric on 11/15/2016.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.app_preferences);
    }
}
//public class DeviceListAdapter extends BaseAdapter {
//
//    private LayoutInflater mInflater;
//    private List<BluetoothDevice> mData;
//    private OnConnectButtonClickListener connectListener;
//
//    public DeviceListAdapter(Context context) {
//        mInflater = LayoutInflater.from(context);
//    }
//
//    public void setData(List<BluetoothDevice> data) {
//        mData = data;
//    }
//
//    public void setListener(OnConnectButtonClickListener listener) {
//        connectListener = listener;
//    }
//
//    public int getCount() {
//        return (mData == null) ? 0 : mData.size();
//    }
//
//    public Object getItem(int position) {
//        return null;
//    }
//
//    public long getItemId(int position) {
//        return position;
//    }
//
//    public View getView(final int position, View convertView, ViewGroup parent) {
//        ViewHolder holder;
//
//        if (bluetoothInfo.isConnected()) {
//            holder.connectBtn.setText("connected");
//        } else {
//            holder.connectBtn.setText("connect");
//        }
//
//        BluetoothDevice device = mData.get(position);
//
//        holder.nameTv.setText(device.getName());
//        holder.addressTv.setText(device.getAddress());
//        holder.connectBtn.setText("connect");
//        holder.connectBtn.setText("connected");
//
//        holder.connectBtn.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                if (connectListener != null) {
//                    connectListener.onConnectButtonClick(position);
//                }
//            }
//        });
//
//        return convertView;
//    }
//
//    static class ViewHolder {
//        TextView nameTv;
//        TextView addressTv;
//        Button connectBtn;
//    }
//
//    public interface OnConnectButtonClickListener {
//        public abstract void onConnectButtonClick(int position);
//
//    }
//
