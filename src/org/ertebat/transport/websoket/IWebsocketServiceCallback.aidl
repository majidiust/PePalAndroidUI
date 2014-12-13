package org.ertebat.transport.websoket;

oneway interface IWebsocketServiceCallback {
	void debug(String msg);
	void loggedInResult(in int code, String details);
	void newMessage(String from, String roomId, String date, String time, String content);
	void connectedToHost(String uri);
	void disConnectedFromHost();
}