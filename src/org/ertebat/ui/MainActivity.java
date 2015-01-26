package org.ertebat.ui;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.events.NgnInviteEventTypes;
import org.doubango.ngn.events.NgnMessagingEventArgs;
import org.doubango.ngn.events.NgnMsrpEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnHistorySMSEvent;
import org.doubango.ngn.model.NgnHistoryEvent.StatusType;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnMsrpSession;
import org.doubango.ngn.utils.NgnDateTimeUtils;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnUriUtils;
import org.ertebat.R;
import org.ertebat.R.id;
import org.ertebat.R.layout;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends BaseActivity {
	private static final int TAB_COUNT = 3;

	private ViewPager mPager;
	private TabListener mTabListener;
	private HomePagerAdapter mAdapter;
	private ActionBar mActionBar;
	private String[] tabNames = { "ê› êÊ Â«", "œÊ” «‰", " „«”" };

	private static int mCount = 0 ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		TAG = "MainActivity";

		mActionBar = getActionBar();

		mPager = (ViewPager)findViewById(R.id.pagerHome);
		mAdapter = new HomePagerAdapter(getSupportFragmentManager(), this);
		mPager.setAdapter(mAdapter);
		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayShowHomeEnabled(false);
		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				try {
					mActionBar.setSelectedNavigationItem(arg0);
				} catch (Exception ex) {
					Log.d(TAG, ex.getMessage());
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}
		});

		mTabListener = new TabListener() {

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				try {
					mPager.setCurrentItem(tab.getPosition());
				} catch (Exception ex) {
					Log.d(TAG, ex.getMessage());
				}				
			}

			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
				// TODO Auto-generated method stub

			}
		};

		for (String tabName : tabNames) {
			LayoutInflater inflator = LayoutInflater.from(this);
			View view = inflator.inflate(R.layout.tab_title, null);

			TextView text = (TextView)view.findViewById(R.id.txtTabTitle);
			text.setText(tabName);
			text.setTypeface(FontKoodak);

			mActionBar.addTab(mActionBar.newTab()
					.setCustomView(view)
					.setTabListener(mTabListener));
		}
		if(mCount == 0){
			mCount++;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
						mWebsocketService.getMyProfile();
						getFriendList();
						//mWebsocketService.getGroupRooms();
						showToast("getFriendList : " + mCount);
					} catch (Exception e) {
						Log.d(TAG, e.getMessage());
					}
				}
			}).start();
		}

	}

	public static class HomePagerAdapter extends FragmentPagerAdapter {
		private MainActivity mParent;
		public HomePagerAdapter(FragmentManager fm, MainActivity parent) {
			super(fm);
			mParent = parent;
		}

		@Override
		public int getCount() {
			return TAB_COUNT;
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return new ChatLogFragment();
			case 1:
				return new ContactListFragment();
			case 2:
				//				Inbox.MessageList = new ArrayList<Message>(mParent.loadMessages());
				return new CallFragment();
			default:
				return null;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (IsTablet)
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
					mWebsocketService.getMyProfile();
					getFriendList();
				} catch (Exception e) {
					Log.d(TAG, e.getMessage());
				}
			}
		}).start();
	}
}
