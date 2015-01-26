package org.ertebat.ui;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ertebat.R;
import org.ertebat.R.dimen;
import org.ertebat.R.drawable;
import org.ertebat.R.id;
import org.ertebat.R.layout;
import org.ertebat.schema.MessageSchema;
import org.ertebat.schema.RoomSchema;
import org.ertebat.schema.SessionStore;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class ChatActivity extends BaseActivity {
	private ListView mListViewMessages;
	private List<ChatMessage> mDataSet;
	private ChatMessageAdapter mAdapter;

	private Button mBtnSendMessage;
	private Button mBtnBarMore;
	private Button mBtnBarAddPicture;
	private Button mBtnBarAddContact;
	private EditText mEditMessageContent;
	private RelativeLayout mLayoutBarExtension;
	private String mRoomId = "";
	private String mOtherParty = "";

	private Object mAddMessageLock = new Object();
	private int maxWidth = 1280;
	private int maxHeight = 720;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		getActionBar().hide();
		TAG = "ChatActivity";

		String content, messageId, from, date, fromId;
		try{
			Bundle b = getIntent().getExtras();
			mRoomId = b.getString("roomId");
			mOtherParty = b.getString("otherParty");
			String origin = b.getString("origin");
			SessionStore.mSessionStore.mCurrentRoomId = mRoomId;

			if(origin.equals("notification")){
				content = b.getString("message");
				messageId = b.getString("messageId");
				from = b.getString("from");
				date = b.getString("date");
				fromId = b.getString("fromId");
				showToast(messageId + " : " + from + " : " + mRoomId);
				SessionStore.mSessionStore.addMessageToRoom(new MessageSchema(messageId, fromId, from, mRoomId, date, date, content));
			}
		}
		catch(Exception ex){
			Log.d(TAG, ex.getMessage());
		}
		////////////////////////////////////////ListView Setup	////////////////////////////////////////

		mListViewMessages = (ListView)findViewById(R.id.listViewChatMessages);

		mDataSet = new ArrayList<ChatMessage>();
		mAdapter = new ChatMessageAdapter(This,
				BitmapFactory.decodeResource(getResources(), R.drawable.ic_contact), mDataSet);
		mListViewMessages.setAdapter(mAdapter);

		// TODO: @Majid, this is for UI test. remember to remove it later
		//	loadSampleData();
		loadHistory();
		////////////////////////////////////////Bottom Bar Setup	////////////////////////////////////

		setupBottomBar();
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkMessages();
	}

	@Override
	protected void onMultiChoiceDialogResult(String dialogTitle,
			String callback, int selectedIndex) {
		try {
			Method method = ChatActivity.class.getDeclaredMethod(callback, int.class);
			method.invoke(this, selectedIndex);
		} catch (Exception ex) {
			Log.d(TAG, ex.getMessage());
		}
	}

	private void onPictureInsertionMethodSelected(int selectedIndex) {
		Log.d(TAG, "selected picture insertion method: " + selectedIndex);
		PictureInsertionMethod method = PictureInsertionMethod.values()[selectedIndex];

		if (method == PictureInsertionMethod.FromGallery) {
			Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(pickPhoto, DIALOG_PICTURE_GALLERY);
		} else {
			Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			startActivityForResult(takePicture, DIALOG_TAKE_PICTURE);
		}
	}

	private void onAdditionalContactSelected(int selectedIndex) {
		String selectedContactId = mContacts.get(selectedIndex).ContactPhone;

		// TODO: @Majid, do whatever you want to the ID
	}

	private void setupBottomBar() {
		mEditMessageContent = (EditText)findViewById(R.id.editChatBottomMessage);

		mBtnSendMessage = (Button)findViewById(R.id.btnChatBottomSend);
		mBtnSendMessage.setTypeface(FontKoodak);
		mBtnSendMessage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!mEditMessageContent.getText().toString().equals("")) {
					sendTextMessageToServer(mRoomId, "Now", "..", mEditMessageContent.getText().toString());
					mEditMessageContent.setText("");
				}
			}
		});

		mLayoutBarExtension = (RelativeLayout)findViewById(R.id.layoutChatBottomExtensionBar);

		mBtnBarMore = (Button)findViewById(R.id.btnChatBottomMore);
		mBtnBarMore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mLayoutBarExtension.getVisibility() != View.VISIBLE)
					mLayoutBarExtension.setVisibility(View.VISIBLE);
				else
					mLayoutBarExtension.setVisibility(View.GONE);
			}
		});

		mBtnBarAddPicture = (Button)findViewById(R.id.btnChatBottomAddPic);
		mBtnBarAddPicture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String[] choices = new String[] { PictureInsertionMethod.FromGallery.toString(),
						PictureInsertionMethod.FromCamera.toString() };
				showMultiChoiceDialog(choices, "onPictureInsertionMethodSelected", "نحوه انتخاب تصویر");
			}
		});

		mBtnBarAddContact = (Button)findViewById(R.id.btnChatBottomAddContact); 
		mBtnBarAddContact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {				
				showMultiChoiceDialog(getContactNames(), "onAdditionalContactSelected", "دعوت از دوستان");
			}
		});

		mLayoutBarExtension.setVisibility(View.GONE);
	}

	private void loadSampleData() {
		mDataSet.clear();

		ChatMessage message = new ChatMessage();
		message.IsSenderSelf = true;
		message.MessageText = "سلام آقا وحید!";
		message.ReceptionTime = "10:19";
		message.Type = ChatMessageType.Text;
		mDataSet.add(message);

		message = new ChatMessage();
		message.IsSenderSelf = true;
		message.MessageText = "حالتون خوبه؟ من دیروز سعی کردم تماس بگیرم گوشیتون خاموش بود";
		message.ReceptionTime = "10:19";
		message.Type = ChatMessageType.Text;
		mDataSet.add(message);

		message = new ChatMessage();
		message.IsSenderSelf = false;
		message.MessageText = "سلام! خیلی ممنون، شما خوبید انشالله؟ بله گوشیم مونده بود منزل یکی از دوستان، می بخشید";
		message.ReceptionTime = "10:20";
		message.Type = ChatMessageType.Text;
		mDataSet.add(message);

		mAdapter.notifyDataSetChanged();
	}

	protected void loadHistory(){
		try{
			RoomSchema room = SessionStore.mSessionStore.getRoomById(mRoomId);
			if(room != null){
				Vector<MessageSchema> messages = room.getAllMessages();
				for(int i = 0 ; i < messages.size() ; i++ ){
					onNewMessage(messages.get(i));
				}
			}
		}
		catch(Exception ex){
			logCatDebug((ex.getMessage()));
		}
	}
	protected void checkMessages() {
		loadHistory();
	}

	private void createPictureMessage(boolean self, Bitmap picture) {
		ChatMessage message = new ChatMessage();
		message.IsSenderSelf = true;
		message.MessagePicture = picture;
		Calendar calendar = GregorianCalendar.getInstance();
		String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
		if (hour.length() == 1)
			hour = "0" + hour;
		String minute = String.valueOf(calendar.get(Calendar.MINUTE));
		if (minute.length() == 1)
			minute = "0" + minute;
		message.ReceptionTime = hour + ":" + minute;
		message.Type = ChatMessageType.Picture;
		addMessage(message);
		mEditMessageContent.setText("");
	}

	private void addMessage(ChatMessage message) {
		mDataSet.add(message);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		SessionStore.mSessionStore.mCurrentRoomId = null;
		super.onDestroy();
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data == null)
			return;
		int width;
		switch (requestCode) {
		case DIALOG_PICTURE_GALLERY:
			final Uri selectedImage = data.getData();
			width = (int)getResources().getDimension(R.dimen.chat_message_item_picture_width);
			uploadImageToTheServer(selectedImage, mRoomId);
			break;
		case DIALOG_TAKE_PICTURE:
			Uri takenImage = data.getData();
			width = (int)getResources().getDimension(R.dimen.chat_message_item_picture_width);
			uploadImageToTheServer(takenImage,mRoomId);
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}		
	}

	private boolean findMessageInRoom(MessageSchema ms){
		for(ChatMessage cm : mDataSet){
			if(cm.MessageId.equals(ms.mId))
				return true;
		}
		return false;
	}
	@Override
	public void onNewMessage(final MessageSchema ms) {
		synchronized (mAddMessageLock) {
			if(ms.mTo.equals(mRoomId) && !findMessageInRoom(ms)){
				final ChatMessage message = new ChatMessage();
				if(ms.mType.equals("Text")){
					message.Type = ChatMessageType.Text;
				}
				else if(ms.mType.equals("Picture")){
					message.Type = ChatMessageType.Picture;
				}
				message.IsSenderSelf = false;
				message.MessageId = ms.mId;
				message.SenderID = ms.mFromId;
				message.SenderUserName = ms.mFromUserName;
				if(ms.mFromUserName.equals(mCurrentUserProfile.m_userName)){
					message.IsSenderSelf = true;
				}
				
				message.MessageText = ms.mBody;
				message.ReceptionTime = ms.mDate;
				mDataSet.add(message);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mAdapter.notifyDataSetChanged();
					}
				});
			}
		}
	}
}
