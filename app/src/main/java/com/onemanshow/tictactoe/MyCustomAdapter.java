package com.onemanshow.tictactoe;

import com.onemanshow.tictactoe.EntryPointActivity.Sample;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MyCustomAdapter extends ArrayAdapter<EntryPointActivity.Sample>{
	
	Context contx;
	Sample[] local;
	public MyCustomAdapter(Context context, int LayoutResourceId, Sample[] dat){
		super(context, LayoutResourceId, dat);
		contx = context;
		local = dat;
	}
	@Override
    public View getView(int position, View convertView, ViewGroup parent){
		
		View vRow = convertView;
		RowDataHolder holder = null;
		
		if(vRow == null){
        LayoutInflater inflater = (LayoutInflater) contx
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		vRow = inflater.inflate(R.layout.my_list_item, parent, false);
		holder = new RowDataHolder();
        holder.text = (TextView) vRow.findViewById(R.id.my_row);
     // Customization to the textView
        holder.text.setTypeface(EntryPointActivity.Sample.tpFace);
        holder.text.setTextSize(20);
        vRow.setTag(holder);
		}
		else{
			holder = (RowDataHolder)vRow.getTag();
		}
		EntryPointActivity.Sample onesamp = local[position];
		holder.text.setText(onesamp.toString());
 
        return vRow;
    
	}
	static class RowDataHolder{
		TextView text;
	}
}