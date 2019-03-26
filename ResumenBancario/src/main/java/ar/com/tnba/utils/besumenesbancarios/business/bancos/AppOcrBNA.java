package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.io.File;
import java.util.StringJoiner;

import net.sourceforge.lept4j.util.LoadLibs;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract1;

public class AppOcrBNA implements BancosInterface {

	@Override
	public String procesarArchivo(String strOcr, File archivo, Integer pagina) throws Exception {
		System.out.println("Procesando Nacion: " + archivo.getName() + " Pagina " + pagina);

		try {
			String strOcrFormateado = strOcr.substring(strOcr.lastIndexOf("SALDO ANTERIOR"), strOcr.lastIndexOf("SALDO FINAL"));

			// ALTA NEGRADA
			strOcrFormateado = strOcrFormateado.replace("                    ", ";");
			strOcrFormateado = strOcrFormateado.replace("                   ", ";");
			strOcrFormateado = strOcrFormateado.replace("                  ", ";");
			strOcrFormateado = strOcrFormateado.replace("                 ", ";");
			strOcrFormateado = strOcrFormateado.replace("                ", ";");
			strOcrFormateado = strOcrFormateado.replace("               ", ";");
			strOcrFormateado = strOcrFormateado.replace("              ", ";");
			strOcrFormateado = strOcrFormateado.replace("             ", ";");
			strOcrFormateado = strOcrFormateado.replace("            ", ";");
			strOcrFormateado = strOcrFormateado.replace("           ", ";");
			strOcrFormateado = strOcrFormateado.replace("          ", ";");
			strOcrFormateado = strOcrFormateado.replace("         ", ";");
			strOcrFormateado = strOcrFormateado.replace("        ", ";");
			strOcrFormateado = strOcrFormateado.replace("       ", ";");
			strOcrFormateado = strOcrFormateado.replace("      ", ";");
			strOcrFormateado = strOcrFormateado.replace("     ", ";");
			strOcrFormateado = strOcrFormateado.replace("    ", ";");
			strOcrFormateado = strOcrFormateado.replace("   ", ";");
			strOcrFormateado = strOcrFormateado.replace("  ", ";");

			strOcrFormateado = strOcrFormateado.replace(";;", ";");
			strOcrFormateado = strOcrFormateado.replace(";;", ";");
			strOcrFormateado = strOcrFormateado.replace(";;", ";");
			// result1 = result1.replaceFirst(" ", ";");

			String[] parts = strOcrFormateado.split("\n");
			for (int i = 0; i < parts.length; i++) {
				parts[i] = parts[i].replaceFirst(" ", ";");
			}
			System.out.println(strOcrFormateado);

			String[] parts2 = new String[parts.length - 1];
			for (int i = 1; i < parts.length; i++) {
				parts2[i - 1] = parts[i];
			}
			StringJoiner sj = new StringJoiner("\n");
			for (String s : parts2) {
				sj.add(s);
			}
			strOcrFormateado = sj.toString();

			return strOcrFormateado;

		} catch (Exception e) {
			e.printStackTrace();
			throw (e);
		}
	}

	@Override
	public String getOCR(File archivoOCR) throws Exception {
		System.out.println("Procesando OCR: " + archivoOCR.getName());
		return getInstanceNacion(archivoOCR).doOCR(archivoOCR);
	}

	private ITesseract getInstanceNacion(File archivoOCR) {
		ITesseract instanceNacion = new Tesseract1(); // JNA Direct Mapping
		instanceNacion.setTessVariable("preserve_interword_spaces", "1");
		instanceNacion.setDatapath(archivoOCR.getParent() + File.separator + "temp\\tessdata"); // path to tessdata directory
		File tessDataFolder = LoadLibs.extractNativeResources("tessdata");
		instanceNacion.setDatapath(tessDataFolder.getAbsolutePath());
		return instanceNacion;
	}
}
