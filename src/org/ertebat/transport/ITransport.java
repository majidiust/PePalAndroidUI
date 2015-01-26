/**
 * 
 */
package org.ertebat.transport;

import org.ertebat.schema.FriendSchema;
import org.ertebat.schema.MessageSchema;
import org.ertebat.schema.RoomSchema;

import android.os.RemoteException;

/**
 * @author Majid
 *
 */
public interface ITransport {
	public void onConnectedToServer();
	public void onDisconnctedFromServer();
	public void onNewFriend(FriendSchema fs);
	public void onNewRoom(RoomSchema rs);
	public void onNewMessage(MessageSchema ms);
	public void onCurrentProfileResult(String username, String userId, String firstName, String lastName, String mobile, String email);
	public void onAuthorizationRequest();
	public void onAuthorized();
	public void onUserProfile(String firstName, String lastName, String uid, String userName, String picUrl, String email);
	public void onRoomAdded(String roomName, String roomId, String roomDesc, String roomLogo, String roomType, String members);
	public void onMembersAddedToRoom(String roomId, String memberId);
	public void notifyAddedByFriend(String invitedBy);
	public void notifyAddedToRoom(String invitedBy, String roomId);
}
