package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.io.File;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.tnba.utils.besumenesbancarios.business.bancos.BancosBusiness.Bancos;

public class AppOcrCredicoop extends BaseBancos {

	public AppOcrCredicoop() {
		super(Bancos.CREDICOOP);
	}

	private static final int POS_FIN_DES = 56;
	private static final int POS_FIN_DEBITO = 76;
	private static final int POS_FIN_CREDITO = 93;
	private static final int POS_FIN_TOTAL = 112;
	private static final int POS_FIN_FECHA = 8;
	private static final int POS_FIN_COMP = 15;
	private static final String HEADER_FECHA = "FECHA   COMBTE              DESCRIPCION                        DEBITO            CREDITO             SALDO";
	private static final String HEADER_SALTO = "SALDO ANTERIOR";

	@Override
	public String[] getRegistrosFromOCR(String strOcr, File archivo, Integer pagina) throws Exception {
		String strOcrFormateado = "";

		Integer vecMax[] = new Integer[2];
		// inicio
		int idxSaldoSaldoAnt_Inicio = strOcr.lastIndexOf(HEADER_SALTO);
		int idxSaldoFecha_Inicio = strOcr.lastIndexOf(HEADER_FECHA);

		vecMax[0] = idxSaldoSaldoAnt_Inicio;
		vecMax[1] = idxSaldoFecha_Inicio;

		Integer idxInicio = CommonUtils.maximo(vecMax);

		// fin
		Integer vecMin[] = new Integer[4];
		int idxContinuaPagina_Fin = strOcr.lastIndexOf("CONTINUA EN PAGINA");
		int idxContinuaSiguiente_Fin = strOcr.lastIndexOf("CONTINUA EN PAGINA SIGUIENTE >>>>>>");
		int idxSaldoAl_Fin = strOcr.lastIndexOf("SALDO AL ");
		int idxPersibido_Fin = strOcr.indexOf("PERCIBIDO DEL ");

		vecMin[0] = idxContinuaPagina_Fin == -1 ? NUMERO_ALTO : idxContinuaPagina_Fin;
		vecMin[1] = idxContinuaSiguiente_Fin == -1 ? NUMERO_ALTO : idxContinuaSiguiente_Fin;
		vecMin[2] = idxSaldoAl_Fin == -1 ? NUMERO_ALTO : idxSaldoAl_Fin;
		vecMin[3] = idxPersibido_Fin == -1 ? NUMERO_ALTO : idxPersibido_Fin;

		Integer idxFinal = CommonUtils.minimo(vecMin);

		if ((idxInicio != -1) && (idxFinal != NUMERO_ALTO) && (idxInicio < idxFinal)) {

			strOcrFormateado = strOcr.substring(idxInicio, idxFinal);

			// ALTA NEGRADA
			strOcrFormateado = strOcrFormateado.replaceAll(", ", ",");
			strOcrFormateado = strOcrFormateado.replaceAll(" ,", ",");
			strOcrFormateado = strOcrFormateado.replaceAll(" \\.", ".");
			strOcrFormateado = strOcrFormateado.replaceAll("~", "");

			String[] parts = strOcrFormateado.split("\n");
			saldoInicial = SALDO_TOTAL_NO_VALIDO;

			for (int i = 0; i < parts.length; i++) {
				if (parts[i].contains(HEADER_FECHA)) {
					parts[i] = "";
				} else {
					if (parts[i].contains(HEADER_SALTO)) {
						String reg[] = parts[i].split(HEADER_SALTO);
						saldoInicial = String2Double(reg[reg.length - 1], SEP_MILES, SEP_DEC);
						parts[i] = "";
					} else {
						if (parts[i].length() <= POS_FIN_DES) {
							if ((i > 0) && !parts[i - 1].equals("")) {
								// es la segunda parte de la descripcion del renglon anterior
								parts[i - 1] = insertarSeparador(parts[i - 1], POS_FIN_DES + 2, parts[i]);
								parts[i] = "";
							} else {
								parts[i] = "";
							}
						} else {
							parts[i] = parts[i] + "                                                                                                "; // no esta bien pero
																																						// bueno,anda
							parts[i] = insertarSeparador(parts[i], POS_FIN_FECHA);
							parts[i] = insertarSeparador(parts[i], POS_FIN_COMP + 1);
							parts[i] = insertarSeparador(parts[i], POS_FIN_DES + 2);
							parts[i] = insertarSeparador(parts[i], POS_FIN_DEBITO + 3);
							parts[i] = insertarSeparador(parts[i], POS_FIN_CREDITO + 4);
							parts[i] = insertarSeparador(parts[i], POS_FIN_TOTAL + 5);
						}
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
