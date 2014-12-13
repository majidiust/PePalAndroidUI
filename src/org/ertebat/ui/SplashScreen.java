package org.ertebat.ui;


import org.ertebat.R;

import android.os.Bundle;
import android.content.Intent;

public final class SplashScreen extends BaseActivity {
	
	protected boolean _active = true;
	protected int _splashTime = 3000; // time to display the splash screen in ms

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.splash_screen);

	    Thread splashTread = new Thread() {
	        @Override
	        public void run() {
	            try {
	                int waited = 0;
	                while (_active && (waited < _splashTime)) {
	                    sleep(100);
	                    if (_active) {
	                        waited += 100;
	                    }
	                }
	            } catch (Exception e) {
	            } finally {

	                startActivity(new Intent(SplashScreen.this, MainActivity.class));
	                finish();
	            }
	        };
	    };
	    splashTread.start();
	}

}
