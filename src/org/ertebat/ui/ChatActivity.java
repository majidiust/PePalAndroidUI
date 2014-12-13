package org.ertebat.ui;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.example.avcalltablet.R;
import com.example.avcalltablet.R.dimen;
import com.example.avcalltablet.R.drawable;
import com.example.avcalltablet.R.id;
import com.example.avcalltablet.R.layout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		getActionBar().hide();
		TAG = "ChatActivity";

		////////////////////////////////////////ListView Setup	////////////////////////////////////////

		mListViewMessages = (ListView)findViewById(R.id.listViewChatMessages);

		mDataSet = new ArrayList<ChatMessage>();
		mAdapter = new ChatMessageAdapter(This,
				BitmapFactory.decodeResource(getResources(), R.drawable.ic_contact), mDataSet);
		mListViewMessages.setAdapter(mAdapter);

		// TODO: @Majid, this is for UI test. remember to remove it later
		loadSampleData();

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
					// TODO: @Majid, send the message via web socket here
					showAlert("Hello!");
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

	protected void checkMessages() {
		// TODO: @Majid, check for messages here
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data == null)
			return;

		int width;
		switch (requestCode) {
		case DIALOG_PICTURE_GALLERY:
			Uri selectedImage = data.getData();
			width = (int)getResources().getDimension(R.dimen.chat_message_item_picture_width);
			createPictureMessage(true, Utilities.getPictureThumbnail(This, selectedImage, width));
			//			showAlert("Result", selectedImage.getPath());
			break;
		case DIALOG_TAKE_PICTURE:
			Uri takenImage = data.getData();
			width = (int)getResources().getDimension(R.dimen.chat_message_item_picture_width);
			createPictureMessage(true, Utilities.getPictureThumbnail(This, takenImage, width));
			//			showAlert("Result", takenImage.getPath());
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}		
	}
}
