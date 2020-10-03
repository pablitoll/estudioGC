package ar.com.tnba.utils.besumenesbancarios.business.procesarImagen;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Cortar {

	public static BufferedImage cortar(File fileOCR, int x, int with) throws IOException {
		BufferedImage img = null;

		// read image
		try {
			img = ImageIO.read(fileOCR);
		} catch (IOException e) {
			System.out.println(e);
		}

		javaxt.io.Image image = new javaxt.io.Image(img);
		image.crop(x, 0, with, img.getHeight());

		return image.getBufferedImage();
	}// main() ends here
}// class ends here