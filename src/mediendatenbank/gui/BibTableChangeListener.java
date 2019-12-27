package mediendatenbank.gui;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import mediendatenbank.BibDatabase;

public class BibTableChangeListener implements TableModelListener {

	public void tableChanged(TableModelEvent e) {
		if (e.getType() == 0) {
			int row = e.getFirstRow();
			int column = e.getColumn();
			TableModel model = (TableModel) e.getSource();
			Integer id = (Integer) model.getValueAt(row, BibTableModel.ID_COL);
			String data = (String) model.getValueAt(row, column);

			BibDatabase db = BibDatabase.get();
			if (column == 0) {
				db.updateTitle(id, data);
			} else if (column == 1) {
				db.updateMedium(id, data);
			} else if (column == 2) {
				db.updateYear(id, data);
			} else if (column == 3) {
				db.updateDetails(id, data);
			}
		}
	}
}
