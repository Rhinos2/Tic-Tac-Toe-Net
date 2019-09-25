package com.onemanshow.wftic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.onemanshow.bttic.Game;
import com.onemanshow.tictactoe.R;

public class ViewProtoWF extends View{
	//constants
	public static final int 	GRID_LINE_WIDTH = 4;
	public static final int 	EMPTY = 0;
	public static final int 	NULL = -1;
	public static final int 	CROSS = -2;
	public static final int 	GRID_SIZE = 9; //3 X 3
	public static final int 	MARGIN = 4;
	public static final int 	DRAW = 0;
	public static final int 	WIN = 1;
	//constants
		//keys
	public static final String TABLE_ROW1 = "horiz1";
	public static final String TABLE_ROW2 = "horiz2";
	public static final String TABLE_ROW3 = "horiz3";
	public static final String TABLE_ROW4 = "vert1";
	public static final String TABLE_ROW5 = "vert2";
	public static final String TABLE_ROW6 = "vert3";
	public static final String TABLE_ROW7 = "diag1";
	public static final String TABLE_ROW8 = "diag2";
	public static final String[] tblRows = {TABLE_ROW1, TABLE_ROW2, TABLE_ROW3, TABLE_ROW4, 
											TABLE_ROW5, TABLE_ROW6, TABLE_ROW7, TABLE_ROW8 };
	public static final String MYWINS_COUNT = "mywins";
	public static final String HISWINS_COUNT = "hiswins";
	public static final String WHOSE_TURN = "turn";
	public static final String GAME_STATE = "gamestate";
	public static final String GAME_OVER_FLAG = "gameover";
	public static final String WIN_INDEX = "winindex";
	public static final String WIN_DRAW = "windraw";
	public static final String SOUND_ON = "soundon";
		// keys end
	public static final int 	MESSAGE_SEND_MOVE = 99;
	public static final int 	MESSAGE_RECEIVE_MOVE = 98;
	public static final int     MESSAGE_LOCAL_TOAST = 90;
	
	// a const copy to refer to in winStroke() func.
	private final static int[][] mGameTableOriginal = { {0,1,2}, {3,4,5}, {6,7,8}, {0,3,6},
				{1,4,7}, {2,5,8}, {0,4,8}, {2,4,6} };
	private int My_wins = 0;
	private int His_wins = 0;//Number of players wins
	
	private Paint mFillPaint;
	private Paint mLineGridPaint;
	private Paint mTouchPaint;
	private Paint mCrossPaint;
	private Paint mStrikeThroughPaint;
	
	private int mSxy;
	private int mOffetX;
	private int mOffetY;
	private final RectF mRectFBorder = new RectF();
	
	private int curMoveCount = 0;
	private int snd_readyID;
	private int snd_successID;
	private SoundPool sp = new SoundPool(3, AudioManager.STREAM_NOTIFICATION, 0);
	Game mGameEngine;
	//Flags
	private int mbGameOverFlag = 0;//Can't use bool; ZERO is false; ONE is true;
	private int nWinDraw = -1;
	private boolean bScreenLock = true; //initially the screen is locked
	boolean bIGoFirst = false;
	boolean bConnected = false;
	///////////////////////////////////////////////////////
	private class Cell{
		int cellIndex;			// 1 to 9
		Rect cellRect;
		RectF cellDrawRect;
		int cellState; 			// EMPTY, FILLED
		//Constructor
		Cell (int index, Rect rt, int state){
			cellIndex = index;
			cellRect = new Rect(rt);
			cellState = state;
			cellDrawRect = new RectF();
			cellDrawRect.set(rt.left + GRID_LINE_WIDTH * 4, rt.top + GRID_LINE_WIDTH * 4,
					rt.right - GRID_LINE_WIDTH * 4, rt.bottom - GRID_LINE_WIDTH * 4);
		}
		 void Render(Canvas canvas){
			 if(cellState == EMPTY ){

	        	}
	        	else if(cellState == NULL){
	        		 canvas.drawOval(cellDrawRect, mTouchPaint);
	        	}
	        	else if(cellState == CROSS){
	        		canvas.drawLine(cellDrawRect.left,cellDrawRect.top, cellDrawRect.right, cellDrawRect.bottom, mCrossPaint);
	                canvas.drawLine(cellDrawRect.right, cellDrawRect.top, cellDrawRect.left, cellDrawRect.bottom, mCrossPaint);
	        	}
			
		 }
	};
	private Cell[] mGrid = new Cell[GRID_SIZE];
	
