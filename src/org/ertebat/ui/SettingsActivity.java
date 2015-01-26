package org.ertebat.ui;
import org.ertebat.R;
import org.ertebat.R.id;
import org.ertebat.R.layout;
import org.ertebat.R.xml;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		getActionBar().hide();
		
		getFragmentManager().beginTransaction()
        .replace(R.id.layoutFragment, new SettingsFragment())
        .commit();
	}
	
	public static class SettingsFragment extends PreferenceFragment {
		@Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);

	        // Load the preferences from an XML resource
	        addPreferencesFromResource(R.xml.settings);
	    }
	}
}
