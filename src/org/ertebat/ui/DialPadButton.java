package org.ertebat.ui;
public enum DialPadButton {
	DPB_1("1"),
	DPB_2("2"),
	DPB_3("3"),
	DPB_4("4"),
	DPB_5("5"),
	DPB_6("6"),
	DPB_7("7"),
	DPB_8("8"),
	DPB_9("9"),
	DPB_0("0"),
	DPB_Star("*"),
	DPB_Sharp("#"),;
	
	private DialPadButton(String title) {
		this.title = title;
	}
	
	private String title;
	
	@Override
	public String toString() {
		return title;
	}
}
