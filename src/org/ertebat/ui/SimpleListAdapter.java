package org.ertebat.ui;
import java.util.Arrays;

import org.ertebat.R;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SimpleListAdapter extends ArrayAdapter<String> {
	
	private Context mContext;
	private Typeface mFont;
	private String[] mChoices;
	private View mViewItem;
	private TextView mTextItem;

	public SimpleListAdapter(Context context, String[] objects) {
		super(context, R.id.txtDefaultListItem, objects);
		
		mContext = context;
		mFont = Typeface.createFromAsset(mContext.getAssets(), "bnazaninbd_0.ttf");
		mChoices = Arrays.copyOf(objects, objects.length);
		
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mViewItem = inflater.inflate(R.layout.data_list_item_default, null, true);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mViewItem = inflater.inflate(R.layout.data_list_item_default, null, true);
		} else {
			mViewItem = convertView;
		}
		
		mTextItem = (TextView)mViewItem.findViewById(R.id.txtDefaultListItem);
		mTextItem.setTypeface(mFont);
		mTextItem.setText(mChoices[position]);
		
		return mViewItem;
	}
}
