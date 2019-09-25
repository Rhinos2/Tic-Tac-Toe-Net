package com.onemanshow.wfsetup;

import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import com.onemanshow.wftic.WFMainActivity;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class MulticastProbe extends HandlerThread {
	static final String MCAST_ADDR = "224.0.0.251";
	static final String ACK_TIC = "AckTic";
	static final String ACK_SIGNOFF = "AckSignOff";
	static final int PORT_1 = 34021;
	static final int PORT_2 = 50383;
	static final int MSG_BUFFER_LENGTH = 64;
	
	private Handler mHandler;
	private WFMainActivity mPareActivity;
	private int PortGame;
	private int PortControl;
	private boolean isHost;
	private boolean bAckReady = false;
	private android.net.wifi.WifiManager.MulticastLock lock;
	private MulticastSocket mSocketProbe = null;
	//constructor
	public MulticastProbe(Activity activity, int port1, int port2, boolean isHostGame){
		super("ProbeHandlerThread", MIN_PRIORITY);
		PortGame = port1; PortControl = port2; isHost = isHostGame;
		mPareActivity = (WFMainActivity)activity;
	}
	 public void TearDown(){
		 if(mSocketProbe != null){
			 lock.release();
			 try{
				mSocketProbe.leaveGroup(InetAddress.getByName(MCAST_ADDR));
					}
			 catch(IOException s){}
			 	mSocketProbe.close();
			 	mSocketProbe = null;
			 }
	 }
	private void SetUpFunc() {
		// to cause the wifi stack to receive packets addressed to multicast addresses
		android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager)
		mPareActivity.getApplicationContext().getSystemService(android.content.Context.WIFI_SERVICE);
		lock = wifi.createMulticastLock("lockthereturn");
        lock.setReferenceCounted(false);
        lock.acquire();
        //Socket init
        try{   		
        	mSocketProbe = new MulticastSocket(PORT_2);
			mSocketProbe.setSoTimeout(12000);
        	mSocketProbe.setTimeToLive(200);
        	mSocketProbe.setReuseAddress(true);
        	mSocketProbe.setLoopbackMode (true);
        	mSocketProbe.joinGroup(InetAddress.getByName(MCAST_ADDR));
        }
        catch(IOException e){
        	e.printStackTrace();
        }
	}
	@Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mHandler = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                	case Constants.MSG_LISTEN:
                		byte[] ReadBuffer = new byte[MSG_BUFFER_LENGTH];
                		String InStr = null;
    					DatagramPacket AckPacket = new DatagramPacket(ReadBuffer, ReadBuffer.length);
        				try{
	        				mSocketProbe.receive(AckPacket);
	        				InStr = new String(AckPacket.getData(), "UTF-8");
	        				if(!InStr.contains("Ack")){
	        					mHandler.sendEmptyMessage(Constants.MSG_LISTEN);
	        					break;//ignore the irrelavant
	        					}
							else if(InStr.contains(ACK_TIC)){
								//Get the sender's IP and the ports it will be listening on
								InetAddress InAddr = AckPacket.getAddress();
								String[] tokens = InStr.split(Constants.delims);
								int GamePort = Integer.parseInt(tokens[1]);
								int ControlPort = Integer.parseInt(tokens[2]);
								mPareActivity.mRevdHandler.obtainMessage(Constants.MESSAGE_HOST_IP_PORT,
										GamePort, ControlPort, (Object)InAddr)
										.sendToTarget();
								if(isHost){
									mHandler.sendEmptyMessageDelayed(Constants.MSG_SEND, 300);
								}
								else{
									mHandler.sendEmptyMessageDelayed(Constants.MSG_SEND_AKN, 300);
								}

								break;
								}
							else if(InStr.contains(ACK_SIGNOFF)){
								if(isHost) {

									mHandler.sendEmptyMessageDelayed(Constants.MSG_SEND_AKN, 300);
									}
								else{
										mPareActivity.mRevdHandler.obtainMessage(Constants.MSG_DONE_MLT_PROBE)
												.sendToTarget();
										quit();
									}
								}
	        				}
	        				catch(IOException e){
	        					e.printStackTrace();
	        					//Opponent's device timed out - close down;
	        					String expt = e.toString();

	        					if(expt.contains("Timeout")){
	        						mPareActivity.mRevdHandler.obtainMessage(Constants.MSG_TIMEOUT)
	                    			.sendToTarget();
	        						break;
	        					}
	        				}

                	case Constants.MSG_SEND:
                		byte[] buffer;
                			String msg_to_serv = ACK_TIC + '*';
                	    	msg_to_serv += String.valueOf(PortGame);
                	    	msg_to_serv += '*';
                	    	msg_to_serv += String.valueOf(PortControl);
                	    	msg_to_serv += '*';
                			buffer = msg_to_serv.getBytes();
                			InetAddress inAdrress = null;
                			DatagramPacket outDtgm = null;
                			try{
                			inAdrress = InetAddress.getByName(MCAST_ADDR);
                			outDtgm = new DatagramPacket(buffer, buffer.length, inAdrress, PORT_2);
                			mSocketProbe.send(outDtgm);
                			}
                			catch(IOException e){
                				e.printStackTrace();}
                			//switch to listening
                				mHandler.sendEmptyMessage(Constants.MSG_LISTEN);

                		break;
                	case Constants.MSG_SEND_AKN:
                			byte[] buff;
                			
                			buff = ACK_SIGNOFF.getBytes();
                			InetAddress inAddrr = null;
                			DatagramPacket outDtg = null;
                			try{
                				inAddrr = InetAddress.getByName(MCAST_ADDR);
                				outDtg = new DatagramPacket(buff, buff.length, inAddrr, PORT_2);
                				mSocketProbe.send(outDtg);
                			}
            			catch(IOException e){
            				e.printStackTrace();
                			}
                		if(isHost){
							mPareActivity.mRevdHandler.obtainMessage(Constants.MSG_DONE_MLT_PROBE)
									.sendToTarget();
							quit();

						}
                		//switch back to listening if it's the client
						else {
							mHandler.sendEmptyMessage(Constants.MSG_LISTEN);
						}
                		break;
                }
            }
        };
        SetUpFunc();
        if(isHost)
        	mHandler.sendEmptyMessage(Constants.MSG_LISTEN);
        else
        	mHandler.sendEmptyMessage(Constants.MSG_SEND);
	}
}