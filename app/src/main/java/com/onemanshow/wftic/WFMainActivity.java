package com.onemanshow.wftic;



import com.onemanshow.wfsetup.Constants;
import com.onemanshow.bttic.Game;
import com.onemanshow.wfsetup.SetupBaseDialog;
import com.onemanshow.tictactoe.R;
import com.onemanshow.wfsetup.MulticastProbe;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
public class WFMainActivity extends Activity{

    public static final int MY_REQUEST_CODE_SOUND = 52;
    public static final int MY_REQUEST_CODE_BTSETUP = 51;
    public static final int MY_REQUEST_CODE_ENABLE_BT = 50;
    public static final String EXTRA_OPPONENT_DEVICE_ADDRESS = "device_address";
    public static final int 	MESSAGE_SEND_MOVE = 99;
    public static final int		MESSAGE_TOAST = 97;
    public static final int 	MSG_SEND_REQUEST_SHUTDOWN = 95;
    public static final int     MSG_LOCAL_WINS_THIS_ROUND = 89;
    public static final int     MSG_REMOTE_WINS_THIS_ROUND = 88;
    public static final int 	RESULT_SECOND_USER = 2;

    private ImageView imgViewTurn;
    public void ToggleImgTurn(boolean bLock){
        if(!bLock)
            imgViewTurn.setImageResource(R.drawable.go);
        else
            imgViewTurn.setImageResource(R.drawable.no_enter);
    }

    private TextView txtThisDevScore;
    private TextView txtRemDevScore;

    private ViewProtoWF ticView;
    private TextView txtThisDevName;
    private TextView txtRemDevName;
    private String ThisDevNickname = "DE:";
    String mThisDeviceName = "";
    private String mRemoteDeviceName = "";

    public String getThisDevName(){ return mThisDeviceName; }
    public String getRemoteDevName(){ return mRemoteDeviceName; }

    private com.onemanshow.wftic.WFMainActivity mContext;

    private BroadcastReceiver mReceiver = null;
    private final IntentFilter mIntentFilter = new IntentFilter();

    private BluetoothAdapter mBluetoothAdapter = null;

    private BluetoothSocket s_socket = null;

    private boolean bServer = true;
    private boolean bAbort = false;
    private MulticastProbe m_MltProbe = null;
    private ConnTaskFragTcp mConnThreadFrag;
    private ArrayList<InetAddress> InetAddrList_Local = new ArrayList<InetAddress>();
    private ArrayList<InetAddress> InetAddrList_Remote = new ArrayList<InetAddress>();

    private int[] mPortLocal = new int[2];
    private int[] mPortRmt = new int[2];

    private Runnable ShowPopup = new Runnable(){
        public void run(){
            openOptionsMenu();
        }
    };

