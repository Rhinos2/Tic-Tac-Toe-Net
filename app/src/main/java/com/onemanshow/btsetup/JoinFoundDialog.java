package com.onemanshow.btsetup;


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
import android.widget.ProgressBar;
import android.widget.TextView;

public class JoinFoundDialog extends Activity {

	private BroadcastReceiver mReceiver = null;
	private final IntentFilter mIntentFilter = new IntentFilter();
	private ListView mFoundDevList = null;
	private ArrayAdapter<String> adapter;
	public ArrayAdapter<String>  getFoundListAdaper(){
		return adapter;
	}
	private BluetoothAdapter mBluetoothAdapter;
	private String address;
	public String getAddress(){
		return address;
	}
	private ProgressBar mProgress_search;
	public ProgressBar getSearchProgress(){
		return mProgress_search;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//Remove the default window/layout white background/frame
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		setFinishOnTouchOutside(false);
		setContentView(R.layout.guest_found);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		mIntentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        mReceiver = new BTBCastReceiver(this);
        registerReceiver(mReceiver, mIntentFilter);
		mProgress_search = (ProgressBar)findViewById(R.id.progressBar2);
		
		mFoundDevList = (ListView)findViewById(R.id.found_devs);
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
		mFoundDevList.setAdapter(adapter);
		mFoundDevList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				// Cancel discovery because it's costly and we're about to connect
				mBluetoothAdapter.cancelDiscovery();
		
				address = ((TextView) view).getText().toString();
				// Get the device MAC address, which is the last 17 chars in the View
				String opponent_device_mac = address.substring(address.length() - 17);
				BluetoothDevice BToppnt_device = mBluetoothAdapter.getRemoteDevice(opponent_device_mac);
				// device may or may not be paired - make sure it is before trying to connect
				if(BToppnt_device.getBondState() != BluetoothDevice.BOND_BONDED){
				}
		        // Create the result Intent and include the MAC address
		        Intent intent = new Intent();
		        intent.putExtra(com.onemanshow.bttic.BTMainActivity.EXTRA_OPPONENT_DEVICE_ADDRESS, address);
	            setResult(Activity.RESULT_OK, intent);
	            finish();
			}
		});
	mBluetoothAdapter.startDiscovery();//Start Search for nearby devices
	}
	 @Override
	    protected void onDestroy() {
	        super.onDestroy();
	        if(mBluetoothAdapter.isDiscovering())
	        	mBluetoothAdapter.cancelDiscovery();
	       	unregisterReceiver(mReceiver);
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

