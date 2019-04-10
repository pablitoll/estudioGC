package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.tnba.utils.besumenesbancarios.business.bancos.BancosBusiness.Bancos;

public class AppOcrICBC extends BaseBancos {

	public AppOcrICBC() {
		super(Bancos.ICBC);
	}

	private static final String REG_EXP_VISA = "[0123456789]{4,9}[ ]+[0123456789]{4,10}";
	private Pattern pattern = Pattern.compile(REG_EXP_VISA);

	@Override
	public String[] getRegistrosFromOCR(String strOcr, File archivo, Integer pagina) throws Exception {
		String strOcrFormateado = "";
		Integer vecMin[] = new Integer[3];

		// inicio
		int idxSaldoUltimo_Inicio = strOcr.lastIndexOf("SALDO ULTIMO");
		int idxSaldoPagina_Inicio = strOcr.lastIndexOf("SALDO PAGINA ANTERIOR");
		int idxSaldoHoja_Inicio = strOcr.lastIndexOf("SALDO HOJA ANTERIOR");

		vecMin[0] = idxSaldoUltimo_Inicio == -1 ? NUMERO_ALTO : idxSaldoUltimo_Inicio;
		vecMin[1] = idxSaldoHoja_Inicio == -1 ? NUMERO_ALTO : idxSaldoHoja_Inicio;
		vecMin[2] = idxSaldoPagina_Inicio == -1 ? NUMERO_ALTO : idxSaldoPagina_Inicio;

		Integer idxInicio = CommonUtils.minimo(vecMin);

		// fin
		int idxContinuaDorso_Fin = strOcr.lastIndexOf("CONTINUA AL DORSO");
		int idxTotal_Fin = strOcr.lastIndexOf("TOTAL IMP.LEY");
		int idxContinuaHoja_Fin = strOcr.lastIndexOf("CONTINUA EN LA HOJA SIGUIENTE");

		vecMin[0] = idxContinuaDorso_Fin == -1 ? NUMERO_ALTO : idxContinuaDorso_Fin;
		vecMin[1] = idxTotal_Fin == -1 ? NUMERO_ALTO : idxTotal_Fin;
		vecMin[2] = idxContinuaHoja_Fin == -1 ? NUMERO_ALTO : idxContinuaHoja_Fin;

		Integer idxFinal = CommonUtils.minimo(vecMin);

		if ((idxInicio != NUMERO_ALTO) && (idxFinal != NUMERO_ALTO) && (idxInicio < idxFinal)) {

			strOcrFormateado = strOcr.substring(idxInicio, idxFinal);

			// ALTA NEGRADA
			strOcrFormateado = strOcrFormateado.replaceAll(", ", ",");
			strOcrFormateado = strOcrFormateado.replaceAll(" ,", ",");
			strOcrFormateado = strOcrFormateado.replaceAll(" \\.", ".");
			strOcrFormateado = strOcrFormateado.replaceAll("~", "");

			// CASO
			// 03-01 VISA 022019347 0022402739 0221 9.200,43
			// 999999999+ n espacios + 9999999999
			Matcher matcher = pattern.matcher(strOcrFormateado);

			while (matcher.find()) {
				String subVisa = strOcrFormateado.substring(matcher.start(), matcher.end());

				String nuevoTexto = subVisa.replaceFirst(" ", ";");

				strOcrFormateado = strOcrFormateado.replace(subVisa, nuevoTexto);
			}

			strOcrFormateado = strOcrFormateado.replace("                    ", ";");
			strOcrFormateado = strOcrFormateado.replace("                   ", ";");
			strOcrFormateado = strOcrFormateado.replace("                  ", ";");
			strOcrFormateado = strOcrFormateado.replace("                 ", ";");
			strOcrFormateado = strOcrFormateado.replace("                ", ";");
			strOcrFormateado = strOcrFormateado.replace("               ", ";");
			strOcrFormateado = strOcrFormateado.replace("              ", ";");
			strOcrFormateado = strOcrFormateado.replace("             ", ";");
			strOcrFormateado = strOcrFormateado.replace("            ", ";");
			strOcrFormateado = strOcrFormateado.replace("           ", ";");
			strOcrFormateado = strOcrFormateado.replace("          ", ";");
			strOcrFormateado = strOcrFormateado.replace("         ", ";");
			strOcrFormateado = strOcrFormateado.replace("        ", ";");
			strOcrFormateado = strOcrFormateado.replace("       ", ";");
			strOcrFormateado = strOcrFormateado.replace("      ", ";");
			strOcrFormateado = strOcrFormateado.replace("     ", ";");
			strOcrFormateado = strOcrFormateado.replace("    ", ";");
			strOcrFormateado = strOcrFormateado.replace("   ", ";");
			strOcrFormateado = strOcrFormateado.replace("  ", ";");

			strOcrFormateado = strOcrFormateado.replace(";;", ";");
			strOcrFormateado = strOcrFormateado.replace(";;", ";");
			strOcrFormateado = strOcrFormateado.replace(";;", ";");

			String[] parts = strOcrFormateado.split("\n");

			// Saldo inicial
			String regZero[] = parts[0].split(";");
			saldoInicial = String2Double(regZero[regZero.length - 1], SEP_MILES, SEP_DEC);
			parts[0] = "";

			for (int i = 1; i < parts.length; i++) {
				parts[i] = parts[i].replaceFirst(" ", ";");
			}

			return parts;
		}

		return null;
	}

	@Override
	protected String armarRegistro(String registro, Double saldoInicial) throws Exception {
		String reg[] = registro.split(";");
		String strSaldo = "";

		Double valor = String2Double(reg[reg.length - 1], SEP_MILES, SEP_DEC);
		Double subTotal = 0.0;
		if (isDouble(reg[reg.length - 2])) {
			valor = String2Double(reg[reg.length - 2], SEP_MILES, SEP_DEC);
			subTotal = String2Double(reg[reg.length - 1], SEP_MILES, SEP_DEC);

			if (saldoInicial == SALDO_TOTAL_NO_VALIDO) {
				saldoInicial = subTotal - valor;
			}
		}

		String debito = CommonUtils.double2String(valor, SEP_MILES, SEP_DEC);
		String credito = "";

		if (valor > 0) {
			credito = debito;
			debito = "";
		}

		if (saldoInicial != SALDO_TOTAL_NO_VALIDO) {

			if (subTotal != 0) {
				if (Math.abs(subTotal - (valor + saldoInicial)) >= 1.0) {
					throw new ExceptionSubTotal();
				}
			}

			strSaldo = CommonUtils.double2String(valor + saldoInicial, SEP_MILES, SEP_DEC);
		}
		return String.format("%s;%s;%s;%s;%s;%s", reg[0], reg[1], reg[2], debito, credito, strSaldo);
	}

	protected Double darSaldoSubTotal(String registro) throws Exception {
		String reg[] = registro.split(";");

		if ((reg.length == 6) &&  !reg[reg.length - 1].trim().equals("")) {
			return String2Double(reg[reg.length - 1], SEP_MILES, SEP_DEC);
		}

		return SALDO_TOTAL_NO_VALIDO;
	}

	private boolean isDouble(String valor) {
		valor = valor.trim();
		valor = valor.replace(".", "");
		valor = valor.replace(",", ".");

		return valor.matches("\\d*\\.\\d+[-+]?");
	}

}
