package com.onemanshow.btsetup;

import com.onemanshow.tictactoe.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;


public class JoinConningDialog extends Activity {
	
	private BroadcastReceiver mReceiver = null;
	private final IntentFilter mIntentFilter = new IntentFilter();

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//Remove the default window/layout white background/frame
		getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		setFinishOnTouchOutside(false);
		
		setContentView(R.layout.guest_connecting);
		

			mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
			mIntentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);			
			mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
			mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
			
			 mReceiver = new BTBCastReceiver(this);
		     registerReceiver(mReceiver, mIntentFilter);
	}
	 @Override
	    protected void onDestroy(){
		  	unregisterReceiver(mReceiver);
	        super.onDestroy();
	    }
	
	public void onCancel(View v){
		setResult(RESULT_CANCELED);
		this.finish();
		}
}