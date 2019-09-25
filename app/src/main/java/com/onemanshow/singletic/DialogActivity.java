package com.onemanshow.singletic;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;

import com.onemanshow.tictactoe.R;

public class DialogActivity extends Activity {
    public static final String SOUND = "sound";
    boolean bSoundCheck;
    CheckBox chkBox;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove the default window/layout white background/frame
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setFinishOnTouchOutside(false);
        setContentView(R.layout.dialog_one);
        Intent intent = getIntent();
        bSoundCheck = intent.getBooleanExtra(MainTicViewActivity.SOUND_VW, true);
        chkBox = (CheckBox)findViewById(R.id.checkBox1);
        chkBox.setChecked(bSoundCheck);
    }
    public void endDilog(View v){
        Intent intent = new Intent();
        intent.putExtra(MainTicViewActivity.SOUND_OK, bSoundCheck);
        setResult(RESULT_OK, intent);
        this.finish();
    }
    public void onSoundCheckboxClicked(View v){
        if(((CheckBox)v).isChecked())
            bSoundCheck = true;
        else
            bSoundCheck = false;
    }
    public void onSaveInstanceState(Bundle savedForOrienChng) {
        // Save the user's current game state
        savedForOrienChng.putBoolean(SOUND, bSoundCheck);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedForOrienChng);
    }
    protected void onRestoreInstanceState(Bundle savedForOrienChng) {
        super.onRestoreInstanceState(savedForOrienChng); // Always call the superclass first
        bSoundCheck = savedForOrienChng.getBoolean(SOUND);
        chkBox.setChecked(bSoundCheck);
    }
}

