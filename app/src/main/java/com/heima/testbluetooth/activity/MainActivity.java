package com.heima.testbluetooth.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.heima.testbluetooth.R;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

@ContentView(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    @ViewInject(R.id.btn_server)
    Button btn_server;
    @ViewInject(R.id.btn_client)
    Button btn_client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
    }

    @Event(value = {R.id.btn_client, R.id.btn_server})
    private void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_server:
                //服务器
                Intent serverIntent = new Intent(MainActivity.this, ServerActivity.class);
                serverIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(serverIntent);
                break;

            case R.id.btn_client:
                //客户端
                Intent clientIntent = new Intent(MainActivity.this, ClientActivity.class);
                clientIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(clientIntent);
                break;
        }


    }

}