package codeday.squareassault.editor;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import codeday.squareassault.client.Loader;

public class View {

	public static final Color BACKGROUND_COLOR = Color.BLACK;
	private static final int OVERLAP_RADIUS = 64;
	private int shiftX, shiftY, dX, dY;
	private boolean shiftKeyHeld;
	private final Map map;
	private int[] offsets = new int[0];
	private int fontHeight = 0;

	public View(Map map) {
		this.map = map;
	}

	public void paint(Graphics go, int width, int height) {
		paintTicks(go, width, height);

		Graphics body = go.create();
		body.translate(64, 64);
		body.clipRect(0, 0, width - 128, height - 128);
		paintBody(body);

		paintCommands(go);
	}

	private void paintCommands(Graphics go) {
		int x = 0;
		FontMetrics fm = go.getFontMetrics();
		int[] noff = new int[Map.commands.length * 2];
		String[] commands = Map.commands;
		for (int i = 0; i < commands.length; i++) {
			String command = commands[i];
			x += 10;
			noff[2 * i] = x;
			go.drawString(command, x, fm.getHeight());
			x += fm.stringWidth(command);
			noff[2 * i + 1] = x;
			x += 10;
		}
		x += 10;
		go.drawString("Total spawnpoints: " + map.spawnXes.size(), x, fm.getHeight());
		offsets = noff;
		fontHeight = fm.getHeight();
	}

	private void paintTicks(Graphics go, int width, int height) {
		go.setColor(Color.WHITE);
		Graphics xt = go.create();
		xt.clipRect(64, 0, width - 128, 64);
		for (int x = 0; x < map.cells.length; x++) {
			xt.drawString(String.valueOf(x), x * 64 + shiftX + 96, 48);
		}
		Graphics yt = go.create();
		yt.clipRect(0, 64, 64, height - 128);
		for (int y = 0; y < map.cells[0].length; y++) {
			yt.drawString(String.valueOf(y), 48, y * 64 + shiftY + 96);
		}
	}

	private void paintBody(Graphics go) {
		for (int x = 0; x < map.cells.length; x++) {
			int[] column = map.cells[x];
			for (int y = 0; y < column.length; y++) {
				go.drawImage(Loader.load(map.names.get(column[y])), x * 64 + shiftX, y * 64 + shiftY, null);
			}
		}
		for (int i = 0; i < map.spawnXes.size(); i++) {
			go.drawImage(Loader.load("user"), map.spawnXes.get(i) + shiftX, map.spawnYes.get(i) + shiftY, null);
		}
	}

	public void pressed(int x, int y, int width, int height) {
		if (y <= fontHeight) {
			for (int i = 0; i < offsets.length; i += 2) {
				if (x >= offsets[i] && x < offsets[i + 1]) {
					map.command(i / 2);
					return;
				}
			}
		}
		if (x >= 64 && x < width - 64 && y >= 64 && y < height - 64) {
			if (shiftKeyHeld) {
				int ex = x - shiftX - 96, ey = y - shiftY - 96;
				boolean done = false;
				for (int i = 0; i < map.spawnXes.size(); i++) {
					int dx = map.spawnXes.get(i) - ex, dy = map.spawnYes.get(i) - ey;
					int distSq = dx * dx + dy * dy;
					if (distSq < OVERLAP_RADIUS * OVERLAP_RADIUS) {
						map.spawnXes.remove(i);
						map.spawnYes.remove(i);
						done = true;
						break;
					}
				}
				if (!done) {
					map.spawnXes.add(ex);
					map.spawnYes.add(ey);
				}
			} else {
				int ix = (x - shiftX) / 64 - 1;
				int iy = (y - shiftY) / 64 - 1;
				if (ix >= 0 && ix < map.cells.length) {
					if (iy >= 0 && iy < map.cells[ix].length) {
						map.cells[ix][iy] = (map.cells[ix][iy] + 1) % map.names.size();
					}
				}
			}
		}
	}

	public void onKey(char keyChar, int keyCode, boolean press) {
		int mul = press ? -1 : 1;
		switch (keyCode) {
		case KeyEvent.VK_LEFT:
			dX -= 8 * mul;
			break;
		case KeyEvent.VK_RIGHT:
			dX += 8 * mul;
			break;
		case KeyEvent.VK_UP:
			dY -= 8 * mul;
			break;
		case KeyEvent.VK_DOWN:
			dY += 8 * mul;
			break;
		case KeyEvent.VK_SHIFT:
			shiftKeyHeld = press;
			break;
		}
		if (dX > 8) {
			dX = 8;
		} else if (dX < -8) {
			dX = -8;
		}
		if (dY > 8) {
			dY = 8;
		} else if (dY < -8) {
			dY = -8;
		}
	}

	public void update() {
		shiftX += dX;
		shiftY += dY;
	}
}
