package org.ertebat.transport.websocket;

oneway interface IWebsocketServiceCallback {
	void debug(String msg);
	void loggedInResult(in int code, String details);
	void newMessage(String messageId, String from, String roomId, String date, String time, String content);
	void connectedToHost(String uri);
	void disConnectedFromHost();
	void authorizationRequest();
	void authorized();
	void currentProfileResult(String username, String userId, String firstName, String lastName, String mobile, String email);
	void friendAdded(String userName, String id, String status);
	void roomAdded(String roomName, String roomId, String roomDesc,  String roomLogo, String roomType);
}