package com.example.avcalltablet;

import android.graphics.Bitmap;
import android.net.Uri;

public class ChatMessage {	
	public boolean IsSenderSelf = false;
	public ChatMessageType Type;
	public String SenderID;
	public String ReceptionDate;
	public String ReceptionTime;
	public String MessageText;
	public Bitmap MessagePicture;
}
