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

public class AppOcrBBVA {
	public static void main(String[] args) throws Exception {
		final ITesseract instance = new Tesseract1(); // JNA Direct Mapping
		// instance.setTessVariable("preserve_interword_spaces", "1");
		// instance.setTessVariable("psm", "6");
		instance.setDatapath("C:\\temp\\tessdata"); // path to tessdata directory
		File tessDataFolder = LoadLibs.extractNativeResources("tessdata");
		instance.setDatapath(tessDataFolder.getAbsolutePath());

		File dir = new File("C:\\Users\\pmv1283\\Desktop\\ESTUDIOGC\\resumenesBBVA\\");

		String[] extensions = new String[] { "tif" };

		List<File> ficheros = (List<File>) FileUtils.listFiles(dir, extensions, true);
		// int i = 0;
		for (final File archivo : ficheros) {
			procesarArchivo(instance, archivo);
		}

	}

	/**
	 * @param instance
	 * @param archivo
	 * @throws Exception 
	 */
	public static void procesarArchivo(ITesseract instance, File archivo) throws Exception {
		try {
			System.out.println("Procesando: " + archivo.getName());
			String result = instance.doOCR(archivo);
			String result1 = result.substring(result.lastIndexOf("FECHA"), result.lastIndexOf("TRANSPORTE SALDO"));

			String[] parts = result1.split("\n");

			String saldoInicial = "";
			// busco el saldo inicial o el transporte saldo

			for (int i = 0; i < parts.length; i++) {
				if (parts[i].contains("SALDO ANTERIOR")) {
					saldoInicial = parts[i].replace("SALDO ANTERIOR ", "");
					break;
				} else {
					if (parts[i].contains("TRANSPORTE SALDO")) {
						saldoInicial = parts[i].replace("TRANSPORTE SALDO ", "");
						break;
					}
				}
			}

			// retiro las 2 primeras lineas
			String[] parts2 = new String[parts.length - 2];
			for (int i = 2; i < parts.length; i++) {
				parts2[i - 2] = parts[i];
			}

			for (int i = 0; i < parts2.length; i++) {

				// saco el blanco despues de la coma en el saldo
				parts2[i] = parts2[i].replace(", ", ",");
				// parts2[i] = parts2[i].replace(" ", ",");
				String c = parts2[i];
				// // separo fecha
				StringBuffer sb = new StringBuffer(c);
				sb.setCharAt(5, ';');
				int finmovi = sb.lastIndexOf(" ");
				sb.setCharAt(finmovi, ';');
				int inimovi = sb.lastIndexOf(" ");
				sb.setCharAt(inimovi, ';');
				// String movimiento = sb.substring(inimovi + 1, finmovi);
				// obtengo el saldo total
				String saldoFinal = sb.substring(sb.lastIndexOf(";") + 1, sb.length()).replace(".", "").replace(",", ".");

				// Si el saldo Final > al Saldo Inicial Credito

				double dblSaldoInicial = Double.parseDouble(saldoInicial.replace(" ", "").replace(".", "").replace(",", "."));
				saldoInicial = sb.substring(sb.lastIndexOf(";") + 1, sb.length());
				double dblSaldoFinal = Double.parseDouble(saldoFinal.replace(" ", ""));
				if (dblSaldoFinal > dblSaldoInicial) {
					// credito

					sb.insert(inimovi, ';');
				} else { // debito
					sb.insert(finmovi, ';');
				}

				parts2[i] = sb.toString();
				System.out.println(c);
				//
				System.out.println(parts2[i]);

			}

			StringJoiner sj = new StringJoiner("\n");
			for (String s : parts2) {
				sj.add(s);
			}
			result1 = sj.toString();

			// String[] parts = result1.split("\n");
			// for (int i = 0; i < parts.length; i++) {
			// parts[i] = parts[i].replaceFirst(" ", ";");
			// }
			System.out.println(result1);
			Files.write(Paths.get(archivo.getName() + ".csv"), result1.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			// Files.write(Paths.get("C:\\Users\\pmv1283\\Desktop\\ESTUDIOGC\\resumenesBBVA\\"
			// + archivo.getName() + ".csv"), result1.getBytes(), StandardOpenOption.CREATE,
			// StandardOpenOption.APPEND);

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
			throw (e);
		}
	}
}
