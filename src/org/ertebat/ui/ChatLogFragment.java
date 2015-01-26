package org.ertebat.ui;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ertebat.R;
import org.ertebat.R.id;
import org.ertebat.R.layout;
import org.ertebat.schema.MessageSchema;
import org.ertebat.schema.SessionStore;
import org.ertebat.schema.SettingSchema;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ChatLogFragment extends BaseFragment implements FragmentDialogResultListener {
	private static final String TAG = "ChatLogFragment";
	private ListView mListViewChats;
	private List<ChatSummary> mChats;
	private ChatLogAdapter mAdapter;
	private Button mBtnNewChat;
	private Button mBtnSettings;
	private BaseActivity mBase;
	private Object newRoomLock = new Object();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View rootView = inflater.inflate(R.layout.fragment_chat_log, container, false);

		LOG_TAG = "ChatLog";
		mBase = (BaseActivity)getActivity();
		mBase.setFragmentDialogResultListener(this);

		////////////////////////////////////////ListView Setup	////////////////////////////////////////

		mListViewChats = (ListView)rootView.findViewById(R.id.listViewChatLogChats);

		mChats = new ArrayList<ChatSummary>();
		mAdapter = new ChatLogAdapter(This, mChats);

		mListViewChats.setAdapter(mAdapter);
		mListViewChats.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int position, long id) {

				if (position != ListView.INVALID_POSITION) {
					setZeroMessageNotification(position);
					Intent intent = new Intent(This, ChatActivity.class);
					Bundle b = new Bundle();
					b.putString("roomId",  mChats.get(position).id);
					b.putString("otherParty",   mChats.get(position).id);
					b.putString("origin", "chatlog");
					intent.putExtras(b);
					startActivity(intent);
				}
			}
		});

		mBtnNewChat = (Button)rootView.findViewById(R.id.btnChatLogBottomNew);
		mBtnNewChat.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mBase.showFragmentMultiChoiceDialog(BaseActivity.getContactNames(), "onChatContactSelected", "شروع گفتگو با");
			}
		});

		mBtnSettings = (Button)rootView.findViewById(R.id.btnChatLogBottomSettings);
		mBtnSettings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(This, UserProfileActivity.class);
				startActivity(intent);
			}
		});

		loadRooms();

		return rootView;
	}

	private void loadSampleData() {
		mChats.clear();
		ChatSummary chat = new ChatSummary();
		chat.Date = "1393/03/27";
		chat.Time = "16:01";
		chat.Title = "وحید ابراهیمی";
		chat.Summary = "سلام! پیام آزمایشی ...";
		mChats.add(chat);
		chat = new ChatSummary();
		chat.Date = "1393/03/29";
		chat.Time = "16:34";
		chat.Title = "وحید ابراهیمی";
		chat.Summary = "سلام! پیام آزمایشی ...";
		mChats.add(chat);
		mAdapter.notifyDataSetChanged();
	}

	private void onChatContactSelected(int selectedIndex) {
		// TODO: @Majid, start a chat with the selected contact here
	}

	public void increaseMessageNotifications(int index) {
		mChats.get(index).NewMessageCount++;
		mAdapter.notifyDataSetChanged();
	}

	public void increaseMessageNotifications(int index, int count) {
		mChats.get(index).NewMessageCount += count;
		mAdapter.notifyDataSetChanged();
	}

	public void setZeroMessageNotification(int index) {
		mChats.get(index).NewMessageCount = 0;
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onMultichoiceDialogResult(String dialogTitle, String callback,
			int selectedIndex) {
		try {
			Method method = ChatLogFragment.class.getDeclaredMethod(callback, String.class);
			method.invoke(this, selectedIndex);
		} catch (Exception ex) {
			if (ex.getMessage() != null)
				Log.d(LOG_TAG, ex.getMessage());
			else
				Log.d(LOG_TAG, "Exception in ChatLog with no message!");
		}
	}

	@Override
	public void onNewMessage(MessageSchema ms) {
		try{
			if(SessionStore.mSessionStore.mCurrentRoomId == null || SessionStore.mSessionStore.mCurrentRoomId.compareTo(ms.mTo) != 0){
				final int location = findRoomLocationInList(ms.mTo) ;
				if(location >= 0){
					mHandler.post(new Runnable() {

						@Override
						public void run() {
							increaseMessageNotifications(location);
						}
					});

				}
			}
		}
		catch(Exception ex){
			Log.d(TAG, ex.getMessage());
		}
	}

	private int findRoomLocationInList(String roomId){
		int result = -1;
		for(int i = 0 ; i < mChats.size() ; i++){
			if(mChats.get(i).id.compareTo(roomId) == 0)
				return i;
		}
		return result;
	}

	private void loadRooms(){
		mChats.clear();
		for(int ii = 0 ; ii < SessionStore.mSessionStore.mRooms.size() ; ii++){
			final String roomId = SessionStore.mSessionStore.mRooms.get(ii).mId;
			final String roomType = SessionStore.mSessionStore.mRooms.get(ii).mType;
			final String members = SessionStore.mSessionStore.mRooms.get(ii).serializeMembers();
			onRoomAdded(SessionStore.mSessionStore.mRooms.get(ii).mName, 
					roomId,
					roomType,
					SessionStore.mSessionStore.mRooms.get(ii).mLogo,
					SessionStore.mSessionStore.mRooms.get(ii).mType,
					members);
		}
	}

	private boolean isExistRoom(String roomId){
		for(ChatSummary cs : mChats){
			if(cs.id.compareTo(roomId) == 0){
				return true;
			}
		}
		return false;
	}
	@Override
	public void onRoomAdded( final String roomName,  final String roomId, String roomDesc,
			String roomLogo, final String roomType, final String members) {
		synchronized (newRoomLock) {
			if(isExistRoom(roomId) == false){
				ChatSummary chat = new ChatSummary();
				chat.Date = "1393/03/29";
				chat.Time = "16:34";
				chat.Summary ="";
				chat.Title = roomId;
				chat.id = roomId;
				try{
					if(roomType.compareTo("I") == 0){
						chat.Type = "I";
						chat.Summary += "شرکت کنندگان در جلسه : ";
						String[] sMember = members.split(",");
						for(int i = 0 ; i < sMember.length ; i++){
							if(sMember[i].compareTo(BaseActivity.mCurrentUserProfile.m_uuid) != 0){
								chat.Title = SessionStore.mSessionStore.getUsernameById(sMember[i]);
								chat.Summary += chat.Title;
								chat.logo = SettingSchema.mBaseRestUrl + "uploaded/profiles/" + sMember[i] + ".png";
							}
							else{
								chat.Summary += BaseActivity.mCurrentUserProfile.m_userName;
							}
							if(i != sMember.length -1){
								chat.Summary += " -- ";
							}
						}
					}
					else{
						chat.Type = "G";
					}
				}
				catch(Exception ex){
				}
				mChats.add(chat);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mAdapter.notifyDataSetChanged();
					}
				});
			}
		};
	}
}
