package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.io.FileUtils;

import net.sourceforge.lept4j.util.LoadLibs;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract1;

public class AppOcrBNA {
	public static void main(String[] args) throws Exception {
		ITesseract instance = new Tesseract1(); // JNA Direct Mapping
		instance.setTessVariable("preserve_interword_spaces", "1");
		instance.setDatapath("C:\\temp\\tessdata"); // path to tessdata directory
		File tessDataFolder = LoadLibs.extractNativeResources("tessdata");
		instance.setDatapath(tessDataFolder.getAbsolutePath());

		File dir = new File("C:\\Users\\pmv1283\\Desktop\\ESTUDIOGC\\resumenesBNA\\");

		String[] extensions = new String[] { "tif" };

		List<File> ficheros = (List<File>) FileUtils.listFiles(dir, extensions, true);

		for (File archivo : ficheros) {

			try {
				String result = instance.doOCR(archivo);
				String result1 = result.substring(result.lastIndexOf("SALDO ANTERIOR"), result.lastIndexOf("SALDO FINAL"));

				// ALTA NEGRADA

				result1 = result1.replace("                    ", ";");
				result1 = result1.replace("                   ", ";");
				result1 = result1.replace("                  ", ";");
				result1 = result1.replace("                 ", ";");
				result1 = result1.replace("                ", ";");
				result1 = result1.replace("               ", ";");
				result1 = result1.replace("              ", ";");
				result1 = result1.replace("             ", ";");
				result1 = result1.replace("            ", ";");
				result1 = result1.replace("           ", ";");
				result1 = result1.replace("          ", ";");
				result1 = result1.replace("         ", ";");
				result1 = result1.replace("        ", ";");
				result1 = result1.replace("       ", ";");
				result1 = result1.replace("      ", ";");
				result1 = result1.replace("     ", ";");
				result1 = result1.replace("    ", ";");
				result1 = result1.replace("   ", ";");
				result1 = result1.replace("  ", ";");

				result1 = result1.replace(";;", ";");
				result1 = result1.replace(";;", ";");
				result1 = result1.replace(";;", ";");
				// result1 = result1.replaceFirst(" ", ";");

				String[] parts = result1.split("\n");
				for (int i = 0; i < parts.length; i++) {
					parts[i] = parts[i].replaceFirst(" ", ";");
				}
				System.out.println(result1);

				String[] parts2 = new String[parts.length - 1];
				for (int i = 1; i < parts.length; i++) {
					parts2[i - 1] = parts[i];
				}
				StringJoiner sj = new StringJoiner("\n");
				for (String s : parts2) {
					sj.add(s);
				}
				result1 = sj.toString();

				Files.write(Paths.get("C:\\Users\\pmv1283\\Desktop\\ESTUDIOGC\\resumenesBNA\\" + archivo.getName() + ".csv"), result1.getBytes(), StandardOpenOption.CREATE,
						StandardOpenOption.APPEND);

			} catch (Exception e) {
				e.printStackTrace();
				System.err.println(e.getMessage());
			}
		}

	}
}
