package org.ertebat.ui;

import org.ertebat.R;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

public class UserProfileActivity extends BaseActivity {
	private EditText mEditFirstName;
	private EditText mEditSurname;
	private ImageView mImageThumbnail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_profile);
		
		getActionBar().hide();
		TAG = "UserProfile";

		mImageThumbnail = (ImageView)findViewById(R.id.imgUserProfileThumbnail);
		
		mEditFirstName = (EditText)findViewById(R.id.editUserProfileFirstName);
		mEditFirstName.setTypeface(FontNazanin);
		mEditSurname = (EditText)findViewById(R.id.editUserProfileSurname);
		mEditSurname.setTypeface(FontNazanin);
	}
}
