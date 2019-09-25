package com.onemanshow.wftic;

import com.onemanshow.wfsetup.Constants;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnTaskFragTcp extends Fragment{
    static final int MSG_BUFFER_LENGTH = 64;

 //   private final Object Lock = new Object();
    private WFMainActivity mPareActivity;
    private ConnectionThread mDataThrd;
    private Handler mHandlerConn;
    private InetAddress mInetAddrRemote;
    private InetAddress mInetAddrLocal;
    private  int mPortLocaGame;
    private  int mPortRemGame;

    private boolean isServerSide;

    public void Initialize(InetAddress InetAddrRem, int portRemGame, int portRemCtrl,
                           InetAddress InetAddrLol, int portLolGame, int portLolCtrl, boolean isHost){

        isServerSide = isHost;
        mInetAddrRemote = InetAddrRem;
        mInetAddrLocal = InetAddrLol;
        mPortRemGame = portRemGame;
        mPortLocaGame = portLolGame;
        mDataThrd = new ConnectionThread();
    }
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        synchronized(this){
            mPareActivity = (WFMainActivity)activity;
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); //onDestroy() will NOT be called
        mDataThrd.start();
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mPareActivity = null;
    }
    @Override
    public void onDestroy(){
        mDataThrd.TearDown();
        mDataThrd.quit();
        super.onDestroy();
    }

    private class ConnectionThread extends HandlerThread {

        private ServerSocket mSocketSrv = null;
        private ServerSocket mSocketSrv2 = null;
        private Socket mSocket = null;
        private Socket mSocket2 = null;

        //constructor
        ConnectionThread(){
            super("ConnectionThrd", MIN_PRIORITY);
        }
        private void TearDown(){
            if(mSocketSrv != null){
                try {mSocketSrv.close();}
                catch(IOException e) {
                    e.printStackTrace();
                }
                mSocketSrv = null;
            }
            if(mSocket != null){
                try {mSocket.close();}
                catch(IOException e) {
                    e.printStackTrace();
                }
                mSocket = null;
            }
            if(mSocketSrv2 != null){
                try {mSocketSrv2.close();}
                catch(IOException e) {
                    e.printStackTrace();
                }
                mSocketSrv2 = null;
            }
            if(mSocket2 != null){
                try {mSocket2.close();}
                catch(IOException e) {
                    e.printStackTrace();
                }
                mSocket2 = null;
            }
        }
        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            mHandlerConn = new Handler(getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch(msg.what) {
                        case Constants.MSG_LISTEN_HANDSHAKE:
                            byte[] byteBuffer = new byte[MSG_BUFFER_LENGTH];
                            String strData1;
                            try{
                                mSocketSrv = new ServerSocket(mPortLocaGame);
                                mSocketSrv.setSoTimeout(10000);
                                mSocketSrv.setReuseAddress(true);

                                Socket clntSock = mSocketSrv.accept();
                                InputStream in = clntSock.getInputStream();
                                OutputStream out = clntSock.getOutputStream();
                                int num = in.read(byteBuffer);
                                strData1 = new String(byteBuffer);
                                clntSock.close () ;
                                mPareActivity.mRevdHandler.obtainMessage(Constants.MESSAGE_RECEIVED_HANDSHAKE,
                                        -1, strData1.length(), strData1)
                                        .sendToTarget();
                            }
                            catch(IOException e) {
                                e.printStackTrace();
                                String expt = e.toString();
                                if(expt.contains("Timeout")){
                                    mPareActivity.mRevdHandler.obtainMessage(Constants.MSG_TIMEOUT_CONN)
                                            .sendToTarget();
                                }
                                break;
                            }

                            if(isServerSide)
                                mHandlerConn.sendEmptyMessageDelayed(Constants.MSG_HANDSHAKE, 100);
                            break;

                        case Constants.MSG_LISTEN:
                            byte[] ReadBuffer = new byte[MSG_BUFFER_LENGTH];
                            String strData;
                            try{
                                if(mSocketSrv != null) {
                                    mSocketSrv.close();
                                    mSocketSrv = null;
                                }
                                if(mSocketSrv2 == null) {
                                    mSocketSrv2 = new ServerSocket(mPortLocaGame);
                                    mSocketSrv2.setSoTimeout(10000);
                                    mSocketSrv2.setReuseAddress(true);
                                }
                                Socket clntSock2 = mSocketSrv2.accept();
                                InputStream in = clntSock2.getInputStream();
                                OutputStream out = clntSock2.getOutputStream();
                                int num = in.read(ReadBuffer);
                                strData = new String(ReadBuffer);
                                strData = strData.substring(0, 15);
                                clntSock2.close () ;
                                mSocketSrv2.close();
                                mSocketSrv2 = null;
                                mPareActivity.mRevdHandler.obtainMessage(Constants.MESSAGE_RECEIVE_MOVE,
                                        -1, strData.length(), strData)
                                        .sendToTarget();
                            }
                            catch(IOException e) {
                                e.printStackTrace();
                                String expt = e.toString();
                                if(expt.contains("Timeout")){
                                    mPareActivity.mRevdHandler.obtainMessage(Constants.MSG_TIMEOUT_CONN)
                                            .sendToTarget();
                                }
                                break;
                            }

                            break;
                        case Constants.MSG_SEND:
                            try {
                                if(mSocket != null) {
                                    mSocket.close();
                                    mSocket = null;
                                }
                                if(mSocket2 == null) {
                                    mSocket2 = new Socket(mInetAddrRemote, mPortRemGame);
                                    mSocket2.setSoTimeout(12000);
                                    mSocket2.setTcpNoDelay(true);
                                }
                                byte[] buffer_send = ((String) msg.obj).getBytes();
                                InputStream in = mSocket2.getInputStream();
                                OutputStream out = mSocket2.getOutputStream();
                                out.write(buffer_send);
                                mSocket2.close();
                                mSocket2 = null;
                            }
                            catch(IOException e){
                                e.printStackTrace();
                                    mPareActivity.mRevdHandler.obtainMessage(Constants.MSG_TIMEOUT_CONN)
                                            .sendToTarget();
                                break;
                            }
                            mHandlerConn.sendEmptyMessage(Constants.MSG_LISTEN);
                            break;
                        case Constants.MSG_HANDSHAKE:
                            try{
                                mSocket = new Socket(mInetAddrRemote, mPortRemGame);
                                mSocket.setSoTimeout(12000);
                                mSocket.setTcpNoDelay(true);
                                InputStream in = mSocket.getInputStream();
                                OutputStream out = mSocket.getOutputStream();
                                byte[] buffer = mPareActivity.mThisDeviceName.getBytes();
                                out.write(buffer);

                            }
                            catch(IOException e){
                                e.printStackTrace();
                                String expt = e.toString();
                                if(expt.contains("Timeout")) {
                                    mPareActivity.mRevdHandler.obtainMessage(Constants.MSG_TIMEOUT_CONN)
                                            .sendToTarget();
                                }
                                break;
                            }
                            //switch to listening for handshake IF it's the client
                            if(isServerSide)
                                mHandlerConn.sendEmptyMessage(Constants.MSG_LISTEN);
                            else
                                mHandlerConn.sendEmptyMessage(Constants.MSG_LISTEN_HANDSHAKE);
                            break;
                    }
                }
            };

            if(isServerSide)
                mHandlerConn.sendEmptyMessage(Constants.MSG_LISTEN_HANDSHAKE);
            else
                mHandlerConn.sendEmptyMessageDelayed(Constants.MSG_HANDSHAKE, 2000);


        }
    }

    /*** Public interface methods*/
    public void ReadFrom(){

    }
    public void Write(String strData){
        Message msgDataOut =  mHandlerConn.obtainMessage(Constants.MSG_SEND, strData);
        mHandlerConn.sendMessage(msgDataOut);
    }

}
