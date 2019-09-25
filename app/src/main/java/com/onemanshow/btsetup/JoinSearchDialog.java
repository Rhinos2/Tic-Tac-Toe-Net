package com.onemanshow.btsetup;

import com.onemanshow.bttic.Constants;
import com.onemanshow.tictactoe.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import android.widget.TextView;

public class JoinSearchDialog extends Activity {

	private BroadcastReceiver mReceiver = null;
	private final IntentFilter mIntentFilter = new IntentFilter();
	
	
	private ListView mBondedDevList = null;
	
	
	private ArrayAdapter<String> adapter;
	public ArrayAdapter<String>  getBondedListAdaper(){
		return adapter;
	}
	private BluetoothAdapter mBluetoothAdapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
	//	Intent intent = getIntent();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//Remove the default window/layout white background/frame
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		setFinishOnTouchOutside(false);		
//		int resID = intent.getIntExtra(MyDirectTest.EXTRA_INT, 100);
		setContentView(R.layout.guest_found);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
 //       mIntentFilter.addAction(BluetoothDevice.ACTION_UUID);
        mReceiver = new BTBCastReceiver(this);
        registerReceiver(mReceiver, mIntentFilter);
		
		mBondedDevList = (ListView)findViewById(R.id.found_devs);
		adapter = new ArrayAdapter<String>(this, R.layout.list_search_item, android.R.id.text1){
			@Override
			public View getView(int position, View convertView, ViewGroup parent){
				View view = super.getView(position, convertView, parent);
				TextView ListItemShow = (TextView) view.findViewById(android.R.id.text1);
				//if(position == 3)
					ListItemShow.setTextColor(Color.parseColor("#fe00fb"));

				return view;
			}
		};
		mBondedDevList.setAdapter(adapter);
		mBondedDevList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				// Cancel discovery because it's costly and we're about to connect
				mBluetoothAdapter.cancelDiscovery();
				String address = ((TextView) view).getText().toString();
		        // Create the result Intent and include the MAC address
		        Intent intent = new Intent();
		        intent.putExtra(com.onemanshow.bttic.BTMainActivity.EXTRA_OPPONENT_DEVICE_ADDRESS, address);
		        // Set the result fo distinguish from the "Search"(RESULT_OK)
	            setResult(Activity.RESULT_FIRST_USER, intent);
	            finish();
			}
		});
	mBluetoothAdapter.startDiscovery();//Start Search for nearby devices
	}
	 @Override
	    protected void onDestroy() {
	        super.onDestroy();
	       
	       	unregisterReceiver(mReceiver);
	    } 
	private void ItemSelectMethod(){
		

		int resId = R.layout.ready_play;
		Intent intent =	new Intent(this, BTBaseDialog.class);
    	intent.putExtra(Constants.EXTRA_INT, resId);
    	this.startActivityForResult(intent, Constants.RQC_JOIN_READY);
    	this.setResult(RESULT_OK);// data);
		this.finish();
	}
	
	public void onOK(View v){
		Intent intent = new Intent();
		setResult(RESULT_OK, intent);
		this.finish();
		}
	public void onCancel(View v){
		setResult(RESULT_CANCELED);
		this.finish();
		}
	public void onHostGame(View v){

		}
}