    private void TearDownMlt(){
        if(m_MltProbe != null){
            m_MltProbe.quit();
            m_MltProbe.TearDown();
            m_MltProbe = null;
        }
    }
    public int[] findFreePort() {
        ServerSocket socket = null;
        ServerSocket socket1 = null;
        int[] ret = new int[2];
        ret[0] = ret[1] = -1;
        try{
            socket = new ServerSocket(0);
            socket1 = new ServerSocket(0);
            ret[0] = socket.getLocalPort();
            ret[1] = socket1.getLocalPort();
                socket.close();
                socket1.close();
        }
        catch (IOException e) {e.printStackTrace(); }
        return ret;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putBoolean(Constants.CONFIG_CHNG, true);
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
        setContentView(R.layout.activity_wifi);
        boolean bConfigChange = false;
        if (savedInstanceState != null) {
            bConfigChange = savedInstanceState.getBoolean(Constants.CONFIG_CHNG);
        }
        ticView = (ViewProtoWF)findViewById(R.id.viewProtoWF);
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

        String btAddr = mBluetoothAdapter.getAddress();
        ThisDevNickname += btAddr.substring(btAddr.length() - 5);
        mPortLocal = findFreePort();
        mContext = this;

        if(bConfigChange){
            FragmentManager fm = getFragmentManager();
            mConnThreadFrag = (ConnTaskFragTcp)fm.findFragmentByTag("threadfragment");
            return;
        }
        Intent intent = new Intent(this, SetupBaseDialog.class);
        int resId = R.layout.host_join;
        intent.putExtra(Constants.EXTRA_INT, resId);
        this.startActivityForResult(intent, Constants.RQC_GAME_HOST_JOIN);


    }/*** onCreate() END */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
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
                if(resultCode == RESULT_OK){    //"Join Game" branches off from here
                    bServer = false;
                    m_MltProbe = new MulticastProbe(this, mPortLocal[0], mPortLocal[1],  false);
                    m_MltProbe.start();
                    Intent intent = new Intent(this, SetupBaseDialog.class);
                    int resId = R.layout.guest_connecting;
                    intent.putExtra(Constants.EXTRA_INT, resId);
                    this.startActivityForResult(intent, Constants.RQC_JOIN_CONNECTING);

                }
                else if(resultCode == RESULT_FIRST_USER){  //"Host Game" branches off from here
                     bServer = true;
                     m_MltProbe = new MulticastProbe(this, mPortLocal[0], mPortLocal[1], true);
                     m_MltProbe.start();
                     Intent intent = new Intent(this, SetupBaseDialog.class);
                     int resId = R.layout.host_waitin;
                     intent.putExtra(Constants.EXTRA_INT, resId);
                     this.startActivityForResult(intent, Constants.RQC_HOST_SHOWME);
                }
                else if(resultCode == RESULT_CANCELED){
                    this.finish();
                }
                else if(resultCode == RESULT_SECOND_USER){
                    mRemoteDeviceName = intent_data.getExtras().getString(Constants.EXTRA_OPPONENT_DEV_NAME);
                    txtRemDevName.setText(mRemoteDeviceName);
                    CustomToast("Ready for your first move.");
                }
                break;
            case Constants.RQC_HOST_SHOWME: /*** Host Game - Show Me*/
                if(resultCode == RESULT_CANCELED){
                    if(bAbort)//Mlt Socket timed out
                        break;
                    TearDownMlt();
                    InetAddrList_Local.add(getIpAddress());
                    FragmentManager fm = getFragmentManager();
                    mConnThreadFrag = (ConnTaskFragTcp)fm.findFragmentByTag("threadfragment");

                    if(mConnThreadFrag == null){
                        FragmentTransaction transon = fm.beginTransaction();
                        mConnThreadFrag = new ConnTaskFragTcp();
                        mConnThreadFrag.Initialize(InetAddrList_Remote.get(0),
                             mPortRmt[0], mPortRmt[1], InetAddrList_Local.get(0),
                                mPortLocal[0], mPortLocal[1], bServer);

                        transon.add(mConnThreadFrag, "threadfragment");
                        transon.commit();

                    }
                }
                else if(resultCode == Constants.RESULT_USER_CANCELED){//Return to the Main menu
                    this.finish();
                }
                 if(resultCode == 22){     //Back to "join-Host" screen after Cancel
                    TearDownMlt();
                    Intent intent = new Intent(this, SetupBaseDialog.class);
                    int resId = R.layout.host_join;
                    intent.putExtra(Constants.EXTRA_INT, resId);
                    this.startActivityForResult(intent, Constants.RQC_GAME_HOST_JOIN);
                }
                else if(resultCode == RESULT_SECOND_USER){
                    mRemoteDeviceName = intent_data.getExtras().getString(Constants.EXTRA_OPPONENT_DEV_NAME);
                    txtRemDevName.setText(mRemoteDeviceName);
                    CustomToast("Ready for your first move.");
                }
                break;

   /////////////////////////////////////////////////////////////////////
            case Constants.RQC_HOST_WAITIN: /*** Host Game - Waitin*/
                if(resultCode == RESULT_OK){
                    mRemoteDeviceName = intent_data.getExtras().getString(Constants.EXTRA_OPPONENT_DEV_NAME);
                    txtRemDevName.setText(mRemoteDeviceName);
                    CustomToast("Your opponent is to go first");
                }/*** LET THE GAME BEGIN! (the host side)*/

                else if(resultCode == RESULT_CANCELED){//Back to "join-Host" screen
                    Intent intent = new Intent(this, SetupBaseDialog.class);
                    int resId = R.layout.host_join;
                    intent.putExtra(Constants.EXTRA_INT, resId);
                    this.startActivityForResult(intent, Constants.RQC_GAME_HOST_JOIN);
                }
                break;
////////////////////////////////////////////////////////////////////////////////////////////
            case Constants.RQC_JOIN_SEARCH:
                if(resultCode == RESULT_OK){

                }
                else if(resultCode == RESULT_FIRST_USER){//Put up the "Connecting" dialog and try to connect
                    ticView.SetGoFirst(true, Game.CROSS);
                }
                else if(resultCode == RESULT_CANCELED){//Back to "join-Host" screen
                    Intent intent = new Intent(this, SetupBaseDialog.class);
                    int resId = R.layout.host_join;
                    intent.putExtra(Constants.EXTRA_INT, resId);
                    this.startActivityForResult(intent, Constants.RQC_GAME_HOST_JOIN);
                }
                break;

