package com.onemanshow.wfsetup;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import com.onemanshow.bttic.Constants;
import com.onemanshow.tictactoe.R;

import java.util.Timer;
import java.util.TimerTask;

public class HostWaitinDialogWF extends Activity {
	
	private BroadcastReceiver mReceiver = null;
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
	}
	 @Override
	    protected void onDestroy(){
		  	unregisterReceiver(mReceiver);
	        super.onDestroy();
	    }
	private void  TimerMethod()
	{	// Call the method that will work with the UI
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