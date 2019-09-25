package com.onemanshow.singletic;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.onemanshow.tictactoe.R;


public class CustomTicView extends View {
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
    public static final String XWINS_COUNT = "xwins";
    public static final String OWINS_COUNT = "owins";
    public static final String WHOSE_TURN = "turn";
    public static final String GAME_STATE = "gamestate";
    public static final String GAME_OVER_FLAG = "gameover";
    public static final String WIN_INDEX = "winindex";
    public static final String WIN_DRAW = "windraw";
    public static final String SOUND_ON = "soundon";

    public static final String TAG = "MyTicTacView";
    public static final int 	GRID_SIZE = 9; //3 X 3
    public static final int 	EMPTY = 0;
    public static final int 	NULL = -1;
    public static final int 	CROSS = -2;
    public static final int MARGIN = 4;
    public static final int 	GRID_LINE_WIDTH = 4;
    public static final int 	DRAW = 0;
    public static final int 	WIN = 1;
    // a const copy to refer to in winStroke() func.
    private final static int[][] mGameTableOriginal = { {0,1,2}, {3,4,5}, {6,7,8}, {0,3,6},
            {1,4,7}, {2,5,8}, {0,4,8}, {2,4,6} };
    //members
    boolean bOnePlayer = true;
    boolean andrWinLast = false;
    boolean bSoundOn = true;
    private boolean mbGameOverFlag = false;
    private int nWinDraw = -1;
    private int curMoveCount = 0;
    private Game mGameEngine;

    private SoundPool sp = new SoundPool(3, AudioManager.STREAM_NOTIFICATION, 0);
    private int snd_readyID;
    private int snd_successID;
    TextView CrossWins;
    TextView NullWins;
    ImageView imgCrossNullTurn;
    Drawable dblCross;
    Drawable dblNull;
    private int X_wins = 0;
    private int O_wins = 0;
    //Paint objs
    private Paint mBorderGridPaint;
    private Paint mFillPaint;
    private Paint mLineGridPaint;
    private Paint mNullPaint;
    private Paint mCrossPaint;
    private Paint mStrikeThroughPaint;
    //colors
    private int mCrossColor;
    private int mNullColor;
    private int mGridColor;
    private int mFillColor;
    private int mBorderColor;
    private int mStrikeThroughColor;

    private final Rect mDstRect = new Rect();
    private final RectF mRectFBorder = new RectF();
    private int mSxy;
    private int mOffetX;
    private int mOffetY;


