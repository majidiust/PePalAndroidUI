package org.ertebat.ui;
import org.ertebat.R;
import org.ertebat.R.id;
import org.ertebat.R.layout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class DialogMultiChoiceActivity extends Activity {
	private String[] mChoices;
	private String mTitle;
	private String mCallback;
	
	private TextView mTextTitle;
	private ListView mListViewChoices;
	private SimpleListAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dialog_multi_choice);
		
		Bundle extra = getIntent().getExtras();
		mChoices = extra.getStringArray("Choices");
		mTitle = extra.getString("Title");
		mCallback = extra.getString("Callback");
		
		mTextTitle = (TextView)findViewById(R.id.txtMultiChoiceTitle);
		mTextTitle.setTypeface(BaseActivity.FontKoodak);
		mTextTitle.setText(mTitle);
		
		mAdapter = new SimpleListAdapter(this, mChoices);
		mListViewChoices = (ListView)findViewById(R.id.listViewDialogMultiChoice);
		mListViewChoices.setAdapter(mAdapter);
		mListViewChoices.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
	            Intent intent = new Intent();
	            intent.putExtra("SelectedIndex", position);
	            intent.putExtra("Callback", mCallback);
	            intent.putExtra("Title", mTitle);
	            setResult(Activity.RESULT_OK, intent);
	            finish();
			}
		});
	}
}