package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.tnba.utils.besumenesbancarios.business.bancos.BancosBusiness.Bancos;

public class AppOcrCiudad extends BaseBancos {

	public AppOcrCiudad() {
		super(Bancos.CIUDAD);
	}

	private static final int POS_FIN_FECHA = 11;
	private static final String HEADER_FECHA = "SISTEMA CUENTA";
	private static final String TRANSPORTE = "TRANSPORTE";
	private static final int POS_MARGEN_IZQ = 4;
	private static final String REG_EXP_FECHA = "[0123456789]{1,2}+[\\-]+\\w{2,5}+[\\-]+[0123456789]{4}";
	private static final int POS_FIN_DESC = 34;
	private static final int POS_SUBTOTAL = 3;
	private static final int POS_VALOR = 2;
	private static final int POS_FECHA = 0;
	private static final int POS_DESC = 1;
	private Pattern pattern = Pattern.compile(REG_EXP_FECHA);
	private static final String ERROR_TIPO_OPERACION = "No se puede determinar el Tipo de Operacion (D/C)";

	@Override
	public String[] getRegistrosFromOCR(String strOcr, File archivo, Integer pagina) throws Exception {
		String strOcrFormateado = "";

		// inicio
		int idxInicio = strOcr.indexOf(HEADER_FECHA);

		// fin
		Integer idxFinal = strOcr.length();

		if (idxInicio != -1) {

			strOcrFormateado = strOcr.substring(idxInicio, idxFinal);

			// ALTA NEGRADA
			strOcrFormateado = strOcrFormateado.replaceAll(", ", ",");
			strOcrFormateado = strOcrFormateado.replaceAll("\\-N\\w*V\\-", "-NOV-");
			strOcrFormateado = strOcrFormateado.replaceAll("\\.,", ".");
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
					if (parts[i].contains(TRANSPORTE) && !procesoRegistro) {
						String reg[] = parts[i].split(TRANSPORTE);
						saldoInicial = String2Double(reg[reg.length - 1], SEP_MILES, SEP_DEC);
						parts[i] = "";
					} else {

						if (parts[i].length() < POS_MARGEN_IZQ) {
							parts[i] = "";
						} else {
							int indFecha = indexFecha(parts[i]);
							if ((indFecha > -1) && !parts[i].contains(TRANSPORTE)) {
								parts[i] = parts[i].substring(indFecha).trim();
								parts[i] = insertarSeparador(parts[i], POS_FIN_FECHA);
								parts[i] = insertarSeparadorConTrim(parts[i], POS_FIN_DESC + 1);

								// Valor
								int idxSpace = parts[i].indexOf(" ", POS_FIN_DESC + 1);
								parts[i] = insertarSeparadorConTrim(parts[i], idxSpace);

								// Subtotal
								idxSpace = parts[i].indexOf(" ", idxSpace);
								if(idxSpace == -1) {
									idxSpace =  parts[i].length();
								}
								parts[i] = insertarSeparadorConTrim(parts[i], idxSpace);

								// Manejo de SubTotal
								// String auxSubTotal = parts[i].substring(POS_FIN_CREDITO + 3).trim();
								// parts[i] = parts[i].substring(0, POS_FIN_CREDITO + 4);
								//
								// int idx = auxSubTotal.indexOf(" ");
								// if (idx == -1) {
								// idx = auxSubTotal.length();
								// }

								// parts[i] += auxSubTotal.substring(0, idx);

								procesoRegistro = true;
							} else {
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
		if (indice <= POS_FIN_FECHA) {
			return indice;
		}
		return -1;
	}

	@Override
	protected Double darSaldoSubTotal(String registro) throws Exception {
		String reg[] = registro.split(";");
		if (!registro.contains(ERROR_TIPO_OPERACION)) {
			return String2Double(reg[POS_SUBTOTAL + 1].trim(), SEP_MILES, SEP_DEC); // Porque el registro de salida le agrogo la separacion entre debito y credio
		} else {
			return String2Double(reg[POS_SUBTOTAL].trim(), SEP_MILES, SEP_DEC); // Porque el registro de salida le agrogo la separacion entre debito y credio
		}
	}

	@Override
	protected String armarRegistro(String registro, Double saldoOperacionAnterior) throws Exception {
		String reg[] = registro.split(";");

		Double valorSubTotal = SALDO_TOTAL_NO_VALIDO;

		if (!reg[POS_SUBTOTAL].trim().equals("")) {
			// es total
			valorSubTotal = String2Double(reg[POS_SUBTOTAL].trim(), SEP_MILES, SEP_DEC);
		}

		Double valor = String2Double(reg[POS_VALOR].trim(), SEP_MILES, SEP_DEC);

		if ((saldoOperacionAnterior != SALDO_TOTAL_NO_VALIDO) && (valorSubTotal != SALDO_TOTAL_NO_VALIDO)) {
			String strDebito = "";
			String strCredito = "";
			String strSaldo = CommonUtils.double2String(valorSubTotal, SEP_MILES, SEP_DEC);

			Double nuevoTotal = saldoOperacionAnterior;

			if (saldoOperacionAnterior > valorSubTotal) {
				strDebito = CommonUtils.double2String(CommonUtils.redondear(valor, 2), SEP_MILES, SEP_DEC);
				nuevoTotal -= valor;
			} else {
				strCredito = CommonUtils.double2String(CommonUtils.redondear(valor, 2), SEP_MILES, SEP_DEC);
				nuevoTotal += valor;
			}

			// Solo valido aca, porque en el caso anterior no tengo el saldo inical
			if (Math.abs(valorSubTotal - nuevoTotal) >= 1.0) {
				throw new ExceptionSubTotal("No coincide el subtotal");
			}

			strSaldo = CommonUtils.double2String(CommonUtils.redondear(valorSubTotal, 2), SEP_MILES, SEP_DEC);

			return String.format("%s;%s;%s;%s;%s", reg[POS_FECHA].trim(), reg[POS_DESC].trim(), strDebito, strCredito, strSaldo);
		} else {
			saldoInicial = SALDO_TOTAL_NO_VALIDO;
			return registro + ";" + ERROR_TIPO_OPERACION;
		}
	}

	@Override
	protected Double darSaldoSubTotalFromErrror(String registro) {
		String reg[] = registro.split(";");

		if (!reg[POS_SUBTOTAL].trim().equals("")) {
			try {
				return String2Double(reg[POS_SUBTOTAL].trim(), SEP_MILES, SEP_DEC);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return SALDO_TOTAL_NO_VALIDO;
	}

}
