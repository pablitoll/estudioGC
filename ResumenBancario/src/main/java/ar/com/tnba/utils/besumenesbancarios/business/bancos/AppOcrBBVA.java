package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.tnba.utils.besumenesbancarios.business.bancos.BancosBusiness.Bancos;
import ar.com.tnba.utils.besumenesbancarios.business.procesarImagen.Cortar;
import ar.com.tnba.utils.besumenesbancarios.business.procesarImagen.SoloNegro;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract1;

public class AppOcrBBVA extends BaseBancos {

	private static final String SOBRE_FOOTER = "Sobre (";
	private static final String HEADER = "FECHA[ ]*ORIGEN";
	private Pattern pHEADER = Pattern.compile(HEADER);
	private static final String TOTAL_MOVIMIENTOS = "TOTAL MOVIMIENTOS";
	private static final String TRANSPORTE_SALDO = "TRANSPORTE SALDO";
	private static final String SALDO_AL = "SALDO AL";
	private static final String SALDO_ANTERIOR = "SALDO ANTERIOR";
	private static final String SIN_MOVIMIENTOS = "S/MOVIMIENTOS";
	private static final int POS_FECHA = 5;

	public AppOcrBBVA() {
		super(Bancos.FRANCES);
	}

	//2019 para atras sin esto, 2020 en adelante co esto 
	@Override
	public String getOCR(File archivoOCR) throws Exception {

		System.out.println("Procesando OCR (Frances): " + archivoOCR.getName());
		BufferedImage bi = Cortar.cortar(archivoOCR, 252, 2145);

		try {
			System.out.println();
			String nombreCopia = archivoOCR.getAbsolutePath().substring(0, archivoOCR.getAbsolutePath().length() - 4) + ".imagenProcesada"
					+ archivoOCR.getAbsolutePath().substring(archivoOCR.getAbsolutePath().length() - 4);
			File fileImagenProcesda = new File(nombreCopia);
			ImageIO.write(bi, "jpg", fileImagenProcesda);
		} catch (Exception e) {
			System.out.println(e);
		}

		return getInstanceTesseract(archivoOCR).doOCR(bi);
	}

	@Override
	protected String[] getRegistrosFromOCR(String strOcr, File archivo, Integer nroPapagina) throws Exception {

		Integer vecMin[] = new Integer[2];
		if (strOcr.indexOf(SALDO_ANTERIOR) != -1) {
			vecMin[0] = strOcr.indexOf(SALDO_ANTERIOR);
		} else {
			vecMin[0] = NUMERO_ALTO;
		}

		
		Matcher matcher = pHEADER.matcher(strOcr);
		if (matcher.find()) {
			vecMin[1] =  matcher.start();
		} else {
			vecMin[1] = NUMERO_ALTO;
		}
		
		Integer idxIni = CommonUtils.minimo(vecMin);
		Integer idxFin = strOcr.indexOf(TOTAL_MOVIMIENTOS);
		if (idxFin == -1) {
			idxFin = strOcr.indexOf(SOBRE_FOOTER);
		}
		if (idxFin == -1) {
			idxFin = strOcr.indexOf(TRANSPORTE_SALDO);
		}

		if (idxIni != NUMERO_ALTO) {

			String strOcrFormateado = strOcr.substring(idxIni, idxFin);

			if (!strOcrFormateado.contains(SIN_MOVIMIENTOS)) {

				strOcrFormateado = strOcrFormateado.replaceAll(", ", ",");
				strOcrFormateado = strOcrFormateado.replaceAll(" ,", ",");
				strOcrFormateado = strOcrFormateado.replaceAll(" \\.", ".");

				String[] parts = strOcrFormateado.split("\n");
				for (int i = 0; i < parts.length; i++) {

					if (parts[i].contains(SALDO_ANTERIOR)) {
						String auxSaldoInicial = parts[i].replace(SALDO_ANTERIOR, "");
						saldoInicial = String2Double(auxSaldoInicial, SEP_MILES, SEP_DEC);
						parts[i] = "";
					}

					if (parts[i].contains(HEADER)) {
						parts[i] = "";
					}
					if (parts[i].contains(SALDO_AL)) {
						parts[i] = "";
					}
					if (parts[i].contains(SIN_MOVIMIENTOS)) {
						parts[i] = "";
					}

					if (parts[i].length() <= 10) {
						parts[i] = "";
					}
				}

				return parts;
			}
		}
		return null;

	}

	@Override
	protected String armarRegistro(String registro, Double saldoInicial) throws Exception {

		// Saco el ultimo caracter que en el pdf esta vertical
		int especial = registro.lastIndexOf(" ");
		if (registro.length() - especial <= 3) {
			registro = registro.substring(0, especial);
		}

		registro = insertarSeparadorConTrim(registro, POS_FECHA);
		int finmovi = registro.lastIndexOf(" ");
		registro = insertarSeparadorConTrim(registro, finmovi);
		int inimovi = registro.lastIndexOf(" ", finmovi);
		registro = insertarSeparadorConTrim(registro, inimovi);

		String rengistroSplit[] = registro.split(";");
		// ya tengo el registro spliteado en registroSplit, de ahora sigo con ese

		String saldoFinal = rengistroSplit[rengistroSplit.length - 1];

		double dblSaldoRenglon = String2Double(saldoFinal, SEP_MILES, SEP_DEC);
		double dblValorMovimiento = String2Double(rengistroSplit[2], SEP_MILES, SEP_DEC);

		String debito = "";
		String credito = "";
		if (dblSaldoRenglon > saldoInicial) {
			// credito
			credito = CommonUtils.double2String(dblValorMovimiento, SEP_MILES, SEP_DEC);
		} else { // debito
			debito = CommonUtils.double2String(dblValorMovimiento, SEP_MILES, SEP_DEC);
		}

		return String.format("%s;%s;%s;%s;%s", rengistroSplit[0], rengistroSplit[1], debito, credito, CommonUtils.double2String(dblSaldoRenglon, SEP_MILES, SEP_DEC));
	}

	@Override
	protected Double darSaldoSubTotal(String registro) throws Exception {
		String reg[] = registro.split(";");
		return String2Double(reg[reg.length - 1].trim(), SEP_MILES, SEP_DEC);
	}

	@Override
	protected ITesseract getInstanceTesseract(File archivoOCR) {
		if (tesseract == null) {
			tesseract = new Tesseract1(); // JNA Direct Mapping
			tesseract.setDatapath("c:\\temp\\tessdata");
		}
		return tesseract;
	}

}
