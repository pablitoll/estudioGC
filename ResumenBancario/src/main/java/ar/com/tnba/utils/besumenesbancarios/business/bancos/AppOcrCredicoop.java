package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.io.File;
import ar.com.rp.rpcutils.CommonUtils;
import ar.com.tnba.utils.besumenesbancarios.business.CommonResumenBancario;
import ar.com.tnba.utils.besumenesbancarios.business.ConstantesTool;
import ar.com.tnba.utils.besumenesbancarios.business.LogManager;
import net.sourceforge.lept4j.util.LoadLibs;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract1;

public class AppOcrCredicoop implements BancosInterface {

	private static final int POS_FIN_DES = 55;
	private static final int POS_FIN_DEBITO = 76;
	private static final int POS_FIN_CREDITO = 93;
	private static final int POS_FIN_TOTAL = 112;
	private static final int POS_FIN_FECHA = 8;
	private static final int POS_FIN_COMP = 15;
	private static final String HEADER_FECHA = "FECHA   COMBTE              DESCRIPCION                        DEBITO            CREDITO             SALDO";
	private static final String HEADER_SALTO = "SALDO ANTERIOR";
	private static final Double SALDO_TOTAL_NO_VALIDO = 9999998.11;

	@Override
	public String procesarArchivo(String strOcr, File archivo, Integer pagina) throws Exception {
		System.out.println("Procesando Crecicop: " + archivo.getName() + " Pagina " + pagina);
		String strOcrFormateado = "";
		try {
			Integer vecMin[] = new Integer[2];

			// inicio
			int idxSaldoSaldoAnt_Inicio = strOcr.lastIndexOf(HEADER_SALTO);
			int idxSaldoFecha_Inicio = strOcr.lastIndexOf(HEADER_FECHA);

			vecMin[0] = idxSaldoSaldoAnt_Inicio;
			vecMin[1] = idxSaldoFecha_Inicio;

			Integer idxInicio = CommonUtils.maximo(vecMin);

			// fin
			int idxContinuaPagina_Fin = strOcr.lastIndexOf("CONTINUA EN PAGINA");
			int idxContinuaSiguiente_Fin = strOcr.lastIndexOf("CONTINUA EN PAGINA SIGUIENTE >>>>>>");

			vecMin[0] = idxContinuaPagina_Fin == -1 ? ConstantesTool.NUMERO_ALTO : idxContinuaPagina_Fin;
			vecMin[1] = idxContinuaSiguiente_Fin == -1 ? ConstantesTool.NUMERO_ALTO : idxContinuaSiguiente_Fin;

			Integer idxFinal = CommonUtils.minimo(vecMin);

			if ((idxInicio != ConstantesTool.NUMERO_ALTO) && (idxFinal != ConstantesTool.NUMERO_ALTO) && (idxInicio < idxFinal)) {

				strOcrFormateado = strOcr.substring(idxInicio, idxFinal);

				// ALTA NEGRADA
				strOcrFormateado = strOcrFormateado.replaceAll(", ", ",");
				strOcrFormateado = strOcrFormateado.replaceAll(" ,", ",");
				strOcrFormateado = strOcrFormateado.replaceAll(" \\.", ".");
				strOcrFormateado = strOcrFormateado.replaceAll("~", "");

				String[] parts = strOcrFormateado.split("\n");
				Double saldoInicial = SALDO_TOTAL_NO_VALIDO;

				for (int i = 0; i < parts.length; i++) {
					if (parts[i].contains(HEADER_FECHA)) {
						parts[i] = "";
					} else {
						if (parts[i].contains(HEADER_SALTO)) {
							String reg[] = parts[i].split(HEADER_SALTO);
							saldoInicial = CommonResumenBancario.String2Double(reg[reg.length - 1], ConstantesTool.SEP_MILES, ConstantesTool.SEP_DEC);
							parts[i] = "";
						} else {
							if (parts[i].length() <= POS_FIN_DES) {
								if ((i > 0) && !parts[i - 1].equals("")) {
									// es la segunda parte de la descripcion del renglon anterior
									parts[i - 1] = insrtarSeparador(parts[i - 1], POS_FIN_DES + 2, parts[i]);
									parts[i] = "";
								} else {
									parts[i] = "";
								}
							} else {
								parts[i] = parts[i] + "                                                "; // no esta bien pero bueno,anda
								parts[i] = insrtarSeparador(parts[i], POS_FIN_FECHA);
								parts[i] = insrtarSeparador(parts[i], POS_FIN_COMP + 1);
								parts[i] = insrtarSeparador(parts[i], POS_FIN_DES + 2);
								parts[i] = insrtarSeparador(parts[i], POS_FIN_DEBITO + 3);
								parts[i] = insrtarSeparador(parts[i], POS_FIN_CREDITO + 4);
								parts[i] = insrtarSeparador(parts[i], POS_FIN_TOTAL + 5);
							}
						}
					}
				}
				// System.out.println(strOcrFormateado);
				StringBuilder part2 = new StringBuilder();

				// parts tiene el formato fecha;desc;numero;valor;saldo
				for (int i = 1; i < parts.length; i++) {
					if (!parts[i].trim().equals("")) {
						try {
							String registro = armarRegistro(parts[i], saldoInicial);
							part2.append(registro + "\n");
							saldoInicial = darValorSubTotal(registro);
						} catch (Exception e) {
							e.printStackTrace();
							part2.append(String.format(ConstantesTool.LEYENDA_FALLO, i + 1) + "\n");
							try {
								LogManager.getLogManager().logError(e);
							} catch (Exception e2) {
								e.printStackTrace();
							}
						}
					}
				}
				strOcrFormateado = part2.toString();
			}
			return strOcrFormateado;

		} catch (

		Exception e) {
			e.printStackTrace();
			throw (e);
		}
	}

