package ar.com.tnba.utils.besumenesbancarios.business.procesarImagen;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class SoloNegroICBC {
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

				// int a = (p >> 24) & 0xff;
				int r = (p >> 16) & 0xff;
				int g = (p >> 8) & 0xff;
				int b = p & 0xff;

				//255 negro
				//0 Blanco
				if (isColor(p, 255)) {
					img.setRGB(x, y, setColor(p, 255));
				} else {
					if ((r > 210) && (g > 210) && (b > 210)) {
						// leyenda
						img.setRGB(x, y, setColor(p, 255));
					} else {
						img.setRGB(x, y, setColor(p, 0));
					}									
				}			
			}
		}

		return img;
	}

	private static int setColor(int point, int color) {
		return (color << 24) | (color << 16) | (color << 8) | 255; //255 opaco		
	}

	private static boolean isColor(int point, int color) {
		int r = (point >> 16) & 0xff;
		int g = (point >> 8) & 0xff;
		int b = point & 0xff;
		return (g == color) && (b == color) && (r == color);
	}
}