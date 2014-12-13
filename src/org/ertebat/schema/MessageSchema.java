package org.ertebat.schema;

public class MessageSchema {
    public String m_from;
    public String m_to;
    public String m_date;
    public String m_time;
    public String m_body;
    public MessageSchema(String from, String to, String date, String time, String body){
    	m_from = from;
    	m_to = to;
    	m_date = date;
    	m_time = time;
    	m_body = body;
    }
}
