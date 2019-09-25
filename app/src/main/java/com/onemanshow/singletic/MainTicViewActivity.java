package com.onemanshow.singletic;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.onemanshow.tictactoe.R;


public class MainTicViewActivity extends Activity{
    //members
    static final String GAME_LEVEL = "GameLevel";
    static final String ONE_PLAYER = "OnePlayerGame";
    static final String GAME_IN_PROGRESS = "CurrentMoveCount";
    static final int EASY = 100;
    static final int MEDIUM = 200;
    static final int HARD = 300;
    static final int MY_REQUEST_CODE = 52;
    public static final String SOUND_OK = "Soundok";
    public static final String SOUND_VW = "soundvw";
    private View ticView;
    private boolean b1Player;
    private RadioGroup radGrLevel;
    private CheckBox chkBox;
    private ImageView imgViewTurn;
    private ImageView imgCrossNought;
    private int GameLevel = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        b1Player = intent.getBooleanExtra(ONE_PLAYER, true);
        if(b1Player)
            GameLevel = intent.getShortExtra(GAME_LEVEL, (short)MEDIUM);

        setContentView(R.layout.activity_main_tic_view);
        ticView = (View)findViewById(R.id.TicView);
        ((CustomTicView)ticView).setGameObj(-2, GameLevel);//set Game obj with WhoseTurn = CROSS
        ((CustomTicView)ticView).bOnePlayer = b1Player;
        ((CustomTicView)ticView).CrossWins = (TextView)findViewById(R.id.cross_wins);
        ((CustomTicView)ticView).NullWins = (TextView)findViewById(R.id.null_wins);

        imgViewTurn = (ImageView)findViewById(R.id.imageTurnCap);
        ((CustomTicView)ticView).imgCrossNullTurn = imgCrossNought =
                (ImageView)findViewById(R.id.imageCrossNull);
        radGrLevel = (RadioGroup)findViewById(R.id.radio);
        chkBox = (CheckBox)findViewById(R.id.checkBox1);
        chkBox.setChecked(!b1Player);
        if(b1Player){
            imgViewTurn.setVisibility(View.INVISIBLE);//One player game
            imgCrossNought.setVisibility(View.INVISIBLE);
            radGrLevel.setVisibility(View.VISIBLE);

        }
        else{
            imgViewTurn.setVisibility(View.VISIBLE);
            imgCrossNought.setVisibility(View.VISIBLE);
            radGrLevel.setVisibility(View.INVISIBLE);//Two player game
        }
        if(b1Player){
            if (GameLevel == EASY)
                radGrLevel.check(R.id.first);
            else if(GameLevel == MEDIUM)
                radGrLevel.check(R.id.second);
            else if(GameLevel == HARD)
                radGrLevel.check(R.id.third);
        }
    }
    public void onCheckboxClicked(View view) {
        // Is the view checked?
        boolean checked = ((CheckBox) view).isChecked();
        CharSequence toastText = "Initial";
        boolean b1Player = true;
        switch(view.getId()) {
            case R.id.checkBox1:
                if (!checked){
                    b1Player = true;
                    toastText = "One player game selected";
                }
                else{
                    b1Player = false;
                    toastText = "Two player game selected";
                }
                break;
        }
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.toast_layout_root));
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(toastText);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
        Intent intent = getIntent();
        intent.putExtra(ONE_PLAYER, b1Player);
        finish();
        startActivity(intent);
    }
    public void onRadioButtonClicked(View view) {

        CharSequence toastText = "Initial";
        switch(view.getId()) {
            case R.id.first:
                GameLevel = EASY;
                toastText = "Game level Easy";
                break;
            case R.id.second:
                GameLevel = MEDIUM;
                toastText = "Game level Medium";
                break;
            case R.id.third:
                GameLevel = HARD;
                toastText = "Game level Hard";
                break;
        }
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast,
                (ViewGroup) findViewById(R.id.toast_layout_root));
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(toastText);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
        ((CustomTicView)ticView).resetGameObj(-2, GameLevel);//player(CROSS) moves first

    }
    @Override
    public void onSaveInstanceState(Bundle savedForOrienChng) {
        // Save the user's current game state
        if(b1Player){
            savedForOrienChng.putInt(GAME_LEVEL, GameLevel);
        }
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedForOrienChng);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedForOrienChng) {
        super.onRestoreInstanceState(savedForOrienChng); // Always call the superclass first
        if(!b1Player)
            return;
        GameLevel = savedForOrienChng.getInt(GAME_LEVEL);
        ((CustomTicView)ticView).restoreGameLevel(GameLevel);
        if (GameLevel == EASY)
            radGrLevel.check(R.id.first);
        else if(GameLevel == MEDIUM)
            radGrLevel.check(R.id.second);
        else if(GameLevel == HARD)
            radGrLevel.check(R.id.third);
    }

    public void onHelp(View view){

        Intent intent = new Intent(this, DialogActivity.class);
        intent.putExtra(SOUND_VW, ((CustomTicView)ticView).bSoundOn);
        this.startActivityForResult(intent, MY_REQUEST_CODE);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == MY_REQUEST_CODE){
            // Make sure the request was successful
            if (resultCode == RESULT_OK){
                ((CustomTicView)ticView).bSoundOn = data.getBooleanExtra(SOUND_OK, true);
            }
        }
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //cannot  catch a backbutton press in View; so have to do it in Activity
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.toast_layout_root));
            TextView text = (TextView) layout.findViewById(R.id.text);
            text.setText("Goodbye!");

            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);
            toast.show();
        }
        return super.onKeyDown(keyCode, event);
    }
}