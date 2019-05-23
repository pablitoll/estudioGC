package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.tnba.utils.besumenesbancarios.business.bancos.BancosBusiness.Bancos;

public class AppOcrGalicia extends BaseBancos {

	public AppOcrGalicia() {
		super(Bancos.GALICIA);
	}

	// private static final int POS_FIN_DES = 91;
	private static final int POS_FIN_FECHA = 10;
	private static final String FOOTER = "TOTAL RETENCION";
	private static final String SALDO_INICIAL = "SALDO INICIAL";
	private static final String SALDO_ANTERIOR = "SALDO ANTERIOR";
	private static final String REG_EXP_HEADER = "Fecha[ ]*Des";
	private static final String REG_EXP_COMIENZO_VALIDO = "([a-zA-Z]|\\d)";
	private static final String REG_EXP_FIN_DESC = "(\\d+[.]+)*(\\d+[,|.]\\d+)\\s{8,}(-?(\\d+[.]+)*(\\d+[,|.]\\d+))";
	private Pattern pattern = Pattern.compile(REG_EXP_HEADER);
	private Pattern patternComienzoValido = Pattern.compile(REG_EXP_COMIENZO_VALIDO);
	private Pattern patternFinDescrip = Pattern.compile(REG_EXP_FIN_DESC);

	@Override
	public String[] getRegistrosFromOCR(String strOcr, File archivo, Integer pagina) throws Exception {

		
		String strOcrFormateado = "";

		// inicio
		int idxInicio = indexFecha(strOcr);

		// fin
		int idxFinal = strOcr.indexOf(FOOTER);
		if (idxFinal == -1) {
			idxFinal = strOcr.length(); // Toda la pagina
		}

		if (idxInicio != -1) {

			strOcrFormateado = strOcr.substring(idxInicio, idxFinal);
			
			// ALTA NEGRADA
			strOcrFormateado = strOcrFormateado.replaceAll(", ", ",");
			strOcrFormateado = strOcrFormateado.replaceAll(" ,", ",");
			strOcrFormateado = strOcrFormateado.replaceAll(" \\.", ".");
			strOcrFormateado = strOcrFormateado.replaceAll("~", "");

			String[] parts = strOcrFormateado.split("\n");
			saldoInicial = SALDO_TOTAL_NO_VALIDO;
			Double subTotalAnt = SALDO_TOTAL_NO_VALIDO;
			for (int i = 0; i < parts.length; i++) {
				parts[i] = sacarCaracteresInvalido(parts[i]).trim();

				if (parts[i].contains("Fecha") && (indexFecha(parts[i]) != -1)) { // Primero pregunto por fecha para que sea mas rapido
					parts[i] = "";
				} else {
					if (parts[i].contains(SALDO_INICIAL) || parts[i].contains(SALDO_ANTERIOR)) {

						String cadenaCorte = parts[i].contains(SALDO_INICIAL) ? SALDO_INICIAL : SALDO_ANTERIOR;
						String reg[] = parts[i].split(cadenaCorte);
						saldoInicial = String2Double(reg[reg.length - 1], SEP_MILES, SEP_DEC);
						subTotalAnt = saldoInicial;
						parts[i] = "";

					} else {

						int posFinDescrip = darPosFinDescrip(parts[i]);

						if (posFinDescrip == -1) {
							if ((i > 0) && !parts[i - 1].trim().equals("") && !parts[i].trim().equals("")) {
								// es la segunda parte de la descripcion del renglon anterior
								String regAnt[] = parts[i - 1].split(";");
								regAnt[1] += " " + parts[i].trim();
								parts[i - 1] = regToString(regAnt);
								parts[i] = "";
							} else {
								parts[i] = "";
							}
						} else {
							String fecha = parts[i].substring(0, POS_FIN_FECHA).trim();
							String desc = parts[i].substring(POS_FIN_FECHA, posFinDescrip).trim();

							String primerValor = darPrimerValor(parts[i].substring(posFinDescrip));
							String subTotal = darSegundoValor(parts[i].substring(posFinDescrip));
							Double dSubTotal = String2Double(subTotal, SEP_MILES, SEP_DEC);

							// Credito
							String credito = primerValor;
							String debito = "";

							if (subTotalAnt > dSubTotal) { // Debito
								debito = primerValor;
								credito = "";
							}

							subTotalAnt = dSubTotal;
							
							parts[i] = String.format("%s;%s;%s;%s;%s", fecha, desc, debito, credito, subTotal);
						}
					}
				}
			}
			return parts;
		}

		return null;
	}

	private int darPosFinDescrip(String registro) {
		Matcher matcher = patternFinDescrip.matcher(registro);
		int retorno = -1;
		while (matcher.find()) {
			retorno = matcher.start(); // Me quedo con el ultima matcher que es el de numeros debito/credito y subtotal
		}
		return retorno;
	}

	private String sacarCaracteresInvalido(String registro) {
		Matcher matcher = patternComienzoValido.matcher(registro);
		if (matcher.find()) {
			return registro.substring(matcher.start());
		}
		return registro;
	}

	private int indexFecha(String texto) {
		Matcher matcher = pattern.matcher(texto);
		if (matcher.find()) {
			return matcher.start();
		}
		return -1;
	}

	private String regToString(String[] registro) {
		String aux = "";
		for (String r : registro) {
			aux += r + ";";
		}
		return aux;
	}

	private String darSegundoValor(String registro) {
		registro = registro.trim();
		String reg[] = registro.split(" ");
		return reg[reg.length - 1];
	}

	private String darPrimerValor(String registro) {
		registro = registro.trim();
		String reg[] = registro.split(" ");
		return reg[0];
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

		if (!reg[4].trim().equals("")) {
			// es total
			valorSubTotal = String2Double(reg[4].trim(), SEP_MILES, SEP_DEC);
		}

		if (!reg[2].trim().equals("")) {
			// es debito
			valor = String2Double(reg[2].trim(), SEP_MILES, SEP_DEC);
			debito = CommonUtils.double2String(valor, SEP_MILES, SEP_DEC);

			valor = valor * -1;
		}

		if (!reg[3].trim().equals("")) {
			// es credito
			valor = String2Double(reg[3].trim(), SEP_MILES, SEP_DEC);
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

		return String.format("%s;%s;%s;%s;%s", reg[0].trim(), reg[1].trim(), debito, credito, strSaldo);
	}

}
