package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.tnba.utils.besumenesbancarios.business.bancos.BancosBusiness.Bancos;
import ar.com.tnba.utils.besumenesbancarios.business.procesarImagen.SoloNegroICBC;

public class AppOcrPatagonia extends BaseBancos {

	private static final int POS_FIN_DES = 40;
	private static final int POS_FIN_DEBITO = 84;
	private static final int POS_FIN_CREDITO = 106; //100
	private static final int POS_FIN_TOTAL = 120;
	private static final int POS_FIN_FECHA = 8;
	private static final int POS_FIN_COMP = 62;
	private static final String HEADER_FECHA = "FECHA[ ]+CONCEPTO[ ]+REFER\\.[ ]+FECHA[ ]+VALOR[ ]+DEBITOS[ ]+CREDITOS[ ]+SALDO";
	private Pattern patternHeaderFecha = Pattern.compile(HEADER_FECHA);

	private static final String FOOTER_SALDO = "SALDO ACTUAL";
	private static final String FOOTER_FIN = "Si usted reviste el caracter de consumidor final, no responsable";											  
	private static final String SALDO_ANTERIOR = "SALDO ANTERIOR";

	public AppOcrPatagonia() {
		super(Bancos.PATAGONIA);
	}
	
	@Override
	public String getOCR(File archivoOCR) throws Exception {

		System.out.println("Procesando OCR (PATAGONIA): " + archivoOCR.getName());
		BufferedImage bi = SoloNegroICBC.procesar(archivoOCR);

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

	public String[] getRegistrosFromOCR(String strOcr, File archivo, Integer pagina) throws Exception {
		String strOcrFormateado = "";
		strOcr = strOcr.replaceAll("~", "");
		strOcr = strOcr.replaceAll("§", "");		
		
		Matcher m = patternHeaderFecha.matcher(strOcr);
		int idxInicio = -1;
		if (m.find()) {
			idxInicio = m.end();
		}

		// fin
		Integer vecMin[] = new Integer[3];
		int idxSaldo_Fin = strOcr.indexOf(FOOTER_SALDO);
		int idxFin_Fin = strOcr.indexOf(FOOTER_FIN);

		vecMin[0] = idxSaldo_Fin == -1 ? NUMERO_ALTO : idxSaldo_Fin;
		vecMin[1] = idxFin_Fin == -1 ? NUMERO_ALTO : idxFin_Fin;
		vecMin[2] = strOcr.length();

		Integer idxFinal = CommonUtils.minimo(vecMin);

		if ((idxInicio != -1) && (idxFinal != NUMERO_ALTO) && (idxInicio < idxFinal)) {

			strOcrFormateado = strOcr.substring(idxInicio, idxFinal);

			strOcrFormateado = strOcrFormateado.replaceAll(", ", ",");
			strOcrFormateado = strOcrFormateado.replaceAll(" ,", ",");
			strOcrFormateado = strOcrFormateado.replaceAll(" \\.", ".");
			

			String[] parts = strOcrFormateado.split("\n");

			for (int i = 0; i < parts.length; i++) {
				if (parts[i].contains(HEADER_FECHA) || parts[i].contains(FOOTER_SALDO) || (parts[i].length() < POS_FIN_DES)) {
					parts[i] = "";
				} else {
					if (parts[i].contains(SALDO_ANTERIOR)) {
						String reg[] = parts[i].split(SALDO_ANTERIOR);
						saldoInicial = String2Double(reg[reg.length - 1], SEP_MILES, SEP_DEC);
						parts[i] = "";
					} else {
						parts[i] = parts[i] + getEmptyRegistro(); // no esta bien pero bueno,anda
						parts[i] = insertarSeparador(parts[i], POS_FIN_FECHA);
						parts[i] = insertarSeparador(parts[i], POS_FIN_DES + 1);
						parts[i] = insertarSeparador(parts[i], POS_FIN_COMP + 2);
						parts[i] = insertarSeparador(parts[i], POS_FIN_DEBITO + 3);
						parts[i] = insertarSeparador(parts[i], POS_FIN_CREDITO + 4);
						parts[i] = insertarSeparador(parts[i], POS_FIN_TOTAL + 5);
					}
				}
			}

			return parts;
		}

		return null;
	}

	@Override
	protected Double darSaldoSubTotal(String registro) throws Exception {
		String reg[] = registro.split(";");
		// Si termina en ;, es porque no esta la ultima columna (la de saldo)
		if (!registro.substring(registro.length() - 1).equals(";") && !reg[reg.length - 1].trim().equals("")) {
			return String2Double(reg[reg.length - 1].trim(), SEP_MILES, SEP_DEC);
		}

		return SALDO_TOTAL_NO_VALIDO;
	}

	@Override
	protected String armarRegistro(String registro, Double saldoInicial) throws Exception {
		String reg[] = registro.split(";");

		// Double nuevoTotal = saldoInicial;
		String debito = "";
		String credito = "";
		Double valor = 0.0;
		Double valorSubTotal = SALDO_TOTAL_NO_VALIDO;

		if (!reg[5].trim().equals("")) {
			// es total
			valorSubTotal = String2Double(reg[5].trim(), SEP_MILES, SEP_DEC);
		}

		if (!reg[3].trim().equals("")) {
			// es debito
			valor = String2Double(reg[3].trim(), SEP_MILES, SEP_DEC);
			debito = CommonUtils.double2String(valor, SEP_MILES, SEP_DEC);

			valor = valor * -1;
		}

		if (!reg[4].trim().equals("")) {
			// es credito
			valor = String2Double(reg[4].trim(), SEP_MILES, SEP_DEC);
			credito = CommonUtils.double2String(valor, SEP_MILES, SEP_DEC);
		}

		String strSaldo = "";
		if ((saldoInicial != SALDO_TOTAL_NO_VALIDO) || (valorSubTotal != SALDO_TOTAL_NO_VALIDO)) {
			Double nuevoTotal = 0.0;

			if (saldoInicial == SALDO_TOTAL_NO_VALIDO) {
				nuevoTotal = valorSubTotal;
			} else {
				nuevoTotal = saldoInicial + valor;

				// Solo valido aca, porque en el caso anterior no tengo el saldo inical
				if ((valorSubTotal != SALDO_TOTAL_NO_VALIDO) && (Math.abs(valorSubTotal - nuevoTotal) >= 1.0)) {
					throw new ExceptionSubTotal("No coincide el subtotal");
				}
			}

			strSaldo = CommonUtils.double2String(CommonUtils.redondear(nuevoTotal, 2), SEP_MILES, SEP_DEC);
		}

		return String.format("%s;%s;%s;%s;%s;%s", reg[0].trim(), reg[1].trim(), reg[2].trim(), debito, credito, strSaldo);
	}

}
