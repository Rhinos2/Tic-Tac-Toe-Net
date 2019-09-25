package com.onemanshow.bttic;

import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BTBMonitor extends BroadcastReceiver {
	
	public static final UUID MY_UUID_TIC =
            UUID.fromString("841280cf-5c25-496d-a2fd-0caa65ffd46f");
	
    private Activity activity_host;

    public BTBMonitor(Activity activity){
        super();       
        this.activity_host = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        //BT State
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
        	final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
        	if(state == BluetoothAdapter.STATE_TURNING_ON){
        	}
        	 
        	if(state == BluetoothAdapter.STATE_ON) {

        	}
        	if(state == BluetoothAdapter.STATE_OFF){

        	}
        }
      //Connection state
        else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)){
        	final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR);
        	if(state == BluetoothAdapter.STATE_CONNECTED){ 
        		
        	}
        	if(state == BluetoothAdapter.STATE_DISCONNECTED){
        		
        	}
        }
        else if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
        	BluetoothDevice device = (BluetoothDevice)intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);      	 
        	String RemoteDeviceName = device.getName();
        	Intent intent_data = new Intent();
        	intent_data.putExtra(Constants.EXTRA_OPPONENT_DEV_NAME, RemoteDeviceName);
        	activity_host.setResult(Constants.RESULT_SECOND_USER, intent_data);
        	activity_host.finish();
        }
        else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
        	
        }
        
    }
}