	/////////////////////////////////////////////////////////////////////////////////////
	
	public ViewProtoWF(Context context, AttributeSet attrs) {
		super(context, attrs);
		mTouchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTouchPaint.setColor(0xFF000080);
		mTouchPaint.setStrokeWidth(8);
		mTouchPaint.setStyle(Style.STROKE);
		mCrossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCrossPaint.setColor(0xFF808000);
		mCrossPaint.setStrokeWidth(8);
		mCrossPaint.setStyle(Style.STROKE);
		mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mFillPaint.setColor(0xFFFFFFFF);
		mFillPaint.setStrokeWidth(5);
		mFillPaint.setStyle(Style.FILL);
		mLineGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mLineGridPaint.setColor(0xFF000000);
		mLineGridPaint.setStrokeWidth(4);
		mLineGridPaint.setStyle(Style.STROKE);
		mStrikeThroughPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mStrikeThroughPaint.setColor(0xFF993300);
		mStrikeThroughPaint.setStrokeWidth(8);
		mStrikeThroughPaint.setStyle(Style.STROKE);
		//Sound
		snd_readyID = sp.load(getContext(), R.raw.ready, 1);
		snd_successID = sp.load(getContext(), R.raw.success, 1);
				//
	}
	public ViewProtoWF(Context context){
		super(context);
	}
	public void setGameObj(int turn){
		mGameEngine = new Game(turn, EMPTY);
	}
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Keep the view squared
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);
       
        int d = w == 0 ? h : h == 0 ? w : w < h ? w : h;
        setMeasuredDimension(d, d);
    }
	@Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int sxy = mSxy;
        int s3  = mSxy * 3;
   //     int x7 = mOffetX;
    //    int y7 = mOffetY;
        mRectFBorder.set(mOffetX, mOffetY, mOffetX + s3, mOffetY + s3);
        canvas.drawRoundRect(mRectFBorder, mSxy/6, mSxy/6, mFillPaint);
        for (int i = 0, k = sxy; i < 2; i++, k += sxy) {	//grid lines
            canvas.drawLine(mOffetX    , mOffetY + k, mOffetX + s3 - 1, mOffetY + k     , mLineGridPaint);
            canvas.drawLine(mOffetX + k, mOffetY    , mOffetX + k     , mOffetY + s3 - 1, mLineGridPaint);
        }
        for(int i = 0; i<GRID_SIZE; i++) mGrid[i].Render(canvas);//crosses & nulls
        
        if(mbGameOverFlag == 2){		//strike a red line through the wining triplet
        	int[] arr_index = mGameEngine.getWinLineInx();
        	if(arr_index[0] >= 0)//sanity check
        		winStroke(canvas, mGameTableOriginal[arr_index[0]][0], mGameTableOriginal[arr_index[0]][2]);
        	if(arr_index[1] >= 0)//sanity check
        		winStroke(canvas, mGameTableOriginal[arr_index[1]][0], mGameTableOriginal[arr_index[1]][2]);
        }
///////////////////FOR Resource editor to show something
        if (isInEditMode()){
        	 mRectFBorder.set(mOffetX, mOffetY, mOffetX + s3, mOffetY + s3);
             canvas.drawRoundRect(mRectFBorder, mSxy/6, mSxy/6, mFillPaint);
             for (int i = 0, k = sxy; i < 2; i++, k += sxy) {	//grid lines
                 canvas.drawLine(mOffetX    , mOffetY + k, mOffetX + s3 - 1, mOffetY + k     , mLineGridPaint);
                 canvas.drawLine(mOffetX + k, mOffetY    , mOffetX + k     , mOffetY + s3 - 1, mLineGridPaint);
             }
        }/////////////////////////
	}
