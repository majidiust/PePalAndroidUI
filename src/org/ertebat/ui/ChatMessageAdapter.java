package org.ertebat.ui;
import java.util.List;

import com.example.avcalltablet.R;
import com.example.avcalltablet.R.id;
import com.example.avcalltablet.R.layout;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {
	
	private final String TAG = "ChatMessageAdapter";
	
	private Context mContext;
	private Bitmap mContactPicture;
	private List<ChatMessage> mDataSet;

	public ChatMessageAdapter(Context context, Bitmap contactPic, List<ChatMessage> objects) {
		super(context, R.id.txtChatMessageListItemContent, objects);
		
		mContext = context;
		mContactPicture = contactPic;
		mDataSet = objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ChatMessage message = mDataSet.get(position);
		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = null;
		TextView text;
		
		if (message.Type == ChatMessageType.Text) {
			itemView = inflater.inflate(message.IsSenderSelf ?
					R.layout.data_list_item_chat_message_right : R.layout.data_list_item_chat_message_left, null, true);
			
			text = (TextView)(itemView.findViewById(R.id.txtChatMessageListItemContent));
			text.setTypeface(BaseActivity.FontRoya);
			text.setText(message.MessageText);		
			
		} else if (message.Type == ChatMessageType.Picture) {
			itemView = inflater.inflate(message.IsSenderSelf ?
					R.layout.data_list_item_chat_message_picture_right : R.layout.data_list_item_chat_message_picture_left, null, true);
			
			ImageView image = (ImageView)itemView.findViewById(R.id.imgChatMessageListItemContent);
//			image.setImageURI(message.MessagePicture);
			image.setImageBitmap(message.MessagePicture);
		}
		
		text = (TextView)(itemView.findViewById(R.id.txtChatMessageListItemDateTime));
		text.setText(message.ReceptionTime);
		
		if (!message.IsSenderSelf) {
			ImageView image = (ImageView)(itemView.findViewById(R.id.imgChatMessageListItemPicture));
			image.setImageBitmap(mContactPicture);
		}
		
		return itemView;
	}
}