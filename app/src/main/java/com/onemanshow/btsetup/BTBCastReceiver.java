package com.onemanshow.btsetup;

import java.util.UUID;

import com.onemanshow.bttic.BTMainActivity;
import com.onemanshow.bttic.Constants;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;


public class BTBCastReceiver extends BroadcastReceiver {
	
	public static final UUID MY_UUID_TIC =
            UUID.fromString("841280cf-5c25-496d-a2fd-0caa65ffd46f");
	
    private Activity activity_host;

    public BTBCastReceiver(Activity activity){
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
        		Toast.makeText(activity_host, "Bluetooth is ON", Toast.LENGTH_SHORT).show();
        	}
        	if(state == BluetoothAdapter.STATE_OFF){
        		Toast.makeText(activity_host, "Bluetooth is OFF", Toast.LENGTH_SHORT).show();
        	}
        }
      //Connection state
        else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)){
        	final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR);
        	if(state == BluetoothAdapter.STATE_CONNECTED){
        	}
        	if(state == BluetoothAdapter.STATE_DISCONNECTED){
        		BluetoothDevice device = (BluetoothDevice)intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                	String str_toast = "Disconnected from  ";
                	str_toast += device.getName();
                	((BTMainActivity)activity_host).CustomToast(str_toast);
                	
                	activity_host.finish();// disconnected for whatever the reason - return to the EntryPoint
        	}
        }
        else if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
        	BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
 
        	String RemoteDeviceName = device.getName();
        	Intent intent_data = new Intent();
        	intent_data.putExtra(Constants.EXTRA_OPPONENT_DEV_NAME, RemoteDeviceName);
        
        	activity_host.setResult(Activity.RESULT_OK, intent_data);
        	activity_host.finish();
        }
        else if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
        	BluetoothDevice device = (BluetoothDevice)intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        //	mRemoteDeviceName = device.getName();
        	String str_toast = "Disconnected from  ";
        	str_toast += device.getName();
        	((BTMainActivity)activity_host).CustomToast(str_toast);
        	
        	activity_host.finish();// disconnected for whatever the reason - return to the EntryPoint
        }
        //Device discovery
        else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
        	(((JoinFoundDialog)activity_host).getSearchProgress()).setVisibility(View.VISIBLE);
			
        }
        else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
        	ProgressBar prgs  = ((JoinFoundDialog)activity_host).getSearchProgress();
        	prgs.setVisibility(View.GONE);
        	ArrayAdapter<String> adapter = ((JoinFoundDialog)activity_host).getFoundListAdaper();
        	if(adapter.getCount() < 1) {
				adapter.add("No paired devices found");
			}
        }
        else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
        	BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        	ArrayAdapter<String> adapter = ((JoinFoundDialog)activity_host).getFoundListAdaper();
        	String item =  (device.getName() + "\n" + device.getAddress());
        	if(adapter.getPosition(item) < 0)
        		adapter.add(item);
        }
        else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {	        	
       	 	final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
       	 	final int prevState	= intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
       	 
       	 	if(state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
       	 	}
  
       }
        else if(BluetoothDevice.ACTION_UUID.equals(action)) {
        	ParcelUuid parcelUUID = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        	
        	if(MY_UUID_TIC == parcelUUID.getUuid()){
	        		 Toast.makeText(activity_host, "Found!", Toast.LENGTH_SHORT).show();
	        		 }
        }
    }
}