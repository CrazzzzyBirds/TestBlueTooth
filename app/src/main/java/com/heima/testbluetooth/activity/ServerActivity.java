package com.heima.testbluetooth.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.heima.testbluetooth.R;
import com.heima.testbluetooth.utils.TimeUtil;
import com.heima.testbluetooth.service.ServerService;
import com.heima.testbluetooth.utils.BluetoothTools;
import com.heima.testbluetooth.entitys.Message;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

@ContentView(R.layout.activity_server)
public class ServerActivity extends AppCompatActivity {

    @ViewInject(R.id.tv_server_state)
    TextView tv_server_state;
    @ViewInject(R.id.tv_server_result)
    TextView tv_server_result;
    @ViewInject(R.id.et_server_send)
    EditText et_server_send;
    @ViewInject(R.id.btn_server_send)
    Button btn_server_send;

    //广播接收器
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (BluetoothTools.ACTION_DATA_TO_GAME.equals(action)) {
                //接收数据
                Message data = (Message) intent.getExtras().getSerializable(BluetoothTools.DATA);
                String msg =  TimeUtil.getDate() + " : " + data.getMsg() + "\r\n";
                tv_server_result.append(msg);

            } else if (BluetoothTools.ACTION_CONNECT_SUCCESS.equals(action)) {
                //连接成功
                tv_server_state.setText("连接成功");
                btn_server_send.setEnabled(true);
            }

        }
    };

    @Override
    protected void onStart() {
        //开启后台service
        Intent startService = new Intent(ServerActivity.this, ServerService.class);
        startService(startService);

        //注册BoradcasrReceiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothTools.ACTION_DATA_TO_GAME);
        intentFilter.addAction(BluetoothTools.ACTION_CONNECT_SUCCESS);

        registerReceiver(broadcastReceiver, intentFilter);
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);

        tv_server_state.setText("正在连接...");
        btn_server_send.setEnabled(false);
    }

    @Event(value = {R.id.btn_server_send})
    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_server_send:
                if ("".equals(et_server_send.getText().toString().trim())) {
                    Toast.makeText(ServerActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
                } else {
                    //发送消息
                    Message data = new Message();
                    data.setMsg(et_server_send.getText().toString());
                    Intent sendDataIntent = new Intent(BluetoothTools.ACTION_DATA_TO_SERVICE);
                    sendDataIntent.putExtra(BluetoothTools.DATA, data);
                    sendBroadcast(sendDataIntent);
                }
                break;

        }

    }


    @Override
    protected void onStop() {
        //关闭后台Service
        Intent startService = new Intent(BluetoothTools.ACTION_STOP_SERVICE);
        sendBroadcast(startService);
        unregisterReceiver(broadcastReceiver);

        super.onStop();
    }
}
