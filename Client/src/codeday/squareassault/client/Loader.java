package codeday.squareassault.client;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Loader {
	public static BufferedImage load(String name) {
		try {
			return ImageIO.read(Loader.class.getResourceAsStream("/codeday/squareassault/resources/" + name + ".png"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
