package org.ertebat.ui;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.ertebat.schema.FriendSchema;
import org.ertebat.schema.MessageSchema;
import org.ertebat.schema.RoomSchema;
import org.ertebat.schema.SessionStore;
import org.ertebat.transport.ITransport;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class BaseFragment extends Fragment implements ITransport {

	protected static String LOG_TAG = "";
	protected BroadcastReceiver mSipBroadCastRecv;
	protected NgnEngine mEngine;
	protected INgnConfigurationService mConfigurationService;
	protected INgnSipService mSipService;
	protected NgnAVSession mAVSession;
	protected INgnHistoryService mHistoryService;
	protected CallActivityStatus mCurrentStatus = CallActivityStatus.CAS_Idle;
	protected Activity Me = null;
	protected String mClassName;
	protected static int mLastCommand = 0;
	protected static SharedPreferences mSharedPref;

	protected Handler mHandler;
	protected ProgressDialog mDialog;
	protected Context This = null;
	protected BaseActivity mBaseActivity = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mHandler = new Handler();
		This = container.getContext();

		if (mSharedPref == null)
			mSharedPref = PreferenceManager.getDefaultSharedPreferences(This);
		
		mBaseActivity = (BaseActivity)getActivity();
		((BaseActivity)getActivity()).registerToTransportListeners(this);
		
		return null;
	}


	protected void SignInNGN() {
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
		mSipService.register(this.getActivity());
	}

	protected void SignOutNGN() {
		if (mSipService != null) {
			if (mSipService.isRegistered())
				mSipService.unRegister();
		}		
	}

	protected void ShowToast(final String message) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(This, message, Toast.LENGTH_SHORT)
				.show();
			}
		});		
	}

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

	protected void ShowDialog(final String title, final String message) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				mDialog = ProgressDialog.show(This, title, message, true);
				TextView tvMessage = (TextView) mDialog
						.findViewById(android.R.id.message);
				tvMessage.setGravity(Gravity.LEFT);
			}
		});		
	}

	protected void CloseDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
	}

	@Override
	public void onConnectedToServer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnctedFromServer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNewFriend(FriendSchema fs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNewRoom(RoomSchema rs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNewMessage(MessageSchema ms) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCurrentProfileResult(String username, String userId,
			String firstName, String lastName, String mobile, String email) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
	}

	@Override
	public void onMembersAddedToRoom(String roomId, String memberId) {
		// TODO Auto-generated method stub
	}
}
