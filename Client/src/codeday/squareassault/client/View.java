package codeday.squareassault.client;

import java.awt.Color;
import java.awt.Graphics;

public class View {

	public static final long UPDATE_DELAY_MILLIS = 20;
	public static final Color BACKGROUND_COLOR = Color.BLACK;
	private static final int BORDER_SIZE_MIN = 256;
	private static final int BORDER_SIZE_MAX = 320;
	private static final int SHIFT_SCALE_FACTOR = 16;
	private static final int METER_SHIFT = 70, METER_RADIUS = 48;
	private final Context context;
	private int shiftX, shiftY;

	public View(Context context) {
		this.context = context;
	}

	public void paint(Graphics go, int width, int height) {
		for (int i = 0; i < context.backgroundImages.length; i++) {
			int[] column = context.backgroundImages[i];
			for (int j = 0; j < column.length; j++) {
				int cell = column[j];
				go.drawImage(Loader.load(context.cells[cell]), i * 64 + shiftX, j * 64 + shiftY, null);
			}
		}

		for (Entity ent : context.entities) {
			go.drawImage(Loader.load(ent.getIconForRender()), ent.x + shiftX, ent.y + shiftY, null);
		}

		int health = context.getHealth();
		drawMeter(go, false, width, height, Color.RED, Color.GREEN, health, 100);
		if (context.turretCount != -1) {
			drawMeter(go, true, width, height, Color.BLACK, Color.YELLOW, context.turretCount, context.turretMaximum);
		}
	}

	private void drawMeter(Graphics go, boolean atLeft, int width, int height, Color low, Color high, int health, int max) {
		go.setColor(Color.LIGHT_GRAY);
		int baseX = atLeft ? METER_SHIFT - METER_RADIUS : width - METER_SHIFT - METER_RADIUS;
		go.fillArc(baseX, METER_SHIFT - METER_RADIUS, METER_RADIUS * 2, METER_RADIUS * 2, 290, 320);
		go.setColor(blendColors(low, high, health / (double) max));
		go.fillArc(baseX, METER_SHIFT - METER_RADIUS, METER_RADIUS * 2, METER_RADIUS * 2, 250 - health * 320 / max, health * 320 / max);
		go.setColor(Color.GRAY);
		go.fillArc(baseX, METER_SHIFT - METER_RADIUS, METER_RADIUS * 2, METER_RADIUS * 2, 250, 40);
		go.drawOval(baseX, METER_SHIFT - METER_RADIUS, METER_RADIUS * 2, METER_RADIUS * 2);
	}

	private Color blendColors(Color zero, Color one, double d) {
		if (d < 0) {
			d = 0;
		} else if (d > 1) {
			d = 1;
		}
		double remain = 1 - d;
		return new Color((int) (one.getRed() * d + zero.getRed() * remain), (int) (one.getGreen() * d + zero.getGreen() * remain), (int) (one.getBlue() * d + zero.getBlue() * remain));
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
