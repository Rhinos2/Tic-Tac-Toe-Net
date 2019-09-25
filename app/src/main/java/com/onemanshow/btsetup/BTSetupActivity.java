
package com.onemanshow.btsetup;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;


import com.onemanshow.tictactoe.R;

public class BTSetupActivity extends Activity {
    boolean mBound = false;
    BluetoothService mService;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    @Override
    protected void onStart() {
        super.onStart();
        //check BT's availability early

        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onStop() {
        super.onStop();


    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_join);
        Intent intent = new Intent(this, BluetoothService.class);
        intent.putExtra(BluetoothService.COMMAND_KEY, BluetoothService.COMMAND_START_LISTENING);
        startService(intent);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        Intent intent = new Intent(this, BluetoothService.class);
        stopService(intent);
    }
    public void onStartListening(View v) {

    }

    public void onStopListening(View v) {
        //  Intent intent = new Intent(this, BluetoothService.class);
        //  stopService(intent);
        if (mBound) {// Call a method from the LocalService.
          //  Toast.makeText(this, "number: " , Toast.LENGTH_SHORT).show();
        }
    }
}