package org.ertebat.ui;

/**
 * @author Majid
 * @author MCastor
 *
 */

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
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
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnDateTimeUtils;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnUriUtils;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import org.ertebat.schema.FriendSchema;
import org.ertebat.schema.MessageSchema;
import org.ertebat.schema.ProfileSchema;
import org.ertebat.schema.RoomSchema;
import org.ertebat.schema.SessionStore;
import org.ertebat.schema.SettingSchema;
import org.ertebat.transport.INGN;
import org.ertebat.transport.ITransport;
import org.ertebat.transport.websocket.IWebsocketServiceCallback;
import org.ertebat.transport.websocket.IWebsocketService;
import org.json.JSONArray;
import org.json.JSONObject;

public class BaseActivity extends FragmentActivity implements ITransport {
	public static final int DIALOG_MULTI_CHOICE = 1;
	public static final int DIALOG_TAKE_PICTURE = 2;
	public static final int DIALOG_PICTURE_GALLERY = 3;
	public static final int DIALOG_FRAGMENT_MULTI_CHOICE = 4;


	protected static List mTransportListeners = new ArrayList<ITransport>();
	protected static List mNGNListeners = new ArrayList<INGN>();
	protected BroadcastReceiver mSipBroadCastRecv;
	protected NgnEngine mEngine;
	protected INgnConfigurationService mConfigurationService;
	protected INgnSipService mSipService;
	protected NgnAVSession mAVSession;
	protected INgnHistoryService mHistoryService;
	protected CallActivityStatus mCurrentStatus = CallActivityStatus.CAS_Idle;
	protected Context This = null;
	protected Handler mHandler;
	protected Dialog mDialog;
	protected FragmentDialogResultListener mFragmentDialogListener;
	protected static String TAG = "BaseActivity";
	protected ITransport mTransportCallback;
	protected Intent mWebsocketIntent;
	protected IWebsocketService mWebsocketService;
	protected IWebsocketServiceCallback mWebsocketServiceCallback = new IWebsocketServiceCallback.Stub(){

		@Override
		public void debug(String msg) throws RemoteException {
			//String m = msg;
			showToast(msg);
		}

		@Override
		public void loggedInResult(int code, String details)
				throws RemoteException {
			Log.d(TAG, String.valueOf(code) + " : " + details);
		}

		@Override
		public void newMessage(String type, String messageId, String fromId, String fromUsername, String roomId, String date,
				String time, String content) throws RemoteException {
			if(mTransportCallback != null)
				mTransportCallback.onNewMessage(new MessageSchema(type, messageId, fromId, fromUsername, roomId, date, time, content));
		}

		@Override
		public void connectedToHost(String uri) throws RemoteException {
			showToast("Connected to the host");
			mWebsocketService.sendTextMessageToRoom("--", "--");
			if(mTransportCallback != null)
				mTransportCallback.onConnectedToServer();
		}

		@Override
		public void disConnectedFromHost() throws RemoteException {
			showToast("Disconnect from the host");
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
				if(SessionStore.mSessionStore != null)
					SessionStore.mSessionStore.addFriend(fs);
				if(mTransportCallback != null)
					mTransportCallback.onNewFriend(fs);
			}
			catch(Exception ex){
				showToast(ex.getMessage());
			}
		}

		@Override
		public void roomAdded(String roomName, String roomId, String roomDesc,
				String roomLogo, String roomType, String members) throws RemoteException {
			//showToast("Get indi room : " + roomId);
			if(mTransportCallback != null)
				mTransportCallback.onRoomAdded(roomName, roomId, roomDesc, roomLogo, roomType, members);

		}

		@Override
		public void membersAddedToRoom(String roomId, String memberId)
				throws RemoteException {
			if(mTransportCallback != null)
				mTransportCallback.onMembersAddedToRoom(roomId, memberId);
		}

		@Override
		public void notifyAddedByFriend(String invitedBy)
				throws RemoteException {
			if(mTransportCallback != null)
				mTransportCallback.notifyAddedByFriend(invitedBy);
		}

