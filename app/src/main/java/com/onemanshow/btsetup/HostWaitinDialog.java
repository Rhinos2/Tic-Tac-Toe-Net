package com.onemanshow.btsetup;

import com.onemanshow.tictactoe.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import java.util.Timer;
import java.util.TimerTask;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.onemanshow.bttic.Constants;

public class HostWaitinDialog extends Activity {
	
	private BroadcastReceiver mReceiver = null;
	private final IntentFilter mIntentFilter = new IntentFilter();
	
	private TextView mTimeLeftField = null; 
	private Timer my_timer = null;
	private long VisibleEndTime;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//Remove the default window/layout white background/frame
		getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		setFinishOnTouchOutside(false);
		
		setContentView(R.layout.host_waitin);
		
	/*** Host Game Timer Dialog	*/
			my_timer = new Timer();
			VisibleEndTime = System.currentTimeMillis() + Constants.VISIBLE_FOR;
			my_timer.schedule(new TimerTask(){          
			        @Override
			        public void run(){
			            TimerMethod();
			        }
			    }, 1000, 1000);
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
	private void  TimerMethod()
	{	//We call the method that will work with the UI
	    //through the runOnUiThread.
	    this.runOnUiThread(new Runnable(){
	    	String str;
	    	@Override
	    	public void run(){
	    		long millis =  VisibleEndTime - System.currentTimeMillis();
	            int seconds = (int)(millis / 1000);
	            str = Integer.toString(seconds);
	            if(seconds < 0){
	            	my_timer.cancel();
	            	return;
	            }       
	            mTimeLeftField = (TextView)findViewById(R.id.txt_time);
	            mTimeLeftField.setText(str);
	    	}
	    });
	}
	public void onCancel(View v){
		setResult(RESULT_CANCELED);
		this.finish();
		}
}