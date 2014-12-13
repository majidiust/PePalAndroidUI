package com.example.avcalltablet;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnInviteEventArgs;
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
		
		try
		{
			mEngine = NgnEngine.getInstance();
			mConfigurationService = mEngine.getConfigurationService();
			mSipService = mEngine.getSipService();
			mSipBroadCastRecv = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					final String action = intent.getAction();

					// Registration Event
					if(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(action)){
						NgnRegistrationEventArgs args = intent.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
						if(args == null){
							return;
						}
						switch(args.getEventType()){
						case REGISTRATION_NOK:
							showToast("Failed to register :(");
							break;
						case UNREGISTRATION_OK:
							showToast("You are now unregistered :)");
							break;
						case REGISTRATION_OK:
							showToast("You are now registered :)");
							break;
						case REGISTRATION_INPROGRESS:
							showToast("Trying to register...");
							break;
						case UNREGISTRATION_INPROGRESS:
							showToast("Trying to unregister...");
							break;
						case UNREGISTRATION_NOK:
							showToast("Failed to unregister :(");
							break;
						}
					}
					//Begin Of SIP Invite Segment
					else if(NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(action)){
						NgnInviteEventArgs args = intent.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
						if(args == null){
							return;
						}

						final NgnMediaType mediaType = args.getMediaType();
						mAVSession = NgnAVSession.getSession(args.getSessionId());
						if(NgnMediaType.isAudioVideoType(mediaType))
							SetCallState(args.getEventType());
						switch(args.getEventType()){							
						case TERMWAIT:
						case TERMINATED:
							if(NgnMediaType.isAudioVideoType(mediaType)){
								//Show UI Function that indicate to terminated call
								//NgnInviteEventTypes
								mEngine.getSoundService().stopRingBackTone();
								mEngine.getSoundService().stopRingTone();
							}
							break;

						case INCOMING:
							if(NgnMediaType.isAudioVideoType(mediaType)){
								if(mAVSession != null){			
									mEngine.getSoundService().startRingTone();

									// CHECK
//									Intent incomingIntent = new Intent(This, CallActivity.class);
//									incomingIntent.putExtra(NgnEventArgs.EXTRA_EMBEDDED, args);
//									startActivity(incomingIntent);
//									finish();
								}
								else{
								}
							}
							break;

						case INPROGRESS:
							if(NgnMediaType.isAudioVideoType(mediaType)){
								//mEngine.showAVCallNotif(R.drawable.phone_call_25, getString(R.string.string_call_outgoing));
								//Trying to send invite to remote party
							}
							break;

						case RINGING:
							if(NgnMediaType.isAudioVideoType(mediaType)){
								mEngine.getSoundService().startRingBackTone();
							}
							break;

						case CONNECTED:
						case EARLY_MEDIA:
							if(NgnMediaType.isAudioVideoType(mediaType)){
								mEngine.getSoundService().stopRingBackTone();
								mEngine.getSoundService().stopRingTone();
							}
							break;

						case MEDIA_UPDATED:
							showToast("Media Updated");
							break;
						default: break;
						}
					}
					//End Of Invite Segment
					//Chat Section 
					else if(NgnMessagingEventArgs.ACTION_MESSAGING_EVENT.equals(action)){
						NgnMessagingEventArgs args = intent.getParcelableExtra(NgnMessagingEventArgs.EXTRA_EMBEDDED);
						if(args == null){
							return;
						}
						switch(args.getEventType()){
						case INCOMING:
							String dateString = intent.getStringExtra(NgnMessagingEventArgs.EXTRA_DATE);
							String remoteParty = intent.getStringExtra(NgnMessagingEventArgs.EXTRA_REMOTE_PARTY);
							if(NgnStringUtils.isNullOrEmpty(remoteParty)){
								remoteParty = NgnStringUtils.nullValue();
							}
							remoteParty = NgnUriUtils.getUserName(remoteParty);
							NgnHistorySMSEvent event = new NgnHistorySMSEvent(remoteParty, StatusType.Incoming);
							event.setContent(new String(args.getPayload()));
							event.setStartTime(NgnDateTimeUtils.parseDate(dateString).getTime());
							mEngine.getHistoryService().addEvent(event);
							//ShowIncomingMessage(remoteParty, dateString, event.getContent());
							//ReloadMessageList();
							break;
						default:
							break;
						}
					}

					// MSRP chat Events
					// For performance reasons, file transfer events will be handled by the owner of the context
					else if(NgnMsrpEventArgs.ACTION_MSRP_EVENT.equals(action)){
						NgnMsrpEventArgs args = intent.getParcelableExtra(NgnMsrpEventArgs.EXTRA_EMBEDDED);
						if(args == null){
							return;
						}
						switch(args.getEventType()){
						case DATA:
							final NgnMsrpSession session = NgnMsrpSession.getSession(args.getSessionId());
							if(session == null){
								return;
							}
							final byte[]content = intent.getByteArrayExtra(NgnMsrpEventArgs.EXTRA_DATA);
							NgnHistorySMSEvent event = new NgnHistorySMSEvent(NgnUriUtils.getUserName(session.getRemotePartyUri()), StatusType.Incoming);
							event.setContent(content==null ? NgnStringUtils.nullValue() : new String(content));
							mEngine.getHistoryService().addEvent(event);
							//ShowIncomingMessage("Pager", "Now", event.getContent());
							//ReloadMessageList();
							break;
						default:
							break;
						}
					}
				}

			};
			final IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
			intentFilter.addAction(NgnInviteEventArgs.ACTION_INVITE_EVENT);
			intentFilter.addAction(NgnMsrpEventArgs.ACTION_MSRP_EVENT);
			intentFilter.addAction(NgnMessagingEventArgs.ACTION_MESSAGING_EVENT);
			registerReceiver(mSipBroadCastRecv, intentFilter);
			
			if(mEngine.isStarted() == false)
				mEngine.start();
		}
		catch(Exception ex)
		{
			Log.d("BASE", ex.getMessage());
			showToast(ex.getMessage());
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
}
