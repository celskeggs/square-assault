package codeday.squareassault.editor;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Launcher extends JPanel implements KeyListener, MouseListener {

	private static final long serialVersionUID = -2851105270669446142L;

	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		Launcher panel = new Launcher();
		final JFrame main = new JFrame("Square Assault Editor");
		main.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		main.setContentPane(panel);
		main.setSize(1024, 768);
		main.addKeyListener(panel);
		panel.addMouseListener(panel);
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				main.setVisible(true);
			}
		});
		panel.execute();
	}
	
	private void execute() throws InterruptedException {
		while (true) {
			view.update();
			repaint();
			Thread.sleep(50);
		}
	}

	private Launcher() throws UnknownHostException, IOException {
		view = new View(new Map(16, 16));
	}

	private final View view;

	@Override
	public void paint(Graphics go) {
		go.setColor(View.BACKGROUND_COLOR);
		go.fillRect(0, 0, this.getWidth(), this.getHeight());
		view.paint(go, getWidth(), getHeight());
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		view.onKey(arg0.getKeyChar(), arg0.getKeyCode(), true);
		repaint();
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		view.onKey(arg0.getKeyChar(), arg0.getKeyCode(), false);
		repaint();
	}

	@Override
	public void keyTyped(KeyEvent arg0) {

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		view.pressed(arg0.getX(), arg0.getY(), getWidth(), getHeight());
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		
	}
}
