package mediendatenbank;

import mediendatenbank.gui.BibGUI2;
import java.awt.EventQueue;
import javax.swing.JOptionPane;

public class BibBootstrap {
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				BibGUI2 gui = null;
				try {
					BibDatabase.init();
					gui = new BibGUI2();
					gui.setVisible(true);
				} catch(BibUserException e) {
					String msg = e.getMessage();
					if (msg.length() > 100) {
						msg = msg.substring(0, 100) + "...";
					}
					JOptionPane.showMessageDialog(gui, msg, "Es ist ein Fehler aufgetreten...", JOptionPane.ERROR_MESSAGE);
					BibDatabase.get().shutdown();
				}
			}
		});
	}
	
}
