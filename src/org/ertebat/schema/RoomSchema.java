package org.ertebat.schema;

import java.util.Vector;

public class RoomSchema {
	public String mName;
	public String mDesc;
	public String mLogo;
	public String mType;
	public String mId;
	public Vector<MessageSchema> mMessages = new Vector<MessageSchema>();
	public Vector<String> mMembers = new Vector<String>();
	public RoomSchema(String id, String name, String desc, String logo, String type){
		mName = name;
		mType = type;
		mLogo = logo;
		mDesc = desc;
		mId = id;
	}
	public boolean addMessage(MessageSchema ms){
		boolean find = false;
		for(int i = 0 ; i < mMessages.size() ; i++){
			if(mMessages.get(i).mId.compareTo(ms.mId) == 0){
				find = true;
				break;
			}
		}
		if(!find)
			mMessages.add(ms);
		
		return !find;
	}
	
	public boolean isExistMessage(String eventId){
		boolean find = false;
		for(int i = 0 ; i < mMessages.size() ; i++){
			if(mMessages.get(i).mId.compareTo(eventId) == 0){
				find = true;
				break;
			}
		}
		return find;
	}
	
	public void addMember(String uid){
		boolean find = false;
		for(int i = 0 ; i < mMembers.size() ; i++){
			if(mMembers.get(i).compareTo(uid) == 0){
				find = true;
				break;
			}
		}
		if(!find)
			mMembers.add(uid);
	}
	public Vector<MessageSchema> getUnReadMessagesAndReadThem(){
		Vector<MessageSchema> result = new Vector<MessageSchema>();
		for(int i = 0 ; i < mMessages.size(); i++){
			if(mMessages.get(i).mIsRead == false)
			{
				mMessages.get(i).mIsRead = true;
				result.add(mMessages.get(i));
			}
		}
		return result;
	}
	public Vector<MessageSchema> getUnReadMessages(){
		Vector<MessageSchema> result = new Vector<MessageSchema>();
		for(int i = 0 ; i < mMessages.size(); i++){
			if(mMessages.get(i).mIsRead == false)
			{
				result.add(mMessages.get(i));
			}
		}
		return result;
	}
	public Vector<MessageSchema> getAllMessages(){
		Vector<MessageSchema> result = new Vector<MessageSchema>();
		for(int i = 0 ; i < mMessages.size(); i++){
			mMessages.get(i).mIsRead = true;
			result.add(mMessages.get(i));
		}
		return result;
	}
	public String serializeMembers(){
		String serial = "";
		for(int i = 0 ; i < mMembers.size() ; i++){
			serial += mMembers.get(i);
			if(i!=mMembers.size() - 1){
				serial += ",";
			}
		}
		return serial;
	}
}
