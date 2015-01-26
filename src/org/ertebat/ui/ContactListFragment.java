package org.ertebat.ui;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ertebat.R;
import org.ertebat.schema.FriendSchema;
import org.ertebat.schema.SessionStore;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

	protected static final String TAG = "ContactListFragment";
	private Button mBtnNewContact;
	private Button mBtnAddContact;
	private EditText mEditNewContact;
	private RelativeLayout mLayoutBottomBarExtension;

	private ListView mListViewContacts;
	private List<ContactSummary> mDataSet;
	private ContactListAdapter mAdapter;
	private Object mAddContactLock = new Object();

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
				if(mEditNewContact.getText().toString() == ""){
					showAlert("لطفا شماره تلفن کاربر مورد نظر را وارد نمایید.");
				}
				else{
					try{
						addFriend(mEditNewContact.getText().toString());
					}
					catch(Exception ex){
						Log.d(TAG, ex.getMessage());
					}
				}
			}
		});

		mListViewContacts = (ListView)rootView.findViewById(R.id.listViewContactList);

		mDataSet = new ArrayList<ContactSummary>();
		mAdapter = new ContactListAdapter(This, mDataSet);
		mListViewContacts.setAdapter(mAdapter);
		mListViewContacts.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				Intent intent = new Intent(This, ContactProfileActivity.class);
				Bundle b = new Bundle();
				b.putString("userId", mDataSet.get(position).ContactId);
				b.putString("phone", mDataSet.get(position).ContactPhone);
				b.putString("name", mDataSet.get(position).ContactName);
				intent.putExtras(b);
				startActivity(intent);
			}
		});

		loadLocal();
		
		return rootView;
	}

	protected void loadLocal(){
		for(int i = 0 ; i < SessionStore.mSessionStore.mFriendList.size() ; i++){
			onNewFriend(SessionStore.mSessionStore.mFriendList.get(i));
		}
	}
	protected  void addFriend(final String userName){
		try{
			new Thread(new Runnable() {
				@Override
				public void run() {
					HttpClient client = new DefaultHttpClient();
					HttpGet getProfileRest = new HttpGet(RestAPIAddress.getAddFriend() + "/" + userName);	
					getProfileRest.addHeader("token", BaseActivity.mCurrentUserProfile.m_token);
					try {
						HttpResponse response = client.execute(getProfileRest);
						if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
							BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
							String line = "";
							String jsonString = "";
							while ((line = rd.readLine()) != null) {
								jsonString += line;						
							}
							JSONObject json = new JSONObject(jsonString);
							JSONObject fri = new JSONObject(json.getString("friend"));
							String friendId = "--";
							String friendUsername = "--";
							String state = "--";
							String code = "--";
							try{
								friendId = fri.getString("friendId");
							}
							catch(Exception ex){
								Log.d(TAG, ex.getMessage());
							}
							try{
								state = fri.getString("state");
							}
							catch(Exception ex){
								Log.d(TAG, ex.getMessage());
							}
							try{
								friendUsername = fri.getString("friendUsername");
							}
							catch(Exception ex){
								Log.d(TAG, ex.getMessage());
							}
							try{
								code = json.getString("code");
							}
							catch(Exception ex){
								Log.d(TAG, ex.getMessage());
							}

							ShowToast(code);

							if(code.compareTo("-19") == 0){
								showAlert("کاربری بااین نام وجود ندارد");
							}
							else{
								//onNewFriend(new FriendSchema(friendId, friendUsername, state));
								mBaseActivity.getFriendList();
								mHandler.post(new Runnable() {
									@Override
									public void run() {
										mLayoutBottomBarExtension.setVisibility(View.GONE);
									}
								});
							}
						} 
					} catch (Exception ex) {
						Log.d(TAG, ex.getMessage());
					}
				}
			}).start();
		}
		catch(Exception ex){
			Log.d(TAG, ex.getMessage());
		}
	}

	@Override
	public void onNewFriend(final FriendSchema fs) {
		synchronized (mAddContactLock ) {
			if(!isExistContact(fs.m_friendUserName))
			{
				ContactSummary contact = new ContactSummary();
				contact.ContactPhone = fs.m_friendUserName;
				contact.ContactName = fs.m_friendUserName;
				contact.ContactId = fs.m_friendId;
				mDataSet.add(contact);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mAdapter.notifyDataSetChanged();
					}
				});
			}
		}
	}

	public boolean isExistContact(String username){
		for(ContactSummary cs : mDataSet){
			if(cs.ContactPhone.compareTo(username) == 0){
				return true;
			}
		}
		return false;
	}
}
