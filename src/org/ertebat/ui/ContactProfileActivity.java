package org.ertebat.ui;

import org.ertebat.R;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ContactProfileActivity extends BaseActivity {
	private TextView mTextName;
	private TextView mTextPhone;
	private Button mBtnChat;
	private Button mBtnAudioCall;
	private Button mBtnVideoCall;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_profile);
		
		getActionBar().hide();
		TAG = "ContactProfile";
		
		mTextName = (TextView)findViewById(R.id.txtContactProfileName);
		mTextName.setTypeface(FontKoodak);
		mTextPhone = (TextView)findViewById(R.id.txtContactProfilePhone);
		mTextPhone.setTypeface(FontKoodak);
		
		mBtnChat = (Button)findViewById(R.id.btnContactChat);
		mBtnChat.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		mBtnAudioCall = (Button)findViewById(R.id.btnContactVoiceCall);
		mBtnAudioCall.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		mBtnVideoCall = (Button)findViewById(R.id.btnContactVideoCall);
		mBtnVideoCall.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
	}
}
