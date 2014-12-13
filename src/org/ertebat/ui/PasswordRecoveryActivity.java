package org.ertebat.ui;

import org.ertebat.R;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PasswordRecoveryActivity extends BaseActivity {
	private final long PASS_RECEIVE_TIMEOUT = 15000;

	private EditText mEditPhoneNumber;
	private TextView mTextResend;
	private Button mBtnSendPassword;
	private boolean mIsFinished = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_password_recovery);
		
		getActionBar().hide();
		TAG = "PasswordRecovery";

		mEditPhoneNumber = (EditText)findViewById(R.id.editPasswordRecoveryPhoneNumber);
		mEditPhoneNumber.setTypeface(FontNazanin);

		mTextResend = (TextView)findViewById(R.id.txtPasswordRecoveryResend);
		mTextResend.setTypeface(FontKoodak);

		mBtnSendPassword = (Button)findViewById(R.id.btnPasswordRecoverySend);
		mBtnSendPassword.setTypeface(FontKoodak);
		mBtnSendPassword.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mEditPhoneNumber.getText().toString().isEmpty()) {
					showAlert("لطفاً شماره خود را وارد نمائید");
					return;
				}
				
				mTextResend.setVisibility(View.GONE);
				mBtnSendPassword.setVisibility(View.INVISIBLE);
				mEditPhoneNumber.setEnabled(false);
				
				startPasswordWait();
				showAlert("رمز عبور شما بزودی از طریق پیامک ارسال می شود");
			}
		});
	}

	private void startPasswordWait() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(PASS_RECEIVE_TIMEOUT);
					if (mIsFinished)
						return;

					mHandler.post(new Runnable() {

						@Override
						public void run() {
							mTextResend.setVisibility(View.VISIBLE);
							mBtnSendPassword.setVisibility(View.VISIBLE);
							mBtnSendPassword.setText(R.string.btn_resend);
						}
					});
				} catch (Exception ex) {
					Log.d(TAG, ex.getMessage());
				}
			}
		}).start();
	}
	
	@Override
	protected void onDestroy() {
		mIsFinished = true;
		super.onDestroy();
	}
}
