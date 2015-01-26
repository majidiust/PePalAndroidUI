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
import org.ertebat.HostConfigActivity;
import org.ertebat.R;
import org.ertebat.schema.SessionStore;
import org.ertebat.schema.SettingSchema;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SignInActivity extends BaseActivity {
	private static int mCount = 0;
	
	private TextView mTextSignUp;
	private TextView mTextForgotPass;
	private EditText mEditUsername;
	private EditText mEditPassword;
	private Button mBtnSignIn;
	private View mViewHidden;
	
	private int mHiddenClickCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_in);
		
		getActionBar().hide();

		mTextSignUp = (TextView)findViewById(R.id.lblSignInNotMember);
		mTextSignUp.setTypeface(FontKoodak);
		mTextSignUp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(This, SignUpActivity.class);
				startActivity(intent);
			}
		});

		mTextForgotPass = (TextView)findViewById(R.id.lblSignInForgotPassword);
		mTextForgotPass.setTypeface(FontKoodak);
		mTextForgotPass.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(This, PasswordRecoveryActivity.class);
				startActivity(intent);
			}
		});

		mEditUsername = (EditText)findViewById(R.id.editSignInUsername);
		mEditUsername.setTypeface(FontNazanin);
		mEditPassword = (EditText)findViewById(R.id.editSignInPassword);
		mEditPassword.setTypeface(FontNazanin);

		mBtnSignIn = (Button)findViewById(R.id.btnSignInEnter);
		mBtnSignIn.setTypeface(FontKoodak);
		mBtnSignIn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mEditUsername.getText().toString().equals("") || mEditPassword.getText().toString().equals("")) {
					showAlert("لطفاً نام کاربری و رمز عبور خود را وارد نمائید");
					return;
				}
				
				showWaitingDialog("در حال اتصال به سرور", "لطفا منتظر بمانید...");

				new Thread(new Runnable() {

					@Override
					public void run() {
						HttpClient client = new DefaultHttpClient();
						Log.d(TAG, RestAPIAddress.getSignIn());
						HttpPost post = new HttpPost(RestAPIAddress.getSignIn());
							
						try {
							List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
							nameValuePairs.add(new BasicNameValuePair("username", mEditUsername.getText().toString()));
							nameValuePairs.add(new BasicNameValuePair("password", mEditPassword.getText().toString()));
							post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

							HttpResponse response = client.execute(post);
							if (response.getStatusLine().getStatusCode() == HttpStatus.SC_ACCEPTED) {
								BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
								String line = "";
								String jsonString = "";
								while ((line = rd.readLine()) != null) {
									jsonString += line;						
								}
								
								closeWaitingDialog();
								showToast("REST : " + (mCount++));
								
								JSONObject json = new JSONObject(jsonString);
								String token = json.optString("token", "");
								if (!token.equals("")) {
									//@1: save token
									mCurrentUserProfile.m_token = token;
									Log.d(TAG, "Sign In was successful. Token = " + mCurrentUserProfile.m_token);
									mCurrentUserProfile.m_userName = mEditUsername.getText().toString();
									//@2: Connect to web socket server
									mWebsocketService.connectToHost(mSettings.mWebSocketUrl);
									closeWaitingDialog();
								} else {
									Log.d("Http", line);
								}		
							} else {
								showAlert("ورود موفقیت آمیز نبود. لطفاً مجدداً تلاش نمائید");
								closeWaitingDialog();
							}

						} catch (Exception ex) {
							Log.d(TAG, ex.getMessage());
						}
					}
				}).start();
			}
		});

		mViewHidden = findViewById(R.id.viewSignInHidden);
		mViewHidden.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mHiddenClickCount > 2) {
					Intent intent = new Intent(This, HostConfigActivity.class);
					startActivity(intent);
					mHiddenClickCount = 0;
				} else {
					mHiddenClickCount++;
				}
			}
		});
		
		if(SessionStore.mSessionStore.mIsLoggedIn == true){
			finish();
			Intent intent = new Intent(This, MainActivity.class);
			startActivity(intent);
		}
	}
	
	@Override
	public void onConnectedToServer() {
		super.onConnectedToServer();
		SessionStore.mSessionStore.mIsLoggedIn = true;
		showToast("hello : " + (mCount++));
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				finish();
				Intent intent = new Intent(This, MainActivity.class);
				startActivity(intent);
			}
		});
	}
}
