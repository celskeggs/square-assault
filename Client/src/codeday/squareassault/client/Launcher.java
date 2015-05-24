package codeday.squareassault.client;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Launcher extends JPanel implements KeyListener {
	private static final long serialVersionUID = -4540493147431023697L;

	public static void main(String[] args) throws UnknownHostException, IOException {
		Launcher panel = new Launcher();
		final JFrame main = new JFrame("Square Assault");
		main.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		main.setContentPane(panel);
		main.setSize(1024, 768);
		main.addKeyListener(panel);
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				main.setVisible(true);
			}
		});
		panel.execute();
		System.exit(1);
	}

	private void execute() {
		try {
			net.handleAll(context);
		} catch (InterruptedException e) {
			e.printStackTrace(); // TODO: logging
		}
	}

	private Launcher() throws UnknownHostException, IOException {
		this.net = new Network("127.0.0.1", "unknown user");
		context = new Context(net);
		gui = new View(context);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				repaint();
			}
		}, View.UPDATE_DELAY_MILLIS, View.UPDATE_DELAY_MILLIS);
	}

	private final Network net;
	private final Context context;
	private final View gui;

	@Override
	public void paint(Graphics go) {
		go.setColor(View.BACKGROUND_COLOR);
		go.fillRect(0, 0, this.getWidth(), this.getHeight());
		gui.paint(go);
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		System.out.println("Said: " + arg0);
		try {
			context.handleKey(arg0.getKeyCode(), arg0.getKeyChar());
		} catch (InterruptedException e) {
			e.printStackTrace(); // TODO: logging
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {

	}

	@Override
	public void keyTyped(KeyEvent arg0) {

	}
}
