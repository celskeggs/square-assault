package codeday.squareassault.client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class View {

	public static final long UPDATE_DELAY_MILLIS = 100;
	public static final Color BACKGROUND_COLOR = Color.BLACK;
	private final Context context;
	private final BufferedImage image = Loader.load("tiletest");

	public View(Context context) {
		this.context = context;
	}

	public void paint(Graphics go) {
		go.setColor(Color.WHITE);
		go.drawString(context.activeString, 100, 100);
		go.drawImage(image, 0, 0, null);
	}
}
