package org.ertebat.ui;
import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.events.NgnInviteEventTypes;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnContact;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnUriUtils;
import org.ertebat.R;
import org.ertebat.R.drawable;
import org.ertebat.R.id;
import org.ertebat.R.layout;
import org.ertebat.R.string;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.format.Time;
//import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewParent;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CallFragment extends BaseFragment {
	
	private static final int MAX_NUMBERS = 20;

	private String mDialedNumber = "";
	private long mCallDuration = 0;
	private long mLastDurationSeconds = 0;

	private TextView mTextNumber;
	private TextView mTextAudioCallDuration;
	private TextView mTextInCallAudioDestination;
	private TextView mTextVideoCallDuration;
	private TextView mTextInCallVideoDestination;
	private TextView mTextDialingDestination;
	private TextView mTextIncomingCaller;
	private Button[] mDialPadButtons;
	private Button mBtnRemove;
	private Button mBtnSwapCamera;
	private Button mBtnSipToggle;
	private Button mBtnSettings;
	private ImageView mImgDialing;

	private RelativeLayout mLayoutNumberBar;
	private LinearLayout mLayoutCallButtons;
	private RelativeLayout mLayoutDialing;
	private RelativeLayout mLayoutInCallAudio;
	private RelativeLayout mLayoutInCallVideo;
	private RelativeLayout mLayoutIncoming;
	private RelativeLayout mLayoutDialPad;

	private FrameLayout mViewRemoteVideoPreview;
	private FrameLayout mViewLocalVideoPreview;

	private boolean mIsSipConnected = false;
	private boolean mIsVideoCall;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		View rootView = inflater.inflate(R.layout.fragment_call, container, false);
	

		mLayoutNumberBar = (RelativeLayout)rootView.findViewById(R.id.layoutCallNumberBar);
		mLayoutCallButtons = (LinearLayout)rootView.findViewById(R.id.layoutCallDialButtons);
		mLayoutDialing = (RelativeLayout)rootView.findViewById(R.id.layoutCallDialingInfo);
		mLayoutInCallAudio = (RelativeLayout)rootView.findViewById(R.id.layoutAudioCallEstablished);
		mLayoutInCallVideo = (RelativeLayout)rootView.findViewById(R.id.layoutVideoCallEstablished);
		mLayoutIncoming = (RelativeLayout)rootView.findViewById(R.id.layoutCallIncoming);
		mLayoutDialPad = (RelativeLayout)rootView.findViewById(R.id.layoutCallDialPad);

		mTextNumber = (TextView)rootView.findViewById(R.id.txtCallNumber);
		mTextAudioCallDuration = (TextView)rootView.findViewById(R.id.txtAudioCallTimer);
		mTextInCallAudioDestination = (TextView)rootView.findViewById(R.id.txtAudioCallDestination);
		mTextVideoCallDuration = (TextView)rootView.findViewById(R.id.txtVideoCallTimer);
		mTextInCallVideoDestination = (TextView)rootView.findViewById(R.id.txtVideoCallDestination);
		mTextDialingDestination = (TextView)rootView.findViewById(R.id.txtCallDialDestination);
		mTextIncomingCaller = (TextView)rootView.findViewById(R.id.txtCallIncomingNumber);

		mBtnRemove = (Button)rootView.findViewById(R.id.btnCallBackspace);
		mBtnRemove.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mDialedNumber.length() > 0) {
					mDialedNumber = mDialedNumber.substring(0, mDialedNumber.length() - 1);
					mTextNumber.setText(mDialedNumber);
				}
			}
		});

		mBtnSwapCamera = (Button)rootView.findViewById(R.id.btnVideoCallSwapCamera);
		mBtnSwapCamera.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mBaseActivity.mAVSession != null){
					mBaseActivity.mAVSession.toggleCamera();
				}
			}
		});
		
		mDialPadButtons = new Button[12];
		mDialPadButtons[0] = (Button)rootView.findViewById(R.id.btnCallDial0);
		mDialPadButtons[1] = (Button)rootView.findViewById(R.id.btnCallDial1);
		mDialPadButtons[2] = (Button)rootView.findViewById(R.id.btnCallDial2);
		mDialPadButtons[3] = (Button)rootView.findViewById(R.id.btnCallDial3);
		mDialPadButtons[4] = (Button)rootView.findViewById(R.id.btnCallDial4);
		mDialPadButtons[5] = (Button)rootView.findViewById(R.id.btnCallDial5);
		mDialPadButtons[6] = (Button)rootView.findViewById(R.id.btnCallDial6);
		mDialPadButtons[7] = (Button)rootView.findViewById(R.id.btnCallDial7);
		mDialPadButtons[8] = (Button)rootView.findViewById(R.id.btnCallDial8);
		mDialPadButtons[9] = (Button)rootView.findViewById(R.id.btnCallDial9);
		mDialPadButtons[10] = (Button)rootView.findViewById(R.id.btnCallDialStar);
		mDialPadButtons[11] = (Button)rootView.findViewById(R.id.btnCallDialSharp);
		
		for (int i = 0; i < 10; i++) {
			mDialPadButtons[i].setTypeface(BaseActivity.FontRoya);
		}

		mDialPadButtons[0].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onNumberPressed(DialPadButton.DPB_0);
			}
		});
		mDialPadButtons[1].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onNumberPressed(DialPadButton.DPB_1);
			}
		});
		mDialPadButtons[2].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onNumberPressed(DialPadButton.DPB_2);
			}
		});
		mDialPadButtons[3].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onNumberPressed(DialPadButton.DPB_3);
			}
		});
		mDialPadButtons[4].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onNumberPressed(DialPadButton.DPB_4);
			}
		});
		mDialPadButtons[5].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onNumberPressed(DialPadButton.DPB_5);
			}
		});
		mDialPadButtons[6].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onNumberPressed(DialPadButton.DPB_6);
			}
		});
		mDialPadButtons[7].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onNumberPressed(DialPadButton.DPB_7);
			}
		});
		mDialPadButtons[8].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onNumberPressed(DialPadButton.DPB_8);
			}
		});
		mDialPadButtons[9].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onNumberPressed(DialPadButton.DPB_9);
			}
		});
		mDialPadButtons[10].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onNumberPressed(DialPadButton.DPB_Star);
			}
		});
		mDialPadButtons[11].setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onNumberPressed(DialPadButton.DPB_Sharp);
			}
		});

		Button btn = (Button)rootView.findViewById(R.id.btnCallVoiceCall);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mBaseActivity.mSipService.isRegistered())
				{
					final String remoteUri = mDialedNumber;
					final String validUri = NgnUriUtils.makeValidSipUri(remoteUri);
					NgnAVSession AVSession = NgnAVSession.createOutgoingSession(mBaseActivity.mSipService.getSipStack(),
							NgnMediaType.Audio);//.AudioVideo);
					AVSession.setRemotePartyUri(remoteUri); // HACK
					if(AVSession.makeCall(validUri)){
						ShowToast("Making a call!");
					} else {
						ShowToast("Failed to make call!");
					}
				}
				else
				{
					showAlert("Please register to server in order to make call.");
				}
			}
		});

		btn = (Button)rootView.findViewById(R.id.btnCallVideoCall);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mBaseActivity.mSipService.isRegistered())
				{
					final String remoteUri = mDialedNumber;
					final String validUri = NgnUriUtils.makeValidSipUri(remoteUri);
					NgnAVSession AVSession = NgnAVSession.createOutgoingSession(mBaseActivity.mSipService.getSipStack(),
							NgnMediaType.AudioVideo);
					AVSession.setRemotePartyUri(remoteUri); // HACK
					if(AVSession.makeCall(validUri)){
						ShowToast("Making a call!");
					} else {
						ShowToast("Failed to make call!");
					}
				}
				else
				{
					showAlert("Please register to server in order to make call.");
				}
			}
		});
		
		btn = (Button)rootView.findViewById(R.id.btnCallCancel);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mDialedNumber = "";
				mTextNumber.setText(mDialedNumber);
				changeStatus(CallActivityStatus.CAS_Idle);
			}
		});

		btn = (Button)rootView.findViewById(R.id.btnDialCancel);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mBaseActivity.mAVSession != null)
					mBaseActivity.mAVSession.hangUpCall();
			}
		});

		btn = (Button)rootView.findViewById(R.id.btnAudioCallEnd);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mBaseActivity.mAVSession != null) {
					mBaseActivity.mAVSession.hangUpCall();
				}
			}
		});
		
		btn = (Button)rootView.findViewById(R.id.btnVideoCallEnd);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mBaseActivity.mAVSession != null) {
					mBaseActivity.mAVSession.hangUpCall();
				}
				//				changeStatus(CallActivityStatus.CAS_NumberEntry);
			}
		});

		btn = (Button)rootView.findViewById(R.id.btnCallIncomingReject);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mBaseActivity.mAVSession != null) {
					mBaseActivity.mAVSession.hangUpCall();
				}
			}
		});

		btn = (Button)rootView.findViewById(R.id.btnCallIncomingAccept);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mBaseActivity.mAVSession != null) {
					mBaseActivity.mAVSession.acceptCall();
				}
			}
		});

		mBtnSipToggle = (Button)rootView.findViewById(R.id.btnCallBottomSip);
		mBtnSipToggle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!mIsSipConnected) {
					mBaseActivity.signInNGN();
					/**
					 * TODO: @Majid, when successfully connected to the SIP server change the button background like below
					 */
					if (!BaseActivity.IsTablet)
						mBtnSipToggle.setBackgroundResource(R.drawable.style_btn_bar_sip_disconnect);
					else
						mBtnSipToggle.setText(getResources().getString(R.string.btn_signout_sip));
					
					mIsSipConnected = true;
				} else {
					mBaseActivity.signOutNGN();
					/**
					 * TODO: @Majid, when successfully disconnected from the SIP server change the button background like below
					 */
					if (!BaseActivity.IsTablet)
						mBtnSipToggle.setBackgroundResource(R.drawable.style_btn_bar_sip_connect);
					else
						mBtnSipToggle.setText(getResources().getString(R.string.btn_signin_sip));
					
					mIsSipConnected = false;
				}
			}
		});
		
		mBtnSettings = (Button)rootView.findViewById(R.id.btnCallBottomSettings);			
		mBtnSettings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(This, SettingsActivity.class);
				startActivity(intent);			}
		});
		
		if (BaseActivity.IsTablet) {
			mBtnSipToggle.setTypeface(BaseActivity.FontNazanin);
			mBtnSettings.setTypeface(BaseActivity.FontNazanin);
		}
		
		mImgDialing = (ImageView)rootView.findViewById(R.id.imgCallDialing);
		mImgDialing.setBackgroundResource(R.drawable.anim_dial);
		
		mViewRemoteVideoPreview = (FrameLayout)rootView.findViewById(R.id.view_call_incall_video_FrameLayout_remote_video);
		mViewLocalVideoPreview = (FrameLayout)rootView.findViewById(R.id.view_call_incall_video_FrameLayout_local_video);

		// CHECK
		NgnInviteEventArgs args = getActivity().getIntent().getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
		if(args != null)
		{
			mBaseActivity.mAVSession = NgnAVSession.getSession(args.getSessionId());
			if(mBaseActivity.mAVSession == null){
				changeStatus(CallActivityStatus.CAS_Idle);
			}
			else {
				mIsVideoCall = mBaseActivity.mAVSession.getMediaType() == NgnMediaType.AudioVideo ||
						mBaseActivity.mAVSession.getMediaType() == NgnMediaType.Video;
				final NgnContact remoteParty = mBaseActivity.mEngine.getContactService().getContactByUri(mBaseActivity.mAVSession.getRemotePartyUri());
				String number;
				if(remoteParty != null){
					number = remoteParty.getDisplayName();
				}
				else{
					number = NgnUriUtils.getDisplayName(mBaseActivity.mAVSession.getRemotePartyUri());
				}
				if(NgnStringUtils.isNullOrEmpty(number)){
					number = "0000000000";
				}

				mDialedNumber = number;
				mTextIncomingCaller.setText(mDialedNumber);
				changeStatus(CallActivityStatus.CAS_Incoming);
				mBaseActivity.mAVSession.setContext(this.getActivity());
			}
		}
		else
		{
			changeStatus(CallActivityStatus.CAS_NumberEntry);
		}
		
		return rootView;
	}

	protected void onNumberPressed(final DialPadButton button) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				if (mDialedNumber.length() >= MAX_NUMBERS ||
						(mBaseActivity.mCurrentStatus != CallActivityStatus.CAS_Idle &&
								mBaseActivity.mCurrentStatus != CallActivityStatus.CAS_NumberEntry))
					return;

				if (mBaseActivity.mCurrentStatus == CallActivityStatus.CAS_Idle)
					changeStatus(CallActivityStatus.CAS_NumberEntry);

				mDialedNumber += button.toString();
				mTextNumber.setText(mDialedNumber);
			}
		});
	}

	protected void changeStatus(CallActivityStatus newStatus) {			
		mBaseActivity.mCurrentStatus = newStatus;
		switch (mBaseActivity.mCurrentStatus) {
		case CAS_Idle:
			mLayoutNumberBar.setVisibility(View.VISIBLE);
			mLayoutCallButtons.setVisibility(View.INVISIBLE);
			mLayoutDialing.setVisibility(View.INVISIBLE);
			mLayoutInCallAudio.setVisibility(View.INVISIBLE);
			mLayoutInCallVideo.setVisibility(View.INVISIBLE);
			mLayoutIncoming.setVisibility(View.INVISIBLE);
			mLayoutDialPad.setVisibility(View.VISIBLE);
			break;
		case CAS_NumberEntry:
			mLayoutNumberBar.setVisibility(View.VISIBLE);
			mLayoutCallButtons.setVisibility(View.VISIBLE);
			mLayoutDialing.setVisibility(View.INVISIBLE);
			mLayoutInCallAudio.setVisibility(View.INVISIBLE);
			mLayoutInCallVideo.setVisibility(View.INVISIBLE);
			mLayoutIncoming.setVisibility(View.INVISIBLE);
			mLayoutDialPad.setVisibility(View.VISIBLE);
			break;
		case CAS_Dialing:
			mLayoutNumberBar.setVisibility(View.INVISIBLE);
			mLayoutCallButtons.setVisibility(View.INVISIBLE);
			mLayoutDialing.setVisibility(View.VISIBLE);
			mLayoutInCallAudio.setVisibility(View.INVISIBLE);
			mLayoutInCallVideo.setVisibility(View.INVISIBLE);
			mLayoutIncoming.setVisibility(View.INVISIBLE);
			if (BaseActivity.IsTablet) {
				mLayoutDialPad.setVisibility(View.VISIBLE);
			} else {
				mLayoutDialPad.setVisibility(View.INVISIBLE);
			}
			break;
		case CAS_InCallAudio:
			mLayoutNumberBar.setVisibility(View.INVISIBLE);
			mLayoutCallButtons.setVisibility(View.INVISIBLE);
			mLayoutDialing.setVisibility(View.INVISIBLE);
			mLayoutInCallAudio.setVisibility(View.VISIBLE);
			mLayoutInCallVideo.setVisibility(View.INVISIBLE);
			mLayoutIncoming.setVisibility(View.INVISIBLE);
			if (BaseActivity.IsTablet) {
				mLayoutDialPad.setVisibility(View.VISIBLE);
			} else {
				mLayoutDialPad.setVisibility(View.INVISIBLE);
			}
			break;
		case CAS_InCallVideo:
			mLayoutNumberBar.setVisibility(View.INVISIBLE);
			mLayoutCallButtons.setVisibility(View.INVISIBLE);
			mLayoutDialing.setVisibility(View.INVISIBLE);
			mLayoutInCallAudio.setVisibility(View.INVISIBLE);
			mLayoutInCallVideo.setVisibility(View.VISIBLE);
			mLayoutIncoming.setVisibility(View.INVISIBLE);
			if (BaseActivity.IsTablet) {
				mLayoutDialPad.setVisibility(View.VISIBLE);
			} else {
				mLayoutDialPad.setVisibility(View.INVISIBLE);
			}
			break;
		case CAS_Incoming:
			mLayoutNumberBar.setVisibility(View.INVISIBLE);
			mLayoutCallButtons.setVisibility(View.INVISIBLE);
			mLayoutDialing.setVisibility(View.INVISIBLE);
			mLayoutInCallAudio.setVisibility(View.INVISIBLE);
			mLayoutInCallVideo.setVisibility(View.INVISIBLE);
			mLayoutIncoming.setVisibility(View.VISIBLE);
			if (BaseActivity.IsTablet) {
				mLayoutDialPad.setVisibility(View.VISIBLE);
			} else {
				mLayoutDialPad.setVisibility(View.INVISIBLE);
			}
			break;
		default:
			break;
		}
	}

	protected void resetCallTimer() {
		mCallDuration = 0;
		
		if (mIsVideoCall) {			
			mTextVideoCallDuration.setText("00:00:00:00");
		}
		else {
			mTextAudioCallDuration.setText("00:00:00:00");
		}
	}

	@Override
	public void OnIncommingCall(NgnInviteEventArgs args) {
		if(args != null)
		{
			mBaseActivity.mAVSession = NgnAVSession.getSession(args.getSessionId());
			if(mBaseActivity.mAVSession == null){
				changeStatus(CallActivityStatus.CAS_Idle);
			}
			else {
				mIsVideoCall = mBaseActivity.mAVSession.getMediaType() == NgnMediaType.AudioVideo ||
						mBaseActivity.mAVSession.getMediaType() == NgnMediaType.Video;
				final NgnContact remoteParty = mBaseActivity.mEngine.getContactService().getContactByUri(mBaseActivity.mAVSession.getRemotePartyUri());
				String number;
				if(remoteParty != null){
					number = remoteParty.getDisplayName();
				}
				else{
					number = NgnUriUtils.getDisplayName(mBaseActivity.mAVSession.getRemotePartyUri());
				}
				if(NgnStringUtils.isNullOrEmpty(number)){
					number = "0000000000";
				}

				mDialedNumber = number;
				mTextIncomingCaller.setText(mDialedNumber);
				changeStatus(CallActivityStatus.CAS_Incoming);
				mBaseActivity.mAVSession.setContext(getActivity());
			}
		}
		else
		{
			changeStatus(CallActivityStatus.CAS_Idle);
		}
	}
	
	@Override
	public void SetCallState(NgnInviteEventTypes callState) {
		AnimationDrawable frameAnimation = null;
		switch (callState) {
		case INPROGRESS:
			changeStatus(CallActivityStatus.CAS_Dialing);
			frameAnimation = (AnimationDrawable)mImgDialing.getBackground();
			frameAnimation.start();
			mTextDialingDestination.setText(mDialedNumber);
			break;
		case CONNECTED:
		case EARLY_MEDIA:
			mIsVideoCall = mBaseActivity.mAVSession.getMediaType() == NgnMediaType.AudioVideo || mBaseActivity.mAVSession.getMediaType() == NgnMediaType.Video;
			frameAnimation = (AnimationDrawable)mImgDialing.getBackground();
			frameAnimation.stop();
			
			if (mIsVideoCall) {
				mTextInCallVideoDestination.setText(mDialedNumber);
				changeStatus(CallActivityStatus.CAS_InCallVideo);
			} else {
				mTextInCallAudioDestination.setText(mDialedNumber);
				changeStatus(CallActivityStatus.CAS_InCallAudio);
			}
			resetCallTimer();
			mBaseActivity.mAVSession.setContext(this.getActivity());
			loadInCallView();
			new Thread(new Runnable() {

				@Override
				public void run() {
					Time time = new Time();
					time.setToNow();
					mLastDurationSeconds = time.second;

					while (mBaseActivity.mCurrentStatus == CallActivityStatus.CAS_InCallAudio ||
							mBaseActivity.mCurrentStatus == CallActivityStatus.CAS_InCallVideo) {
						time.setToNow();
						long nowSeconds = time.second;

						if (mLastDurationSeconds != nowSeconds) {
							mLastDurationSeconds = nowSeconds;

							mCallDuration += 1;

							int sec = (int) (mCallDuration % 60);
							int min = (int) ((mCallDuration / 60) % 60);
							int hour = (int) ((mCallDuration / 3600) % 24);
							int day = (int)(mCallDuration / 86400);

							String strZero = "0";

							String strSec = String.valueOf(sec);
							if (sec < 10)
								strSec = strZero + strSec;

							String strMin = String.valueOf(min);
							if (min < 10)
								strMin = strZero + strMin;

							String strHour = String.valueOf(hour);
							if (hour < 10)
								strHour = strZero + strHour;

							String strDay = String.valueOf(day);
							if (day < 10)
								strDay = strZero + strDay;

							final String strTotal = strDay + ":" + strHour + ":" + strMin + ":" + strSec;

							mHandler.post(new Runnable() {

								@Override
								public void run() {
									if (mIsVideoCall)
										mTextVideoCallDuration.setText(strTotal);
									else
										mTextAudioCallDuration.setText(strTotal);
								}
							});
						}

						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}).start();
			break;
		case TERMINATED:
		case TERMWAIT:
			changeStatus(CallActivityStatus.CAS_NumberEntry);
			clearVideoPreview();
			break;
		default:
			break;
		}
	}

	protected void loadInCallView(){
		if(mIsVideoCall)
			loadInCallVideoView();
	}
	
	private void loadInCallVideoView(){
		loadVideoPreview();
		startStopVideo(mBaseActivity.mAVSession.isSendingVideo());
	}

	protected void clearVideoPreview()
	{
		try
		{
			mViewRemoteVideoPreview.removeAllViews();
			mViewLocalVideoPreview.removeAllViews();
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
	}

	private void loadVideoPreview(){
		mViewRemoteVideoPreview.removeAllViews();
		final View remotePreview = mBaseActivity.mAVSession.startVideoConsumerPreview();
		if(remotePreview != null){
			final ViewParent viewParent = remotePreview.getParent();
			if(viewParent != null && viewParent instanceof ViewGroup){
				((ViewGroup)(viewParent)).removeView(remotePreview);
			}
			mViewRemoteVideoPreview.addView(remotePreview);
		}
	}

	private void startStopVideo(boolean bStart){
		if(!mIsVideoCall){
			return;
		}

		mBaseActivity.mAVSession.setSendingVideo(bStart);

		if(mViewLocalVideoPreview != null){
			mViewLocalVideoPreview.removeAllViews();
			if(bStart){
				final View localPreview = mBaseActivity.mAVSession.startVideoProducerPreview();
				if(localPreview != null){
					final ViewParent viewParent = localPreview.getParent();
					if(viewParent != null && viewParent instanceof ViewGroup){
						((ViewGroup)(viewParent)).removeView(localPreview);
					}
					if(localPreview instanceof SurfaceView){
						((SurfaceView)localPreview).setZOrderOnTop(true);
					}
					mViewLocalVideoPreview.addView(localPreview);
					mViewLocalVideoPreview.bringChildToFront(localPreview);
				}
			}
			mViewLocalVideoPreview.setVisibility(bStart ? View.VISIBLE : View.GONE);
			mViewLocalVideoPreview.bringToFront();
		}
	}

	public enum CallState {
		CS_Idle,
		CS_Dialing
	}
}
