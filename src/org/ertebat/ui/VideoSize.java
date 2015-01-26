package org.ertebat.ui;

public enum VideoSize {
	VS_SQCIF(0, "SQCIF", "tmedia_pref_video_size_sqcif"),
	VS_QCIF(1, "QCIF", "tmedia_pref_video_size_qcif"),
	VS_QVGA(2, "QVGA", "tmedia_pref_video_size_qvga"),
	VS_CIF(3, "CIF", "tmedia_pref_video_size_cif"),
	VS_HVGA(4, "HVGA", "tmedia_pref_video_size_hvga"),
	VS_VGA(5, "VGA", "tmedia_pref_video_size_vga"),
	VS_4CIF(6, "4CIF", "tmedia_pref_video_size_4cif");
	
	private VideoSize(int index, String title, String value) {
		this.index = index;
		this.title = title;
		this.value = value;
	}
	
	private int index;
	private String title;
	private String value;
	
	@Override
	public String toString() {
		return title;
	}
	
	public String getValue() {
		return value;
	}
	
	public int getIndex() {
		return index;
	}
}
