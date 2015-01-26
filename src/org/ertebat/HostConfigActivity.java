package org.ertebat;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class HostConfigActivity extends Activity {
	private EditText mEditHostAddress;
	private Button mBtnApply;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_host_config);
		
		mEditHostAddress = (EditText)findViewById(R.id.editHostConfigAddress);
		
		mBtnApply = (Button)findViewById(R.id.btnHostConfigApply);
		mBtnApply.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String addressString = mEditHostAddress.getText().toString();
				if (addressString.isEmpty())
					return;
				
				// TODO @Majid, do what you want in here
				
				finish();
			}
		});
	}
}
