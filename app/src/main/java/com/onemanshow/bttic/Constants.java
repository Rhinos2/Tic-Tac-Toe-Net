package com.onemanshow.bttic;


public interface Constants{
	//BT communication messages
	
	//public static final int 	MESSAGE_SEND_MOVE = 99;
	//public static final int		MESSAGE_TOAST = 97;
	//public static final int 	MSG_SEND_REQUEST_SHUTDOWN = 95;
	//public static final int     MSG_LOCAL_WINS_THIS_ROUND = 89;
	//public static final int     MSG_REMOTE_WINS_THIS_ROUND = 88;
	
	//Request codes
	//public static final int MY_REQUEST_CODE_SOUND = 52;
	//public static final int MY_REQUEST_CODE_BTSETUP = 51;
	public static final int MY_REQUEST_CODE_ENABLE_BT = 50;
	public static final int RQC_GAME_HOST_JOIN = 12;
	public static final int RQC_JOIN_SEARCH = 13;
	public static final int RQC_JOIN_FOUND =14;
	public static final int RQC_JOIN_CONNECTING =19;
	public static final int RQC_JOIN_READY = 15;
	public static final int RQC_HOST_SHOWME = 16;
	public static final int RQC_HOST_WAITIN = 17;
	public static final int RQC_HOST_VISIBLE = 18;
	public static final int RQC_BT_GAME_END = 20;
	//Symbolic names
	public static final String EXTRA_INT = "extraint";
	//public static final String EXTRA_OPPONENT_DEVICE_ADDRESS = "that_device_address";
	public static final String EXTRA_OPPONENT_DEV_NAME = "that_device_name";
	//public static final String EXTRA_THIS_DEVICE_NAME = "this_device_name";
	
	//Other constants
	public static final int VISIBLE_FOR = 90000;
	public static final int 	RESULT_SECOND_USER = 2;
}