/**
 * 
 */
package org.ertebat.schema;

import java.util.Vector;

/**
 * @author Majid
 *
 */
public class SessionStore {
	public Vector<FriendSchema> mFriendList = new Vector<FriendSchema>();
	public Vector<RoomSchema> mRooms = new Vector<RoomSchema>();
	public void addFriend(FriendSchema fs){
		boolean find = false;
		for(int i = 0 ; i < mFriendList.size() ; i++){
			FriendSchema tmpFS = (FriendSchema) mFriendList.get(i);
			if(tmpFS.m_friendId.compareTo(fs.m_friendId) == 0){
				find = true;
				break;
			}
		}
		if(!find){
			mFriendList.add(fs);
		}
	}
}
