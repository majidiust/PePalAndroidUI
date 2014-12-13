package org.ertebat.ui;

/**
 * @author Majid
 * @author MCastor
 *
 */

import java.util.ArrayList;
import java.util.List;

import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnInviteEventTypes;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnAVSession;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import org.ertebat.transport.websocket.IWebsocketServiceCallback;
import org.ertebat.transport.websocket.IWebsocketService;

public class BaseActivity extends FragmentActivity {
	public static final int DIALOG_MULTI_CHOICE = 1;
	public static final int DIALOG_TAKE_PICTURE = 2;
	public static final int DIALOG_PICTURE_GALLERY = 3;
	public static final int DIALOG_FRAGMENT_MULTI_CHOICE = 4;

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
	protected String TAG = "BaseActivity";

	//Websocket service
	protected Intent mWebsocketIntent;
	protected IWebsocketService mWebsocketService;
	protected IWebsocketServiceCallback mDataCallback = new IWebsocketServiceCallback.Stub(){

		@Override
		public void debug(String msg) throws RemoteException {
			Log.d(TAG, msg);
		}

		@Override
		public void loggedInResult(int code, String details)
				throws RemoteException {
			Log.d(TAG, String.valueOf(code) + " : " + details);
		}

		@Override
		public void newMessage(String from, String roomId, String date,
				String time, String content) throws RemoteException {
			Log.d(TAG, from + " : " + roomId + " : " + date + " : " + time + " : " + content);
		}

		@Override
		public void connectedToHost(String uri) throws RemoteException {
			Log.d(TAG,uri);
		}

		@Override
		public void disConnectedFromHost() throws RemoteException {
			Log.d(TAG, "disConnectedFromHost");
		}
	};
	
	protected ServiceConnection mWebsocketServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if(mWebsocketService != null)
			{
				try {
					mWebsocketService.unregisterCallback(mDataCallback);
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
				mWebsocketService.registerCallback(mDataCallback);
			}
			catch (RemoteException e) {
				Log.d(TAG, e.getMessage());
			}
		}
	};
	
	protected static int mLastCommand = 0;

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
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mHandler = new Handler();
		This = this;

		int screenLayout = getResources().getConfiguration().screenLayout;
		screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;
		IsTablet = screenLayout == Configuration.SCREENLAYOUT_SIZE_XLARGE;

		if (FontRoya == null)
			FontRoya = Typeface.createFromAsset(getAssets(), "broya.ttf");
		if (FontKoodak == null)
			FontKoodak = Typeface.createFromAsset(getAssets(), "bkoodkbd.ttf");
		if (FontNazanin == null)
			FontNazanin = Typeface.createFromAsset(getAssets(), "bnazaninbd_0.ttf");
		
		mWebsocketIntent = new Intent(IWebsocketService.class.getName());
		startService(mWebsocketIntent);
		bindService(mWebsocketIntent, mWebsocketServiceConnection, 0);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (IsTablet)
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
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
		}
		catch (Exception ex) {
			showToast(ex.getMessage());
		}
		super.onDestroy();
	}

	protected void SetCallState(NgnInviteEventTypes callState) {
		// TODO Auto-generated method stub

	}

	protected void loadInCallView(){

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

	public static String[] getContactNames() {
		String[] names = new String[mContacts.size()];

		for (int i = 0; i < mContacts.size(); i++) {
			names[i] = mContacts.get(i).ContactName;
		}

		return names;
	}
}