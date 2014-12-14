/**
 * 
 */
package org.ertebat.transport;

import org.ertebat.schema.FriendSchema;
import org.ertebat.schema.MessageSchema;
import org.ertebat.schema.RoomSchema;

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
	public void onCurrentProfileResult(String username, String userId,
			String firstName, String lastName, String mobile, String email);
}