          ///////////////////////////////////////////////////////////////////////////////////////////////
            case Constants.RQC_JOIN_CONNECTING:
                if(resultCode == 0){
                    if(bAbort)//Mlt Socket timed out
                        break;
                    TearDownMlt();
                    InetAddrList_Local.add(getIpAddress());
                    FragmentManager fm = getFragmentManager();
                    mConnThreadFrag = (ConnTaskFragTcp)fm.findFragmentByTag("threadfragment");
                    // If the Fragment is non-null, then it is currently being
                    // retained across a configuration change.
                    if(mConnThreadFrag == null){
                        FragmentTransaction transon = fm.beginTransaction();
                        mConnThreadFrag = new ConnTaskFragTcp();
                        mConnThreadFrag.Initialize(InetAddrList_Remote.get(0),
                            mPortRmt[0], mPortRmt[1], InetAddrList_Local.get(0),
                                mPortLocal[0], mPortLocal[1], bServer);

                        transon.add(mConnThreadFrag, "threadfragment");
                        transon.commit();


                    }
                }
                else if(resultCode == Constants.RESULT_USER_CANCELED){//Return to the Main menu
                    this.finish();
                }
                if(resultCode == 22){               //Back to "join-Search" dialog
                    bServer = true;//default
                    TearDownMlt();
                    Intent intent = new Intent(this, SetupBaseDialog.class);
                    int resId = R.layout.host_join;
                    intent.putExtra(Constants.EXTRA_INT, resId);
                    this.startActivityForResult(intent, Constants.RQC_GAME_HOST_JOIN);
                }

                if(resultCode == RESULT_OK){//Setup is finished
                    mRemoteDeviceName = intent_data.getExtras().getString(Constants.EXTRA_OPPONENT_DEV_NAME);
                    txtRemDevName.setText(mRemoteDeviceName);
                    CustomToast("Ready for your first move.");
                }/*** LET THE GAME BEGIN! (the guest side)*/
                break;
        /////////////////////////////////////////////////////////////////////////
            case MY_REQUEST_CODE_BTSETUP:
                if(resultCode == RESULT_CANCELED){
                    ticView.SetGoFirst(false, Game.NULL);//
                }
                if(resultCode == RESULT_OK){
                    if(s_socket != null){//already accepted a connection - ignore
                        ticView.SetGoFirst(false, Game.NULL);//
                        break;
                    }
                    String RemoteDeviceAddr = intent_data.getExtras().getString(EXTRA_OPPONENT_DEVICE_ADDRESS);
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

        super.onDestroy();
    }
    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

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
                    String str = new String(buffer);
                    mConnThreadFrag.Write(str);
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
     public final Handler mRevdHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constants.MSG_TIMEOUT_CONN:
                    ticView.bConnected = false;
                    Toast toast2 = Toast.makeText(getApplicationContext(), "Connection Socket Timed Out", Toast.LENGTH_SHORT);
                    toast2.show();
                    mContext.finish();
                    break;
                case Constants.MSG_TIMEOUT:
                    bAbort = true;
                    TearDownMlt();
                    if(bServer)
                        finishActivity(Constants.RQC_HOST_SHOWME);
                    else
                        finishActivity(Constants.RQC_JOIN_CONNECTING);
                    Toast toast = Toast.makeText(getApplicationContext(), "Socket Timed Out", Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                case Constants.MSG_DONE_MLT_PROBE:

                    if (bServer)
                        finishActivity(Constants.RQC_HOST_SHOWME);
                    else
                        finishActivity(Constants.RQC_JOIN_CONNECTING);
                    break;
                case Constants.MESSAGE_HOST_IP_PORT:
                    InetAddress inet_addresses = (InetAddress)msg.obj;
                    InetAddrList_Remote.add(inet_addresses);
                    mPortRmt[0] = msg.arg1;
                    mPortRmt[1] = msg.arg2;

                    break;
                case Constants.MESSAGE_RECEIVED_HANDSHAKE:
                    mRemoteDeviceName = (String)msg.obj;
                    txtRemDevName.setText(mRemoteDeviceName);
                    if(bServer){
                        ticView.bIGoFirst = false;
                        ticView.mGameEngine.SetMyPiece(Game.NULL);
                        ticView.SetLockScreen(true);
                        CustomToast("Your opponent is to go first");
                    }
                    else {
                        ticView.bIGoFirst = true;
                        ticView.mGameEngine.SetMyPiece(Game.CROSS);
                        ticView.SetLockScreen(false);
                        CustomToast("Ready for your first move.");
                    }
                    ticView.bConnected = true;
                    //////////////////////////////////////////////////////

                    break;
                case Constants.MESSAGE_RECEIVE_MOVE:
                    ticView.mReceiveHandler.obtainMessage(ViewProtoWF.MESSAGE_RECEIVE_MOVE,
                            -1, ((String)msg.obj).length(), (String)msg.obj)
                            .sendToTarget();
                    break;
                case Constants.MSG_KILL_DNS:
                    TearDownMlt();
                    break;

            }
        }
    };
    ///////////////////////////////////////
    private InetAddress getIpAddress() {
        InetAddress ret = null;
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                boolean mutlt = networkInterface.supportsMulticast();
                boolean loopback = networkInterface.isLoopback();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ret = inetAddress;
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ret;
    }

}