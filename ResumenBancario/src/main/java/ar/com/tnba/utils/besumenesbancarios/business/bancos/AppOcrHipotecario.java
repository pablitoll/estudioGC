package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.tnba.utils.besumenesbancarios.business.bancos.BancosBusiness.Bancos;

public class AppOcrHipotecario extends BaseBancos {

	public AppOcrHipotecario() {
		super(Bancos.HIPOTECARIO);
	}

	private static final String REG_EXP_FECHA = "[0123456789]{1,3}/[0123456789]{1,3}/[0123456789]{3,5}";
	private static final String HEADER = "FECHA  DESCRIPCION                                          suc.";
	private static final String HEADER2 = "DETALLE DE MOVIMIENTOS";
	private static final String SALDO = "SALDO INICIAL";
	private static final String SALDO_FIN = "SALDO FINAL DEL DIA";
	private static final int POS_FIN_FECHA = 10;
	private static final int POS_FIN_DESC = 85;
	private static final int LARGO_DEBITO = 131;
	private static final int LARGO_TOTAL = 170;
	private static final int IDX_DEBITO = 2;
	private static final int IDX_CREDITO = 3;
	private static final int IDX_SALDO = 4;
	private static final int IDX_FECHA = 0;
	private static final int IDX_DESC = 1;
	private static final String SEP_MILES_HIPOTECARIO = ",";
	private static final String SEP_DEC_HIPOTECARIO = ".";
	private static final String SALDO_FINAL = "SALDO FINAL AL DIA";
	private Pattern pattern = Pattern.compile(REG_EXP_FECHA);

	@Override
	public String[] getRegistrosFromOCR(String strOcr, File archivo, Integer pagina) throws Exception {
		String strOcrFormateado = "";
		Integer vec[] = new Integer[2];
		// inicio
		vec[0] = strOcr.indexOf(HEADER);
		vec[1] = strOcr.indexOf(HEADER2);

		Integer idxInicio = CommonUtils.maximo(vec);
		// fin
		Integer idxFinal = strOcr.length();

		if (idxInicio != -1) {

			strOcrFormateado = strOcr.substring(idxInicio, idxFinal);

			// ALTA NEGRADA
			strOcrFormateado = strOcrFormateado.replaceAll(", ", ",");
			strOcrFormateado = strOcrFormateado.replaceAll(" ,", ",");
			strOcrFormateado = strOcrFormateado.replaceAll(" \\.", ".");
			strOcrFormateado = strOcrFormateado.replaceAll("~", "");

			String[] parts = strOcrFormateado.split("\n");

			boolean procesoRegistro = false;
			Double saldo = 0.0;

			for (int i = 0; i < parts.length; i++) {
				if (parts[i].contains(HEADER) || (parts[i].length() < POS_FIN_FECHA)) {
					parts[i] = "";
				} else {
					if (parts[i].contains(SALDO) || parts[i].contains(SALDO_FIN) || parts[i].contains(SALDO_FINAL)) {
						saldo = getSaldo(parts[i]);
						parts[i] = "";
						if (!procesoRegistro) {
							saldoInicial = saldo;
						}
					} else {
						// Le voy calculando el sub total a medida que voy avanzando
						int indFecha = indexFecha(parts[i]);
						if ((indFecha > -1) && !parts[i].contains(SALDO)) {
							parts[i] = insertarSeparadorConTrim(parts[i], indFecha);
							// Valor
							// Determino si es debto o credito por el largo total de la cadena
							if (parts[i].length() <= LARGO_DEBITO) {
								// es debito
								parts[i] = insertarSeparadorConTrim(parts[i], POS_FIN_DESC, ";") + ";;";
								Double deb = getDebito(parts[i]);
								saldo -= deb;
								parts[i] += CommonUtils.double2String(saldo, SEP_MILES_HIPOTECARIO, SEP_DEC_HIPOTECARIO);
							} else {
								if (parts[i].length() <= LARGO_TOTAL) {
									// es credito
									parts[i] = insertarSeparadorConTrim(parts[i], POS_FIN_DESC, ";;") + ";";
									Double cred = getCredito(parts[i]);
									saldo += cred;
									parts[i] += CommonUtils.double2String(saldo, SEP_MILES_HIPOTECARIO, SEP_DEC_HIPOTECARIO);
									// } else {
									// // es total;
									// parts[i] = insertarSeparadorConTrim(parts[i], POS_FIN_DESC, ";;;");
								}
							}

							procesoRegistro = true;
						} else {
							parts[i] = "";
						}
					}
				}
			}
			return parts;
		}

		return null;
	}

	private Double getDebito(String registro) throws Exception {
		String reg[] = registro.split(";");
		return String2Double(reg[IDX_DEBITO], SEP_MILES_HIPOTECARIO, SEP_DEC_HIPOTECARIO);
	}

	private Double getCredito(String registro) throws Exception {
		String reg[] = registro.split(";");
		return String2Double(reg[IDX_CREDITO], SEP_MILES_HIPOTECARIO, SEP_DEC_HIPOTECARIO);
	}

	private Double getSaldo(String registro) throws Exception {
		try {
			registro = insertarSeparador(registro, POS_FIN_DESC);
			String reg[] = registro.split(";");
			return String2Double(reg[reg.length - 1], SEP_MILES_HIPOTECARIO, SEP_DEC_HIPOTECARIO);
		} catch (Exception e) {
			e.printStackTrace();
			return SALDO_TOTAL_NO_VALIDO;
		}
	}

	private int indexFecha(String cadena) {
		Matcher matcher = pattern.matcher(cadena);
		int indice = -1;
		if (matcher.find()) {
			indice = matcher.end();
		}
		if (indice <= POS_FIN_FECHA) {
			return indice;
		}
		return -1;
	}

	@Override
	protected String armarRegistro(String registro, Double saldoInicial) throws Exception {
		String reg[] = registro.split(";");
		String strSaldo = "";
		String strDebito = "";
		String strCredito = "";

		Double debito = 0.0;
		Double credito = 0.0;
		Double subTotal = String2Double(reg[IDX_SALDO], SEP_MILES_HIPOTECARIO, SEP_DEC_HIPOTECARIO);
		strSaldo = CommonUtils.double2String(subTotal, SEP_MILES_HIPOTECARIO, SEP_DEC_HIPOTECARIO);

		if (!reg[IDX_DEBITO].equals("")) {
			debito = String2Double(reg[IDX_DEBITO], SEP_MILES_HIPOTECARIO, SEP_DEC_HIPOTECARIO);
			strDebito = CommonUtils.double2String(debito, SEP_MILES_HIPOTECARIO, SEP_DEC_HIPOTECARIO);
		} else {
			if (!reg[IDX_CREDITO].equals("")) {
				credito = String2Double(reg[IDX_CREDITO], SEP_MILES_HIPOTECARIO, SEP_DEC_HIPOTECARIO);
				strCredito = CommonUtils.double2String(credito, SEP_MILES_HIPOTECARIO, SEP_DEC_HIPOTECARIO);
			}
		}

		if (subTotal != 0) {
			if (Math.abs(subTotal - (-debito + credito + saldoInicial)) >= 1.0) {
				throw new ExceptionSubTotal();
			}
		}

		return String.format("%s;%s;%s;%s;%s", reg[IDX_FECHA].trim(), reg[IDX_DESC].trim(), strDebito, strCredito, strSaldo);
	}

	protected Double darSaldoSubTotal(String registro) throws Exception {
		String reg[] = registro.split(";");
		return String2Double(reg[IDX_SALDO], SEP_MILES_HIPOTECARIO, SEP_DEC_HIPOTECARIO);
	}

}
