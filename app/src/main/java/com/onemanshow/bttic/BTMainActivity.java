package com.onemanshow.bttic;

import com.onemanshow.tictactoe.R;
import com.onemanshow.btsetup.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;



public class BTMainActivity extends Activity {

	private static final String TAG = "MYEXEPTION";
	public static final int MY_REQUEST_CODE_SOUND = 52;
	public static final int MY_REQUEST_CODE_BTSETUP = 51;
	public static final int MY_REQUEST_CODE_ENABLE_BT = 50;
	public static final String EXTRA_OPPONENT_DEVICE_ADDRESS = "device_address";
	public static final String NAME_GAME = "TicTacToe_onemanarmy";
	public static final String SOUND = "Sound";
	
	public static final int     MSG_BUFFER_SIZE = 15;
	public static final int 	MESSAGE_SEND_MOVE = 99;
	public static final int		MESSAGE_TOAST = 97;
	public static final int 	MSG_SEND_REQUEST_SHUTDOWN = 95;
	public static final int     MSG_LOCAL_WINS_THIS_ROUND = 89;
	public static final int     MSG_REMOTE_WINS_THIS_ROUND = 88;
	public static final int 	RESULT_SECOND_USER = 2;
	 // Unique UUID for this application
    public static final UUID MY_UUID_TIC =
            UUID.fromString("841280cf-5c25-496d-a2fd-0caa65ffd46f");
	private RadioGroup radGrLevel;
	private ImageView imgViewTurn;
	public void ToggleImgTurn(boolean bLock){
		if(!bLock)
			imgViewTurn.setImageResource(R.drawable.go);
		else
			imgViewTurn.setImageResource(R.drawable.no_enter);
	}
	
	private TextView txtThisDevScore;
	private TextView txtRemDevScore;
	private ViewProto ticView;
	private TextView txtThisDevName;
	private TextView txtRemDevName;
	private String ThisDevNickname = "DE:";
	private String mThisDeviceName = "";
	private String mRemoteDeviceName = "";
	/*** getters */
	public String getThisDevName(){ return mThisDeviceName; }
	public String getRemoteDevName(){ return mRemoteDeviceName; }
	/***/
	private BroadcastReceiver mReceiver = null;
	private final IntentFilter mIntentFilter = new IntentFilter();
	
	private BluetoothAdapter mBluetoothAdapter = null;
	private AcceptThread mServerSocketThread = null;
	private ConnectThread mClientSocketThread = null;
	private ConnectedThread mConnedThread = null;
	private BluetoothSocket s_socket = null;
	private BluetoothSocket c_socket = null;
	private BluetoothSocket WorkHorseSocket = null;
	public boolean isSocketAlive(){
		if(s_socket != null && s_socket.isConnected())
			return true;
		else if(c_socket != null && c_socket.isConnected())
			return true;
		else return false;
	}
/*** Handlers */
	private Handler handler = new Handler();
	private Runnable runStartServer = new Runnable(){
		public void run(){
			funcBTsetup();
		}
	};
	private void funcBTsetup(){
		if(mServerSocketThread != null){/*Start the server component*/
			mServerSocketThread.cancel();
			mServerSocketThread = null;
		}	
		mServerSocketThread= new AcceptThread();
		mServerSocketThread.start();
	
	}//end of funcBTsetup
	private Runnable ShowPopup = new Runnable(){
		public void run(){
			openOptionsMenu();
		}
	};

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {

	    // Always call the superclass so it can save the view hierarchy state
	    super.onSaveInstanceState(savedInstanceState);
	}
	@Override
	protected void onRestoreInstanceState(Bundle savedForOrienChng) {
	    super.onRestoreInstanceState(savedForOrienChng); // Always call the superclass first
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_radio);
	    ticView = (ViewProto)findViewById(R.id.viewProto);
	    ticView.setGameObj(-1);//set Game obj with null WhoseTurn
	    
	    TextView myTextTitle = (TextView)findViewById(R.id.bannerTitle);
	    Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/planetbe.ttf");
	    myTextTitle.setTypeface(myTypeface);
		imgViewTurn = (ImageView)findViewById(R.id.imageTurn);
		txtThisDevScore = (TextView)findViewById(R.id.this_dev_score);
		txtRemDevScore = (TextView)findViewById(R.id.that_dev_score);
		
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mThisDeviceName = mBluetoothAdapter.getName();
		txtThisDevName = (TextView)findViewById(R.id.this_dev_cap);
		txtThisDevName.setText(mThisDeviceName);
		txtRemDevName = (TextView)findViewById(R.id.that_dev_cap);
		
		mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		mIntentFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
		mIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
		mIntentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
		mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		mIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

	 String btAddr = mBluetoothAdapter.getAddress();	         
	 ThisDevNickname += btAddr.substring(btAddr.length() - 5);
	   
	/*** BT is on, start the setup */
	handler.postDelayed(runStartServer, 300);
	Intent intent = new Intent(this, BTBaseDialog.class);	  
	int resId = R.layout.host_join;
	intent.putExtra(Constants.EXTRA_INT, resId);
	this.startActivityForResult(intent, Constants.RQC_GAME_HOST_JOIN);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.help:

	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	
	public void onHelp(View view){

	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent_data){
		switch(requestCode){
		case MY_REQUEST_CODE_SOUND: 
			if(resultCode == RESULT_OK){
			}
			break;
		case Constants.RQC_BT_GAME_END:
			if(resultCode == RESULT_CANCELED){//ignore
			}
			if(resultCode == RESULT_OK){
				this.finish();
			}
			break;
		case Constants.RQC_GAME_HOST_JOIN: /*** Host Game - Join Game*/
			if(resultCode == RESULT_OK){//"Join Game" branches off from here
				Intent intent = new Intent(this, BTBaseDialog.class);	  
				int resId = R.layout.guest_search;
				intent.putExtra(Constants.EXTRA_INT, resId);
				this.startActivityForResult(intent, Constants.RQC_JOIN_SEARCH);
			}
			else if(resultCode == RESULT_FIRST_USER){//"Host Game" branches off from here
				Intent intent = new Intent(this, BTBaseDialog.class);	  
				int resId = R.layout.host_show_me;
				intent.putExtra(Constants.EXTRA_INT, resId);
				this.startActivityForResult(intent, Constants.RQC_HOST_SHOWME);	
			}
			else if(resultCode == RESULT_CANCELED){
				this.finish();
			}
			else if(resultCode == RESULT_SECOND_USER){
				mReceiver = new BTBCastReceiver(this);
				registerReceiver(mReceiver, mIntentFilter);
				mRemoteDeviceName = intent_data.getExtras().getString(Constants.EXTRA_OPPONENT_DEV_NAME);
				txtRemDevName.setText(mRemoteDeviceName);
				CustomToast("Ready for your first move.");	
			}
			break;
		case Constants.RQC_HOST_SHOWME: /*** Host Game - Show Me*/
			if(resultCode == RESULT_OK){//Make "device visible" request
				if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
		            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 90);
		            startActivityForResult(discoverableIntent, Constants.RQC_HOST_VISIBLE);
		        }
				else{ // It's visible already - move on to the next dialog
					Intent intent = new Intent(this, HostWaitinDialog.class);	  
					this.startActivityForResult(intent, Constants.RQC_HOST_WAITIN);		
				}
			}
			else if(resultCode == RESULT_FIRST_USER){//skip to the next dialog
				Intent intent = new Intent(this, HostWaitinDialog.class);	  
				this.startActivityForResult(intent, Constants.RQC_HOST_WAITIN);	
			}
			else if(resultCode == RESULT_CANCELED){//Back to "join-Host" screen
				Intent intent = new Intent(this, BTBaseDialog.class);	  
				int resId = R.layout.host_join;
				intent.putExtra(Constants.EXTRA_INT, resId);
				this.startActivityForResult(intent, Constants.RQC_GAME_HOST_JOIN);			
			}
			else if(resultCode == RESULT_SECOND_USER){
				mReceiver = new BTBCastReceiver(this);
				registerReceiver(mReceiver, mIntentFilter);
				mRemoteDeviceName = intent_data.getExtras().getString(Constants.EXTRA_OPPONENT_DEV_NAME);
				txtRemDevName.setText(mRemoteDeviceName);
				CustomToast("Ready for your first move.");	
			}
			break;
		case Constants.RQC_HOST_VISIBLE:  /*** Request Visible*/
			if(resultCode == 90){//user OK's 90 sec - move on to "host waiting"
				Intent intent = new Intent(this, HostWaitinDialog.class);	  
				this.startActivityForResult(intent, Constants.RQC_HOST_WAITIN);	
			}
			if(resultCode == RESULT_CANCELED){//user declines to make it visible - back to "ShowMe"
				Intent intent = new Intent(this, BTBaseDialog.class);	  
				int resId = R.layout.host_show_me;
				intent.putExtra(Constants.EXTRA_INT, resId);
				this.startActivityForResult(intent, Constants.RQC_HOST_SHOWME);	
			}
			break;
		case Constants.RQC_HOST_WAITIN: /*** Host Game - Waitin*/
			if(resultCode == RESULT_OK){
				mRemoteDeviceName = intent_data.getExtras().getString(Constants.EXTRA_OPPONENT_DEV_NAME);
				txtRemDevName.setText(mRemoteDeviceName);
				CustomToast("Your opponent is to go first");
			}/*** LET THE GAME BEGIN! for the host*/
			
