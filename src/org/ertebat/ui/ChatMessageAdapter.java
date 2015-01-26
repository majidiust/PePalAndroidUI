package org.ertebat.ui;
import java.io.IOException;
import java.util.List;

import org.ertebat.R;
import org.ertebat.R.id;
import org.ertebat.R.layout;
import org.ertebat.schema.SettingSchema;

import com.hipmob.gifanimationdrawable.GifAnimationDrawable;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
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
			GifAnimationDrawable dr = null;
			itemView = inflater.inflate(message.IsSenderSelf ?
					R.layout.data_list_item_chat_message_picture_right : R.layout.data_list_item_chat_message_picture_left, null, true);
			ImageView image = (ImageView)itemView.findViewById(R.id.imgChatMessageListItemContent);
			try {
				dr = (new GifAnimationDrawable(getContext().getResources().openRawResource(R.drawable.ajax_loader)));
				dr.setVisible(true, true);
				int width = (int) getContext().getResources().getDimension(R.dimen.chat_message_item_picture_width);
				Picasso.with(getContext()).load(SettingSchema.mBaseRestUrl + "uploaded/entities/" + message.MessageText).resize(width, width)
				  .centerInside().placeholder(dr).into(image);
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		text = (TextView)(itemView.findViewById(R.id.txtChatMessageListItemDateTime));
		text.setText(message.ReceptionTime);
		
		if (!message.IsSenderSelf) {
			ImageView image = (ImageView)(itemView.findViewById(R.id.imgChatMessageListItemPicture));
			image.setImageBitmap(mContactPicture);
			try{
				ImageView senderImage = (ImageView)itemView.findViewById(R.id.imgChatMessageListItemPicture);
				int width = (int) getContext().getResources().getDimension(org.ertebat.R.dimen.chat_message_item_thumb_size);
				Picasso.with(getContext()).load(SettingSchema.mBaseRestUrl + "uploaded/profiles/" + message.SenderID + ".png").resize(width, width)
				  .centerInside().placeholder(org.ertebat.R.drawable.ic_default_user_picture).error(org.ertebat.R.drawable.ic_default_user_picture).into(image);

			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			
		}
		
		
		
		return itemView;
	}
}