package org.ertebat.ui;
import java.util.List;

import org.ertebat.R;
import org.ertebat.schema.SettingSchema;

import com.squareup.picasso.Picasso;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactListAdapter extends ArrayAdapter<ContactSummary> {
	private final String TAG = "ContactListAdapter";

	private Context mContext;
	private List<ContactSummary> mDataSet;
	private Typeface mFont;

	public ContactListAdapter(Context context, List<ContactSummary> objects) {
		super(context, R.id.txtContactListItemTitle, objects);

		mContext = context;
		mFont = Typeface.createFromAsset(mContext.getAssets(), "bnazaninbd_0.ttf");
		
		mDataSet = objects;
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.data_list_item_contact_list, null, true);
		
		String title = mDataSet.get(position).ContactPhone;
		String status = mDataSet.get(position).ContactName;
		String id     = mDataSet.get(position).ContactId;
//		String picAddress = mDataSet.get(position).get("picAddress");
		
		Log.d(TAG, title + ": " + status);
		
		TextView text = (TextView)itemView.findViewById(R.id.txtContactListItemTitle);
		text.setTypeface(mFont);
		text.setText(title);
		
		text = (TextView)itemView.findViewById(R.id.txtContactListItemStatus);
		text.setTypeface(mFont);
		text.setText(status);
		
		ImageView image = (ImageView)itemView.findViewById(R.id.imgContactListItemPicture);
		int width = (int) getContext().getResources().getDimension(org.ertebat.R.dimen.user_profile_thumbnail_size);
		Picasso.with(getContext()).load(SettingSchema.mBaseRestUrl + "uploaded/profiles/" + id + ".png").resize(width, width)
		  .centerInside().placeholder(org.ertebat.R.drawable.ic_default_user_picture).error(org.ertebat.R.drawable.ic_default_user_picture).into(image);
		
		return itemView;
	}
}
