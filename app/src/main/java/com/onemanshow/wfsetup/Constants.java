

package com.onemanshow.wfsetup;

import android.app.Activity;

public interface Constants{
    //symbolic names

    static final String EXTRA_INT = "extraint"; /***from the BT part*/
    //messages to handlers
    public static final int MESSAGE_HOST_IP_PORT = 1001;

    public static final int MESSAGE_RECEIVE_MOVE = 98; /***from the BT part*/
    public static final int MESSAGE_RECEIVED_HANDSHAKE = 100;
    public static final int MSG_LISTEN = 1005;
    public static final int MSG_SEND = 1006;
    public static final int MSG_TIMEOUT = 1007;
    public static final int MSG_TIMEOUT_CONN = 1017;
    public static final int MSG_HANDSHAKE = 1008;
    public static final int MSG_LISTEN_HANDSHAKE = 1016;
    public static final int MSG_DONE_MLT_PROBE = 1011;
    public static final int MSG_SEND_AKN = 1012;
    public static final int MSG_KILL_DNS = 1015;
    //request codes
    public static final int RQC_GAME_HOST_JOIN = 12;/***from the BT part*/
    public static final int RQC_JOIN_READY = 15;
    public static final int RQC_HOST_WAITIN = 17;/***from the BT part*/
    public static final int RQC_JOIN_SEARCH = 13; /***from the BT part*/
    public static final int RQC_HOST_SHOWME = 16;
    public static final int RQC_BT_GAME_END = 20;
    public static final String EXTRA_OPPONENT_DEV_NAME = "that_device_name";
    public static final int RQC_JOIN_CONNECTING =19;
    //Other constants
    static final String CONFIG_CHNG = "ConfigChange";
    public static final String delims = "[*]";
    public static final int VISIBLE_FOR = 90000;/***from the BT part*/
    public static final int RESULT_USER_CANCELED = Activity.RESULT_FIRST_USER + 1;
    public static final int RESULT_CANCELED = 22;
}