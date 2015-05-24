package codeday.squareassault.client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class View {

	public static final long UPDATE_DELAY_MILLIS = 100;
	public static final Color BACKGROUND_COLOR = Color.BLACK;
	private final Context context;

	public View(Context context) {
		this.context = context;
	}

	public void paint(Graphics go) {
		go.setColor(Color.WHITE);
		go.drawString(context.activeString, 100, 100);
		for (int i = 0; i < context.backgroundImages.length; i++) {
			int[] column = context.backgroundImages[i];
			for (int j = 0; j < column.length; j++) {
				int cell = column[j];
				go.drawImage(Loader.load(context.cells[cell]), i*64, j*64, null);
			}
		}
	}
}
