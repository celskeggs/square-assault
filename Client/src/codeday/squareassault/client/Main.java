package codeday.squareassault.client;

import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main extends JPanel {
	private static final long serialVersionUID = -4540493147431023697L;

	public static void main(String[] args) {
		final JFrame main = new JFrame("Square Assault");
		main.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		Main panel = new Main();
		main.setContentPane(panel);
		main.setSize(1024, 768);
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				main.setVisible(true);
			}
		});
	}

	@Override
	public void paint(Graphics go) {
	}
}
