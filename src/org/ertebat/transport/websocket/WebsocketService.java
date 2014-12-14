/**
 * 
 */
package org.ertebat.transport.websocket;

import java.util.Vector;

import org.ertebat.schema.FriendSchema;
import org.ertebat.transport.websocket.IWebsocketService;
import org.ertebat.transport.websocket.IWebsocketServiceCallback;
import org.json.JSONArray;
import org.json.JSONObject;

import de.tavendo.autobahn.WebSocketConnection;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;


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

	private void start() {
		try {
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
						debug(payload);
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
							debug(jsonObject.getString("profile"));
							JSONObject tmpObject = new JSONObject(jsonObject.getString("profile"));
							String firstName = "--";
							String lastName = "--";
							String mobile = "--";
							String email = "--";
							try{
								firstName = tmpObject.getString("firstName");
							}
							catch(Exception ex){
								debug(ex.getMessage());
							}
							try{
								lastName = tmpObject.getString("lastName");
							}
							catch(Exception ex){
								debug(ex.getMessage());
							}
							try{
								mobile = tmpObject.getString("mobileNumber");
							}
							catch(Exception ex){
								debug(ex.getMessage());
							}
							try{
								email = tmpObject.getString("email");
							}
							catch(Exception ex){
								debug(ex.getMessage());
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
		} catch (WebSocketException e) {
			logCatDebug(e.toString());
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
			//		Log.d(TAG, txt);
		}
		catch(Exception ex){

		}
	}
}
