package org.ertebat.ui;
public enum PictureInsertionMethod {
	FromGallery("انتخاب از گالری"),
	FromCamera("گرفتن عکس با دوربین");
	
	private PictureInsertionMethod(String text) {
		this.mText = text;
	}
	
	private String mText;
	
	@Override
	public String toString() {
		return mText;
	}
}
