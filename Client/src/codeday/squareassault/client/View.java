package codeday.squareassault.client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class View {

	public static final long UPDATE_DELAY_MILLIS = 100;
	public static final Color BACKGROUND_COLOR = Color.BLACK;
	private final Context context;
	private final BufferedImage image = Loader.load("tiletest");
	private final BufferedImage image2 = Loader.load("tiletest2");

	public View(Context context) {
		this.context = context;
	}

	public void paint(Graphics go) {
		go.setColor(Color.WHITE);
		go.drawString(context.activeString, 100, 100);
		for (int i = 0; i < context.backgroundImages.length; i++) {
			BufferedImage[] column = context.backgroundImages[i];
			for (int j = 0; j < column.length; j++) {
				BufferedImage cell = column[j];
				go.drawImage(cell, i, j, null);
			}
		}
	}
}
