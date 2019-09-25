package com.onemanshow.tictactoe;

import com.onemanshow.bttic.Constants;
import com.onemanshow.singletic.MainTicViewActivity;
import com.onemanshow.bttic.BTMainActivity;
import com.onemanshow.wftic.WFMainActivity;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class EntryPointActivity extends ListActivity
{
	static class Sample {
        private String mTitle;
        private Class<? extends Activity> mActivityClass;

        private Sample(String title, Typeface tpeF, Class<? extends Activity> activityClass){
            mTitle = title;
            mActivityClass = activityClass;
            tpFace = tpeF;
        }
        static Typeface tpFace;
        
        @Override
        public String toString() {
            return mTitle;
        }

        Class<? extends Activity> getActivityClass() {
            return mActivityClass;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
     // Hide the Title bar of this activity screen
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        int resId;
        switch(screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
               resId = R.layout.main_entry_large;
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
            	resId = R.layout.main_entry_norm;
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
            	resId = R.layout.main_entry_norm;
                break;
            default:
            	resId = R.layout.main_entry_norm;
        }
        setContentView(resId);
        TextView myTextTitle = (TextView)findViewById(R.id.tvBox);
	    Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/planetbe.ttf");
	    myTextTitle.setTypeface(myTypeface);
	    
        Sample[] samples = {
                new Sample("Single Device Game", myTypeface, MainTicViewActivity.class),
                new Sample("Game via Bluetooth", myTypeface, BTMainActivity.class),
                new Sample("Game via WiFi", myTypeface, WFMainActivity.class),
            };
   
            setListAdapter(
                    new MyCustomAdapter(this, R.layout.my_list_item, samples));
    }
    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id){
    	switch (position){
    	case 0:
            Sample sample0 = (Sample) listView.getItemAtPosition(position);
            Intent intent0 = new Intent(this.getApplicationContext(), sample0.getActivityClass());
            startActivity(intent0);
    		break;
    	case 1:
    	//check BT's availability early
    	BluetoothAdapter	btAdapter	= BluetoothAdapter.getDefaultAdapter();	
    	if (btAdapter == null){
    		Toast.makeText(getApplicationContext(), "Bluetooth is NOT supported",
					Toast.LENGTH_SHORT).show(); 
    				break;
    	}
    	//enforce BT enabled
    	else if(!btAdapter.isEnabled()){
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, Constants.MY_REQUEST_CODE_ENABLE_BT);
			break;
		}
        Sample sample1 = (Sample) listView.getItemAtPosition(position);
        Intent intent1 = new Intent(this.getApplicationContext(), sample1.getActivityClass());
        startActivity(intent1);
    
    	break;
    	case 2:
            //check if WIFI is on
            ConnectivityManager conman = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo result_wifi = conman.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if(!result_wifi.isConnected()){
                Toast.makeText(getApplicationContext(), "No WiFi connection detected.",
                        Toast.LENGTH_SHORT).show();
                break;
            }
            Sample sample3 = (Sample) listView.getItemAtPosition(position);
            Intent intent3 = new Intent(this.getApplicationContext(), sample3.getActivityClass());
            startActivity(intent3);
            break;

    	}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == Constants.MY_REQUEST_CODE_ENABLE_BT){
			 // Make sure the request was successful 
			if (resultCode != RESULT_OK){
	    	    Toast.makeText(getApplicationContext(), "Bluetooth MUST be enabled.",
						Toast.LENGTH_SHORT).show(); 
			}
		}
	}
}
