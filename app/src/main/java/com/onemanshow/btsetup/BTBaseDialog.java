package com.onemanshow.btsetup;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.onemanshow.bttic.BTBMonitor;
import com.onemanshow.bttic.Constants;
import com.onemanshow.tictactoe.R;

public class BTBaseDialog extends Activity {

	private TextView mTimeLeftField = null; 
	private TextView mtvThisDevName;
	private Timer my_timer = null;
	private long VisibleEndTime;
	private ListView mFoundDevList = null;
	private ListView mBondedDevList = null;
	private ArrayAdapter<String> adapter;
	private BluetoothAdapter mBluetoothAdapter;
	private Set<BluetoothDevice> mPairedDevices;
	private String mThisDeviceName;
	private ProgressBar mProgress_search;
	private int resID;
	private BroadcastReceiver mReceiver = null;
	private final IntentFilter mIntentFilter = new IntentFilter();
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//Remove the default window/layout white background/frame
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		setFinishOnTouchOutside(false);
		
		resID = intent.getIntExtra(Constants.EXTRA_INT, 100);
		setContentView(resID);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mThisDeviceName = mBluetoothAdapter.getName();
	
	/*** Host Game ShowMe Dialog */
		if(resID == R.layout.host_show_me){
			mtvThisDevName = (TextView)findViewById(R.id.this_dev_name);			
			mtvThisDevName.setText("This Device: " + mThisDeviceName);
		}
	/*** Host Game Timer Dialog	*/
		else if(resID == R.layout.host_waitin){
			TextView mTextField = (TextView)findViewById(R.id.txt_time);
			my_timer = new Timer();
			VisibleEndTime = System.currentTimeMillis() + Constants.VISIBLE_FOR;
			my_timer.schedule(new TimerTask(){          
			        @Override
			        public void run(){
			            TimerMethod();
			        }
			    }, 1000, 1000);
		}/*** Join Game Found Dialog */
		else if(resID == R.layout.guest_found){
			mProgress_search = (ProgressBar) findViewById(R.id.progressBar2);
			mProgress_search.setVisibility(View.VISIBLE);
			mFoundDevList = (ListView)findViewById(R.id.found_devs);
			adapter = new ArrayAdapter<String>(this, R.layout.list_search_item, android.R.id.text1){
				@Override
				public View getView(int position, View convertView, ViewGroup parent){
					View view = super.getView(position, convertView, parent);
					TextView ListItemShow = (TextView) view.findViewById(android.R.id.text1);
					ListItemShow.setTextColor(Color.parseColor("#fe00fb"));

					return view;
				}
			};
			mFoundDevList.setAdapter(adapter);
			adapter.add("Dummy Device");
			mFoundDevList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id){
					ItemSelectMethod();
				}
			});
		mBluetoothAdapter.startDiscovery();//Start Search for nearby devices
			
		}/*** Join Game Search Dialog */
		else if(resID == R.layout.guest_search){
			mBondedDevList = (ListView)findViewById(R.id.bonded_list);
			adapter = new ArrayAdapter<String>(this, R.layout.list_search_item, android.R.id.text1){
				@Override
				public View getView(int position, View convertView, ViewGroup parent){
					View view = super.getView(position, convertView, parent);
					TextView ListItemShow = (TextView) view.findViewById(android.R.id.text1);
					ListItemShow.setTextColor(Color.parseColor("#fe00fb"));

					return view;
				}
			};
			mBondedDevList.setAdapter(adapter);
			mtvThisDevName = (TextView)findViewById(R.id.this_dev_name);
			mtvThisDevName.setText("Device: " + mThisDeviceName);
			mBondedDevList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id){
					// Cancel discovery because it's costly and we're about to connect
					mBluetoothAdapter.cancelDiscovery();
					String address = ((TextView) view).getText().toString();
					if(address.equals("No paired devices found."))
						return;
			        // Create the result Intent and include the MAC address
			        Intent intent = new Intent();
			        intent.putExtra(com.onemanshow.bttic.BTMainActivity.EXTRA_OPPONENT_DEVICE_ADDRESS, address);
			        // Set the result to distinguish from the "Search"(RESULT_OK)
		            setResult(Activity.RESULT_FIRST_USER, intent);
		            finish();
				}
			});
			//get paired devices list
			mPairedDevices = mBluetoothAdapter.getBondedDevices();
			if(mPairedDevices.size() == 0 ){
				adapter.add("No paired devices found.");
			}
			else{
				for(BluetoothDevice device : mPairedDevices){
					//add the name and address to the array
					adapter.add(device.getName() + "\n" + device.getAddress());
				}
			}
		}
		mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		mIntentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);			
		mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		mReceiver = new BTBMonitor(this);
	    registerReceiver(mReceiver, mIntentFilter);
	}
	private void ItemSelectMethod(){
		int resId = R.layout.ready_play;
		Intent intent =	new Intent(this, BTBaseDialog.class);
    	intent.putExtra(Constants.EXTRA_INT, resId);
    	this.startActivityForResult(intent, Constants.RQC_JOIN_READY);
    	this.setResult(RESULT_OK);
		this.finish();
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

	public void onOK(View v){
		Intent intent = new Intent();
		setResult(RESULT_OK, intent);
		this.finish();
		}
	public void onCancel(View v){//"multi-purpose" cancel
		setResult(RESULT_CANCELED);
		this.finish();
		}
	public void onConfirmEndBtGame(View v){
		setResult(RESULT_OK);
		this.finish();
	}
	public void onHostGame(View v){
		this.setResult(RESULT_FIRST_USER);
		this.finish();
		}
	public void onShowMe(View v){
		this.setResult(RESULT_OK);
		this.finish();
	}
	public void onSkip(View v){
		this.setResult(RESULT_FIRST_USER);
		this.finish();
	}
	public void onJoinGame(View v){
		this.setResult(RESULT_OK);
		this.finish();
		}
	public void onStartSearch(View v){
		this.setResult(RESULT_OK);
		this.finish();
		}
	
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
		  if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
		  }
		 
		    return super.onKeyDown(keyCode, event);
		}
}

