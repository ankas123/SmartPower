package com.kaushik.app.smartpower;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anant on 15/01/17.
 */

public class DeviceScanService extends Service {
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice btDevice;
    private Handler mHandler = new Handler();
    private Message mMessage = new Message();
    private Messenger messenger = new Messenger(new MessageHandler());

    private final int START = 1;
    private final int DEVICE = 2;
    private String device = "80:EA:CA:00:00:03";
    private ArrayList<ScanFilter> list = new ArrayList<>() ;
    private ScanSettings scanSettings ;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;




    public DeviceScanService(){
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return messenger.getBinder();
    }

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case START:
                    final BluetoothManager bluetoothManager =
                            (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                    mBluetoothAdapter = bluetoothManager.getAdapter();
                    scanLeDevice(true);
                    break;
                case DEVICE:
                    Message message = new Message();
                    message.obj = btDevice;
                    try {
                        msg.replyTo.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }

        }
    }




    private void scanLeDevice(final boolean enable) {

        if (enable) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            ScanFilter filter = new ScanFilter.Builder().setDeviceAddress(device).build();
            list.add(filter);

            scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothLeScanner.stopScan(mScanCallback);
                    if(btDevice==null){
                        Intent intent = new Intent().setAction("com.kaushik.intent.NODEVICE");
                        sendBroadcast(intent);
                    }
                }
            }, SCAN_PERIOD);


            mBluetoothLeScanner.startScan(list,scanSettings,mScanCallback);
        } else {

            mBluetoothLeScanner.stopScan(mScanCallback);
        }

    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            btDevice= result.getDevice();
            Intent intent = new Intent().setAction("com.kaushik.intent.DEVICE");
            sendBroadcast(intent);

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

}