			else if(resultCode == RESULT_CANCELED){//Back to the "join-Host" screen
				Intent intent = new Intent(this, BTBaseDialog.class);	  
				int resId = R.layout.host_join;
				intent.putExtra(Constants.EXTRA_INT, resId);
				this.startActivityForResult(intent, Constants.RQC_GAME_HOST_JOIN);	
			}
			break;
			 /*** Join Game - Search Or select from the paired*/
		case Constants.RQC_JOIN_SEARCH:
			if(resultCode == RESULT_OK){//on to the "Join Game" Found dialog
				Intent intent = new Intent(this, JoinFoundDialog.class);	  
				this.startActivityForResult(intent, Constants.RQC_JOIN_FOUND);
			}
			else if(resultCode == RESULT_FIRST_USER){//Put up "Connecting" dialog and try to connect
				Intent intent = new Intent(this, JoinConningDialog.class);	  
				this.startActivityForResult(intent, Constants.RQC_JOIN_CONNECTING);
				String RemoteDeviceAddr = intent_data.getExtras().getString(EXTRA_OPPONENT_DEVICE_ADDRESS);
				// Get the device MAC address, which is the last 17 chars in the View
				String opponent_device_mac = RemoteDeviceAddr.substring(RemoteDeviceAddr.length() - 17);
				BluetoothDevice BToppnt_device = mBluetoothAdapter.getRemoteDevice(opponent_device_mac);
				if(mClientSocketThread != null){
					mClientSocketThread.cancel();
					mClientSocketThread = null;
				}
					mClientSocketThread = new ConnectThread(BToppnt_device);
					mClientSocketThread.start();
				//whoever initiates connection gets to move first; the piece is CROSS
					ticView.SetGoFirst(true, Game.CROSS);
			}/*** PAIRED CLIENT CONNECTION HERE ABOVE*/
			else if(resultCode == RESULT_CANCELED){//Back to "join-Host" screen
				Intent intent = new Intent(this, BTBaseDialog.class);	  
				int resId = R.layout.host_join;
				intent.putExtra(Constants.EXTRA_INT, resId);
				this.startActivityForResult(intent, Constants.RQC_GAME_HOST_JOIN);	
			}
			break;
			 /*** Join Game - Found new devices*/
		case Constants.RQC_JOIN_FOUND:
			if(resultCode == RESULT_CANCELED){//Back to "join-Host" screen
				Intent intent = new Intent(this, BTBaseDialog.class);	  
				int resId = R.layout.guest_search;
				intent.putExtra(Constants.EXTRA_INT, resId);
				this.startActivityForResult(intent, Constants.RQC_JOIN_SEARCH);	
			}
			if(resultCode == RESULT_OK){//same as Join Search item click
				Intent intent = new Intent(this, JoinConningDialog.class);	  
				this.startActivityForResult(intent, Constants.RQC_JOIN_CONNECTING);
				String RemoteDeviceAddr = intent_data.getExtras().getString(EXTRA_OPPONENT_DEVICE_ADDRESS);
				// Get the device MAC address, which is the last 17 chars in the View
				String opponent_device_mac = RemoteDeviceAddr.substring(RemoteDeviceAddr.length() - 17);
				BluetoothDevice BToppnt_device = mBluetoothAdapter.getRemoteDevice(opponent_device_mac);
				// device may or may not be paired - make sure it is before trying to connect
				if(BToppnt_device.getBondState() != BluetoothDevice.BOND_BONDED){
					
				}
				if(mClientSocketThread != null){
					mClientSocketThread.cancel();
					mClientSocketThread = null;
				}
					mClientSocketThread = new ConnectThread(BToppnt_device);
					mClientSocketThread.start();
				//whoever initiates the connection gets to move first; the piece is CROSS
					ticView.SetGoFirst(true, Game.CROSS);
			}
			break;
		case Constants.RQC_JOIN_CONNECTING:
			if(resultCode == RESULT_CANCELED){//Back to "join-Search" dialog
				Intent intent = new Intent(this, BTBaseDialog.class);	  
				int resId = R.layout.host_join;
				intent.putExtra(Constants.EXTRA_INT, resId);
				this.startActivityForResult(intent, Constants.RQC_GAME_HOST_JOIN);						
			}
		
