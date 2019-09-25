package com.onemanshow.bttic;


public final class Game {
	//constants
	public static final int GRID_SIZE_SQUARED = 3;
	public static final int NULL = -1;
	public static final int CROSS = -2;
	public static final int EMPTY = 0;
	//members
	//9 winning possitions; each number(0-8) corresponds to a square(our Cell) index
	private int[][] mGameTable = { {0,1,2}, {3,4,5}, {6,7,8}, {0,3,6},
			{1,4,7}, {2,5,8}, {0,4,8}, {2,4,6} };
	private int[] mGameState = {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY};
	private int mWhoseTurn;
	private int[] mWinLineInx = {-1,-1};
	private int mMyPiece = EMPTY;
	//Constructor
	public Game(int turn, int piece){
		if(turn != 0)mWhoseTurn = turn;// 
		mMyPiece = piece;
		}
	//public members
	public int getMyPiece(){
		return mMyPiece;
	}
	public int getOpponPiece(){
		int ret = EMPTY;
		if(mMyPiece == CROSS)
			ret = NULL;
		else if(mMyPiece == NULL)
			ret = CROSS;
		return ret;
	}
	
	public int ToggleMyPiece(){
		int ret = EMPTY;
		if(mMyPiece == CROSS)
			ret = mMyPiece = NULL;
		else if(mMyPiece == NULL)
			ret = mMyPiece = CROSS;
		return ret;
	}
	
	public void SetMyPiece(int cross_null){
		mMyPiece = cross_null;
		}
	public final int[] getGameState(){
		return mGameState;
	}

	public final void putGameState(int[] gameSt){
		mGameState = gameSt;
	}
	public final int[] getGameTableRow(int ind){
		return mGameTable[ind];
	}
	
	public final void putGameTableRow(int[] tblRow, int ind){
		mGameTable[ind] = tblRow;
	}
	public final int getWhoseTurn(){
		return mWhoseTurn;
	}
	public final void putWhoseTurn(int turn){
		mWhoseTurn = turn;
	}
	public final void toggleWhoseTurn(){
		if(mWhoseTurn == NULL)
			mWhoseTurn = CROSS;
		else if(mWhoseTurn == CROSS)
			mWhoseTurn = NULL;
	}
	public final boolean IsTie(){		
		for(int i = 0; i < 8; i++){
			
			int nlCount = 0;
			int csCount = 0;
			int vacCount = 0;
			for(int j = 0; j < 3; j++){
					if(mGameTable[i][j] == mMyPiece && mMyPiece == NULL)
						nlCount++;
					else if(mGameTable[i][j] == mMyPiece && mMyPiece == CROSS)
						csCount++;
					else
						vacCount++;
			}
			if(vacCount > 0 && (nlCount > 1 || csCount > 1))
				return false;
			else
				continue;
			
			}
		return true;
	}
	public final boolean IsGameOver(){
		for(int i = 0; i < 8; i++){
		
			if(mGameTable[i][0] == mGameTable[i][1] && mGameTable[i][1] == mGameTable[i][2]){
				if(mWinLineInx[0] == -1)
					mWinLineInx[0]=i;
				else if(mWinLineInx[1] == -1)
					mWinLineInx[1]=i;
			}
		}
		if((mWinLineInx[0] > -1) || (mWinLineInx[1] > -1))
			return true;
		else 
			return false;
	}
	
	public final  boolean isCellVacant(int index){
			
			if(mGameState[index] == EMPTY)
					return true;
			else 
				return false;
	}
	
	public final boolean setCell(int index, int piece){
		if(!isCellVacant(index))
			return false;
		mGameState[index] = piece;
		for(int i = 0; i < 8; i++){
			for(int j = 0; j < 3; j++){
				if(mGameTable[i][j] == index)
					mGameTable[i][j] = piece;
			}
		}
		return true;
	}
	public final int[] getWinLineInx(){ return mWinLineInx; }	
}		

