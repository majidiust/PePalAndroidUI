package org.ertebat.ui;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.avcalltablet.R;
import com.example.avcalltablet.R.id;
import com.example.avcalltablet.R.layout;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class ContactListFragment extends BaseFragment {

	private Button mBtnNewContact;
	private Button mBtnAddContact;
	private EditText mEditNewContact;
	private RelativeLayout mLayoutBottomBarExtension;

	private ListView mListViewContacts;
	private List<Map<String, String>> mDataSet;
	private ContactListAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		LOG_TAG = "ContactListFragment";

		View rootView = inflater.inflate(R.layout.fragment_contact_list, container, false);

		mLayoutBottomBarExtension = (RelativeLayout)rootView.findViewById(R.id.layoutContactListBottomBarExtension);

		mEditNewContact = (EditText)rootView.findViewById(R.id.editNewContact);
		mEditNewContact.setTypeface(BaseActivity.FontNazanin);

		mBtnNewContact = (Button)rootView.findViewById(R.id.btnBarBottomNewContact);
		mBtnNewContact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mLayoutBottomBarExtension.getVisibility() == View.VISIBLE)
					mLayoutBottomBarExtension.setVisibility(View.GONE);
				else
					mLayoutBottomBarExtension.setVisibility(View.VISIBLE);
			}
		});

		mBtnAddContact = (Button)rootView.findViewById(R.id.btnContactListAdd);
		mBtnAddContact.setTypeface(BaseActivity.FontNazanin);
		mBtnAddContact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO: @Majid, this is for UI test. You should instead add the person to contact list
				Intent intent = new Intent(This, ChatActivity.class);
				startActivity(intent);
			}
		});

		mListViewContacts = (ListView)rootView.findViewById(R.id.listViewContactList);

		mDataSet = new ArrayList<Map<String,String>>();
		mAdapter = new ContactListAdapter(This, mDataSet);
		mListViewContacts.setAdapter(mAdapter);
		mListViewContacts.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				// TODO: @Majid, create a room and then start the ChatActivity				
			}
		});

//		loadSampleData();

		new Thread(new Runnable() {

			@Override
			public void run() {
				loadData();
			}
		}).start();

		return rootView;
	}	

	private void loadData() {
		// TODO: @Majid, get list of friends

		//		HttpClient client = new DefaultHttpClient();
		//		HttpGet get = new HttpGet("http://13x17.org/api/userList");
		//
		//		try {
		//			get.addHeader("token", mConnectionToken);
		//			HttpResponse response = client.execute(get);
		//
		//			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
		//				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		//				String jsonString = "";
		//				String line = "";
		//				while ((line = rd.readLine()) != null) {
		//					jsonString += line;							
		//				}
		//
		//				Log.d(TAG, jsonString);
		//
		//				mDataSet.clear();
		//
		//				JSONArray array = new JSONArray(jsonString);
		//				for (int i = 0; i < array.length(); i++) {
		//					JSONObject json = array.getJSONObject(i);
		//
		//					Map<String, String> map = new HashMap<String, String>();
		//					map.put("id", json.getString("id"));
		//					map.put("title", json.optString("username", ""));
		//					map.put("status", "");
		//					map.put("picAddress", "");
		//					mDataSet.add(map);
		//				}
		//
		//				mHandler.post(new Runnable() {
		//
		//					@Override
		//					public void run() {
		//						mAdapter.notifyDataSetChanged();
		//					}
		//				});				
		//			} else {
		//				Log.d(TAG, "Failed: " + response.getStatusLine().getReasonPhrase());
		//			}
		//		} catch (Exception ex) {
		//			Log.d(TAG, ex.getMessage());
		//		}
	}
}
