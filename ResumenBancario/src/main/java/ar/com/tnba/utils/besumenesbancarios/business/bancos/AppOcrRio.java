package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.tnba.utils.besumenesbancarios.business.bancos.BancosBusiness.Bancos;
import net.sourceforge.lept4j.util.LoadLibs;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract1;

public class AppOcrRio extends BaseBancos {

	public AppOcrRio() {
		super(Bancos.RIO);
	}

	private static final int POS_FIN_FECHA = 8;
	private static final String HEADER_FECHA = "CTA CORR PESOS Nro.";
	private static final String FOOT_SALTO = "CTA CORR DOLAR Nro.";
	private static final String SALDO = "SALDO RESUMEN ANTERIOR";
	private static final int POS_MARGEN_IZQ = 3;
	private static final String REG_EXP_FECHA = "[0123456789]{2}+[\\/]+[0123456789]{2}+[\\/]+[0123456789]{2}";
	private static final String ERROR_TIPO_OPERACION = "No se puede determinar el Tipo de Operacion (D/C)";
	private Pattern pattern = Pattern.compile(REG_EXP_FECHA);

	@Override
	public String[] getRegistrosFromOCR(String strOcr, File archivo, Integer pagina) throws Exception {
		String strOcrFormateado = "";

		// inicio
		int idxInicio = strOcr.indexOf(HEADER_FECHA);

		// fin
		int idxDolar_Fin = strOcr.lastIndexOf(FOOT_SALTO);

		Integer idxFinal = strOcr.length();
		if (idxDolar_Fin > -1) {
			idxFinal = idxDolar_Fin;
		}

		if (idxInicio != -1) {

			strOcrFormateado = strOcr.substring(idxInicio, idxFinal);

			// ALTA NEGRADA
			strOcrFormateado = strOcrFormateado.replaceAll(", ", ",");
			strOcrFormateado = strOcrFormateado.replaceAll(" ,", ",");
			strOcrFormateado = strOcrFormateado.replaceAll("[0123456789] -", "-");
			strOcrFormateado = strOcrFormateado.replaceAll(" \\.", ".");
			strOcrFormateado = strOcrFormateado.replaceAll("~", "");

			String[] parts = strOcrFormateado.split("\n");
			saldoInicial = SALDO_TOTAL_NO_VALIDO;
			boolean procesoRegistro = false;

			for (int i = 0; i < parts.length; i++) {
				if (parts[i].contains(HEADER_FECHA)) {
					parts[i] = "";
				} else {

					// hay casos que el OCR no reconoce el fin de SALDO, y se esta lletendo el saldo
					// de DOLAR, por eso pongo la bandera de si hay registros procesados no tome el
					// saldo (porque es el dolar)
					if (parts[i].contains(SALDO) && !procesoRegistro) {
						String reg[] = parts[i].split(SALDO);
						saldoInicial = String2Double(reg[reg.length - 1], SEP_MILES, SEP_DEC);
						parts[i] = "";
					} else {

						if (parts[i].length() < 8) {
							parts[i] = "";
						} else {
							int indFecha = indexFecha(parts[i]);
							// parts[i] = parts[i].substring(POS_MARGEN_IZQ).trim(); gfdg //puede ser 2 o
							// tres la longitud
							if (indFecha > -1) {
								parts[i] = parts[i].substring(indFecha).trim();
								int idx_lastSpace = parts[i].lastIndexOf(" "); // El total
								parts[i] = insertarSeparador(parts[i], idx_lastSpace);
								idx_lastSpace = parts[i].lastIndexOf(" ", idx_lastSpace); // El el valor
								parts[i] = insertarSeparador(parts[i], idx_lastSpace);

								parts[i] = insertarSeparador(parts[i], POS_FIN_FECHA);
								procesoRegistro = true;
							} else {
								parts[i] = parts[i].substring(POS_MARGEN_IZQ).trim();
								int indx = parts[i - 1].indexOf(";", POS_FIN_FECHA + 1);
								if (indx > -1) {
									parts[i - 1] = insertarSeparador(parts[i - 1], indx, parts[i]);
								}
								parts[i] = "";
							}
						}
					}
				}
			}
			return parts;
		}

		return null;
	}

