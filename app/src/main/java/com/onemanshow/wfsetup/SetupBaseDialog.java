package com.onemanshow.wfsetup;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import com.onemanshow.tictactoe.R;
import java.util.Timer;
import java.util.TimerTask;

public class SetupBaseDialog extends Activity {
	
	private TextView mTimeLeftField;
	private long VisibleEndTime;
	private Timer my_timer = null;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//Remove the default window/layout white background/frame
		getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		setFinishOnTouchOutside(false);
		
		int resID = intent.getIntExtra(Constants.EXTRA_INT, 100);
		setContentView(resID);
		/*** Host Game Timer Dialog	*/
		if(resID == R.layout.host_waitin){
			TextView mTextField = (TextView)findViewById(R.id.txt_time);
			my_timer = new Timer();
			VisibleEndTime = System.currentTimeMillis() + Constants.VISIBLE_FOR;
			my_timer.schedule(new TimerTask(){
			        @Override
			        public void run(){
			            TimerMethod();
			        }
			    }, 1000, 1000);
		}
		
	}
	private void ItemSelectMethod(){
		int resId = com.onemanshow.tictactoe.R.layout.ready_play;
		Intent intent =	new Intent(this, SetupBaseDialog.class);
		intent.putExtra(Constants.EXTRA_INT, resId);
		this.startActivityForResult(intent, Constants.RQC_JOIN_READY);
		this.setResult(RESULT_OK);// data);
		this.finish();
	}
	private void  TimerMethod()
	{	//works with the UI through the runOnUiThread method.
		//has to be called from the inside of the 'run()'
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
	public void onHostGame(View v){
		this.setResult(RESULT_FIRST_USER);
		this.finish();
		}
	public void onJoinGame(View v){
		this.setResult(RESULT_OK);
		this.finish();
		}
	public void onCancel(View v){//"multi-purpose" cancel
		setResult(Constants.RESULT_CANCELED);
		this.finish();
		}


	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
		}

		return super.onKeyDown(keyCode, event);
	}
}