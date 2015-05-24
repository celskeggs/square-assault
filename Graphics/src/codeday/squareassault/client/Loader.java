package codeday.squareassault.client;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.imageio.ImageIO;

public class Loader {
	private static final HashMap<String, BufferedImage> loaded = new HashMap<>();
	
	private static final BufferedImage nonexistent = new BufferedImage(64, 64, BufferedImage.TYPE_4BYTE_ABGR);
	
	static {
		Graphics g = nonexistent.getGraphics();
		g.setColor(Color.PINK);
		g.fillRect(0, 0, nonexistent.getWidth(), nonexistent.getHeight());
	}

	public static synchronized BufferedImage load(String name) {
		try {
			if (loaded.containsKey(name)) {
				return loaded.get(name);
			}
			InputStream x = Loader.class.getResourceAsStream("/codeday/squareassault/resources/" + name + ".png");
			BufferedImage out;
			if (x == null) {
				out = nonexistent;
				System.err.println("Cannot find image: " + name);
			} else {
				out = ImageIO.read(x);
			}
			loaded.put(name, out);
			return out;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
