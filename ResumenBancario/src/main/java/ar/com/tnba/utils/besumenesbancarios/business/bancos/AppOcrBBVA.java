package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.io.File;
import java.util.StringJoiner;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.rp.ui.common.Common;
import ar.com.tnba.utils.besumenesbancarios.business.CommonResumenBancario;
import net.sourceforge.lept4j.util.LoadLibs;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract1;

public class AppOcrBBVA implements BancosInterface {

	private static final String SEP_MIL_FRANCES = ".";
	private static final String SEP_DEC_FRANCES = ",";
	private static final String TRANSPORTE_SALDO = "TRANSPORTE SALDO";
	private static final String SALDO_AL = "SALDO AL";
	private static final String SALDO_ANTERIOR = "SALDO ANTERIOR";
	private static final String SIN_MOVIMIENTOS = "S/MOVIMIENTOS";

	public String procesarArchivo(String strOcr, File archivo, Integer pagina) throws Exception {
		try {
			System.out.println("Procesando Frances: " + archivo.getName() + " Pagina " + pagina);

			String strOcrFormateado = "";

			int idxTransporteFin = strOcr.lastIndexOf(TRANSPORTE_SALDO);
			int idxSaldoAl = strOcr.indexOf(SALDO_AL);
			int idxFin = (idxTransporteFin > -1 ? idxTransporteFin : idxSaldoAl);

			int idxTransporteInicio = strOcr.indexOf(TRANSPORTE_SALDO);
			int idxSaldoAnt = strOcr.indexOf(SALDO_ANTERIOR);
			int idxIni = (idxTransporteInicio > -1 ? idxTransporteInicio : idxSaldoAnt);

			if ((idxIni > -1) && (idxFin > -1) && (idxIni < idxFin)) {

				strOcrFormateado = strOcr.substring(idxIni, idxFin);

				if (!strOcrFormateado.contains(SIN_MOVIMIENTOS)) {

					String[] parts = strOcrFormateado.split("\n");

					String saldoInicial = parts[0].replace(TRANSPORTE_SALDO, "").replaceAll(SALDO_ANTERIOR, "");
					Double dblSaldoAnterior = CommonResumenBancario.String2Double(saldoInicial, SEP_MIL_FRANCES, SEP_DEC_FRANCES);

					// retiro las 2 primeras lineas
					String[] parts2 = new String[parts.length - 1];
					for (int i = 1; i < parts.length; i++) {
						if (!parts[i].contains(SIN_MOVIMIENTOS)) {
							parts2[i - 1] = parts[i];
						}
					}

					for (int i = 0; i < parts2.length; i++) {

						// saco el blanco despues de la coma en el saldo
						parts2[i] = parts2[i].replace(", ", ",").replace(" ,", ",");
						String c = parts2[i];
						// // separo fecha
						StringBuffer sb = new StringBuffer(c);
						sb.setCharAt(5, ';');
						int finmovi = sb.lastIndexOf(" ");
						sb.setCharAt(finmovi, ';');
						int inimovi = sb.lastIndexOf(" ");
						sb.setCharAt(inimovi, ';');

						String rengistroSplit[] = sb.toString().split(";");
						// ya tengo el registro spliteado en registroSplit, de ahora sigo con ese

						String saldoFinal = rengistroSplit[rengistroSplit.length - 1];

						double dblSaldoRenglon = CommonResumenBancario.String2Double(saldoFinal, SEP_MIL_FRANCES, SEP_DEC_FRANCES);
						double dblValorMovimiento = CommonResumenBancario.String2Double(rengistroSplit[2], SEP_MIL_FRANCES, SEP_DEC_FRANCES);

						String debito = "";
						String credito = "";
						if (dblSaldoRenglon > dblSaldoAnterior) {
							// credito
							credito = CommonUtils.double2String(dblValorMovimiento, Common.getGeneralSettings().getSeparadorMiles(),
									Common.getGeneralSettings().getSeparadorDecimal());

						} else { // debito
							debito = CommonUtils.double2String(dblValorMovimiento, Common.getGeneralSettings().getSeparadorMiles(),
									Common.getGeneralSettings().getSeparadorDecimal());
						}
						dblSaldoAnterior = dblSaldoRenglon;

						parts2[i] = String.format("%s;%s;%s;%s;%s", rengistroSplit[0], rengistroSplit[1], debito, credito,
								CommonUtils.double2String(dblSaldoRenglon, Common.getGeneralSettings().getSeparadorMiles(), Common.getGeneralSettings().getSeparadorDecimal()));
					}

					StringJoiner sj = new StringJoiner("\n");
					for (String s : parts2) {
						sj.add(s);
					}
					strOcrFormateado = sj.toString();
				} else {
					strOcrFormateado = "";
				}
			}
			return strOcrFormateado;

		} catch (Exception e) {
			e.printStackTrace();
			throw (e);
		}
	}

	// private double SanityDouble(String cadena) {
	// double dblSaldoFinal;
	// String aux = cadena;
	// // saco los blancos
	// aux = aux.replace(" ", "");
	// // saco todos los puntos
	// aux = aux.replace(".", "");
	// // saco todos las comas
	// aux = aux.replace(",", "");
	// // le agrego un punto a 2 caracteres del final
	// String saldoFinalaux = new StringBuilder(aux).insert(aux.length() - 2,
	// ".").toString(); // asumo que el ultimo
	//
	// aux = saldoFinalaux.toString();
	//
	// dblSaldoFinal = Double.parseDouble(aux);
	//
	// return dblSaldoFinal;
	// }

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
