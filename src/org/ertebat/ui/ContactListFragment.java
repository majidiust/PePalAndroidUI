package org.ertebat.ui;
import java.util.ArrayList;
import java.util.List;

import org.ertebat.R;
import org.ertebat.schema.FriendSchema;

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
	private List<ContactSummary> mDataSet;
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
				if(mEditNewContact.getText().toString() == ""){
					showAlert("لطفا شماره تلفن کاربر مورد نظر را وارد نمایید.");
				}
				else{
					
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
				// TODO: @Majid, create a room and then start the ChatActivity				
			}
		});

		return rootView;
	}

	@Override
	public void onNewFriend(FriendSchema fs) {
		ContactSummary contact = new ContactSummary();
		contact.ContactPhone = fs.m_friendUserName;
		contact.ContactName = "";
		mDataSet.add(contact);
		mAdapter.notifyDataSetChanged();
	}
}