	private String insrtarSeparador(String valor, int pos, String cadena) {
		return valor.substring(0, pos) + cadena + valor.substring(pos, valor.length());
	}

	private String insrtarSeparador(String valor, int pos) {
		return insrtarSeparador(valor, pos, ";");
	}

	private Double darValorSubTotal(String registro) throws Exception {
		String reg[] = registro.split(";");
		//Si termina en ;, es porque no esta la ultima columna (la de saldo)
		if (!registro.substring(registro.length() - 1).equals(";") && !reg[reg.length - 1].trim().equals("")) {
			return CommonResumenBancario.String2Double(reg[reg.length - 1].trim(), ConstantesTool.SEP_MILES, ConstantesTool.SEP_DEC);
		}

		return SALDO_TOTAL_NO_VALIDO;
	}

	private String armarRegistro(String registro, Double saldoInicial) throws Exception {
		String reg[] = registro.split(";");

		// Double nuevoTotal = saldoInicial;
		String debito = "";
		String credito = "";
		Double valor = 0.0;
		Double valorSubTotal = SALDO_TOTAL_NO_VALIDO;

		if (!reg[5].trim().equals("")) {
			// es total
			valorSubTotal = CommonResumenBancario.String2Double(reg[5].trim(), ConstantesTool.SEP_MILES, ConstantesTool.SEP_DEC);
		}

		if (!reg[3].trim().equals("")) {
			// es debito
			valor = CommonResumenBancario.String2Double(reg[3].trim(), ConstantesTool.SEP_MILES, ConstantesTool.SEP_DEC);
			debito = CommonUtils.double2String(valor, ConstantesTool.SEP_MILES, ConstantesTool.SEP_DEC);

			valor = valor * -1;
		}

		if (!reg[4].trim().equals("")) {
			// es credito
			valor = CommonResumenBancario.String2Double(reg[4].trim(), ConstantesTool.SEP_MILES, ConstantesTool.SEP_DEC);
			credito = CommonUtils.double2String(valor, ConstantesTool.SEP_MILES, ConstantesTool.SEP_DEC);
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
					throw new Exception("No coincide el subtotal");
				}
			}

			strSaldo = CommonUtils.double2String(CommonUtils.redondear(nuevoTotal, 2), ConstantesTool.SEP_MILES, ConstantesTool.SEP_DEC);
		}

		return String.format("%s;%s;%s;%s;%s;%s", reg[0].trim(), reg[1].trim(), reg[2].trim(), debito, credito, strSaldo);
	}

	@Override
	public String getOCR(File archivoOCR) throws Exception {
		System.out.println("Procesando OCR: " + archivoOCR.getName());
		return getInstanceICBC(archivoOCR).doOCR(archivoOCR);
	}

	private ITesseract getInstanceICBC(File archivoOCR) {
		ITesseract instanceNacion = new Tesseract1(); // JNA Direct Mapping
		instanceNacion.setTessVariable("preserve_interword_spaces", "1");
		instanceNacion.setDatapath(archivoOCR.getParent() + File.separator + "temp\\tessdata"); // path to tessdata directory
		File tessDataFolder = LoadLibs.extractNativeResources("tessdata");
		instanceNacion.setDatapath(tessDataFolder.getAbsolutePath());
		return instanceNacion;
	}
}