	private int indexFecha(String cadena) {
		Matcher matcher = pattern.matcher(cadena);
		int indice = -1;
		if (matcher.find()) {
			indice = matcher.start();
		}
		if (indice <= 5) {
			return indice;
		}
		return -1;
	}

	@Override
	protected Double darSaldoSubTotal(String registro) throws Exception {
		String reg[] = registro.split(";");
		if (!reg[reg.length - 1].trim().equals(ERROR_TIPO_OPERACION) && !reg[reg.length - 1].trim().contains(LEYENDA_FALLO)) {
			return String2Double(reg[reg.length - 1].trim(), SEP_MILES, SEP_DEC);
		} else {
			return String2Double(reg[reg.length - 2].trim(), SEP_MILES, SEP_DEC);
		}
	}

	@Override
	protected String armarRegistro(String registro, Double saldoOperacionAnterior) throws Exception {
		String reg[] = registro.split(";");
		Double valor = 0.0;
		Double valorSubTotal = SALDO_TOTAL_NO_VALIDO;

		if (!reg[3].trim().equals("")) {
			// es total
			valorSubTotal = String2Double(reg[3].trim(), SEP_MILES, SEP_DEC);
		}

		if (!reg[2].trim().equals("")) {
			// es debito
			valor = String2Double(reg[2].trim(), SEP_MILES, SEP_DEC);
		}

		if ((saldoOperacionAnterior != SALDO_TOTAL_NO_VALIDO) && (valorSubTotal != SALDO_TOTAL_NO_VALIDO)) {
			String strSaldo = "";
			String debito = "";
			String credito = "";

			Double nuevoTotal = saldoOperacionAnterior;

			if (saldoOperacionAnterior > valorSubTotal) {
				debito = CommonUtils.double2String(CommonUtils.redondear(valor, 2), SEP_MILES, SEP_DEC);
				nuevoTotal -= valor;
			} else {
				credito = CommonUtils.double2String(CommonUtils.redondear(valor, 2), SEP_MILES, SEP_DEC);
				nuevoTotal += valor;
			}

			// Solo valido aca, porque en el caso anterior no tengo el saldo inical
			if (Math.abs(valorSubTotal - nuevoTotal) >= 1.0) {
				throw new ExceptionSubTotal("No coincide el subtotal");
			}

			strSaldo = CommonUtils.double2String(CommonUtils.redondear(valorSubTotal, 2), SEP_MILES, SEP_DEC);

			return String.format("%s;%s;%s;%s;%s", reg[0].trim(), reg[1].trim(), debito, credito, strSaldo);
		} else {
			saldoInicial = SALDO_TOTAL_NO_VALIDO;
			return registro + ";" + ERROR_TIPO_OPERACION;
		}

	}

	@Override
	protected ITesseract getInstanceTesseract(File archivoOCR) {
		if (tesseract == null) {
			tesseract = new Tesseract1(); // JNA Direct Mapping
			//tesseract.setTessVariable("preserve_interword_spaces", "1");
			
		//	instanceFrances // path to tessdata directory
			File tessDataFolder = LoadLibs.extractNativeResources("tessdata");
			//instanceFrances.setDatapath(tessDataFolder.getAbsolutePath());
		
			
			
			tesseract.setDatapath("c:\\temp\\tessdata");//.setDatapath(archivoOCR.getParent() + File.separator + "temp\\tessdata"); // path to tessdata directory
			//File tessDataFolder = LoadLibs.extractNativeResources("tessdata");
			//tesseract.setDatapath(tessDataFolder.getAbsolutePath());
		}
		return tesseract;	}

	@Override
	protected Double darSaldoSubTotalFromErrror(String registro) {
		String reg[] = registro.split(";");

		if (!reg[3].trim().equals("")) {
			try {
				return String2Double(reg[3].trim(), SEP_MILES, SEP_DEC);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return SALDO_TOTAL_NO_VALIDO;
	}

}