    private class Cell{
        int cellIndex;			// 1 to 9
        Rect cellRect;
        RectF cellDrawRect;
        int cellState; 			// EMPTY, NULL, CROSS
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
                return;
            }
            else if(cellState == NULL){
                canvas.drawOval(cellDrawRect, mNullPaint);
            }
            else if(cellState == CROSS){
                canvas.drawLine(cellDrawRect.left,cellDrawRect.top, cellDrawRect.right, cellDrawRect.bottom, mCrossPaint);
                canvas.drawLine(cellDrawRect.right, cellDrawRect.top, cellDrawRect.left, cellDrawRect.bottom, mCrossPaint);
            }
        }
    };
    class runAILogic implements Runnable{

        public void run() {
            Message msg = new Message();
            if(300 == mGameEngine.getGameLevel() && curMoveCount > 0)//Level HARD
                msg.arg1 = mGameEngine.BestMoveMinMax(curMoveCount);
            else
                msg.arg1 = mGameEngine.BestMove(curMoveCount, NULL);
            h.sendMessage(msg);

        }
    };
    private Runnable runCleanUp = new Runnable(){
        public void run() {
            for(int i=0; i<9; i++){
                mGrid[i].cellState = EMPTY;
            }
            mbGameOverFlag = false;
            curMoveCount = 0;
            int move = -1;
            if(nWinDraw == WIN){
                move = mGameEngine.getWhoseTurn();
                if(move == NULL && bOnePlayer)
                    andrWinLast = true;
                else if(move == CROSS && bOnePlayer)
                    andrWinLast = false;
            }
            else if(nWinDraw == DRAW){
                if(mGameEngine.getWhoseTurn()== NULL && bOnePlayer)  //DROID had the first move
                    andrWinLast = false;
                else if(mGameEngine.getWhoseTurn()== CROSS && bOnePlayer)//DROID gets to move first
                    andrWinLast = true;
                mGameEngine.toggleWhoseTurn();
                move = mGameEngine.getWhoseTurn();
            }
            mGameEngine = new Game(move, mGameEngine.getGameLevel()); //keep the same AI level
            nWinDraw = -1;
            invalidate();
            if(andrWinLast && bOnePlayer)
                new Thread(new runAILogic()).start();//DROID gets to move first
        }
    };
    private Runnable r = new Runnable(){

        public void run(){
            mGameEngine.toggleWhoseTurn(); //this was the last turn
            String winner = "The game is a draw";
            String cnt = "0";
            if(nWinDraw == WIN){
                if(NULL == mGameEngine.getWhoseTurn()){
                    O_wins++;
                    NullWins.setText(String.valueOf(O_wins));
                    if(bOnePlayer)
                        winner = "Android won this round";//one player game
                    else
                        winner = "O-player won this round";
                }
                else if(CROSS == mGameEngine.getWhoseTurn()){
                    X_wins++;
                    CrossWins.setText(String.valueOf(X_wins));
                    winner = "X-player won this round";
                }
            }
            Context context = getContext();

            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.toast_layout_root));
            TextView textV = (TextView) layout.findViewById(R.id.text);
            textV.setText(winner);

            Toast toast = new Toast(context);
            toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.show();
            h.postDelayed(runCleanUp, 500);
        }

    };
    // Grid - array of Cell(s)
    private Cell[] mGrid = new Cell[GRID_SIZE];
    /////////////////////////////////////handler class
    class MyHandler extends Handler {
        public void handleMessage (Message msg){
            int res = msg.arg1;

            mGrid[res].cellState = mGameEngine.getWhoseTurn();
            mGameEngine.setCell(res);
            mGameEngine.toggleWhoseTurn();
            curMoveCount++;
            if(curMoveCount > 4){
                if(mGameEngine.IsGameOver()){
                    mbGameOverFlag = true; // to be used in OnDraw()
                    nWinDraw = WIN; //to be used in Toast()
                    invalidate(); //strike across
                    if(bSoundOn)
                        sp.play(snd_successID, 1, 1, 0, 0, 1);
                    h.postDelayed(r, 300);
                    return;
                }
                else if(curMoveCount == 9){
                    //if(mGameEngine.IsTie()){
                    nWinDraw = DRAW;
                    h.postDelayed(r, 300);
                    //}
                }

            }
            if(bSoundOn)
                sp.play(snd_readyID, 1, 1, 0, 0, 1);
            invalidate(mGrid[res].cellRect);

        }
    }
    private Handler h = new MyHandler();

    void winStroke(Canvas canvas, int begCellInx, int endCellInx){
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
    public CustomTicView(Context context){
        super(context);
    }
    public CustomTicView(Context context, AttributeSet attrs) {
        super(context, attrs);

        requestFocus();
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.CustomTicView, 0, 0);
        try{
            mFillColor = a.getColor(R.styleable.CustomTicView_fillColor, 0xffffffff);
            mBorderColor = a.getColor(R.styleable.CustomTicView_borderColor, 0xff000000);
            mGridColor = a.getColor(R.styleable.CustomTicView_gridColor, 0xff000000);
            mCrossColor = a.getColor(R.styleable.CustomTicView_crossColor, 0xff000000);
            mNullColor = a.getColor(R.styleable.CustomTicView_nullColor, 0xff000000);
            mStrikeThroughColor = a.getColor(R.styleable.CustomTicView_winColor, 0xffff0000);
        }finally{
            a.recycle();
        }
        mLineGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLineGridPaint.setColor(mGridColor);
        mLineGridPaint.setStrokeWidth(GRID_LINE_WIDTH);
        mLineGridPaint.setStyle(Style.STROKE);

        mNullPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNullPaint.setColor(mNullColor);
        mNullPaint.setStrokeWidth(8);
        mNullPaint.setStyle(Style.STROKE);

        mCrossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCrossPaint.setColor(mCrossColor);
        mCrossPaint.setStrokeWidth(8);
        mCrossPaint.setStyle(Style.STROKE);

        mBorderGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderGridPaint.setColor(mBorderColor);
        mBorderGridPaint.setStrokeWidth(10);
        mBorderGridPaint.setStyle(Style.STROKE);

        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setColor(mFillColor);
        mFillPaint.setStrokeWidth(GRID_LINE_WIDTH);
        mFillPaint.setStyle(Style.FILL);

        mStrikeThroughPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStrikeThroughPaint.setColor(mStrikeThroughColor);
        mStrikeThroughPaint.setStrokeWidth(10);
        mStrikeThroughPaint.setStyle(Style.STROKE);
        //Sound
        snd_readyID = sp.load(getContext(), R.raw.ready, 1);
        snd_successID = sp.load(getContext(), R.raw.success, 1);
        //
        Resources res = getResources();
        dblCross = res.getDrawable(R.drawable.cross);
        dblNull = res.getDrawable(R.drawable.img_null);
    }
    public void resetGameObj(int turn, int gameLevel){
        mGameEngine = new Game(turn, gameLevel);
        for(int i=0; i<9; i++){
            mGrid[i].cellState = EMPTY;
        }
        mbGameOverFlag = false;
        curMoveCount = 0;
        invalidate();
    }
    public void setGameObj(int turn, int gameLevel){
        mGameEngine = new Game(turn, gameLevel);
    }
    public void restoreGameLevel(int GameLevel){
        mGameEngine.setGameLevel(GameLevel);
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
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int sx = (w - 2 * MARGIN) / 3;
        int sy = (h - 2 * MARGIN) / 3;

        int size = sx < sy ? sx : sy;

        mSxy = size;
        mOffetX = (w - 3 * size) / 2;
        mOffetY = (h - 3 * size) / 2;

        mDstRect.set(MARGIN, MARGIN, size - MARGIN, size - MARGIN);

        Rect tempRt = new Rect();
        int indx =0;
        int state = EMPTY;
        for( int i = 0, top = MARGIN, bottom = MARGIN + size; i < 3; i++, top += size, bottom += size){
            for(int j = 0, left = mOffetX, right = mOffetX + size; j < 3; j++, left += size, right += size){
                tempRt.set(left + GRID_LINE_WIDTH /2, top + GRID_LINE_WIDTH /2, right - GRID_LINE_WIDTH /2, bottom - GRID_LINE_WIDTH /2);
                //initialize the array of Cell objects
                if(mGrid[indx] == null){
                    state = mGameEngine.getCellState(indx);
                    mGrid[indx] = new Cell(indx, tempRt, state);
                    //		Log.d(TAG, mGrid[indx].cellRect.toShortString());
                }
                else{
                    mGrid[indx].cellRect.set(tempRt);
                    mGrid[indx].cellDrawRect.set(tempRt.left + GRID_LINE_WIDTH * 3, tempRt.top + GRID_LINE_WIDTH * 3,
                            tempRt.right - GRID_LINE_WIDTH * 3, tempRt.bottom - GRID_LINE_WIDTH * 3);
                }
                indx += 1;
            }

        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int sxy = mSxy;
        int s3  = sxy * 3;
        int x7 = mOffetX;
        int y7 = mOffetY;
        mRectFBorder.set(x7, y7, x7 + s3, y7 + s3);
        canvas.drawRoundRect(mRectFBorder, mSxy/6, mSxy/6, mFillPaint);//filled rect

        for (int i = 0, k = sxy; i < 2; i++, k += sxy) {	//grid lines
            canvas.drawLine(x7    , y7 + k, x7 + s3 - 1, y7 + k     , mLineGridPaint);
            canvas.drawLine(x7 + k, y7    , x7 + k     , y7 + s3 - 1, mLineGridPaint);
        }
        canvas.drawRoundRect(mRectFBorder, mSxy/6, mSxy/6, mBorderGridPaint);// border around the rect

        for(int i = 0; i<GRID_SIZE; i++){	//crosses & nulls
            mGrid[i].Render(canvas);
        }
        if(mbGameOverFlag){		//strike red line through the wining triplet
            int[] arr_index = mGameEngine.getWinLineInx();
            if(arr_index[0] >= 0)//sanity check
                winStroke(canvas, mGameTableOriginal[arr_index[0]][0], mGameTableOriginal[arr_index[0]][2]);
            if(arr_index[1] >= 0)//sanity check
                winStroke(canvas, mGameTableOriginal[arr_index[1]][0], mGameTableOriginal[arr_index[1]][2]);
        }
        ///////////////////FOR the resource editor to show something
        if (isInEditMode()){
            mRectFBorder.set(mOffetX, mOffetY, mOffetX + s3, mOffetY + s3);
            canvas.drawRoundRect(mRectFBorder, mSxy/6, mSxy/6, mFillPaint);
            for (int i = 0, k = sxy; i < 2; i++, k += sxy) {	//grid lines
                canvas.drawLine(mOffetX    , mOffetY + k, mOffetX + s3 - 1, mOffetY + k     , mLineGridPaint);
                canvas.drawLine(mOffetX + k, mOffetY    , mOffetX + k     , mOffetY + s3 - 1, mLineGridPaint);
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        int eventaction = event.getAction();

        switch (eventaction) {
            case MotionEvent.ACTION_DOWN:
                // the finger touches the screen
                return true;

            case MotionEvent.ACTION_UP:

                // the finger leaves the screen
                int x = (int) event.getX();
                int y = (int) event.getY();

                for(int i = 0; i<GRID_SIZE; i++){
                    //	Log.d(TAG, mGrid[i].cellRect.toShortString());
                    if(mGrid[i].cellRect.contains(x, y)){
                        if(mGameEngine.getCellState(i) != EMPTY || mbGameOverFlag)
                            return true; // the square is occupied already - do nothing
                        mGrid[i].cellState = mGameEngine.getWhoseTurn();
                        mGameEngine.setCell(i);
                        mGameEngine.toggleWhoseTurn();
                        curMoveCount++;
                        if(curMoveCount > 4){

                            //check for the end of game - WIN
                            if(mGameEngine.IsGameOver()){
                                mbGameOverFlag = true; // to be used in OnDraw()
                                nWinDraw = WIN; //to be used in Toast()
                                invalidate(); //strike across
                                if(bSoundOn)
                                    sp.play(snd_successID, 1, 1, 0, 0, 1);
                                h.postDelayed(r, 300);

                                return true;
                            }
                            else if(curMoveCount == 9){
                                nWinDraw = DRAW;
                                h.postDelayed(r, 300);
                            }

                        }
                        invalidate(mGrid[i].cellRect);
                        if(!bOnePlayer){
                            if(mGameEngine.getWhoseTurn() == CROSS)
                                imgCrossNullTurn.setImageDrawable(dblCross);
                            else
                                imgCrossNullTurn.setImageDrawable(dblNull);
                        }
                        else if(bOnePlayer && nWinDraw != DRAW && nWinDraw != WIN)
                            new Thread(new runAILogic()).start(); // if WIN or DRAW - game's over
                        return true;
                    }
                }
                break;
        }
        // tell the system that we handled the event and no further processing is required
        return true;
    }
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bndl = new Bundle();
        Parcelable s = super.onSaveInstanceState();
        bndl.putParcelable("gv_super_state", s);
        //save the game state
        bndl.putIntArray(GAME_STATE, mGameEngine.getGameState());
        //save the game table
        for(int i=0; i<8; i++){
            bndl.putIntArray(tblRows[i], mGameEngine.getGameTableRow(i));
        }
        bndl.putInt(WHOSE_TURN, mGameEngine.getWhoseTurn());
        bndl.putIntArray(WIN_INDEX, mGameEngine.getWinLineInx());
        bndl.putInt(WIN_DRAW, curMoveCount);
        bndl.putBoolean(GAME_OVER_FLAG, mbGameOverFlag);
        bndl.putBoolean(SOUND_ON, bSoundOn);
        bndl.putInt(XWINS_COUNT, X_wins);
        bndl.putInt(OWINS_COUNT, O_wins);
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
        mGameEngine.saveWinLineInx(bndl.getIntArray(WIN_INDEX));
        mbGameOverFlag = bndl.getBoolean(GAME_OVER_FLAG);
        bSoundOn = bndl.getBoolean(SOUND_ON);
        curMoveCount = bndl.getInt(WIN_DRAW);
        X_wins =  bndl.getInt(XWINS_COUNT);
        O_wins =  bndl.getInt(OWINS_COUNT);
        String hlp = "0";
        CrossWins.setText(hlp.valueOf(X_wins));
        NullWins.setText(hlp.valueOf(O_wins));
        if(CROSS == bndl.getInt(WHOSE_TURN))
            imgCrossNullTurn.setImageDrawable(dblCross);
        else
            imgCrossNullTurn.setImageDrawable(dblNull);
        super.onRestoreInstanceState(superState);
    }

}
