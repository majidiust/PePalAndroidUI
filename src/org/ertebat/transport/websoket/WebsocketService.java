/**
 * 
 */
package org.ertebat.transport.websoket;

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
		Log.d(TAG, "Service Started");
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
			Log.d(TAG, e.toString());
		}
	}

	private final IWebsocketService.Stub mBinder = new IWebsocketService.Stub() {

		@Override
		public void registerCallback(IWebsocketServiceCallback cb)
				throws RemoteException {
			try{
				if (cb != null)
					mCallbacks.unregister(cb);
			}
			catch(Exception ex){
				logCatDebug(ex.getMessage());
			}

		}

		@Override
		public void unregisterCallback(IWebsocketServiceCallback cb)
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
			Log.d(TAG, txt);
		}
		catch(Exception ex){

		}
	}
}
