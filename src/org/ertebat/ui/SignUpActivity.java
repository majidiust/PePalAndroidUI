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
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SignUpActivity extends BaseActivity {
	private EditText mEditPhoneNumber;
	private EditText mEditPassword;
	private EditText mEditPasswordConfirm;
	private EditText mEditEmail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);

		getActionBar().hide();

		mEditPhoneNumber = (EditText)findViewById(R.id.editSignUpPhoneNumber);
		mEditPhoneNumber.setTypeface(FontNazanin);
		mEditPassword = (EditText)findViewById(R.id.editSignUpPassword);
		mEditPassword.setTypeface(FontNazanin);
		mEditPasswordConfirm = (EditText)findViewById(R.id.editSignUpPasswordConfirm);
		mEditPasswordConfirm.setTypeface(FontNazanin);
		mEditEmail = (EditText)findViewById(R.id.editSignUpEmail);
		mEditEmail.setTypeface(FontNazanin);

		Button btn = (Button)findViewById(R.id.btnSignUpEnter);
		btn.setTypeface(FontKoodak);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mEditPhoneNumber.getText().toString().equals("") ||
						mEditPassword.getText().toString().equals("") ||
						mEditPasswordConfirm.getText().toString().equals("") ||
						mEditEmail.getText().toString().equals("")) {
					showAlert("لطفاً اطلاعات خود را به طور کامل وارد کنید");
					return;
				}

				// Check if the two password entries do not match
				if (!mEditPassword.getText().toString().equals(mEditPasswordConfirm.getText().toString())) {
					showAlert("رمز عبور دوم مطابقت ندارد");
					return;
				}

				new Thread(new Runnable() {

					@Override
					public void run() {
						HttpClient client = new DefaultHttpClient();
						HttpPost post = new HttpPost(RestAPIAddress.getSignUp());

						try {
							List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
							nameValuePairs.add(new BasicNameValuePair("phonenumber", mEditPhoneNumber.getText().toString()));
							nameValuePairs.add(new BasicNameValuePair("password", mEditPassword.getText().toString()));
							nameValuePairs.add(new BasicNameValuePair("email", mEditEmail.getText().toString()));
							post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

							HttpResponse response = client.execute(post);
							if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {

								showAlert("ثبت نام شما با موفقیت انجام شد.");
								
								startActivity(new Intent(This, SignInActivity.class));
								finish();
															
							}

							//							client.getConnectionManager().shutdown();

						} catch (Exception ex) {
							Log.d(TAG, ex.getMessage());
						}
					}
				}).start();
			}
		});
	}
}
