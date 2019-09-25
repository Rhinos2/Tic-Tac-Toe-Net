package com.onemanshow.btsetup;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import java.io.IOException;
import java.util.UUID;


public class BluetoothService extends Service {

    private static final String TAG = "BluetoothService";
    public static final String COMMAND_KEY = "command_key";
    public static final String COMMAND_START_LISTENING = "command_start_discovery";

    private static final UUID MY_UUID = UUID.fromString("841280cf-5c25-496d-a2fd-0caa65ffd46f");
    private static final String SDP_NAME = "TicTacToe_onemanarmy";

    private BluetoothAdapter mAdapter;
    private BluetoothServerSocket mServerSocket;
    private boolean mListening = false;
    private Thread listeningThread;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        BluetoothService getService() { // Return this instance of LocalService so clients can call public methods
            return BluetoothService.this;
        }
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_REDELIVER_INTENT;
    }

    public BluetoothAdapter getBTadapter() {
        return mAdapter;
    }
    private void startListening() {
        mListening = true;
        listeningThread = new Thread(new Runnable() {

            @Override
            public void run() {
                BluetoothSocket socket = null;
                try {
                    mServerSocket = mAdapter.listenUsingInsecureRfcommWithServiceRecord(SDP_NAME, MY_UUID);
                    socket = mServerSocket.accept();

                    if (socket != null) {
                    }

                } catch (IOException e) {
                    Log.d(TAG, "Server socket closed");
                }
            }
        });
        listeningThread.start();
    }

    private void stopListening() {
        mListening = false;
        try {
            if (mServerSocket != null) {
                mServerSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
