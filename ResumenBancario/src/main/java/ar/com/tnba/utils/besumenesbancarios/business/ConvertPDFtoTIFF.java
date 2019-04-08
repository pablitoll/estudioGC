package ar.com.tnba.utils.besumenesbancarios.business;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

public class ConvertPDFtoTIFF {

	public static List<File> convert(File PDFfile, File directorioDestino) throws Exception, IOException {
		List<File> listaFiles = new ArrayList<File>();
		PDDocument document = PDDocument.load(PDFfile);
		PDFRenderer pdfRenderer = new PDFRenderer(document);
		for (int page = 0; page < document.getNumberOfPages(); ++page) {
			BufferedImage bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.GRAY);
			String filename = directorioDestino.getPath() + File.separator + ManejoDeArchivos.getNombreArchivoTIF(PDFfile.getName(), page + 1);
			ImageIOUtil.writeImage(bim, filename, 300);
			listaFiles.add(new File(filename));
		}
		document.close();

		return listaFiles;
	}
}