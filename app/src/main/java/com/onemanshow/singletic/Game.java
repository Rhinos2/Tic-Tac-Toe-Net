package com.onemanshow.singletic;


import java.util.Random;
import java.util.Vector;


public final class Game {
    //constants
    public static final int GRID_SIZE_SQUARED = 3;
    public static final int NULL = -1;
    public static final int CROSS = -2;
    public static final int EMPTY = 0;
    //members
    //9 winning possitions; each nummber(0-8) corresponds to a square(Cell) index
    private int[][] mGameTable = { {0,1,2}, {3,4,5}, {6,7,8}, {0,3,6},
            {1,4,7}, {2,5,8}, {0,4,8}, {2,4,6} };
    private int[] mGameState = {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY};
    private int mWhoseTurn;// = CROSS; //FOR NOW INIT MANUALLY
    private int[] mWinLineInx = {-1,-1};
    private int gLevel;
    private int andPiece = NULL; //Android's is NULL piece
    public final int getGameLevel(){ return gLevel; }
    public final void setGameLevel(int GameLevel){ gLevel = GameLevel; }

    class MiniMaxNode{
        int movePiece;
        Vector<MiniMaxNode> nodChilds = new Vector<MiniMaxNode>();
        int[] curGameSt = new int[9];
        int[][] curBoardTable = new int[8][3];
        boolean bMAX;
        int squ_chld_pointer = 0;
        int squ_indx;
        int best_val;
    };
    //Constructor
    public Game(int turn, int gameLevel){
        if(turn != 0)mWhoseTurn = turn;//
        gLevel = gameLevel;
    }
    //public members
    public final int[] getGameState(){
        return mGameState;
    }
    public final int[] getWinLineInx(){ return mWinLineInx; }
    public final void saveWinLineInx(int[] arr_index){ mWinLineInx[0] = arr_index[0];
        mWinLineInx[1] = arr_index[1];}
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
                if(mGameTable[i][j] == mWhoseTurn && mWhoseTurn == NULL)
                    nlCount++;
                else if(mGameTable[i][j] == mWhoseTurn && mWhoseTurn == CROSS)
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

    public final  boolean isCellVacant(int index){

        if(mGameState[index] == EMPTY)
            return true;
        else
            return false;
    }
    public final int getCellState(int index){
        return mGameState[index];
    }
    public final void setCellState(int index, int state){
        mGameState[index] = state;
    }

