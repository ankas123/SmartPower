package com.kaushik.app.smartpower;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

import static android.R.color.darker_gray;

public class SwitchActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private boolean lock=false;
    private BluetoothDevice mDevice;
    private BluetoothGatt mGatt;
    private BluetoothGattCharacteristic characteristic;
    private boolean setNot;

    private Messenger messenger ;

    private int START = 1;
    private final int DEVICE = 2;

    private boolean serviceBind = false;
    private DeviceBroadcastReceiver deviceBroadcastReceiver = new DeviceBroadcastReceiver();
    private IntentFilter intentFilter;
    private FloatingActionButton fab;
    private TextView conn;
    private Button button;
    private ProgressBar conn_prog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        conn = (TextView) findViewById(R.id.conn);
        button = (Button) findViewById(R.id.conn_button);
        conn_prog = (ProgressBar) findViewById(R.id.conn_prog);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                checkBle();
            }
        });

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();

        intentFilter= new IntentFilter();
        intentFilter.addAction("com.kaushik.intent.DEVICE");
        intentFilter.addAction("com.kaushik.intent.NODEVICE");
        this.registerReceiver(deviceBroadcastReceiver,intentFilter);
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.


    }
    private void checkBle(){
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {

            bleRequest();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode == -1) {
               bleRequest();
            }
    }



    protected void bleRequest(){
        conn_prog.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this,DeviceScanService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        //deviceScanActivity.status(lock);

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            messenger = new Messenger(iBinder);
            serviceBind= true;
           startBle();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceBind = false;
        }
    };

    public class DeviceBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case "com.kaushik.intent.DEVICE":
                    getDevice();
                    break;
                case "com.kaushik.intent.NODEVICE":
                    noDevice();
                    break;
            }

        }
    }




    private void getDevice(){
        Message msg = Message.obtain(null, DEVICE);
        msg.replyTo = new Messenger(new MessageHandler());
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void startBle(){
        Message msg = Message.obtain(null,START);

        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private class MessageHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mDevice = (BluetoothDevice) msg.obj;
            connectToDevice(mDevice);
        }

    }
    private void noDevice() {
        conn_prog.setVisibility(View.INVISIBLE);
        Toast.makeText(getApplicationContext(),"Device was not found",Toast.LENGTH_SHORT).show();
        unbindService(serviceConnection);
        serviceBind = false;
    }
    private void connected(){
        conn_prog.setVisibility(View.INVISIBLE);
        conn.setText("Connected");
        conn.setTextColor(getResources().getColor(R.color.ble,null));
        button.setVisibility(View.INVISIBLE);

        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent,null)));
        fab.setEnabled(true);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(lock==false){
                    on();
                }
                else {
                    off();
                }
            }
        });
    }

    private void disconnected(){
        conn_prog.setVisibility(View.INVISIBLE);
        conn.setText("Disconnected");
        conn.setTextColor(conn.getTextColors().getDefaultColor());
        button.setVisibility(View.VISIBLE);

        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(darker_gray,null)));
        fab.setEnabled(false);

    }


    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback);
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            disconnected();
                        }
                    });
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());

            characteristic = gatt.getService(UUID.fromString("edfec62e-9910-0bac-5241-d8bda6932a2f")).getCharacteristic(UUID.fromString("5a87b4ef-3bfa-76a8-e642-92933c31434f"));
            if(checkGatt()) {
                mGatt.readCharacteristic(characteristic);
            }


            Log.i("onServicesDiscoveredc", services.get(3).getCharacteristics().get(0).getUuid().toString());
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if(checkGatt()) {
                mGatt.readCharacteristic(characteristic);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());

            byte val[] = characteristic.getValue();
            if(setNot==false) {
                mGatt.setCharacteristicNotification(characteristic, true);
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));

                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mGatt.writeDescriptor(descriptor);
                setNot= true;
            }

            if (val[0]==0x00) lock = false;
            else lock= true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connected();
                }
            });

        }
    };






    public void on(){
        if(checkGatt()) {
            characteristic.setValue(0x01, android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            mGatt.writeCharacteristic(characteristic);
            lock = true;
        }
    }

    public void off(){
        if(checkGatt()) {
            characteristic.setValue(0x00, android.bluetooth.BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            mGatt.writeCharacteristic(characteristic);
            lock = false;
        }
    }
    protected boolean checkGatt(){
        if (mGatt != null){
            return true;
        }
        return false;
    }


    @Override
    protected void onStop() {
        if (serviceBind == true){
        serviceBind = false ;
        messenger = null;
        unbindService(serviceConnection);}
        super.onStop();
    }

   @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(deviceBroadcastReceiver);

    }

    @Override
    protected void onResume() {
        super.onResume();
        this.registerReceiver(deviceBroadcastReceiver,intentFilter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mGatt!=null)
        mGatt.close();
        mGatt = null;
        Log.i("des","d");

    }

}
