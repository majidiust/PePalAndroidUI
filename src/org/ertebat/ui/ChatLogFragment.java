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
	private ListView mListViewChats;
	private List<ChatSummary> mChats;
	private ChatLogAdapter mAdapter;
	private Button mBtnNewChat;
	private Button mBtnSettings;
	private BaseActivity mBase;

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
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				if (arg2 != ListView.INVALID_POSITION) {
					Intent intent = new Intent(This, ChatActivity.class);
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

		loadSampleData();

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
}
