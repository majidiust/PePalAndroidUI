package org.ertebat.schema;

public class MessageSchema {
    public String mFrom;
    public String mTo;
    public String mDate;
    public String mTime;
    public String mBody;
    public String mId;
    public boolean mIsRead;
    public MessageSchema(String id, String from, String to, String date, String time, String body){
    	mFrom = from;
    	mTo = to;
    	mDate = date;
    	mTime = time;
    	mBody = body;
    	mIsRead = false;
    	mId = id;
    }
}
