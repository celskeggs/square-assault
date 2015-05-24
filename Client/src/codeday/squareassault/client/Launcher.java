package codeday.squareassault.client;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Launcher extends JPanel implements KeyListener, MouseListener {
	private static final long serialVersionUID = -4540493147431023697L;

	public static void main(String[] args) throws UnknownHostException, IOException {
		Launcher panel = new Launcher();
		final JFrame main = new JFrame("Square Assault");
		main.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		main.setContentPane(panel);
		main.setSize(768, 768);
		main.setResizable(false);
		main.addKeyListener(panel);
		panel.addMouseListener(panel);
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
		view = new View(context);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					context.tick();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				view.tick(getWidth(), getHeight());

				if (getWidth() == 0 && getHeight() == 0) {
					return;
				} else if (image == null) {
					image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
					imgG = image.createGraphics();
				}

				synchronized (image) {
					imgG.setColor(View.BACKGROUND_COLOR);
					imgG.fillRect(0, 0, getWidth(), getHeight());
					view.paint(imgG);
				}

				repaint();
			}
		}, View.UPDATE_DELAY_MILLIS, View.UPDATE_DELAY_MILLIS);
	}

	private final Network net;
	private final Context context;
	private final View view;
	private BufferedImage image;
	private Graphics2D imgG;

	@Override
	public void paint(Graphics go) {
		if (image != null) {
			synchronized (image) {
				go.drawImage(image, 0, 0, null);
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		try {
			context.handleKey(arg0.getKeyCode(), arg0.getKeyChar(), true);
		} catch (InterruptedException e) {
			e.printStackTrace(); // TODO: logging
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		try {
			context.handleKey(arg0.getKeyCode(), arg0.getKeyChar(), false);
		} catch (InterruptedException e) {
			e.printStackTrace(); // TODO: logging
		}
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
		try {
			view.mousePress(arg0.getX(), arg0.getY(), arg0.getButton());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

	}
}
