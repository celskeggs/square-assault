package codeday.squareassault.client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

public class Loader {
	private static final HashMap<String, BufferedImage> loaded = new HashMap<>();

	public static synchronized BufferedImage load(String name) {
		try {
			if (loaded.containsKey(name)) {
				return loaded.get(name);
			}
			BufferedImage out = ImageIO.read(Loader.class.getResourceAsStream("/codeday/squareassault/resources/" + name + ".png"));
			loaded.put(name, out);
			return out;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
