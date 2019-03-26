package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.io.File;
import java.util.StringJoiner;

import net.sourceforge.lept4j.util.LoadLibs;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract1;

public class AppOcrBBVA implements BancosInterface {

	public String procesarArchivo(String strOcr, File archivo, Integer pagina) throws Exception {
		try {
			System.out.println("Procesando Frances: " + archivo.getName() + " Pagina " + pagina);

			String strOcrFormateado = " ";

			switch (pagina) {
			case 1:
				strOcrFormateado = strOcr.substring(strOcr.indexOf("FECHA"), strOcr.lastIndexOf("TRANSPORTE SALDO"));
				break;
			default:
				// si es ultimapagina
				if (strOcr.indexOf("TOTAL MOVIMIENTOS") > 0) {
					strOcrFormateado = strOcr.substring(strOcr.lastIndexOf("TRANSPORTE SALDO"), strOcr.indexOf("SALDO AL"));
				} else {
					strOcrFormateado = strOcr.substring(strOcr.indexOf("TRANSPORTE SALDO"), strOcr.lastIndexOf("TRANSPORTE SALDO"));
				}
				break;
			}

			String[] parts = strOcrFormateado.split("\n");

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

				double dblSaldoInicial = SanityDouble(saldoInicial);
				// .parseDouble(saldoInicial.replace(" ", "").replace(".", "").replace(",",
				// "."));
				saldoInicial = sb.substring(sb.lastIndexOf(";") + 1, sb.length());
				double dblSaldoFinal = SanityDouble(saldoFinal);
				// solo tiene un punto.
				// dblSaldoFinal = SanityDouble(saldoFinal);

				if (dblSaldoFinal > dblSaldoInicial) {
					// credito

					sb.insert(inimovi, ';');
				} else { // debito
					sb.insert(finmovi, ';');
				}

				parts2[i] = sb.toString();

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

	private double SanityDouble(String cadena) {
		double dblSaldoFinal;
		String aux = cadena;
		// saco los blancos
		aux = aux.replace(" ", "");
		// saco todos los puntos
		aux = aux.replace(".", "");
		// saco todos las comas
		aux = aux.replace(",", "");
		// le agrego un punto a 2 caracteres del final
		String saldoFinalaux = new StringBuilder(aux).insert(aux.length() - 2, ".").toString(); // asumo que el ultimo

		aux = saldoFinalaux.toString();

		dblSaldoFinal = Double.parseDouble(aux);

		return dblSaldoFinal;
	}

	public String getOCR(File archivoOCR) throws Exception {
		System.out.println("Procesando OCR: " + archivoOCR.getName());
		return getInstanceFrances(archivoOCR).doOCR(archivoOCR);
	}

	private ITesseract getInstanceFrances(File archivoOCR) {
		Tesseract1 instanceFrances = new Tesseract1(); // JNA Direct Mapping
		instanceFrances.setDatapath(archivoOCR.getParent() + File.separator + "temp\\tessdata"); // path to tessdata directory
		File tessDataFolder = LoadLibs.extractNativeResources("tessdata");
		instanceFrances.setDatapath(tessDataFolder.getAbsolutePath());
		return instanceFrances;
	}
}
