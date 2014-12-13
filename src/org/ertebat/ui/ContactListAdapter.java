package org.ertebat.ui;
import java.util.List;
import java.util.Map;

import org.ertebat.R;
import org.ertebat.R.id;
import org.ertebat.R.layout;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ContactListAdapter extends ArrayAdapter<Map<String, String>> {
	private final String TAG = "ContactListAdapter";

	private Context mContext;
	private List<Map<String, String>> mDataSet;
	private Typeface mFont;

	public ContactListAdapter(Context context, List<Map<String, String>> objects) {
		super(context, R.id.txtContactListItemTitle, objects);

		mContext = context;
		mFont = Typeface.createFromAsset(mContext.getAssets(), "bnazaninbd_0.ttf");
		
		mDataSet = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.data_list_item_contact_list, null, true);
		
		String title = mDataSet.get(position).get("title");
		String status = mDataSet.get(position).get("status");
//		String picAddress = mDataSet.get(position).get("picAddress");
		
		Log.d(TAG, title + ": " + status);
		
		TextView text = (TextView)itemView.findViewById(R.id.txtContactListItemTitle);
		text.setTypeface(mFont);
		text.setText(title);
		
		text = (TextView)itemView.findViewById(R.id.txtContactListItemStatus);
		text.setTypeface(mFont);
		text.setText(status);
		
		//	TODO: Put a proper picture on the ImageView
		
		return itemView;
	}
}
