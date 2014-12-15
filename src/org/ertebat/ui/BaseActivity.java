package org.ertebat.ui;

/**
 * @author Majid
 * @author MCastor
 *
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnMsrpSession;
import org.doubango.ngn.utils.NgnDateTimeUtils;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnUriUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import org.ertebat.schema.FriendSchema;
import org.ertebat.schema.MessageSchema;
import org.ertebat.schema.ProfileSchema;
import org.ertebat.schema.RoomSchema;
import org.ertebat.schema.SessionStore;
import org.ertebat.schema.SettingSchema;
import org.ertebat.transport.ITransport;
import org.ertebat.transport.websocket.IWebsocketServiceCallback;
import org.ertebat.transport.websocket.IWebsocketService;
import org.json.JSONObject;

public class BaseActivity extends FragmentActivity implements ITransport {
	public static final int DIALOG_MULTI_CHOICE = 1;
	public static final int DIALOG_TAKE_PICTURE = 2;
	public static final int DIALOG_PICTURE_GALLERY = 3;
	public static final int DIALOG_FRAGMENT_MULTI_CHOICE = 4;

	protected Vector<ITransport> mTransportListeners = new Vector<ITransport>();
	protected BroadcastReceiver mSipBroadCastRecv;
	protected NgnEngine mEngine;
	protected INgnConfigurationService mConfigurationService;
	protected INgnSipService mSipService;
	protected NgnAVSession mAVSession;
	protected INgnHistoryService mHistoryService;
	protected CallActivityStatus mCurrentStatus = CallActivityStatus.CAS_Idle;
	protected Context This = null;
	protected Handler mHandler;
	protected FragmentDialogResultListener mFragmentDialogListener;
	protected static String TAG = "BaseActivity";
	protected ITransport mTransportCallback;
	protected ITransport mTransportFragmentCallback = null;
	protected Intent mWebsocketIntent;
	protected IWebsocketService mWebsocketService;
	protected IWebsocketServiceCallback mWebsocketServiceCallback = new IWebsocketServiceCallback.Stub(){

		@Override
		public void debug(String msg) throws RemoteException {
			//showToast(msg);
		}

		@Override
		public void loggedInResult(int code, String details)
				throws RemoteException {
			Log.d(TAG, String.valueOf(code) + " : " + details);
		}

		@Override
		public void newMessage(String from, String roomId, String date,
				String time, String content) throws RemoteException {
			if(mTransportCallback != null)
				mTransportCallback.onNewMessage(new MessageSchema(from, roomId, date, time, content));
		}

		@Override
		public void connectedToHost(String uri) throws RemoteException {
			mWebsocketService.sendTextMessageToRoom("--", "--");
			if(mTransportCallback != null)
				mTransportCallback.onConnectedToServer();
		}

		@Override
		public void disConnectedFromHost() throws RemoteException {
			if(mTransportCallback != null)
				mTransportCallback.onDisconnctedFromServer();
		}

		@Override
		public void authorizationRequest() throws RemoteException {
			mWebsocketService.authorizeToWs(mCurrentUserProfile.m_token);
			if(mTransportCallback != null)
				mTransportCallback.onAuthorizationRequest();
		}

		@Override
		public void authorized() throws RemoteException {
			if(mTransportCallback != null)
				mTransportCallback.onAuthorized();
		}

		@Override
		public void currentProfileResult(String username, String userId,
				String firstName, String lastName, String mobile, String email)
						throws RemoteException {
			if(mTransportCallback != null)
				mTransportCallback.onCurrentProfileResult(username, userId, firstName, lastName, mobile, email);
		}

		@Override
		public void friendAdded(String userName, String id, String status)
				throws RemoteException {
			try{
				FriendSchema fs = new FriendSchema(id, userName, status);
				if(mSessionStore != null)
					mSessionStore.addFriend(fs);
				if(mTransportCallback != null)
					mTransportCallback.onNewFriend(fs);
			}
			catch(Exception ex){
				showToast(ex.getMessage());
			}
		}

		@Override
		public void roomAdded(String roomName, String roomId, String roomDesc,
				String roomLogo, String roomType) throws RemoteException {
			// TODO Auto-generated method stub
			
		}
	};

	protected ServiceConnection mWebsocketServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if(mWebsocketService != null)
			{
				try {
					mWebsocketService.unregisterCallback(mWebsocketServiceCallback);
					Log.d(TAG, "disConnected from the web socket service");
				} catch (RemoteException e) {
					Log.d(TAG, e.getMessage());
				}
				mWebsocketService = null;
			}
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mWebsocketService = IWebsocketService.Stub.asInterface((IBinder)service);
			try {
				mWebsocketService.registerCallback(mWebsocketServiceCallback);
				Log.d(TAG, "Connected to the web socket service");
			}
			catch (RemoteException e) {
				Log.d(TAG, e.getMessage());
			}
		}
	};

	protected static int mLastCommand = 0;
	protected static ProfileSchema mCurrentUserProfile;
	protected static SettingSchema mSettings = new SettingSchema();
	protected static SessionStore mSessionStore = new SessionStore();
	// TODO: @Majid, load the contacts into this list and use it anywhere you need. I have used it for adding a contact to a chat
	protected static List<ContactSummary> mContacts;

	public static Typeface FontRoya;
	public static Typeface FontKoodak;
	public static Typeface FontNazanin;
	public static boolean IsTablet;

	static {
		mContacts = new ArrayList<ContactSummary>();

		ContactSummary contact = new ContactSummary();
		contact.ContactName = "Majid";
		contact.ContactPhone = "6363";
		mContacts.add(contact);

		contact = new ContactSummary();
		contact.ContactName = "Morteza";
		contact.ContactPhone = "6364";
		mContacts.add(contact);

		contact = new ContactSummary();
		contact.ContactName = "Sadegh";
		contact.ContactPhone = "6365";
		mContacts.add(contact);

		contact = new ContactSummary();
		contact.ContactName = "Mehdi";
		contact.ContactPhone = "6366";
		mContacts.add(contact);

		mCurrentUserProfile = new ProfileSchema();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mHandler = new Handler();
		This = this;
		mTransportCallback = this;

		int screenLayout = getResources().getConfiguration().screenLayout;
		screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;
		IsTablet = screenLayout == Configuration.SCREENLAYOUT_SIZE_XLARGE;

		if (FontRoya == null)
			FontRoya = Typeface.createFromAsset(getAssets(), "broya.ttf");
		if (FontKoodak == null)
			FontKoodak = Typeface.createFromAsset(getAssets(), "bkoodkbd.ttf");
		if (FontNazanin == null)
			FontNazanin = Typeface.createFromAsset(getAssets(), "bnazaninbd_0.ttf");

		try{
			mWebsocketIntent = new Intent(IWebsocketService.class.getName());
			startService(mWebsocketIntent);
			bindService(mWebsocketIntent, mWebsocketServiceConnection, 0);
		}
		catch(Exception ex){
			Log.d(TAG, ex.getMessage());
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

	@Override
	protected void onDestroy() {
		//		if(mEngine.isStarted()){
		//			if(mSipService.isRegistered())
		//				SignOutNGN();
		//			mEngine.stop();
		//		}
		//		if (mSipBroadCastRecv != null) {
		//			unregisterReceiver(mSipBroadCastRecv);
		//			mSipBroadCastRecv = null;
		//		}

		try {
			unregisterReceiver(mSipBroadCastRecv);
			if(mWebsocketService != null)
			{
				mWebsocketService.unregisterCallback(mWebsocketServiceCallback);
				unbindService(mWebsocketServiceConnection);
				Log.d(TAG, "Unbind from data services");
			}
		}
		catch (Exception ex) {
			showToast("destroy : " + ex.getMessage());
		}
		super.onDestroy();
	}

	protected void SetCallState(NgnInviteEventTypes callState) {
		// TODO Auto-generated method stub

	}

	protected void loadInCallView(){

	}

	
	protected void getUserProfile(final String uid){
		try{
			new Thread(new Runnable() {
				@Override
				public void run() {
					HttpClient client = new DefaultHttpClient();
					Log.d(TAG, RestAPIAddress.getSignIn());
					HttpGet getProfileRest = new HttpGet(RestAPIAddress.getUserProfile() + "/" + uid);	
					getProfileRest.addHeader("token", mCurrentUserProfile.m_token);
					try {
						HttpResponse response = client.execute(getProfileRest);
						if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
							BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
							String line = "";
							String jsonString = "";
							while ((line = rd.readLine()) != null) {
								jsonString += line;						
							}

							JSONObject json = new JSONObject(jsonString);
							String firstName = "--";
							String lastName = "--";
							String email = "--";
							String id = "--";
							String picUrl = "--";
							String userName = "--";
							try{
								firstName = json.getString("firstName");
							}
							catch(Exception ex){
								logCatDebug(ex.getMessage());
							}
							try{
								lastName = json.getString("lastName");
							}
							catch(Exception ex){
								logCatDebug(ex.getMessage());
							}
							try{
								email = json.getString("email");
							}
							catch(Exception ex){
								logCatDebug(ex.getMessage());
							}
							try{
								id = json.getString("id");
							}
							catch(Exception ex){
								logCatDebug(ex.getMessage());
							}
							try{
								picUrl = json.getString("picUrl");
							}
							catch(Exception ex){
								logCatDebug(ex.getMessage());
							}
							try{
								userName = json.getString("userName");
							}
							catch(Exception ex){
								logCatDebug(ex.getMessage());
							}

							if(mTransportCallback != null)
								mTransportCallback.onUserProfile(firstName, lastName, id, userName, picUrl, email);
						} else {
							showAlert("سیستم قادر به دریافت اطلاعات کاربر نمی باشد.");
						}



					} catch (Exception ex) {
						Log.d(TAG, ex.getMessage());
					}
				}
			}).start();
		}
		catch(Exception ex){
			logCatDebug(ex.getMessage());
		}
	}
	
	protected void saveProfile(final String uid, final String firstName, final String lastName, final String email){
		try{
			new Thread(new Runnable() {

				@Override
				public void run() {
					HttpClient client = new DefaultHttpClient();
					Log.d(TAG, RestAPIAddress.getSignIn());
					HttpPost post = new HttpPost(RestAPIAddress.getSaveProfile());

					try {
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
						nameValuePairs.add(new BasicNameValuePair("firstName", firstName));
						nameValuePairs.add(new BasicNameValuePair("lastName", lastName));
						nameValuePairs.add(new BasicNameValuePair("email", email));
						post.setHeader("token", mCurrentUserProfile.m_token);
						post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
						HttpResponse response = client.execute(post);
						if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
							mCurrentUserProfile.m_firstName = firstName;
							mCurrentUserProfile.m_lastName = lastName;
							mCurrentUserProfile.m_email = email;
							showAlert("پروفایل شما با موفقیت بروز شد.");
						} else {
							showAlert("ورود موفقیت آمیز نبود. لطفاً مجدداً تلاش نمائید");
						}
					} catch (Exception ex) {
						Log.d(TAG, ex.getMessage());
					}
				}
			}).start();
		}
		catch(Exception ex){
			logCatDebug(ex.getMessage());
		}
	}
	public static void logCatDebug(String msg){
		Log.d(TAG, msg);
	}
	/**
	 * displays a given message at the bottom of the page
	 * @param message
	 */
	protected void showToast(final String message) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(This, message, Toast.LENGTH_SHORT)
				.show();
			}
		});		
	}

	/**
	 * displays an alert window and asks for user acknowledgment
	 * @param message
	 */
	protected void showAlert(final String message) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(This);
				builder.setMessage(message)
				.setCancelable(false)
				.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// do things
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
	}

	protected void showAlert(final String message, final int alertID) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(This);
				builder.setMessage(message)
				.setCancelable(false)
				.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						onAlertClosed(alertID);
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
			}
		});
	}

	protected void onAlertClosed(int alertID) {

	}

	protected void showMultiChoiceDialog(String[] choices, String callback, String dialogTitle) {
		Intent intent = new Intent(this , DialogMultiChoiceActivity.class);
		Bundle b = new Bundle();
		b.putStringArray("Choices", choices);
		b.putString("Callback", callback);
		b.putString("Title", dialogTitle);
		intent.putExtras(b);
		startActivityForResult(intent, DIALOG_MULTI_CHOICE);
	}

	protected void showFragmentMultiChoiceDialog(String[] choices, String callback, String dialogTitle) {
		Intent intent = new Intent(this , DialogMultiChoiceActivity.class);
		Bundle b = new Bundle();
		b.putStringArray("Choices", choices);
		b.putString("Callback", callback);
		b.putString("Title", dialogTitle);
		intent.putExtras(b);
		startActivityForResult(intent, DIALOG_FRAGMENT_MULTI_CHOICE);
	}

	protected void onMultiChoiceDialogResult(String dialogTitle, String callback, int selectedIndex) {
		// Child classes should override and define the body
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data == null)
			return;

		switch (requestCode) {
		case DIALOG_MULTI_CHOICE:
			if (resultCode == Activity.RESULT_OK) {
				int selected = data.getExtras().getInt("SelectedIndex");
				onMultiChoiceDialogResult(data.getExtras().getString("Title"), data.getExtras().getString("Callback"), selected);
			}
			break;
		case DIALOG_FRAGMENT_MULTI_CHOICE:
			if (resultCode == Activity.RESULT_OK) {
				int selected = data.getExtras().getInt("SelectedIndex");
				mFragmentDialogListener.onMultichoiceDialogResult(data.getExtras().getString("Title"),
						data.getExtras().getString("Callback"), selected);
			}
			break;
		default:
			break;
		}
	}

	public void setFragmentDialogResultListener(FragmentDialogResultListener listener) {
		mFragmentDialogListener = listener;
	}

	public void setFragmentTransportCallback(ITransport callback) {
		mTransportFragmentCallback = callback;
	}

	public void getFriendList(){
		try {
			mWebsocketService.getFriendList();
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
		}
	}
	
	public static String[] getContactNames() {
		String[] names = new String[mContacts.size()];

		for (int i = 0; i < mContacts.size(); i++) {
			names[i] = mContacts.get(i).ContactName;
		}

		return names;
	}

	@Override
	public void onConnectedToServer() {
		if (mTransportFragmentCallback != null)
			mTransportFragmentCallback.onConnectedToServer();
	}

	@Override
	public void onDisconnctedFromServer() {
		if (mTransportFragmentCallback != null)
			mTransportFragmentCallback.onDisconnctedFromServer();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(5000);
					mWebsocketService.connectToHost(mSettings.mWebSocketUrl);
				} catch (Exception e) {
					Log.d(TAG, e.getMessage());
				}
			}
		}).start();
	}

	@Override
	public void onNewFriend(FriendSchema fs) {
		if (mTransportFragmentCallback != null)
			mTransportFragmentCallback.onNewFriend(fs);
	}

	@Override
	public void onNewRoom(RoomSchema rs) {
		if (mTransportFragmentCallback != null)
			mTransportFragmentCallback.onNewRoom(rs);
	}

	@Override
	public void onNewMessage(MessageSchema ms) {
		
		if (mTransportFragmentCallback != null)
			mTransportFragmentCallback.onNewMessage(ms);
	}

	@Override
	public void onCurrentProfileResult(String username, String userId,
			String firstName, String lastName, String mobile, String email) {
		mCurrentUserProfile.m_uuid = userId;
		mCurrentUserProfile.m_email = email;
		mCurrentUserProfile.m_firstName = firstName;
		mCurrentUserProfile.m_lastName = lastName;
		if (mTransportFragmentCallback != null)
			mTransportFragmentCallback.onCurrentProfileResult(username, userId, firstName, lastName, mobile, email);
	}

	@Override
	public void onAuthorizationRequest() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAuthorized() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUserProfile(String firstName, String lastName, String uid,
			String userName, String picUrl, String email) {
		// TODO Auto-generated method stub

	}

	@Override
	public void roomAdded(String roomName, String roomId, String roomDesc,
			String roomLogo, String roomType) {
		// TODO Auto-generated method stub
		
	}
}