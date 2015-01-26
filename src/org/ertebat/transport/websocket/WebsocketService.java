/**
 * 
 */
package org.ertebat.transport.websocket;

import java.util.Vector;

import org.ertebat.schema.FriendSchema;
import org.ertebat.schema.MessageSchema;
import org.ertebat.schema.RoomSchema;
import org.ertebat.schema.SessionStore;
import org.ertebat.transport.websocket.IWebsocketService;
import org.ertebat.transport.websocket.IWebsocketServiceCallback;
import org.ertebat.ui.BaseActivity;
import org.ertebat.ui.ChatActivity;
import org.ertebat.ui.MainActivity;
import org.json.JSONArray;
import org.json.JSONObject;

import de.tavendo.autobahn.WebSocketConnection;
import android.R;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import android.view.ViewDebug.FlagToString;
import de.tavendo.autobahn.WebSocket;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;

/**
 * @author Majid
 *
 */
public class WebsocketService extends Service {
	private static final String TAG = "WebsocketService";
	final RemoteCallbackList<IWebsocketServiceCallback> mCallbacks = new RemoteCallbackList<IWebsocketServiceCallback>();
	protected  String wsuri = "ws://192.168.1.5:1337";
	private int mNotificationID;
	private PendingIntent mAlarmIntent;
	private Context context = this;
	//websocket requirements
	protected  WebSocketConnection mConnection = new WebSocketConnection();

	@Override
	public void onCreate() {
		super.onCreate();
		logCatDebug("Service Started");
	}

	@Override
	public boolean onUnbind (Intent intent){
		return true;
	}

