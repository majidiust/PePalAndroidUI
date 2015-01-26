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

public class ChatLogAdapter extends ArrayAdapter<ChatSummary> {

	private final String TAG = "ChatLogAdapter";

	private Context mContext;
	private List<ChatSummary> mChats;
	private Typeface mFont;

	public ChatLogAdapter(Context context, List<ChatSummary> chats) {
		super(context, R.id.txtChatLogListItemTitle, chats);

		mContext = context;
		mFont = Typeface.createFromAsset(mContext.getAssets(), "bnazaninbd_0.ttf");
		mChats = chats;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.data_list_item_chat_log, null, true);

		String date = mChats.get(position).Date;
		String time = mChats.get(position).Time;
		String title = mChats.get(position).Title;
		String summary = mChats.get(position).Summary;
		int count = mChats.get(position).NewMessageCount;

		Log.d(TAG, date + " " + time);

		TextView text = (TextView)itemView.findViewById(R.id.txtChatLogListItemDateTime);
		text.setTypeface(mFont);
		text.setText(date + "\n" + time);

		text = (TextView)itemView.findViewById(R.id.txtChatLogListItemTitle);
		text.setTypeface(mFont);
		text.setText(title);

		text = (TextView)itemView.findViewById(R.id.txtChatLogListItemContent);
		text.setTypeface(mFont);
		text.setText(summary);

		text = (TextView)itemView.findViewById(R.id.txtChatLogListItemCount);
		text.setTypeface(mFont);
		if (count > 0) {
			text.setText(String.valueOf(count));
		} else {
			text.setVisibility(View.GONE);
		}

		try{
			String logo = mChats.get(position).logo;
			ImageView image = (ImageView)itemView.findViewById(R.id.imgChatLogListItemPicture);
			int width = (int) getContext().getResources().getDimension(org.ertebat.R.dimen.user_profile_thumbnail_size);
			Picasso.with(getContext()).load(logo).resize(width, width)
			.centerInside().placeholder(org.ertebat.R.drawable.ic_chat_logo_default).error(org.ertebat.R.drawable.ic_chat_logo_default).into(image);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return itemView;
	}
}
