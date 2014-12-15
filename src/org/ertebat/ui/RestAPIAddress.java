package org.ertebat.ui;

import java.security.InvalidParameterException;

public class RestAPIAddress {
	public static String baseAddress = "http://192.168.10.129:4000/";
	public static String getSignIn(){
		return baseAddress + "api/signin";
	}
	public static String getSignUp(){
		return baseAddress + "api/signup";
	}
	public static String getSignOut(){
		return baseAddress + "api/signout";
	}
	public static String getUsernameViaUserId(){
		return baseAddress + "api/getUsernameViaUserId";
	}
	public static String getUserByMail(){
		return baseAddress + "api/getUserByMail";
	}
	public static String getGroupContacts(){
		return baseAddress + "api/getGroupContacts";
	}
	public static String getIndividualContacts(){
		return baseAddress + "api/getIndividualContacts";
	}
	public static String getCurrentProfile(){
		return baseAddress + "api/getCurrentProfile";
	}
	public static String getMyProfile(){
		return baseAddress + "api/getCurrentProfile";
	}
	public static String getUserProfile(){
		return baseAddress + "api/getUserProfile";
	}
	public static String getSaveProfile(){
		return baseAddress + "api/saveProfile";
	}
	public static String getUploadProfilePicture(){
		return baseAddress + "api/uploadProfilePic";
	}
	public static String getAddFriend(){
		return baseAddress + "api/addFriendToTheList";
	}
	public static String getCreateIndividualRoom(){
		return baseAddress + "chat/createIndividualRoom";
	}
}
