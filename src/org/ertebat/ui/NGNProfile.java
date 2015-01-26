package org.ertebat.ui;
public class NGNProfile {
	private final static String DEF_NAME = "6363";
	private final static String DEF_PUBLIC_ID = "sip:6363@192.168.78.245";
	private final static String DEF_PRIVATE_ID = "6363";
	private final static String DEF_PASSWORD = "123456";
	private final static String DEF_REALM = "192.168.78.245";
	private final static String DEF_CSCF_HOST = "192.168.78.245";
	private final static String DEF_CSCF_PORT = "6050";
	private final static TransportProtocol DEF_PROTOCOL = TransportProtocol.TP_UDP;
	private final static VideoSize DEF_VIDEO_SIZE = VideoSize.VS_SQCIF;
	
	public static String DisplayName = DEF_NAME;
	public static String PublicIdentity = DEF_PUBLIC_ID;
	public static String PrivateIdentity = DEF_PRIVATE_ID;
	public static String Password = DEF_PASSWORD;
	public static String Realm = DEF_REALM;
	public static String CSCFHost = DEF_CSCF_HOST;
	public static String CSCFPort = DEF_CSCF_PORT;
	public static TransportProtocol Protocol = DEF_PROTOCOL;
	public static VideoSize CurrentVideoSize = DEF_VIDEO_SIZE;
	public static boolean HasIMSSecurity = false;
	
	public final static int DefaultPort = 6050;
}