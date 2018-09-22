package com.heima.testbluetooth.thread;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import com.heima.testbluetooth.utils.BluetoothTools;

import java.io.IOException;

/**
 * 客户端连接
 */
public class ClientConnectThread extends Thread {

    private Handler serviceHandler;
    private BluetoothDevice serverDevice;
    private BluetoothSocket socket;

    /**
     * @param handler      向ClientService回传消息
     * @param serverDevice
     */
    public ClientConnectThread(Handler handler, BluetoothDevice serverDevice) {
        this.serviceHandler = handler;
        this.serverDevice = serverDevice;
    }

    @Override
    public void run() {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        try {
            socket = serverDevice.createRfcommSocketToServiceRecord(BluetoothTools.PRIVATE_UUID);
            socket.connect();

        } catch (Exception ex) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //连接失败-->ClientService Handler
            serviceHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
            return;
        }

        //连接成功，socket-->ClientService
        Message msg = serviceHandler.obtainMessage();
        msg.what = BluetoothTools.MESSAGE_CONNECT_SUCCESS;
        msg.obj = socket;
        msg.sendToTarget();
    }
}
