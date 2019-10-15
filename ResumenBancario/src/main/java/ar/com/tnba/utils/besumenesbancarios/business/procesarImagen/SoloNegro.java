package ar.com.tnba.utils.besumenesbancarios.business.procesarImagen;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class SoloNegro {
	public static BufferedImage procesar(File fileOCR) throws IOException {
		BufferedImage img = null;

		// read image
		try {
			img = ImageIO.read(fileOCR);
		} catch (IOException e) {
			System.out.println(e);
		}

		// get image width and height
		int width = img.getWidth();
		int height = img.getHeight();

		// convert to grayscale
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int p = img.getRGB(x, y);

				//int a = (p >> 24) & 0xff;
				
				int r = (p >> 16) & 0xff;
				int g = (p >> 8) & 0xff;
				int b = p & 0xff;
				
				//Todo 0 es negro (Si no es negro, lo paso a blanco)
				if((g > 0) && (b > 0) && (r > 0)) { 
					//Todo 255 es blanco y el ultimo es la transparencia
					p = (255 << 24) | (255 << 16) | (255 << 8) | 255;
					
					img.setRGB(x, y, p);	
				}
			}
		}

		return img;
	}// main() ends here
}// class ends here