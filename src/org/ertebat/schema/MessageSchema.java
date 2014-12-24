package org.ertebat.schema;

public class MessageSchema {
    public String mFromId;
    public String mFromUserName;
    public String mTo;
    public String mDate;
    public String mTime;
    public String mBody;
    public String mId;
    public boolean mIsRead;
    public String mType;
    public MessageSchema(String id, String fromId, String fromUsername, String to, String date, String time, String body){
    	mFromId = fromId;
    	mFromUserName = fromUsername;
    	mTo = to;
    	mDate = date;
    	mTime = time;
    	mBody = body;
    	mIsRead = false;
    	mId = id;
    	mType = "Text";
    }
    public MessageSchema(String type, String id, String fromId, String fromUsername, String to, String date, String time, String body){
    	mFromId = fromId;
    	mFromUserName = fromUsername;
    	mTo = to;
    	mDate = date;
    	mTime = time;
    	mBody = body;
    	mIsRead = false;
    	mId = id;
    	mType = type;
    }
}