    public final boolean setCell(int index){
        if(!isCellVacant(index))
            return false;
        mGameState[index] = mWhoseTurn;
        for(int i = 0; i < 8; i++){
            for(int j = 0; j < 3; j++){
                if(mGameTable[i][j] == index)
                    mGameTable[i][j] = mWhoseTurn;
            }
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
    ////////////////////////////////////////AI
    int MiniMaxFunc(MiniMaxNode mMiniMaxNode, int depth, boolean iMAX, int movePiece,
                    int mMoveCount){
        ////////////////////////////////////////////////
        mMiniMaxNode.bMAX = iMAX;
        if(iMAX){	//maximizer
            mMiniMaxNode.best_val = -1000;
            int val = - 1001;
            //////////////////////////////////////////
            for(int i=0; i<9-mMoveCount; i++){//copy parent level game state
                MiniMaxNode nodChild = new MiniMaxNode();
                nodChild.movePiece = movePiece;
                for(int n=0; n<9; n++){						//
                    nodChild.curGameSt[n] = mMiniMaxNode.curGameSt[n];
                }
                for(int k=0; k<8; k++){
                    for(int j=0; j<3; j++){
                        nodChild.curBoardTable[k][j] = mMiniMaxNode.curBoardTable[k][j];
                    }
                }
                //	nodChild.chldCount = mMiniMaxNode.chldCount -1;
                boolean bFound = false;
                out:
                for(int m = mMiniMaxNode.squ_chld_pointer; m<9; m++){//take first avail. square; register as 'move'
                    if(bFound) break out;
                    if(nodChild.curGameSt[m] == EMPTY){
                        bFound = true;
                        nodChild.curGameSt[m] = movePiece;
                        nodChild.squ_indx = m;
                        mMiniMaxNode.squ_chld_pointer = m+1;
                        for(int k=0; k<8; k++){
                            for(int j=0; j<3; j++){
                                if(nodChild.curBoardTable[k][j] == m){
                                    nodChild.curBoardTable[k][j] = movePiece;
                                }
                            }
                        }
                    }
                }
                mMiniMaxNode.nodChilds.add(nodChild);
                System.out.println("Max: Value of nodChild.squ_indx: " + nodChild.squ_indx);
                depth--;

                while(depth > 0){
                    movePiece = (movePiece == CROSS)? NULL : CROSS;
                    MiniMaxFunc(nodChild, depth, false, movePiece, mMoveCount);
                }

                val = evaluate(nodChild, movePiece, iMAX, mMoveCount);
                System.out.println("Eval MAX returns:" + val + "Depth: " + depth);
                nodChild.best_val = val;
                mMiniMaxNode.best_val = (mMiniMaxNode.best_val >= val)? mMiniMaxNode.best_val : val;
            }
            /////////////////////////////////////////////////////////////////////
        }
        else if(!iMAX){ //MINIMIZER
            mMiniMaxNode.best_val = 1000;
            int val = 1001;
            for(int i=0; i<9-mMoveCount; i++){//copy parent level game state
                MiniMaxNode nodChild = new MiniMaxNode();
                for(int n=0; n<9; n++){						//
                    nodChild.curGameSt[n] = mMiniMaxNode.curGameSt[n];
                }
                for(int k=0; k<8; k++){
                    for(int j=0; j<3; j++){
                        nodChild.curBoardTable[k][j] = mMiniMaxNode.curBoardTable[k][j];
                    }
                }
                boolean bFound = false;
                outout:
                for(int m = mMiniMaxNode.squ_chld_pointer; m<9; m++){	//take first avail. square; register as 'move'
                    if(bFound) break outout;
                    if(nodChild.curGameSt[m] == EMPTY){
                        bFound = true;
                        nodChild.curGameSt[m] = movePiece;
                        nodChild.squ_indx = m;
                        mMiniMaxNode.squ_chld_pointer = m+1;
                        for(int k=0; k<8; k++){
                            for(int j=0; j<3; j++){
                                if(nodChild.curBoardTable[k][j] == m) nodChild.curBoardTable[k][j] = movePiece;
                            }
                        }
                    }
                }
                mMiniMaxNode.nodChilds.add(nodChild);
                System.out.println("Min: Value of nodChild.squ_indx: " + nodChild.squ_indx);
                depth--;

                while(depth > 0){
                    movePiece = (movePiece == CROSS)? NULL : CROSS;
                    MiniMaxFunc(nodChild, depth, true, movePiece, mMoveCount);
                }

                val = evaluate(nodChild, movePiece, iMAX, mMoveCount);
                System.out.println("Eval MIN returns:" + val + " Depth: " + depth);
                nodChild.best_val = val;
                mMiniMaxNode.best_val = (mMiniMaxNode.best_val <= val)? mMiniMaxNode.best_val : val;
            }
           ////////////////////////////////////////////////////////////////////////
        }
        return mMiniMaxNode.best_val;
    }//End of MiniMaxFunc

    int evaluate(MiniMaxNode Node, int thisMovePiece, boolean maxFlag, int mMoveCount){
        //criterium: WIN = 8; LOSS = -8; WARDOFF = 6; CENTER/CORNER = 3; OTHER = 2;
        int nextMovePiece = (thisMovePiece == CROSS)? NULL : CROSS;
        boolean bAndMove = (thisMovePiece == andPiece)? true : false;

        ////WIN ?
        for(int i = 0; i < 8; i++){
            int threeAndrPieceCount = 0;
            for(int j = 0; j < 3; j++){
                if(Node.curBoardTable[i][j] == thisMovePiece && bAndMove)
                    threeAndrPieceCount++;
            }
            if(threeAndrPieceCount == 3)
                return (maxFlag)? 9 : -9; //WIN
        }
        //LOSS Looming?
        int retVal = 0;
        for(int n=0; n<8; n++){
            int threeOpponPieceCount = 0;
            boolean emptySqu = false;
            for(int k = 0; k < 3; k++){
                if(Node.curBoardTable[n][k] == nextMovePiece && bAndMove)
                    threeOpponPieceCount++;
                else if(Node.curBoardTable[n][k] != thisMovePiece && bAndMove)
                    emptySqu = true;
            }
            if(threeOpponPieceCount == 2 && emptySqu)//look ahead one level
                retVal = (maxFlag)?	retVal - 9 : retVal + 9 ;
        }
        if(retVal != 0) return retVal;
        //CORNER OR CENTER OR OTHER
        //the opposite corner is preferred
        if((Node.squ_indx == 0 && (Node.curGameSt[8] == thisMovePiece))
                ||( Node.squ_indx == 2 && (Node.curGameSt[6] == thisMovePiece))
                || (Node.squ_indx == 6 && (Node.curGameSt[2] == thisMovePiece))
                ||(Node.squ_indx == 8 && (Node.curGameSt[0] == thisMovePiece)))
        {
            retVal = (maxFlag)? 7 : -7; //CORNER OPPOSITE may lead to a fork
        }
        else if(Node.squ_indx == 4)
            retVal = (maxFlag)? 6 : -6;//center
        else if(Node.squ_indx == 0 || Node.squ_indx == 2 || Node.squ_indx == 6 || Node.squ_indx == 8)
            retVal = (maxFlag)? 5 : -5; //CORNER
        else
            retVal = (maxFlag)? 4 : -4;//OTHER

        for(int i = 0; i < 8; i++){
            int twoAndrCount = 0;
            boolean vacantSquFlag = false;
            for(int j = 0; j < 3; j++){
                if(Node.curBoardTable[i][j] == thisMovePiece && bAndMove)
                    twoAndrCount++;
                else if(Node.curBoardTable[i][j] != nextMovePiece)
                    vacantSquFlag = true;
            }
            if(twoAndrCount == 2 && vacantSquFlag){// Two pieces in a line plus empty square
                retVal = (maxFlag)? retVal + 1 : retVal - 1; // good for last movePiece player (Android)
                if(mMoveCount == 3 && (i == 6 || i == 7))
                    retVal = (maxFlag)? retVal -4 : retVal + 4;
            }
        }

        return retVal;
    }
    public final int BestMoveMinMax(int mMoveCount){
        MiniMaxNode nodOrigin = new MiniMaxNode();
        for(int i=0; i<9; i++){						//
            nodOrigin.curGameSt[i] = mGameState[i];
        }
        for(int k=0; k<8; k++){
            for(int j=0; j<3; j++){
                nodOrigin.curBoardTable[k][j] = mGameTable[k][j];
            }
        }
//		Android is max
        int bestValue =	MiniMaxFunc(nodOrigin, 1, true, andPiece, mMoveCount);
        //look inside nodOrigin
        int size = nodOrigin.nodChilds.size();
        int bestMove = -1;//from best minimax value to best move
        Vector<Integer> bestMoveCandidate = new Vector<Integer>();
        for(int inx=0; inx<size; inx++){
            if(nodOrigin.nodChilds.elementAt(inx).best_val == bestValue) //the node with the best val move
                bestMoveCandidate.add(nodOrigin.nodChilds.elementAt(inx).squ_indx);
        }
        Random rand = new Random();
        int index = rand.nextInt(bestMoveCandidate.size());	//random choice from node with the same val
        bestMove = bestMoveCandidate.elementAt(index);
        return bestMove;
    }
    //Rule-based only implementation for the EASY and MEDIUM levels
    public final int BestMove( int mMoveCount, int andrPiece){
        //Look ahead moves not generated; evaluation is based on the following:
        //1. Immediate WIN
        // 2. Ward off the opponent's immenent WIN move.
        //3. Take the center cell
        // 4. Take a corner at random
        //5. Take whatever you can
        int oppnPiece = (andrPiece == CROSS)? NULL : CROSS;
        if(mMoveCount == 0){//andr goes first.
            Vector<Integer> firstMove = new Vector<Integer>();
            firstMove.add(8);
            firstMove.add(0);
            firstMove.add(2);
            firstMove.add(6);
            firstMove.add(4);
            if(gLevel == 100){//EASY level
                firstMove.add(7);
                firstMove.add(3);
                firstMove.add(5);
                firstMove.add(1);
            }
            //shuffle the indeces
            Random rand = new Random();
            int inx = rand.nextInt(firstMove.size());
            int bestMove = firstMove.elementAt(inx);
            return bestMove;
        }//First move is a random chose from corners plus center

        ///////////////////////////WIN?
        for(int i = 0; i < 8; i++){
            int andrPieceCount = 0;
            //	int oppnPieceCount = 0;
            int vacantSqu = 0;
            int bestMove = -10; //illegal value
            for(int j = 0; j < 3; j++){
                if(mGameTable[i][j] == andrPiece)
                    andrPieceCount++;
                else if(mGameTable[i][j] != oppnPiece){
                    vacantSqu++;
                    bestMove = mGameTable[i][j]; //Best move candidate
                }
            }
            if(andrPieceCount == 2 && vacantSqu == 1){
                return bestMove;
            }
        }/////////////////end WIN?
        ////For EASY level the Ward-Off guard works at RANDOM

        for(int i = 0; i < 8; i++){
            //		int andrPieceCount = 0;
            int oppnPieceCount = 0;
            int vacantSqu = 0;
            int bestMove = -10; //illegal value
            for(int j = 0; j < 3; j++){
                if(mGameTable[i][j] == oppnPiece)
                    oppnPieceCount++;
                else if(mGameTable[i][j] != andrPiece){
                    vacantSqu++;
                    bestMove = mGameTable[i][j]; //Best move candidate
                }
            }
            if(oppnPieceCount == 2 && vacantSqu == 1){
                if(gLevel == 100){//Level EASY tweak
                    Vector<Integer> allMoves;
                    allMoves = new Vector<Integer>();
                    int count = 0;
                    for(int v=0; v<9; v++){
                        if(mGameState[v] == EMPTY){
                            count++;
                            allMoves.add(v);
                            if(count==1) break; //Randdom (3 + bestMove)
                        }

                    }
                    allMoves.add(bestMove);
                    allMoves.trimToSize();
                    if(allMoves.size() > 0){
                        Random rand = new Random();
                        int inx = rand.nextInt(allMoves.size());
                        bestMove = allMoves.elementAt(inx);
                    }
                }//Level EASY tweak's end
                return bestMove;
            }
        }

        /////////////////End Ward-Off guard
        Vector<Integer> corners;
        //if EASY or MEDIUM add all others with the corners
        corners = new Vector<Integer>();
        if(gLevel == 100 || gLevel == 200){
            for(int v=0; v<9; v++){
                if(mGameState[v] == EMPTY)
                    corners.add(v);
            }
        }
        else if(gLevel == 300){//if HARD
            if(mGameState[0] == EMPTY)
                corners.add(0);
            if(mGameState[2] == EMPTY)
                corners.add(2);
            if(mGameState[4] == EMPTY)
                corners.add(4);
            if(mGameState[6] == EMPTY)
                corners.add(6);
            if(mGameState[8] == EMPTY)
                corners.add(8);
        }
        corners.trimToSize();
        //		Out of the empty corners, pick at random
        if(corners.size() > 0){
            Random rand = new Random();
            int inx = rand.nextInt(corners.size());
            int bestMove = corners.elementAt(inx);
            return bestMove;
        }
        //Taking wharever is left to be taken
        Vector<Integer> vacantSqures = new Vector<Integer>();
        for(int i=0; i<9; i++ ){
            if(mGameState[i] == EMPTY)
                vacantSqures.add(i);
        }
        Random rand = new Random();
        int inex = rand.nextInt(vacantSqures.size());
        int bestMove = vacantSqures.elementAt(inex);
        return bestMove;
    }
}

