package mediendatenbank.gui;

import javax.swing.table.DefaultTableModel;

public class BibTableModel extends DefaultTableModel {
	
	private Class[] types = {String.class, String.class, String.class, String.class, Integer.class};
	private boolean[] canEdit = {true, true, true, true, false};
	private BibGUI2 gui;
	
	public static final int ID_COL = 4;
	
	public BibTableModel(BibGUI2 gui) {
		this.gui = gui;
		addColumn("Interpret / Titel");
		addColumn("Medium");
		addColumn("Jahr");
		addColumn("Anmerkungen");
		addColumn("ID");
	}

	public Class getColumnClass(int columnIndex) {
		return this.types[columnIndex];
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return !this.gui.isReadOnly() && this.canEdit[columnIndex];
	}
	
	public int getColumnPreferredWidth(int columnIndex) {
		switch(columnIndex) {
			case 0:
				return BibFont.getSize() * 50;
			case 1:
				return BibFont.getSize() * 8;
			case 2:
			case 4:
				return BibFont.getSize() * 6;
			case 3:
				return BibFont.getSize() * 30;
		}
		return 0;
	}
	
	public int getColumnMinWidth(int columnIndex) {
		return BibFont.getSize() * 4;
	}
	
	public int getColumnMaxWidth(int columnIndex) {
		switch(columnIndex) {
			case 1:
				return BibFont.getSize() * 16;
			case 2:
			case 4:
				return BibFont.getSize() * 12;
		}
		return Integer.MAX_VALUE;
	}
	
}
