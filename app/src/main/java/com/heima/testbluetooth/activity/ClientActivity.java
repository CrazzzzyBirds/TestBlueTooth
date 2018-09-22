package com.heima.testbluetooth.activity;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.heima.testbluetooth.adapter.DeviceListAdapter;
import com.heima.testbluetooth.R;
import com.heima.testbluetooth.utils.TimeUtil;
import com.heima.testbluetooth.service.ClientService;
import com.heima.testbluetooth.utils.BluetoothTools;
import com.heima.testbluetooth.entitys.Message;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

@ContentView(R.layout.activity_client)
public class ClientActivity extends AppCompatActivity {
    @ViewInject(R.id.tv_server_text)
    TextView tv_server_text;
    @ViewInject(R.id.tv_client_result)
    TextView tv_client_result;
    @ViewInject(R.id.et_client_send)
    EditText et_client_send;
    @ViewInject(R.id.btn_client_send)
    Button btn_client_send;
    @ViewInject(R.id.btn_client_search)
    Button btn_client_search;
    @ViewInject(R.id.lv_client_device)
    ListView lv_client_device;

    DeviceListAdapter deviceListAdapter;
    private List<BluetoothDevice> device_list = new ArrayList<>();

    //广播接收器
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothTools.ACTION_NOT_FOUND_SERVER.equals(action)) {
                //未发现设备
                tv_server_text.append("not found device\r\n");

            } else if (BluetoothTools.ACTION_FOUND_DEVICE.equals(action)) {
                //获取到设备对象
                BluetoothDevice device = (BluetoothDevice) intent.getExtras().get(BluetoothTools.DEVICE);
                device_list.add(device);
                deviceListAdapter.notifyDataSetChanged();
            } else if (BluetoothTools.ACTION_CONNECT_SUCCESS.equals(action)) {
                //连接成功
                tv_server_text.append("连接成功");
                btn_client_send.setEnabled(true);

            } else if (BluetoothTools.ACTION_DATA_TO_GAME.equals(action)) {
                //接收数据
                Message data = (Message) intent.getExtras().getSerializable(BluetoothTools.DATA);
                String msg =  TimeUtil.getDate() + " : " + data.getMsg() + "\r\n";
                tv_client_result.append(msg);

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);

        setView();
        setConfig();
    }

    @Event(value = {R.id.btn_client_search,R.id.btn_client_send})
    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_client_search:    //搜索
                device_list.clear();
                Intent startSearchIntent = new Intent(BluetoothTools.ACTION_START_DISCOVERY);
                sendBroadcast(startSearchIntent);
                break;
            case R.id.btn_client_send: //发送消息
                if ("".equals(et_client_send.getText().toString().trim())) {
                    Toast.makeText(ClientActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    //发送消息
                    Message data = new Message();
                    data.setMsg(et_client_send.getText().toString());
                    Intent sendDataIntent = new Intent(BluetoothTools.ACTION_DATA_TO_SERVICE);
                    sendDataIntent.putExtra(BluetoothTools.DATA, data);
                    sendBroadcast(sendDataIntent);
                }
                break;
        }
    }


    /**
     * view设置
     */
    private void setView(){
        deviceListAdapter=new DeviceListAdapter(device_list,this);
        lv_client_device.setAdapter(deviceListAdapter);

        lv_client_device.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //传递device到service
                Intent selectDeviceIntent = new Intent(BluetoothTools.ACTION_SELECTED_DEVICE);
                selectDeviceIntent.putExtra(BluetoothTools.DEVICE, device_list.get(position));
                sendBroadcast(selectDeviceIntent);
            }
        });
    }

    /**
     * 配置基础设置
     */
    private  void setConfig(){
        Intent startService = new Intent(ClientActivity.this, ClientService.class);
        startService(startService);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothTools.ACTION_NOT_FOUND_SERVER);
        intentFilter.addAction(BluetoothTools.ACTION_FOUND_DEVICE);
        intentFilter.addAction(BluetoothTools.ACTION_DATA_TO_GAME);
        intentFilter.addAction(BluetoothTools.ACTION_CONNECT_SUCCESS);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        //关闭资源
        Intent startService = new Intent(BluetoothTools.ACTION_STOP_SERVICE);
        sendBroadcast(startService);
        unregisterReceiver(broadcastReceiver);
        super.onStop();
    }


}
