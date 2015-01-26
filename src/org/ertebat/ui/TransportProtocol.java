package org.ertebat.ui;
public enum TransportProtocol {
	TP_UDP("UDP"),
	TP_TCP("TCP"),
	TP_TLS("TLS");
	
	private TransportProtocol(String title) {
		this.title = title;
	}
	
	private String title;
	
	@Override
	public String toString() {
		return title;
	}
}
