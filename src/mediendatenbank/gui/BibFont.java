package mediendatenbank.gui;

import java.awt.Font;

public class BibFont {
	
	protected static Integer size = 11;
	protected static String family = "Tahoma";
	
	public static void setSize(int newSize) {
		size = newSize;
	}
	
	public static int getSize() {
		return size;
	}
	
	public static Font getBold() {
		return new Font(family, 1, size);
	}
	
	public static Font getRegular() {
		return new Font(family, 0, size);
	}
	
	public static Font getMenu() {
		return new Font(family, 0, size);
	}
	
}
