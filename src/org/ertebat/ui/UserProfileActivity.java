package org.ertebat.ui;

import org.ertebat.R;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class UserProfileActivity extends BaseActivity {
	private static final int PICK_FROM_GALLERY = 1;
	private TextView mTextPhoneNumber;
	private EditText mEditFirstName;
	private EditText mEditSurname;
	private EditText mEditEmail;
	private ImageView mImageThumbnail;
	private Button mBtnSubmit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_profile);
		
		getActionBar().hide();
		TAG = "UserProfile";

		mImageThumbnail = (ImageView)findViewById(R.id.imgUserProfileThumbnail);
		mImageThumbnail.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
                // call android default gallery
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // ******** code for crop image
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 0);
                intent.putExtra("aspectY", 0);
                intent.putExtra("outputX", 200);
                intent.putExtra("outputY", 200);

                try {
                    intent.putExtra("return-data", true);
                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_GALLERY);
                } catch (ActivityNotFoundException e) {
                    // Do nothing for now
                }
			}
		});
		
		mTextPhoneNumber = (TextView)findViewById(R.id.txtUserProfilePhoneNumber);
		mTextPhoneNumber.setTypeface(FontKoodak);
		
		mEditFirstName = (EditText)findViewById(R.id.editUserProfileFirstName);
		mEditFirstName.setTypeface(FontNazanin);
		mEditSurname = (EditText)findViewById(R.id.editUserProfileSurname);
		mEditSurname.setTypeface(FontNazanin);
		mEditEmail = (EditText)findViewById(R.id.editUserProfileEmail);
		mEditEmail.setTypeface(FontNazanin);
		
		String email = mCurrentUserProfile.m_email != null ? mCurrentUserProfile.m_email : "----";
		String firstName = mCurrentUserProfile.m_firstName != null ? mCurrentUserProfile.m_firstName : "----";;
		String lastName = mCurrentUserProfile.m_lastName != null ? mCurrentUserProfile.m_lastName : "----";;
		String userName = mCurrentUserProfile.m_userName != null ? mCurrentUserProfile.m_userName : "----";;
		
		mEditEmail.setText(email);
		mEditFirstName.setText(firstName);
		mEditSurname.setText(lastName);
		mTextPhoneNumber.setText(userName);
		
		mBtnSubmit = (Button)findViewById(R.id.btnUserProfileSubmit);
		mBtnSubmit.setTypeface(FontKoodak);
		mBtnSubmit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String firstName = mEditFirstName.getText().toString();
				String lastName = mEditSurname.getText().toString();
				String email = mEditEmail.getText().toString();
				saveProfile(mCurrentUserProfile.m_uuid, firstName, lastName, email);
			}
		});
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        Bundle extras;

        switch (requestCode) {
        case PICK_FROM_GALLERY:
            extras = data.getExtras();
            if (extras != null) {
                Bitmap photo = extras.getParcelable("data");
                mImageThumbnail.setImageBitmap(photo);
            }
            break;
        default:
            break;
        }
    }
}
