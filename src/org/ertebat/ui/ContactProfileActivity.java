package org.ertebat.ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.ertebat.R;
import org.ertebat.schema.RoomSchema;
import org.ertebat.schema.SessionStore;
import org.ertebat.schema.SettingSchema;
import org.json.JSONObject;

import com.squareup.picasso.Picasso;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactProfileActivity extends BaseActivity {
	private TextView mTextName;
	private TextView mTextPhone;
	private Button mBtnChat;
	private Button mBtnAudioCall;
	private Button mBtnVideoCall;
	private ImageView mProfileImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_profile);

		getActionBar().hide();
		TAG = "ContactProfile";
		String userId = mCurrentUserProfile.m_uuid;
		String phone = userId;
		String name = userId;
		try{
			Bundle b = getIntent().getExtras();
			userId = b.getString("userId");
			phone = b.getString("phone");
			name = b.getString("name");
		}
		catch(Exception ex){
			Log.d(TAG, ex.getMessage());
		}
		final String fUserId = userId;

		mTextName = (TextView)findViewById(R.id.txtContactProfileName);
		mTextName.setTypeface(FontKoodak);
		mTextPhone = (TextView)findViewById(R.id.txtContactProfilePhone);
		mTextPhone.setTypeface(FontKoodak);

		mBtnChat = (Button)findViewById(R.id.btnContactChat);
		mBtnChat.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				CreateIndividualRoom(fUserId);
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

		mTextName.setText(name);
		mTextPhone.setText(phone);
		getUserProfile(userId);
		
		mProfileImage = (ImageView)  findViewById(R.id.imgContactProfileThumbnail);
		int width = (int) This.getResources().getDimension(org.ertebat.R.dimen.user_profile_thumbnail_size);
		Picasso.with(This).load(SettingSchema.mBaseRestUrl + "uploaded/profiles/" + userId + ".png").resize(width, width)
		  .centerInside().placeholder(org.ertebat.R.drawable.ic_default_user_picture).error(org.ertebat.R.drawable.ic_default_user_picture).into(mProfileImage);
	}

	@Override
	public void onUserProfile(final String firstName, final String lastName, String uid,
			final String userName, String picUrl, String email) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				mTextName.setText(firstName + " " + lastName);
				mTextPhone.setText(userName);
				//TODO: Get the picture of the user
			}
		});
	}

	public void CreateIndividualRoom(final String otherParty){
		try{
			showWaitingDialog("توجه", "لطفا منتظر بمانید، درحال دریافت اطلاعات از سرور ...");
			new Thread(new Runnable() {
				@Override
				public void run() {
					HttpClient client = new DefaultHttpClient();
					Log.d(TAG, RestAPIAddress.getSignIn());
					HttpPost post = new HttpPost(RestAPIAddress.getCreateIndividualRoom());

					try {
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
						nameValuePairs.add(new BasicNameValuePair("otherParty", otherParty));
						post.setHeader("token", mCurrentUserProfile.m_token);
						post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
						HttpResponse response = client.execute(post);
						if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
							BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
							String line = "";
							String jsonString = "";
							while ((line = rd.readLine()) != null) {
								jsonString += line;						
							}
							
							closeWaitingDialog();

							JSONObject json = new JSONObject(jsonString);
							String code = "-1";
							try{
								code = json.getString("code");
							}
							catch(Exception ex){
								logCatDebug(ex.getMessage());
							}

							if(Integer.parseInt(code) == 2 || Integer.parseInt(code) == 3){
								showToast("اتاق گفتگو ایجاد گردید");
								String roomId = "-1";
								try{
									roomId = json.getString("roomId");
								}
								catch(Exception ex){
									logCatDebug(ex.getMessage());
								}
								final String tmpRoomId = roomId;
								SessionStore.mSessionStore.addRoom(new RoomSchema(roomId, "room", "--", "--", "I"));
								mHandler.post(new Runnable() {
									@Override
									public void run() {
										Intent intent = new Intent(This, ChatActivity.class);
										Bundle b = new Bundle();
										b.putString("roomId", tmpRoomId);
										b.putString("otherParty", otherParty);
										b.putString("origin", "contact");
										intent.putExtras(b);
										startActivity(intent);
									}
								});
							}
						} else {
							showToast("قادر به ایجاد اتاق گفتگو نمی باشیم.");
						}
					} catch (Exception ex) {
						Log.d(TAG, ex.getMessage());
					}
				}
			}).start();
		}
		catch(Exception ex){
			logCatDebug(ex.getMessage());
		}
	}
}
