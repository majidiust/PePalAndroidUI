package org.ertebat.transport.websocket;

import org.ertebat.transport.websocket.IWebsocketServiceCallback;

interface IWebsocketService {
    void registerCallback(IWebsocketServiceCallback cb);
    void unregisterCallback(IWebsocketServiceCallback cb);
    void connectToHost(String uri);
    void authorizeToWs(String token);
    void disConnectFromHost();
    void getIndividualRooms();
    void getGroupRooms();
    void getMyProfile();
    void getFriendList();
    void addFriendToList(String username);
    void createGroupRoom(String roomName);
    void AddMemberToGroup(String roomId, String memberId);
    void GetGroupMembers(String roomId);
    void sendTextMessageToRoom(String message, String to);
}