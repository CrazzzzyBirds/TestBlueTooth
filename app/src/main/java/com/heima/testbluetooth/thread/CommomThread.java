package com.heima.testbluetooth.thread;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import com.heima.testbluetooth.utils.BluetoothTools;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * 通用蓝牙通讯
 */
public class CommomThread extends Thread {

    private Handler serviceHandler;        //与Service通信的Handler
    private BluetoothSocket socket;
    private ObjectInputStream inStream;
    private ObjectOutputStream outStream;
    public volatile boolean isRun = true;    //运行标志位

    /**
     * @param handler 接收消息
     * @param socket
     */
    public CommomThread(Handler handler, BluetoothSocket socket) {
        this.serviceHandler = handler;
        this.socket = socket;
        try {
            this.outStream = new ObjectOutputStream(socket.getOutputStream());
            this.inStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        } catch (Exception e) {
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            //发送连接失败消息
            serviceHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            if (!isRun) {
                break;
            }
            try {
                Object obj = inStream.readObject();
                //接受消息-->Service
                Message msg = serviceHandler.obtainMessage();
                msg.what = BluetoothTools.MESSAGE_READ_OBJECT;
                msg.obj = obj;
                msg.sendToTarget();
            } catch (Exception ex) {
                //连接失败
                serviceHandler.obtainMessage(BluetoothTools.MESSAGE_CONNECT_ERROR).sendToTarget();
                ex.printStackTrace();
                return;
            }
        }

        //关闭流
        if (inStream != null) {
            try {
                inStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (outStream != null) {
            try {
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 写入可序列化对象
     *
     * @param obj 需要传输内容
     */
    public void writeObject(Object obj) {
        try {
            outStream.flush();
            outStream.writeObject(obj);
            outStream.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