		@Override
		public void notifyAddedToRoom(String invitedBy, String roomId)
				throws RemoteException {
			if(mTransportCallback != null)
				mTransportCallback.notifyAddedToRoom(invitedBy, roomId);
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
	protected static SharedPreferences mSharedPref;

	protected static int mLastCommand = 0;
	protected static ProfileSchema mCurrentUserProfile;
	protected static SettingSchema mSettings = new SettingSchema();
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

		if (mSharedPref == null)
			mSharedPref = PreferenceManager.getDefaultSharedPreferences(This);


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
									OnIncommingCall(args);
									// CHECK
									//																		Intent incomingIntent = new Intent(This, CallActivity.class);
									//																		incomingIntent.putExtra(NgnEventArgs.EXTRA_EMBEDDED, args);
									//																		startActivity(incomingIntent);
									//																		finish();
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



	protected void signInNGN() {
		// Set credentials
		String logStrValue;
		boolean logBoolValue = false;
		int logIntValue;

		mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI,
				logStrValue = mSharedPref.getString("pref_key_private_identity",""));		
		Log.d("SignInNGN", logStrValue);

		mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU, 
				logStrValue = mSharedPref.getString("pref_key_public_identity",""));
		Log.d("SignInNGN", logStrValue);

		mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD,
				logStrValue = mSharedPref.getString("pref_key_password",""));
		Log.d("SignInNGN", logStrValue);

		mConfigurationService.putString(NgnConfigurationEntry.NETWORK_PCSCF_HOST,
				logStrValue = mSharedPref.getString("pref_key_CSCFHost",""));
		Log.d("SignInNGN", logStrValue);

		logStrValue = mSharedPref.getString("pref_key_port", "0");
		mConfigurationService.putInt(NgnConfigurationEntry.NETWORK_PCSCF_PORT,
				Integer.parseInt(logStrValue));
		Log.d("SignInNGN", logStrValue);

		mConfigurationService.putString(NgnConfigurationEntry.NETWORK_TRANSPORT, 
				logStrValue = mSharedPref.getString("pref_key_network_protocol",""));
		Log.d("SignInNGN", logStrValue);

		mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM,
				logStrValue = mSharedPref.getString("pref_key_network_realm",""));
		Log.d("SignInNGN", "Realm: " + logStrValue);

		mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_WIFI, 
				logBoolValue = mSharedPref.getBoolean("",true));
		Log.d("SignInNGN", String.valueOf(logBoolValue));

		mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_3G, 
				logBoolValue = mSharedPref.getBoolean("",true));

		mConfigurationService.putString(NgnConfigurationEntry.QOS_PREF_VIDEO_SIZE, 
				logStrValue = mSharedPref.getString("pref_key_qos_video_size",""));
		Log.d("SignInNGN", logStrValue);

		//SMS Settings
		//TODO: Change
		mConfigurationService.putString(NgnConfigurationEntry.RCS_CONF_FACT, logStrValue = mSharedPref.getString("pref_key_RCS_CONF_FACT","sip:Conferense@doubango.org"));
		mConfigurationService.putString(NgnConfigurationEntry.RCS_SMSC, logStrValue = mSharedPref.getString("pref_key_RCS_SMSC","sip:+331000000000@doubango.org"));
		mConfigurationService.putBoolean(NgnConfigurationEntry.RCS_USE_BINARY_SMS, logBoolValue = mSharedPref.getBoolean("pref_key_",true));
		mConfigurationService.putBoolean(NgnConfigurationEntry.RCS_USE_MSRP_SUCCESS, logBoolValue = mSharedPref.getBoolean("pref_key_",true));
		mConfigurationService.putBoolean(NgnConfigurationEntry.RCS_USE_MSRP_FAILURE, logBoolValue = mSharedPref.getBoolean("pref_key_",true));
		mConfigurationService.putBoolean(NgnConfigurationEntry.RCS_USE_OMAFDR, logBoolValue = mSharedPref.getBoolean("pref_key_",true));
		mConfigurationService.putBoolean(NgnConfigurationEntry.RCS_USE_MWI, logBoolValue = mSharedPref.getBoolean("pref_key_",true));

		// VERY IMPORTANT: Commit changes
		mConfigurationService.commit();

		// register (log in)
		mSipService.register(This);
	}

	protected void signOutNGN() {
		if (mSipService != null) {
			if (mSipService.isRegistered())
				mSipService.unRegister();
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
		for(int i = 0 ; i < mNGNListeners.size() ; i++){
			((INGN)mNGNListeners.get(i)).SetCallState(callState);
		}
	}

	public void OnIncommingCall(NgnInviteEventArgs args){
		for(int i = 0 ; i < mNGNListeners.size() ; i++){
			((INGN)mNGNListeners.get(i)).OnIncommingCall(args);
		}
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
							showAlert("ط³غŒط³طھظ… ظ‚ط§ط¯ط± ط¨ظ‡ ط¯ط±غŒط§ظپطھ ط§ط·ظ„ط§ط¹ط§طھ ع©ط§ط±ط¨ط± ظ†ظ…غŒ ط¨ط§ط´ط¯.");
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

	public void getIncommingMessage(){
		try{
			new Thread(new Runnable() {
				@Override
				public void run() {
					HttpClient client = new DefaultHttpClient();
					Log.d(TAG, RestAPIAddress.getSignIn());
					HttpGet getProfileRest = new HttpGet(RestAPIAddress.getIncommingMessage());	
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
							if(json.getString("code").equals("103")){
								JSONObject value = new JSONObject(json.getString("value"));
								String type = "";
								String date = "";
								String from = "";
								String content = "";
								String roomId = "";
								String id  =  "";
								String fromId = "";
								try{
									type = value.getString("type");
								}
								catch(Exception ex){
									logCatDebug(ex.getMessage());
								}
								try{
									fromId = value.getString("fromId");
								}
								catch(Exception ex){
									logCatDebug(ex.getMessage());
								}
								try{
									date = value.getString("date");
								}
								catch(Exception ex){
									logCatDebug(ex.getMessage());
								}
								try{
									from = value.getString("from");
								}
								catch(Exception ex){
									logCatDebug(ex.getMessage());
								}
								try{
									id = value.getString("id");
								}
								catch(Exception ex){
									logCatDebug(ex.getMessage());
								}
								try{
									content = value.getString("content");
								}
								catch(Exception ex){
									logCatDebug(ex.getMessage());
								}
								try{
									roomId = value.getString("roomId");
								}
								catch(Exception ex){
									logCatDebug(ex.getMessage());
								}

								if(mTransportCallback != null)
									mTransportCallback.onNewMessage(new MessageSchema(id, fromId, from, roomId, date, date, content));
							}
						} else {
							showAlert("ط³غŒط³طھظ… ظ‚ط§ط¯ط± ط¨ظ‡ ط¯ط±غŒط§ظپطھ ط§ط·ظ„ط§ط¹ط§طھ ع©ط§ط±ط¨ط± ظ†ظ…غŒ ط¨ط§ط´ط¯.");
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

						} else {

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


	public void getFriendList(){
		try{
			new Thread(new Runnable() {

				@Override
				public void run() {
					HttpClient client = new DefaultHttpClient();
					Log.d(TAG, RestAPIAddress.getSignIn());
					HttpGet get = new HttpGet(RestAPIAddress.getFriendList());

					try {
						get.setHeader("token", mCurrentUserProfile.m_token);
						HttpResponse response = client.execute(get);
						if (response.getStatusLine().getStatusCode() == 200) {
							BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
							String line = "";
							String jsonString = "";
							while ((line = rd.readLine()) != null) {
								jsonString += line;						
							}
							JSONObject jsonObject = new JSONObject(jsonString);
							String code = jsonObject.getString("code");
							if(code.compareTo("9") == 0){
								JSONArray friends = jsonObject.getJSONArray("friends");
								//showToast((jsonObject.getString("friends")));
								Vector<FriendSchema> listOfFriends = new Vector<FriendSchema>();
								for(int i = 0 ; i < friends.length() ; i++){
									JSONObject obj = friends.getJSONObject(i);
									String id ,userName, status;
									id = obj.getString("friendId");
									userName = obj.getString("friendUsername");
									status = obj.getString("status");
									listOfFriends.add(new FriendSchema(id, userName, status));
								}


								for(int j = 0 ; j < listOfFriends.size() ; j++){
									FriendSchema fs = (FriendSchema) listOfFriends.get(j);
									try{
										if(SessionStore.mSessionStore != null)
											SessionStore.mSessionStore.addFriend(fs);
										if(mTransportCallback != null)
											mTransportCallback.onNewFriend(fs);
									}
									catch(Exception ex){
										showToast(ex.getMessage());
									}
								}

								mWebsocketService.getIndividualRooms();

							}
							else{
								showAlert("i can get the friend list. internet connection is so low");
							}
						} else {
							showAlert("ظˆط±ظˆط¯ ظ…ظˆظپظ‚غŒطھ ط¢ظ…غŒط² ظ†ط¨ظˆط¯. ظ„ط·ظپط§ظ‹ ظ…ط¬ط¯ط¯ط§ظ‹ طھظ„ط§ط´ ظ†ظ…ط§ط¦غŒط¯");
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

	protected void sendTextMessageToServer(final String roomId, final String publishType, final String publishDate, final String content){
		showWaitingDialog("منتظر بمانید", "در حال ارسال اطلاعات به سرور ...");
		try{
			new Thread(new Runnable() {

				@Override
				public void run() {
					HttpClient client = new DefaultHttpClient();
					Log.d(TAG, RestAPIAddress.getSignIn());
					HttpPost post = new HttpPost(RestAPIAddress.getSendMessage());

					try {
						List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
						nameValuePairs.add(new BasicNameValuePair("publishType", publishType));
						nameValuePairs.add(new BasicNameValuePair("roomId", roomId));
						nameValuePairs.add(new BasicNameValuePair("publishDate", publishDate));
						nameValuePairs.add(new BasicNameValuePair("messageContent", content));
						post.setHeader("token", mCurrentUserProfile.m_token);
						post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
						HttpResponse response = client.execute(post);
						closeWaitingDialog();
						if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
							//showAlert("ظ¾غŒط§ظ… ط´ظ…ط§ ط¨ط§ ظ…ظˆظپظ‚غŒطھ ط§ط±ط³ط§ظ„ ط´ط¯.");
						} else {
							showAlert("خطا در ارسال اطلاعات به سرور ...");
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

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	protected void uploadImageToTheServer(Uri selectedImage, String mRoomId){
		try{
			showWaitingDialog("منتظر بمانید", "در حال ارسال اطلاعات به سرور ...");
			final String url = RestAPIAddress.getSendPictureMessage() + "/" + mRoomId + "/Now/Now";
			final String path =  getPath(selectedImage);

			new Thread(new Runnable() {

				@Override
				public void run() {
					try{
						//1. Get the thumbnail
						int width = (int) getResources().getDimension(org.ertebat.R.dimen.chat_message_item_picture_width);
						Bitmap image = Utilities.getPictureThumbnail(This, path, width);
						//2. 
						File pictureFileDirectory = new File(Environment.getExternalStoragePublicDirectory(
								Environment.DIRECTORY_PICTURES).getPath() + "/ertebat/tmp");
						pictureFileDirectory.mkdirs();
						Calendar cal = Calendar.getInstance();
						long id = cal.getTimeInMillis();
						File pictureFile = new File(pictureFileDirectory, id + ".png");
						FileOutputStream fos = new FileOutputStream(pictureFile);
						image.compress(Bitmap.CompressFormat.PNG, 0, fos);
						fos.close();
						HttpClient httpclient = new DefaultHttpClient();
						HttpPost httppost = new HttpPost(url);
						httppost.setHeader("token", mCurrentUserProfile.m_token);
						MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
						ContentBody content = new FileBody(pictureFile);//, "multipart/form-data");
						reqEntity.addPart("uploaded", content);
						httppost.setEntity(reqEntity);
						HttpResponse response = httpclient.execute(httppost);
						pictureFile.delete();
						closeWaitingDialog();
						Log.d("File", "Response: " + response.getStatusLine().getReasonPhrase());

					} catch (Exception ex) {
						Log.d("File", ex.getMessage());
					}
				}
			}).start();
		}
		catch(Exception ex){
			showToast(ex.getMessage());
		}
	}

	protected void uploadProfilePictureToTheServer(Uri selectedImage){
		try{
			final String url = RestAPIAddress.getUploadProfilePicture();
			final String path =  getPath(selectedImage);

			new Thread(new Runnable() {

				@Override
				public void run() {
					try{
						//1. Get the thumbnail
						int width = (int) getResources().getDimension(org.ertebat.R.dimen.user_profile_thumbnail_size);
						Bitmap image = Utilities.getPictureThumbnail(This, path, width);
						//2. 
						File pictureFileDirectory = new File(Environment.getExternalStoragePublicDirectory(
								Environment.DIRECTORY_PICTURES).getPath() + "/ertebat/tmp");
						pictureFileDirectory.mkdirs();
						Calendar cal = Calendar.getInstance();
						long id = cal.getTimeInMillis();
						File pictureFile = new File(pictureFileDirectory, id + ".png");
						FileOutputStream fos = new FileOutputStream(pictureFile);
						image.compress(Bitmap.CompressFormat.PNG, 0, fos);
						fos.close();
						HttpClient httpclient = new DefaultHttpClient();
						HttpPost httppost = new HttpPost(url);
						httppost.setHeader("token", mCurrentUserProfile.m_token);
						MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
						ContentBody content = new FileBody(pictureFile);//, "multipart/form-data");
						reqEntity.addPart("uploaded", content);
						httppost.setEntity(reqEntity);
						HttpResponse response = httpclient.execute(httppost);
						pictureFile.delete();
						Log.d("File", "Response: " + response.getStatusLine().getReasonPhrase());
						closeWaitingDialog();
						showAlert("تصویر شما به تصویر خواسته شده تغییر داده شد.");

					} catch (Exception ex) {
						Log.d("File", ex.getMessage());
					}
				}
			}).start();
		}
		catch(Exception ex){
			showToast(ex.getMessage());
		}
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

	/**
	 * displays a custom wait message
	 * @param title: title of the message
	 * @param message: the message text
	 */
	protected void showWaitingDialog(final String title, final String message) {
		showTimedWaitingDialog(title, message, 10000);
	}

	/**
	 * displays a custom wait timeout based message
	 * @param title: title of the message
	 * @param message: the message text
	 */
	protected void showTimedWaitingDialog(final String title, final String message, final int msec) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				mDialog = ProgressDialog.show(This, title, message, true);
				TextView tvMessage = (TextView) mDialog
						.findViewById(android.R.id.message);
				tvMessage.setGravity(Gravity.LEFT);
			}
		});	

		new Thread(new Runnable() {

			@Override
			public void run() {
				try{
					Thread.sleep(msec);
					closeWaitingDialog();
				}
				catch(Exception ex){
					showAlert(ex.getMessage());
				}
			}
		}).start();
	}

	/**
	 * closes the wait message dialog if it is open
	 */
	protected void closeWaitingDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
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

	@SuppressWarnings("deprecation")
	private void Notify(String notificationTitle, String notificationMessage) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		@SuppressWarnings("deprecation")
		Notification notification = new Notification(R.drawable.ic_dialog_alert,
				"New Message", System.currentTimeMillis());

		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(BaseActivity.this, notificationTitle,
				notificationMessage, pendingIntent);
		notificationManager.notify(9999, notification);
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


	public static String[] getContactNames() {
		String[] names = new String[mContacts.size()];

		for (int i = 0; i < mContacts.size(); i++) {
			names[i] = mContacts.get(i).ContactName;
		}

		return names;
	}

	public void registerToTransportListeners(ITransport transport){
		mTransportListeners.add(transport);
	}

	public void unRegisterFromTransportListeners(ITransport transport){
		mTransportListeners.remove(transport);
	}

	public void registerToNGNListeners(INGN ngn){
		mNGNListeners.add(ngn);
	}

	public void unRegisterFromNGNListeners(INGN ngn){
		mNGNListeners.remove(ngn);
	}

	@Override
	public void onConnectedToServer() {
		for(int i = 0 ; i < mTransportListeners.size() ; i++){
			((ITransport) mTransportListeners.get(i)).onConnectedToServer();
		}
	}

	@Override
	public void onDisconnctedFromServer() {
		for(int i = 0 ; i < mTransportListeners.size() ; i++){
			((ITransport) mTransportListeners.get(i)).onDisconnctedFromServer();
		}
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
		for(int i = 0 ; i < mTransportListeners.size() ; i++){
			((ITransport) mTransportListeners.get(i)).onNewFriend(fs);
		}
	}

	@Override
	public void onNewRoom(RoomSchema rs) {
		for(int i = 0 ; i < mTransportListeners.size() ; i++){
			((ITransport) mTransportListeners.get(i)).onNewRoom(rs);
		}
	}

	@Override
	public void onNewMessage(final MessageSchema ms) {
		if(SessionStore.mSessionStore.addMessageToRoom(ms))
		{
			try {
				Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
				r.play();

			} catch (Exception e) {
				e.printStackTrace();
			}
			if(ms.mType.equals("Text")){
				for(int i = 0 ; i < mTransportListeners.size() ; i++){
					((ITransport) mTransportListeners.get(i)).onNewMessage(ms);
				}
			}
			else if(ms.mType.equals("Picture")){
				Thread imageLoader = new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							//							URL url = new URL(SettingSchema.mBaseRestUrl + "uploaded/entities/" + ms.mBody);
							//							showToast(SettingSchema.mBaseRestUrl + "uploaded/entities/" + ms.mBody);
							//							Log.d(TAG, url.toExternalForm());
							//							HttpGet httpRequest = null;
							//							httpRequest = new HttpGet(url.toURI());
							//							HttpClient httpclient = new DefaultHttpClient();
							//							HttpResponse response = (HttpResponse) httpclient
							//							.execute(httpRequest);
							//							HttpEntity entity = response.getEntity();
							//							BufferedHttpEntity b_entity = new BufferedHttpEntity(entity);
							//							InputStream input = b_entity.getContent();
							//							Bitmap image = BitmapFactory.decodeStream(input);
							//							File pictureFileDirectory = new File(Environment.getExternalStoragePublicDirectory(
							//									Environment.DIRECTORY_PICTURES).getPath() + "/entities");
							//							pictureFileDirectory.mkdirs();
							//							File pictureFile = new File(pictureFileDirectory, ms.mId + ".png");
							//							FileOutputStream fos = new FileOutputStream(pictureFile);
							//							image.compress(Bitmap.CompressFormat.PNG, 100, fos);
							//							fos.close();
							for(int i = 0 ; i < mTransportListeners.size() ; i++){
								((ITransport) mTransportListeners.get(i)).onNewMessage(ms);
							}
						} catch (final Exception ex) {
							showToast(ex.getMessage());
						}
					}
				});
				imageLoader.start();
			}
		}
	}

	@Override
	public void onCurrentProfileResult(String username, String userId,
			String firstName, String lastName, String mobile, String email) {
		mCurrentUserProfile.m_uuid = userId;
		mCurrentUserProfile.m_email = email;
		mCurrentUserProfile.m_firstName = firstName;
		mCurrentUserProfile.m_lastName = lastName;
		for(int i = 0 ; i < mTransportListeners.size() ; i++){
			((ITransport) mTransportListeners.get(i)).onCurrentProfileResult(username, userId, firstName, lastName, mobile, email);
		}
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
	public void onRoomAdded(String roomName, String roomId, String roomDesc,
			String roomLogo, String roomType, String members) {
		RoomSchema rs = new RoomSchema(roomId, roomName, roomDesc, roomLogo, roomType);
		SessionStore.mSessionStore.addRoom(rs);
		String[] sMember = members.split(",");
		for(String str: sMember){
			rs.addMember(str);
		}
		for(int i = 0 ; i < mTransportListeners.size() ; i++){
			((ITransport) mTransportListeners.get(i)).onRoomAdded(roomName, roomId, roomDesc, roomLogo, roomType, members);
		}
	}

	@Override
	public void onMembersAddedToRoom(String roomId, String memberId) {
		// TODO Auto-generated method stub
		RoomSchema room = SessionStore.mSessionStore.getRoomById(roomId);
		if(room != null){
			room.addMember(memberId);
		}
	}

	@Override
	public void notifyAddedByFriend(String invitedBy) {
		try{
			//TODO: Show Notification
			//TODO: Get lists via rest
			//showToast("Invited by : " + invitedBy);
			getFriendList();
		}
		catch(Exception ex){
			logCatDebug(ex.getMessage());
		}
	}

	@Override
	public void notifyAddedToRoom(String invitedBy, String roomId) {
		try{
			//TODO: Show Notification
			//TODO: Get lists via rest
			//showToast("Invited by : " + invitedBy + " : " + roomId);
			mWebsocketService.getIndividualRooms();
		}
		catch(Exception ex){
			logCatDebug(ex.getMessage());
		}
	}

}