			if(resultCode == RESULT_OK){//Setup is finished;
				mReceiver = new BTBCastReceiver(this);
				registerReceiver(mReceiver, mIntentFilter);
				mRemoteDeviceName = intent_data.getExtras().getString(Constants.EXTRA_OPPONENT_DEV_NAME);
				txtRemDevName.setText(mRemoteDeviceName);
				CustomToast("Ready for your first move.");	
			}/*** LET THE GAME BEGIN! for the guest*/
			break;
		case MY_REQUEST_CODE_BTSETUP:
			if(resultCode == RESULT_CANCELED){/*** BTsetup dialog dismissed*/
				ticView.SetGoFirst(false, Game.NULL);
			}
			if(resultCode == RESULT_OK){/***Initiate a BT connection if it doesn't exist already*/
				if(s_socket != null){//already accepted a connection - ignore
					ticView.SetGoFirst(false, Game.NULL);//
					break;
				}
				String RemoteDeviceAddr = intent_data.getExtras().getString(EXTRA_OPPONENT_DEVICE_ADDRESS);
				// Get the device MAC address, which is the last 17 chars in the View
				String opponent_device_mac = RemoteDeviceAddr.substring(RemoteDeviceAddr.length() - 17);
				BluetoothDevice BToppnt_device = mBluetoothAdapter.getRemoteDevice(opponent_device_mac);
				if(mClientSocketThread != null){
					mClientSocketThread.cancel();
					mClientSocketThread = null;
				}
					mClientSocketThread = new ConnectThread(BToppnt_device);
					mClientSocketThread.start();
					
					//whoever initiates the connection gets to move first; the piece is CROSS
					ticView.SetGoFirst(true, Game.CROSS);
			}
			break;
		case MY_REQUEST_CODE_ENABLE_BT:
			if(resultCode == RESULT_OK){

			}
			break;
		}
	
	}
	public void CustomToast(String text){
		LayoutInflater inflater = getLayoutInflater();
    	View layout = inflater.inflate(R.layout.ready_play,
              (ViewGroup) findViewById(R.id.toast_layout_root));
    	TextView vThat = (TextView)layout.findViewById(R.id.join_game_move);
    	vThat.setText(text);
    	Toast toast = new Toast(getApplicationContext());
    	toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
    	toast.setDuration(Toast.LENGTH_SHORT);
    	toast.setView(layout);
    	toast.show();	
	}
	
	  @Override
	    protected void onDestroy() {
	     //   mAdView.destroy();
		  	if(mServerSocketThread != null){
		  		mServerSocketThread.cancel();
		  		mServerSocketThread = null;
		  	}
		  	
		  	if(mClientSocketThread != null){
		  		mClientSocketThread.cancel();
		  		mClientSocketThread = null;
		  	}
	
		  	if(mReceiver != null)
		  		unregisterReceiver(mReceiver);
	        super.onDestroy();
	    }
	  @Override
	    protected void onPause() {
		  super.onPause();
		 if(mConnedThread != null){
		  		mConnedThread.cancel();
		 		mConnedThread = null;
		  	}
	       
	    }

	    @Override
	    protected void onResume() {
	        super.onResume();
	    }
	    /*** This thread runs while listening for incoming connections. It behaves
	     * like a server-side client. It runs until a connection is accepted
	     * (or until cancelled).
	     */
	    private class AcceptThread extends Thread {
	        // The local server socket
	        private final BluetoothServerSocket mmServerSocket;
	        
	        public AcceptThread() {
	            BluetoothServerSocket tmp = null;	           
	            // Create a new listening server socket
	            try {
	                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME_GAME, MY_UUID_TIC);
	            } catch (IOException e) {}
	            	mmServerSocket = tmp;
	        }

	        public void run(){
	        	setPriority(NORM_PRIORITY);

	            while(true)
	            {
	            	runOnUiThread(new Runnable() {
     	                @Override
     	                    public void run() {

     	                }
	            	 });
	                try {
	                    // This is a blocking call and will only return on a
	                    // successful connection or an exception
	                	s_socket = mmServerSocket.accept();
	                }
	                catch (IOException e){           
	                    break;
	                }

	                // If a connection was accepted
	       // manage the connection in a separate thread; close mmServerSocket
	                if (s_socket != null){
	                	 runOnUiThread(new Runnable() {
	     	                @Override
	     	                    public void run() {
	     	                	ticView.bIGoFirst = false;
	     	                	ticView.mGameEngine.SetMyPiece(Game.NULL);
	     	                	ticView.SetLockScreen(true);
	     	                	}
	     	                });
	                	 if(mConnedThread == null){
	       		    	  mConnedThread  = new ConnectedThread(s_socket);
	       		    	  mConnedThread.start();
	       		    	  this.cancel();
	                	 }
	                	 try{
	                		 mmServerSocket.close();
	                	 }
	                	 catch(IOException e){}
	                	 break;
	                }
	            }
	        }
	        	public void cancel() {
	        		try {
	        			mmServerSocket.close();
	        			} catch (IOException e) {}
	        		}
	       }/***END OF AcceptThread class */ 
	    /**
	     * This thread runs while attempting to make an outgoing connection
	     * with a device.
	     */	    
	    private class ConnectThread extends Thread {

	        private final BluetoothDevice mmDevice;

	        public ConnectThread(BluetoothDevice device) {
	            mmDevice = device;
	            BluetoothSocket tmp = null;
	            // Get a BluetoothSocket for a connection with the
	            // given BluetoothDevice
	            try {
	                   tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID_TIC);
	                    }
	            catch (IOException e) {}
	            c_socket = tmp;
	        }

	        public void run() {      
	            // Always cancel discovery because it will slow down a connection
	            mBluetoothAdapter.cancelDiscovery();

	            // Make a connection to the BluetoothSocket
	            try {
	                // This is a blocking call and will only return on a
	                // successful connection or an exception
	            	c_socket.connect();
	            } catch (IOException e) {
	                // Unable to connect; close the socket
	            	runOnUiThread(new Runnable() {
		                @Override
		                    public void run() {
		                	
		                	CustomToast("Failed to connect");
		                    }
		             });
	                try {
	                	c_socket.close();
	                } catch (IOException e2) {
	                }
	                return;//Failed, closed, returned
	            }
	       //Success; start the connection management thread
	       //set the piece and first move
	            runOnUiThread(new Runnable() {
	                @Override
	                    public void run() {
	                	ticView.bIGoFirst = true;
	                	ticView.mGameEngine.SetMyPiece(Game.CROSS);
	                	ticView.SetLockScreen(false);
	                	}
	                });
	            if(mConnedThread == null){
     		    	  mConnedThread  = new ConnectedThread(c_socket);
     		    	  mConnedThread.start();
     		       }
	
	        }

	        public void cancel() {
	            try {
	            	c_socket.close();
	            } catch (IOException e) {}
	        }
	    }/*** END of ConnectThread*/
	    /**
	     * This thread runs during a connection with a remote device.
	     * It handles all incoming and outgoing transmissions.
	     */
	    private class ConnectedThread extends Thread {
	        
	        private InputStream InStream = null;
	        private OutputStream OutStream = null;

	        public  ConnectedThread(BluetoothSocket socket) {
	            
	            WorkHorseSocket = socket;
	            InputStream tmpIn = null;
	            OutputStream tmpOut = null;

	            // Get the BluetoothSocket input and output streams
	            try {
	                tmpIn = socket.getInputStream();
	                tmpOut = socket.getOutputStream();
	            } catch (IOException e){
	            	//DebugToast("Failed to get streams");
	            }

	            InStream = tmpIn;
	            OutStream = tmpOut;
	        }

	        public void run() {
	            Log.i(TAG, "BEGIN mConnectedThread");
	            byte[] buffer = new byte[MSG_BUFFER_SIZE];
	            int bytes;
	            ticView.bConnected = true;
	            while (true) {
	                try {
	                    // Read from the InputStream
	                    bytes = InStream.read(buffer);
	                    String str = new String(buffer);
	                    ticView.mReceiveHandler.obtainMessage(ViewProto.MESSAGE_RECEIVE_MOVE,
	                    		-1, bytes, str)
	                    	.sendToTarget();	            
	                } catch (IOException e) {
	                	this.cancel();
	                	runOnUiThread(new Runnable() {
			                @Override
			                    public void run() {
			                	String str_toast = "Disconnected from  ";
			                	str_toast = str_toast += mRemoteDeviceName;
			                	CustomToast(str_toast);
			                	}
			                });
	                		 finish();               	
	                    break;
	                }
	            }
	        }

	        /**
	         * Write to the connected OutStream.
	         */
	         void write(byte[] buffer) {
	            try {
	                OutStream.write(buffer);

	            } catch (IOException e) {
	            	DebugToast("Exception during write");
	            }
	        }

	        public void cancel() {
	            try {
	            	if(InStream != null){
                		InStream.close();
                		InStream = null;
	            	}
	            	if(OutStream != null){
	            		OutStream.close();
	            		OutStream = null;
	            	}
	            	if(WorkHorseSocket != null){
	            		WorkHorseSocket.close();
	            		WorkHorseSocket = null;
	            	}
	            	if(mServerSocketThread != null){
	            		mServerSocketThread.cancel();
	           		mServerSocketThread = null;
	    	  	}
	    		  	
	    		  	if(mClientSocketThread != null){
	    		  		mClientSocketThread.cancel();
	    		  		mClientSocketThread = null;
	    		  	}

	            } catch (IOException e) {}
	            
	        }
	    }/*** END of ConnectedThread	    
	  /***Broadcast receiver*/
	/*** Message Handler*/
		 final Handler mSendHandler = new Handler() {
		        @Override
		        public void handleMessage(Message msg) {
		            switch (msg.what) {
		                case MESSAGE_TOAST:
		                    	LayoutInflater inflater = getLayoutInflater();
		                    	View layout = inflater.inflate(R.layout.custom_toast,
		                              (ViewGroup) findViewById(R.id.toast_layout_root));
		                    	 TextView text = (TextView) layout.findViewById(R.id.text);
		                    	 text.setText((CharSequence) msg.obj);
		                    	    Toast toast = new Toast(getApplicationContext());
		                    	    toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
		                    	    toast.setDuration(Toast.LENGTH_SHORT);
		                    	    toast.setView(layout);
		                    	    toast.show();
		               break;
		                case MESSAGE_SEND_MOVE:
		                	String message_togo = ThisDevNickname + '*';
		                	message_togo += Integer.toString(msg.arg1) + '*';
		                	message_togo += Integer.toString(msg.arg2) + '*';
		                	message_togo += Integer.toString((Integer) msg.obj);
		                	byte[] buffer = new byte[message_togo.length()];
		                	buffer = message_togo.getBytes();
		                	write(buffer);
		               break;
		                case MSG_SEND_REQUEST_SHUTDOWN:
		                	//opponent exits the game
		                	String str_quit = mRemoteDeviceName + " quit the game.";
		                	mSendHandler.obtainMessage(MESSAGE_TOAST, str_quit)
		        			.sendToTarget();
		                	break;
		                case MSG_LOCAL_WINS_THIS_ROUND:
		                	txtThisDevScore.setText(Integer.toString((Integer) msg.obj));
		                	break;
		                case MSG_REMOTE_WINS_THIS_ROUND:
		                	txtRemDevScore.setText(Integer.toString((Integer) msg.obj));
		                	break;
		            }
		            
		        }
		    };
		    public void write(byte[] out) {
		       BluetoothSocket sock = null;
		       if(s_socket != null)
		    	   sock = s_socket;
		       else if(c_socket != null && c_socket.isConnected())
		    	   sock = c_socket;
		       else{
		    	   CustomToast("No Active Socket");
		    	   return;
		       }

		       mConnedThread.write(out); 
		    }
	private synchronized void DebugToast(final String str){
		 runOnUiThread(new Runnable() {
             @Override
                 public void run() {
             	Toast.makeText(getApplicationContext(), str,
     					Toast.LENGTH_SHORT).show(); 
                 }
          });
	}
	
}
