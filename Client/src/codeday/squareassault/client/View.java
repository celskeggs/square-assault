package codeday.squareassault.client;

import java.awt.Color;
import java.awt.Graphics;

public class View {

	public static final long UPDATE_DELAY_MILLIS = 20;
	public static final Color BACKGROUND_COLOR = Color.BLACK;
	private static final int BORDER_SIZE_MIN = 128;
	private static final int BORDER_SIZE_MAX = 196;
	private static final int SHIFT_SCALE_FACTOR = 16;
	private final Context context;
	private int shiftX, shiftY;

	public View(Context context) {
		this.context = context;
	}

	public void paint(Graphics go) {
		for (int i = 0; i < context.backgroundImages.length; i++) {
			int[] column = context.backgroundImages[i];
			for (int j = 0; j < column.length; j++) {
				int cell = column[j];
				go.drawImage(Loader.load(context.cells[cell]), i * 64 + shiftX, j * 64 + shiftY, null);
			}
		}

		for (Entity ent : context.entities) {
			go.drawImage(Loader.load(ent.icon), ent.x + shiftX, ent.y + shiftY, null);
		}
	}

	public void tick(int width, int height) {
		int realX = context.getX() + shiftX;
		if (realX < BORDER_SIZE_MIN) {
			shiftX += (BORDER_SIZE_MIN - realX) / SHIFT_SCALE_FACTOR;
		}
		if (realX > width - BORDER_SIZE_MAX) {
			shiftX -= (realX - width + BORDER_SIZE_MAX) / SHIFT_SCALE_FACTOR;
		}

		int realY = context.getY() + shiftY;
		if (realY < BORDER_SIZE_MIN) {
			shiftY += (BORDER_SIZE_MIN - realY) / SHIFT_SCALE_FACTOR;
		}
		if (realY > height - BORDER_SIZE_MAX) {
			shiftY -= (realY - height + BORDER_SIZE_MAX) / SHIFT_SCALE_FACTOR;
		}
	}

	public void mousePress(int x, int y, int button) throws InterruptedException {
		context.mousePress(x - shiftX, y - shiftY, button);
	}
}
