package com.heima.testbluetooth.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.heima.testbluetooth.utils.BluetoothTools;
import com.heima.testbluetooth.thread.ClientConnectThread;
import com.heima.testbluetooth.thread.CommomThread;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 客户端主控Service
 */
public class ClientService extends Service {
	private  final  static  String TAG="ClientService";
	private List<BluetoothDevice> discoveredDevices = new ArrayList<BluetoothDevice>();
	private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private CommomThread communThread;

	//广播接收器-控制信息
	private BroadcastReceiver controlReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (BluetoothTools.ACTION_START_DISCOVERY.equals(action)) {
				//开始搜索
				discoveredDevices.clear();	//清空存放设备的集合
				bluetoothAdapter.enable();	//打开蓝牙
				bluetoothAdapter.startDiscovery();	//开始搜索

			} else if (BluetoothTools.ACTION_SELECTED_DEVICE.equals(action)) {
				//选择了连接的服务器设备
				BluetoothDevice device = (BluetoothDevice)intent.getExtras().get(BluetoothTools.DEVICE);
				//开启设备连接线程
				new ClientConnectThread(handler, device).start();
			} else if (BluetoothTools.ACTION_STOP_SERVICE.equals(action)) {
				//停止后台服务
				if (communThread != null) {
					communThread.isRun = false;
				}
				stopSelf();
			} else if (BluetoothTools.ACTION_DATA_TO_SERVICE.equals(action)) {
				//获取数据
				Object data = intent.getSerializableExtra(BluetoothTools.DATA);
				if (communThread != null) {
					communThread.writeObject(data);
				}

			}
		}
	};

	//广播的接收器-蓝牙搜索状态
	private BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			//获取广播的Action
			String action = intent.getAction();

			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				Log.d(TAG,"开始搜索");
			} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				discoveredDevices.add(bluetoothDevice);
				//发现设备-->ClientService
				Intent deviceListIntent = new Intent(BluetoothTools.ACTION_FOUND_DEVICE);
				deviceListIntent.putExtra(BluetoothTools.DEVICE, bluetoothDevice);
				sendBroadcast(deviceListIntent);

			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				Log.d(TAG,"搜索结束");
				//搜索结束
				if (discoveredDevices.isEmpty()) {
					//未找到设备-->ClientService
					Intent foundIntent = new Intent(BluetoothTools.ACTION_NOT_FOUND_SERVER);
					sendBroadcast(foundIntent);
				}
			}
		}
	};


	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			//处理消息
			switch (msg.what) {
				case BluetoothTools.MESSAGE_CONNECT_ERROR:
					//连接错误
					//发送连接错误广播
					Intent errorIntent = new Intent(BluetoothTools.ACTION_CONNECT_ERROR);
					sendBroadcast(errorIntent);
					break;
				case BluetoothTools.MESSAGE_CONNECT_SUCCESS:
					//连接成功

					//开启通讯线程
					communThread = new CommomThread(handler, (BluetoothSocket)msg.obj);
					communThread.start();

					//发送连接成功广播
					Intent succIntent = new Intent(BluetoothTools.ACTION_CONNECT_SUCCESS);
					sendBroadcast(succIntent);
					break;
				case BluetoothTools.MESSAGE_READ_OBJECT:
					//读取到对象
					//发送数据广播（包含数据对象）
					Intent dataIntent = new Intent(BluetoothTools.ACTION_DATA_TO_GAME);
					dataIntent.putExtra(BluetoothTools.DATA, (Serializable)msg.obj);
					sendBroadcast(dataIntent);
					break;
			}
			super.handleMessage(msg);
		}

	};

	/**
	 * 获取通讯线程
	 * @return
	 */
	public CommomThread getBluetoothCommunThread() {
		return communThread;
	}





	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		//discoveryReceiver的IntentFilter
		IntentFilter discoveryFilter = new IntentFilter();
		discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		discoveryFilter.addAction(BluetoothDevice.ACTION_FOUND);

		//controlReceiver的IntentFilter
		IntentFilter controlFilter = new IntentFilter();
		controlFilter.addAction(BluetoothTools.ACTION_START_DISCOVERY);
		controlFilter.addAction(BluetoothTools.ACTION_SELECTED_DEVICE);
		controlFilter.addAction(BluetoothTools.ACTION_STOP_SERVICE);
		controlFilter.addAction(BluetoothTools.ACTION_DATA_TO_SERVICE);

		//注册BroadcastReceiver
		registerReceiver(discoveryReceiver, discoveryFilter);
		registerReceiver(controlReceiver, controlFilter);

		return super.onStartCommand(intent, flags, startId);
	}


	@Override
	public void onDestroy() {
		if (communThread != null) {
			communThread.isRun = false;
		}
		//解除绑定
		unregisterReceiver(discoveryReceiver);
		super.onDestroy();
	}


	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