private	void winStroke(Canvas canvas, int begCellInx, int endCellInx){
		//There are 3 line patterns: hotizontal, vert, diagonal
		//for the horiz. endCellInx - begCellInx = 2; for the vers - 6; the diags. - 8 or 4
		int startX, startY, stopX, stopY;
		switch(endCellInx - begCellInx){
		case 2:
			startX = mGrid[begCellInx].cellRect.left;
			startY = (mGrid[begCellInx].cellRect.top + mGrid[begCellInx].cellRect.bottom)/2;
			stopX = mGrid[endCellInx].cellRect.right;
			stopY = startY + GRID_LINE_WIDTH * 3;// To make the line slightly slanted
			break;
		case 6:
			startX = (mGrid[begCellInx].cellRect.left + mGrid[begCellInx].cellRect.right)/2;
			startY = mGrid[begCellInx].cellRect.top;
			stopX = startX + GRID_LINE_WIDTH * 3;
			stopY = mGrid[endCellInx].cellRect.bottom;
			break;
		case 8:
			startX = mGrid[begCellInx].cellRect.left;
			startY = mGrid[begCellInx].cellRect.top;
			stopX = mGrid[endCellInx].cellRect.right;
			stopY = mGrid[endCellInx].cellRect.bottom - GRID_LINE_WIDTH * 3;
			break;
		case 4:
			startX = mGrid[begCellInx].cellRect.right;
			startY = mGrid[begCellInx].cellRect.top;
			stopX = mGrid[endCellInx].cellRect.left;
			stopY = mGrid[endCellInx].cellRect.bottom - GRID_LINE_WIDTH * 3;
			break;
			default:
				startX = startY = stopX = stopY = 0;
				break;
		}
		canvas.drawLine(startX, startY, stopX, stopY, mStrikeThroughPaint);
}
	@Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int sx = (w - 2 * 4) / 3;
        int sy = (h - 2 * 4) / 3;

        int size = sx < sy ? sx : sy;

        mSxy = size;
        mOffetX = (w - 3 * size) / 2;
        mOffetY = (h - 3 * size) / 2;
        
        Rect tempRt = new Rect();
        int indx =0;
        int state = EMPTY;
        for( int i = 0, top = MARGIN + mOffetY, bottom = MARGIN +mOffetY + size; i < 3; i++, top += size, bottom += size){
        	for(int j = 0, left = mOffetX, right = mOffetX + size; j < 3; j++, left += size, right += size){
        		tempRt.set(left + GRID_LINE_WIDTH /2, top + GRID_LINE_WIDTH /2, right - GRID_LINE_WIDTH /2, bottom - GRID_LINE_WIDTH /2);
        		//initialize the array of Cell objects
        		if(mGrid[indx] == null){
        			mGrid[indx] = new Cell(indx, tempRt, state);
        	//		Log.d(TAG, mGrid[indx].cellRect.toShortString());
        		}
        		else{	//assign new values
        			mGrid[indx].cellRect.set(tempRt);
        			mGrid[indx].cellDrawRect.set(tempRt.left + GRID_LINE_WIDTH * 3, tempRt.top + GRID_LINE_WIDTH * 3,
        					tempRt.right - GRID_LINE_WIDTH * 3, tempRt.bottom - GRID_LINE_WIDTH * 3);
        		}
    			indx += 1;
        	}
        	
        }
	}

	public boolean onTouchEvent(MotionEvent event) {
		
		if(!bConnected)
			return true;
		if(bScreenLock)
			return true;
	    int eventaction = event.getAction();
	    switch (eventaction) {
	        case MotionEvent.ACTION_DOWN: 
	            // finger touches the screen
	            return true;

	        case MotionEvent.ACTION_UP: 
	        	
	            // finger leaves the screen
	            int x = (int) event.getX();
	            int y = (int) event.getY();
	            
	            for(int i = 0; i<GRID_SIZE; i++){
	            //	Log.d(TAG, mGrid[i].cellRect.toShortString());
	            	if(mGrid[i].cellRect.contains(x, y)){
	            		if(mGrid[i].cellState != EMPTY)
	            			return true; // the square is occupied already - do nothing
	            		int piece = mGameEngine.getMyPiece();
	            		mGrid[i].cellState = piece;
	            		mGameEngine.setCell(i, piece);
	            		//lock the screen
	            		bScreenLock = true;
	            		((WFMainActivity)getContext()).ToggleImgTurn(bScreenLock);
	            		curMoveCount++;
	            		if(curMoveCount > 4){
	            			//check for the end of game - WIN
	            			if(mGameEngine.IsGameOver()){
	            			mbGameOverFlag = 2; //TRUE - to be used in OnDraw()
	            			nWinDraw = WIN; //to used in Toast()
	            			invalidate(); //strike across

	            			h.postDelayed(runLocalEndGameToast, 300);
	            			h.postDelayed(runResetGame, 900);
	            			((WFMainActivity)getContext()).mSendHandler.obtainMessage(MESSAGE_SEND_MOVE,
		            				piece, i, mbGameOverFlag)
		            				.sendToTarget();
	            			return true;
	                 		}
	            			else if(curMoveCount == 9){
	    		            		nWinDraw = DRAW;
	    		            		mbGameOverFlag = 1;
	    		            		handle.postDelayed(runLocalEndGameToast, 300);
	    		    			    h.postDelayed(runResetGame, 500);	
	            			}

	            		}
	            		invalidate(mGrid[i].cellRect);
	            		
	            		((WFMainActivity)getContext()).mSendHandler.obtainMessage(MESSAGE_SEND_MOVE,
	            				piece, i, mbGameOverFlag)
	            		.sendToTarget();
	            		return true;
	            	}
	       	    }
	            break;
	    }

	    // tell the system that we handled the event and no further processing is required
	    return true; 
	}
	/*** Message Received Handler*/
	 final Handler mReceiveHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            case MESSAGE_RECEIVE_MOVE:
	            	int bytes_reced = msg.arg2;
	            	curMoveCount++;
	            	String msg_reced = (String) msg.obj;
	            	String piece = msg_reced.substring(9, 11);
	            	String square = msg_reced.substring(12, 13);
	            	String game_over_flag = msg_reced.substring(14);
	            	int square_inx = Integer.parseInt(square);
	            	int ipiece = Integer.parseInt(piece);
	            	mbGameOverFlag =  Integer.parseInt(game_over_flag);
	            	mGrid[square_inx].cellState = ipiece;
	            	mGameEngine.setCell(square_inx, ipiece);
	            	if(mbGameOverFlag == 0 || mbGameOverFlag == 1)//if not WIN
	            		sp.play(snd_readyID, 1, 1, 0, 0, 1);
	            	invalidate(mGrid[square_inx].cellRect);
	            	bScreenLock = false; //unlock the screen here
	            	((WFMainActivity)getContext()).ToggleImgTurn(bScreenLock);
	            	if(mbGameOverFlag == 1){//DRAW
	            		nWinDraw = DRAW;
	            		handle.postDelayed(runRemoteEndGameToast, 300);
	    			    h.postDelayed(runResetGame, 500);
	            	}
	            	else if(mbGameOverFlag == 2){ //WIN
	            		nWinDraw = WIN;
	            		mGameEngine.IsGameOver();
	            		sp.play(snd_successID, 1, 1, 0, 0, 1);
	            		invalidate(); //strike across
	            		handle.postDelayed(runRemoteEndGameToast, 300);
	    			    h.postDelayed(runResetGame, 600);
	            	}
	            	else if(mbGameOverFlag == 3){ //request to quit
	            		((WFMainActivity)getContext()).mSendHandler.obtainMessage
	            		(WFMainActivity.MSG_SEND_REQUEST_SHUTDOWN, 0, 0, 3)
	            		.sendToTarget();
	            	}
	            	
	            	break;
	            case MESSAGE_LOCAL_TOAST:
                	LayoutInflater inflater = ((WFMainActivity)getContext()).getLayoutInflater();
                	View layout = inflater.inflate(R.layout.custom_toast,
                          (ViewGroup) findViewById(R.id.toast_layout_root));
                	 TextView text = (TextView) layout.findViewById(R.id.text);
                	 text.setText((CharSequence) msg.obj);
                	    Toast toast = new Toast(((WFMainActivity)getContext()));
                	    toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
                	    toast.setView(layout);
                	    toast.show();
                	break;
	          
	            }
	        }
	 };
	public void SetGoFirst(boolean b, int cross_null){
					bIGoFirst = b; 
					mGameEngine.SetMyPiece(cross_null);
							}
	public void SetLockScreen(boolean b){
		bScreenLock = b;
		((WFMainActivity)getContext()).ToggleImgTurn(bScreenLock);
	}
	//End of game runnable here
	private Handler handle = new Handler();
	//EndGame Toast
	private Runnable runLocalEndGameToast = new Runnable(){
		
		public void run(){
			String winner = "The game is a draw";
	
			if(nWinDraw == WIN){
				My_wins ++;
				winner = ((WFMainActivity)getContext()).getThisDevName() +" won this round";
			}
			((WFMainActivity)getContext()).mSendHandler.obtainMessage(WFMainActivity.MESSAGE_TOAST, winner)
    			.sendToTarget();
			((WFMainActivity)getContext()).mSendHandler.obtainMessage(WFMainActivity.MSG_LOCAL_WINS_THIS_ROUND, My_wins)
			.sendToTarget();
		}		
	};
	private Runnable runRemoteEndGameToast = new Runnable(){
		
		public void run(){
			String winner = "The game is a draw";

			if(nWinDraw == WIN){
				His_wins ++;
				winner = ((WFMainActivity)getContext()).getRemoteDevName() +" won this round";
			}
			((WFMainActivity)getContext()).mSendHandler.obtainMessage(WFMainActivity.MESSAGE_TOAST, winner)
    			.sendToTarget();
			((WFMainActivity)getContext()).mSendHandler.obtainMessage(WFMainActivity.MSG_REMOTE_WINS_THIS_ROUND, His_wins)
			.sendToTarget();
		}		
	};
	private Handler h = new Handler();
	private Runnable runResetGame = new Runnable(){
		public void run() {
			int move = -1;
			if(nWinDraw == WIN ){
				if(mGameEngine.getWhoseTurn() == mGameEngine.getMyPiece())
					move = mGameEngine.getWhoseTurn();
				else{
					mGameEngine.toggleWhoseTurn(); 
					move = mGameEngine.getWhoseTurn();	
				}
					
			}
			else if(nWinDraw == DRAW){
				mGameEngine.toggleWhoseTurn(); 
				move = mGameEngine.getWhoseTurn();
			}
			for(int i=0; i<9; i++){
			mGrid[i].cellState = EMPTY;
			}
			mbGameOverFlag = 0;//set to FALSE
			curMoveCount = 0;
			nWinDraw = -1;
			int piece = mGameEngine.ToggleMyPiece();
			mGameEngine = new Game(move, piece);
			invalidate();
		}
	};
	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle bndl = new Bundle();
		Parcelable s = super.onSaveInstanceState();
        bndl.putParcelable("gv_super_state", s);
        //save game state
        bndl.putIntArray(GAME_STATE, mGameEngine.getGameState());
        //save game table
        for(int i=0; i<8; i++){
        	bndl.putIntArray(tblRows[i], mGameEngine.getGameTableRow(i));
        }
		bndl.putInt(WHOSE_TURN, mGameEngine.getWhoseTurn());
		bndl.putIntArray(WIN_INDEX, mGameEngine.getWinLineInx());
		bndl.putInt(WIN_DRAW, curMoveCount);
		bndl.putInt(GAME_OVER_FLAG, mbGameOverFlag);
		bndl.putInt(MYWINS_COUNT, My_wins);
		bndl.putInt(HISWINS_COUNT, His_wins);
		return bndl;
	}
	 @Override
    protected void onRestoreInstanceState(Parcelable state) {
    	if (!(state instanceof Bundle)) {
            super.onRestoreInstanceState(state);
            return;
        }
        Bundle bndl = (Bundle) state;
        Parcelable superState = bndl.getParcelable("gv_super_state");
        //restore the game state
        mGameEngine.putGameState(bndl.getIntArray(GAME_STATE));
        //restore the game table
        int[] arHelper;
        for(int i=0; i<8; i++){
        	arHelper = bndl.getIntArray(tblRows[i]);
        	mGameEngine.putGameTableRow(arHelper, i);
        }
        mGameEngine.putWhoseTurn(bndl.getInt(WHOSE_TURN));
        mbGameOverFlag = bndl.getInt(GAME_OVER_FLAG);
        curMoveCount = bndl.getInt(WIN_DRAW);
        My_wins =  bndl.getInt(MYWINS_COUNT);
        His_wins =  bndl.getInt(HISWINS_COUNT);

        super.onRestoreInstanceState(superState);
    }
}
