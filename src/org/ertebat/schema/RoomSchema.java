package org.ertebat.schema;

public class RoomSchema {
	    public String m_name;
	    public String m_desc;
	    public String m_logo;
	    public String m_type;
	    public RoomSchema(String name, String desc, String logo, String type){
	    	m_name = name;
	    	m_type = type;
	    	m_logo = logo;
	    	m_desc = desc;
	    }
}
