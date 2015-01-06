package org.pepal.ui;
import org.pepal.R;
import org.pepal.R.id;
import org.pepal.R.layout;
import org.pepal.R.xml;

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
