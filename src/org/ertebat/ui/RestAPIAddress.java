package org.ertebat.ui;

import java.security.InvalidParameterException;

import org.ertebat.schema.SettingSchema;

public class RestAPIAddress {
	public static String getSignIn(){
		return SettingSchema.mBaseRestUrl + "api/signin";
	}
	public static String getSignUp(){
		return SettingSchema.mBaseRestUrl + "api/signup";
	}
	public static String getSignOut(){
		return SettingSchema.mBaseRestUrl + "api/signout";
	}
	public static String getUsernameViaUserId(){
		return SettingSchema.mBaseRestUrl + "api/getUsernameViaUserId";
	}
	public static String getUserByMail(){
		return SettingSchema.mBaseRestUrl + "api/getUserByMail";
	}
	public static String getGroupContacts(){
		return SettingSchema.mBaseRestUrl + "api/getGroupContacts";
	}
	public static String getIndividualContacts(){
		return SettingSchema.mBaseRestUrl + "api/getIndividualContacts";
	}
	public static String getCurrentProfile(){
		return SettingSchema.mBaseRestUrl + "api/getCurrentProfile";
	}
	public static String getMyProfile(){
		return SettingSchema.mBaseRestUrl + "api/getCurrentProfile";
	}
	public static String getUserProfile(){
		return SettingSchema.mBaseRestUrl + "api/getUserProfile";
	}
	public static String getSaveProfile(){
		return SettingSchema.mBaseRestUrl + "api/saveProfile";
	}
	public static String getUploadProfilePicture(){
		return SettingSchema.mBaseRestUrl + "api/uploadProfilePic";
	}
	public static String getAddFriend(){
		return SettingSchema.mBaseRestUrl + "api/addFriendToTheList";
	}
	public static String getCreateIndividualRoom(){
		return SettingSchema.mBaseRestUrl + "chat/createIndividualRoom";
	}
	public static String getFriendList(){
		return SettingSchema.mBaseRestUrl + "api/getFriendList";
	}
	public static String getSendMessage(){
		return SettingSchema.mBaseRestUrl + "chat/sendTextMessageTo";
	}
	public static String getIncommingMessage(){
		return SettingSchema.mBaseRestUrl + "chat/getIncomingMessage";
	}
	public static String getSendPictureMessage(){
		return SettingSchema.mBaseRestUrl + "chat/sendPictureMessageTo";
	}
}