	private void showNotification(String title, String text, Intent intent){
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

		NotificationManager notificationManager;
		notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
		.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_menu_help))
		.setSmallIcon(R.drawable.ic_dialog_info)
		.setContentTitle(title)
		.setContentText(text)
		.setContentIntent(contentIntent).setAutoCancel(true);
		notificationManager.notify(mNotificationID, builder.build());
	}

	private void getPictureImage(){
		try{

		}
		catch(Exception ex){
			logCatDebug(ex.getMessage());
		}
	}

	private void start() {
		try {
			if(mConnection.isConnected() == false){
				mConnection.connect(wsuri, new WebSocketConnectionHandler() {
					@Override
					public void onOpen() {
						logCatDebug("Status: Connected to " + wsuri);
						int N = mCallbacks.beginBroadcast();
						for (int i = 0; i < N; i++) {
							try {
								mCallbacks.getBroadcastItem(i).connectedToHost(wsuri);
							} 
							catch (RemoteException e) {
								logCatDebug(e.getMessage());
							}
						}
						mCallbacks.finishBroadcast();
					}

					@Override
					public void onTextMessage(String payload) {
						logCatDebug("Got echo: " + payload);
						try{
							JSONObject jsonObject = new JSONObject(payload);
							//debug(payload);
							int code = jsonObject.getInt("code");
							if(code == 100){
								int N = mCallbacks.beginBroadcast();
								for (int i = 0; i < N; i++) {
									try {
										mCallbacks.getBroadcastItem(i).authorizationRequest();
									} 
									catch (RemoteException e) {
										logCatDebug(e.getMessage());
									}
								}
								mCallbacks.finishBroadcast();
							}
							else if(code == 101){
								int N = mCallbacks.beginBroadcast();
								for (int i = 0; i < N; i++) {
									try {
										mCallbacks.getBroadcastItem(i).authorized();
									} 
									catch (RemoteException e) {
										logCatDebug(e.getMessage());
									}
								}
								mCallbacks.finishBroadcast();
							}
							else if(code == 6){
								JSONObject tmpObject = new JSONObject(jsonObject.getString("profile"));
								String firstName = "--";
								String lastName = "--";
								String mobile = "--";
								String email = "--";
								try{
									firstName = tmpObject.getString("firstName");
								}
								catch(Exception ex){
									logCatDebug(ex.getMessage());
								}
								try{
									lastName = tmpObject.getString("lastName");
								}
								catch(Exception ex){
									logCatDebug(ex.getMessage());
								}
								try{
									mobile = tmpObject.getString("mobileNumber");
								}
								catch(Exception ex){
									logCatDebug(ex.getMessage());
								}
								try{
									email = tmpObject.getString("email");
								}
								catch(Exception ex){
									logCatDebug(ex.getMessage());
								}
								int N = mCallbacks.beginBroadcast();
								for (int i = 0; i < N; i++) {
									try {

										mCallbacks.getBroadcastItem(i).currentProfileResult(
												tmpObject.getString("username"), 
												tmpObject.getString("id"), 
												firstName, lastName, mobile, email);
									} 
									catch (RemoteException e) {
										logCatDebug(e.getMessage());
									}
								}
								mCallbacks.finishBroadcast();
							}
							else if(code == 9){
								try{
									JSONArray friends = jsonObject.getJSONArray("friends");
									//debug(jsonObject.getString("friends"));
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
										int N = mCallbacks.beginBroadcast();
										for (int i = 0; i < N; i++) {
											try {

												mCallbacks.getBroadcastItem(i).friendAdded(fs.m_friendUserName, fs.m_friendId, fs.m_state);
											} 
											catch (RemoteException e) {
												logCatDebug(e.getMessage());
											}
										}
										mCallbacks.finishBroadcast();
									}
								}
								catch(Exception ex){
									debug(ex.getMessage());
								}
							}
							else if(code == 8){
								Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
								Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
								r.play();
								
								JSONObject tmpObject = new JSONObject(jsonObject.getString("friend"));
								String id ,userName, status;
								id = tmpObject.getString("friendId");
								userName = tmpObject.getString("friendUsername");
								status = tmpObject.getString("status");
								int N = mCallbacks.beginBroadcast();
								for (int i = 0; i < N; i++) {
									try {

										mCallbacks.getBroadcastItem(i).friendAdded(userName, id, status);
									} 
									catch (RemoteException e) {
										logCatDebug(e.getMessage());
									}
								}
								mCallbacks.finishBroadcast();
							}
							else if(code == 103){
								try{
									Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
									Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
									r.play();
									JSONObject tmpObject = new JSONObject(jsonObject.getString("value"));
									String type = tmpObject.getString("type");
									String roomId = tmpObject.getString("roomId");
									String from = tmpObject.getString("from");
									String content = tmpObject.getString("content");
									String date = tmpObject.getString("date");
									String fromId = tmpObject.getString("fromId");
									String messageId = tmpObject.getString("id");
									sendTextMessageAck(messageId);

									boolean isExist = false;
									//debug(payload);
									int N = mCallbacks.beginBroadcast();
									for (int i = 0; i < N; i++) {
										try {
											isExist = true;
											mCallbacks.getBroadcastItem(i).newMessage(type, messageId, fromId, from, roomId, date, date, content);
										} 
										catch (RemoteException e) {
											logCatDebug(e.getMessage());
										}
									}
									mCallbacks.finishBroadcast();
									if(isExist == false){
										//if(SessionStore.mSessionStore != null)
										//{
										//	if(!SessionStore.mSessionStore.getRoomById(roomId).isExistMessage(messageId)){
												//BaseActivity.mSessionStore.addMessageToRoom(new MessageSchema(messageId, from, roomId, date, date, content));
												Intent showMessage = new Intent(context, ChatActivity.class);
												showMessage.putExtra("origin", "notification");
												showMessage.putExtra("roomId", roomId);
												showMessage.putExtra("otherParty", from);
												showMessage.putExtra("message", content);
												showMessage.putExtra("messageId", messageId);
												showMessage.putExtra("from", from);
												showMessage.putExtra("fromId", fromId);
												showMessage.putExtra("date", date);
												showNotification("پیام از " + from, content, showMessage);

										//	}
										////	else{
										//		logCatDebug("exist in the list");
										//	}
										//}
										//else{
										//	logCatDebug("not exist");

										//}
									}
								}
								catch(Exception ex){
									logCatDebug(ex.getMessage());
								}
							}
							else if(code == 104){
								
								try{
									Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
									Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
									r.play();
									String invitedBy = (jsonObject.getString("invitedBy"));

									int N = mCallbacks.beginBroadcast();
									for (int i = 0; i < N; i++) {
										try {

											mCallbacks.getBroadcastItem(i).notifyAddedByFriend(invitedBy);
										} 
										catch (RemoteException e) {
											logCatDebug(e.getMessage());
										}
									}
									mCallbacks.finishBroadcast();
								}
								catch(Exception ex){
									logCatDebug(ex.getMessage());
								}
							}
							else if(code == 106){
								try{
									//debug(payload);
									String invitedBy = (jsonObject.getString("invitedBy"));
									String roomId = (jsonObject.getString("roomId"));
									//debug("%%%% : "  + invitedBy + " : " + roomId);
									int N = mCallbacks.beginBroadcast();
									for (int i = 0; i < N; i++) {
										try {

											mCallbacks.getBroadcastItem(i).notifyAddedToRoom(invitedBy, roomId);
										} 
										catch (RemoteException e) {
											logCatDebug(e.getMessage());
										}
									}
									mCallbacks.finishBroadcast();
								}
								catch(Exception ex){
									debug(ex.getMessage());
								}
							}
							else if(code == 5){
								try{
									//Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
									//Ringtone r2 = RingtoneManager.getRingtone(getApplicationContext(), notification);
									//r2.play();
									//debug(payload);
									//{"message":"IndividualContacts","code":5,"rooms":[{"_id":"548efd505855476c1364b6a1","Type":"I","StartType":"Now","Creator":"548cd282a62cf7e43605052e","__v":0,"StartDate":"2014-12-15T13:35:20.000Z","CreateDate":"2014-12-15T13:35:20.000Z","Requests":[],"Invited":[],"Admins":["548cd282a62cf7e43605052e","548caa70d347180c181583b4"],"Members":["548cd282a62cf7e43605052e","548caa70d347180c181583b4"],"Entities":[]},{"_id":"548f00675855476c1364b6a6","Type":"I","StartType":"Now","Creator":"548caa70d347180c181583b4","__v":0,"StartDate":"2014-12-15T13:35:20.000Z","CreateDate":"2014-12-15T13:35:20.000Z","Requests":[],"Invited":[],"Admins":["548caa70d347180c181583b4","548ee113346798fc22d906d7"],"Members":["548caa70d347180c181583b4","548ee113346798fc22d906d7"],"Entities":[]}]}
									JSONArray rooms = jsonObject.getJSONArray("rooms");
									//debug(jsonObject.getString("friends"));
									Vector<RoomSchema> listOfRooms = new Vector<RoomSchema>();
									for(int i = 0 ; i < rooms.length() ; i++){
										JSONObject obj = rooms.getJSONObject(i);
										String id;
										id = obj.getString("_id");
										RoomSchema r = new RoomSchema(id, "room", "", "", "I");
										JSONArray members = obj.getJSONArray("Members");
										for(int j = 0 ; j < members.length() ; j++)
										{
											String o = (String) members.get(j);
											//debug(o);
											r.addMember(o);
										}
										listOfRooms.add(r);
									}

									for(int j = 0 ; j < listOfRooms.size() ; j++){
										RoomSchema fs = (RoomSchema) listOfRooms.get(j);
										String mem = "";
										for(int k = 0 ; k < fs.mMembers.size() ; k++)
										{
											mem += (String)fs.mMembers.get(k);
											if(k != fs.mMembers.size() - 1){
												mem += ",";
											}
										}
										int N = mCallbacks.beginBroadcast();
										for (int i = 0; i < N; i++) {
											try {
												mCallbacks.getBroadcastItem(i).roomAdded("room", fs.mId, fs.mDesc, fs.mLogo, fs.mType, mem);
											} 
											catch (RemoteException e) {
												logCatDebug(e.getMessage());
											}
										}
										mCallbacks.finishBroadcast();

									}
								}
								catch(Exception ex){
									debug(ex.getMessage());
								}
							}
						}
						catch(Exception ex){
							logCatDebug(ex.getMessage());
						}
					}

					@Override
					public void onClose(int code, String reason) {
						debug("Connection lost.");
						int N = mCallbacks.beginBroadcast();
						for (int i = 0; i < N; i++) {
							try {
								mCallbacks.getBroadcastItem(i).disConnectedFromHost();
							} 
							catch (RemoteException e) {
								logCatDebug(e.getMessage());
							}
						}
						mCallbacks.finishBroadcast();
					}
				});
			}
		} catch (WebSocketException e) {
			logCatDebug(e.toString());
		}
	}

	private void sendTextMessageAck(String eventId){
		try{
			String cmd = "{\"requestCode\" : \"12\", \"message\": \"AckOfTextMessage\", \"eventId\" : \"" + eventId + "\"}";
			mConnection.sendTextMessage(cmd);
		}
		catch(Exception ex){
			logCatDebug(ex.getMessage());
		}
	}

	private final IWebsocketService.Stub mBinder = new IWebsocketService.Stub() {

		@Override
		public void registerCallback(IWebsocketServiceCallback cb)
				throws RemoteException {
			try
			{
				if (cb != null)
				{
					mCallbacks.register(cb);
				}

			}
			catch(Exception ex)
			{
				logCatDebug(ex.getMessage());
			}
		}

		@Override
		public void unregisterCallback(IWebsocketServiceCallback cb)
				throws RemoteException {
			if (cb != null)
				mCallbacks.unregister(cb);
		}

		@Override
		public void connectToHost(String uri) throws RemoteException {
			try{
				wsuri = uri;
				if(mConnection.isConnected() == true){
					mConnection.disconnect();
				}
				start();
			}
			catch(Exception ex){
				logCatDebug(ex.getMessage());
			}
		}

		@Override
		public void authorizeToWs(String token) throws RemoteException {
			try{
				String cmd = "{\"token\":\"" + token + "\"}";
				mConnection.sendTextMessage(cmd);
			}
			catch(Exception ex){
				logCatDebug(ex.getMessage());
			}
		}

		@Override
		public void disConnectFromHost() throws RemoteException {
			try{
				mConnection.disconnect();
			}
			catch(Exception ex){
				logCatDebug(ex.getMessage());
			}
		}

		@Override
		public void getIndividualRooms() throws RemoteException {
			try{
				String cmd = "{\"requestCode\" : \"3\", \"message\": \"GetIndividualRooms\"}";
				mConnection.sendTextMessage(cmd);
			}
			catch(Exception ex){
				logCatDebug(ex.getMessage());
			}
		}

		@Override
		public void getGroupRooms() throws RemoteException {
			try{
				String cmd = "{\"requestCode\" : \"8\", \"message\": \"GetGroupContacts\"}";
				mConnection.sendTextMessage(cmd);
			}
			catch(Exception ex){
				logCatDebug(ex.getMessage());
			}
		}

		@Override
		public void getMyProfile() throws RemoteException {
			try{
				String cmd = "{\"requestCode\" : \"4\", \"message\": \"GetCurrentProfile\"}";
				mConnection.sendTextMessage(cmd);
			}
			catch(Exception ex){
				logCatDebug(ex.getMessage());
			}
		}

		@Override
		public void getFriendList() throws RemoteException {
			try{
				String cmd = "{\"requestCode\" : \"7\", \"message\": \"GetFriendList\"}";
				mConnection.sendTextMessage(cmd);
			}
			catch(Exception ex){
				logCatDebug(ex.getMessage());
			}
		}

		@Override
		public void addFriendToList(String username) throws RemoteException {
			try{
				String cmd = "{\"requestCode\" : \"6\", \"message\": \"AddUserToFriend\", \"username\" : \"" + username + "\"}";
				mConnection.sendTextMessage(cmd);
			}
			catch(Exception ex){
				logCatDebug(ex.getMessage());
			}
		}

		@Override
		public void createGroupRoom(String roomName) throws RemoteException {
			try{
				String cmd = "{\"requestCode\" : \"9\", \"message\": \"CreateGroupRoom\", \"roomName\" : \"" + roomName + "\"}";
				mConnection.sendTextMessage(cmd);
			}
			catch(Exception ex){
				logCatDebug(ex.getMessage());
			}
		}

		@Override
		public void AddMemberToGroup(String roomId, String memberId)
				throws RemoteException {
			try{
				String cmd = "{\"requestCode\" : \"10\", \"message\": \"AddMemberToGroup\", \"roomId\" : \"" + roomId + ",\"memberId\" : \"" + memberId + "\"}";
				mConnection.sendTextMessage(cmd);
			}
			catch(Exception ex){
				logCatDebug(ex.getMessage());
			}
		}

		@Override
		public void GetGroupMembers(String roomId) throws RemoteException {
			try{
				String cmd = "{\"requestCode\" : \"11\", \"message\": \"GetGroupMembers\", \"roomId\" : \"" + roomId + "\"}";
				mConnection.sendTextMessage(cmd);
			}
			catch(Exception ex){
				logCatDebug(ex.getMessage());
			}
		}

		@Override
		public void sendTextMessageToRoom(String message, String to)
				throws RemoteException {
			try{
				String cmd = "{\"requestCode\" : \"1\", \"message\":\"SendTextMessage\", \"messageContent\":\"" + message + "\", \"publishType\":\"Now\", \"roomId\":\"" + to + "\"}";
				mConnection.sendTextMessage(cmd);
			}
			catch(Exception ex){
				logCatDebug(ex.getMessage());
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	} 

	@Override
	public void onDestroy() {
		if(mConnection != null)
			mConnection.disconnect();
	}

	protected void debug(String txt)
	{
		logCatDebug(txt);
		int N = mCallbacks.beginBroadcast();
		for (int i = 0; i < N; i++) {
			try {
				mCallbacks.getBroadcastItem(i).debug(txt);
			} catch (RemoteException e) {
			}
		}
		mCallbacks.finishBroadcast();
		logCatDebug("Debug : " + txt);
	}

	protected void logCatDebug(String txt){
		try{
			//	if(false)
					Log.d(TAG, txt);
		}
		catch(Exception ex){

		}
	}
}
