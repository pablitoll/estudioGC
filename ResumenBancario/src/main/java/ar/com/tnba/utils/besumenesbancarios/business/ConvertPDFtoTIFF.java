package ar.com.tnba.utils.besumenesbancarios.business;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

public class ConvertPDFtoTIFF {

	public static void main(String[] args) throws Exception {

		File dir = new File("C:\\Users\\pmv1283\\Desktop\\ESTUDIOGC\\resumenesBNA\\");

		String[] extensions = new String[] { "pdf" };

		List<File> ficheros = (List<File>) FileUtils.listFiles(dir, extensions, true);

		for (File archivo : ficheros) {

			// convert("C:\\Users\\pmv1283\\Documents\\BNA\\2017 12 - BANCO NACION.pdf");
			convert(archivo);
		}

	}

	public static void convert(File PDFfile) throws Exception, IOException {

		PDDocument document = PDDocument.load(PDFfile);
		PDFRenderer pdfRenderer = new PDFRenderer(document);
		for (int page = 0; page < document.getNumberOfPages(); ++page) {
			BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.GRAY);
			String filename = Paths.get(PDFfile.getPath()).getParent().toString() + "\\" + PDFfile.getName() + "-" + (page + 1) + ".tif";
			// suffix in filename will be used as the file format
			// PDFfile.getPath()
			ImageIOUtil.writeImage(bim, filename, 300);
		}
		document.close();

	}

}