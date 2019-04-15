package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.tnba.utils.besumenesbancarios.business.bancos.BancosBusiness.Bancos;

public class AppOcrICBC extends BaseBancos {

	private static final int IDX_TOTAL_REGISTRO = 4;
	private static final int IDX_DESC = 1;
	private static final int IDX_FECHA = 0;
	private static final int IDX_SUBTOTAL = 3;
	private static final String SALDO_HOJA_ANTERIOR = "SALDO HOJA ANTERIOR";
	private static final String SALDO_PAGINA_ANTERIOR = "SALDO PAGINA ANTERIOR";
	private static final String SALDO_ULTIMO = "SALDO ULTIMO";

	private static final int IDX_VALOR = 2;

	private static final String REG_EXP_FECHA = "[0123456789]{1,3}[-]+[0123456789]{1,3}";
	private Pattern patternFecha = Pattern.compile(REG_EXP_FECHA);
	private ValorICBC v1 = new ValorICBC();
	private ValorICBC v2 = new ValorICBC();

	public AppOcrICBC() {
		super(Bancos.ICBC);
	}

	@Override
	public String[] getRegistrosFromOCR(String strOcr, File archivo, Integer pagina) throws Exception {
		String strOcrFormateado = "";
		Integer vecMin[] = new Integer[3];

		// inicio
		int idxSaldoUltimo_Inicio = strOcr.lastIndexOf(SALDO_ULTIMO);
		int idxSaldoPagina_Inicio = strOcr.lastIndexOf(SALDO_PAGINA_ANTERIOR);
		int idxSaldoHoja_Inicio = strOcr.lastIndexOf(SALDO_HOJA_ANTERIOR);

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
			strOcrFormateado = strOcrFormateado.replaceAll("~", "-");

			String[] parts = strOcrFormateado.split("\n");

			for (int i = 0; i < parts.length; i++) {
				parts[i] = parts[i].trim();
				if (parts[i].contains(SALDO_ULTIMO) || parts[i].contains(SALDO_PAGINA_ANTERIOR) || parts[i].contains(SALDO_HOJA_ANTERIOR)) {
					int idc = parts[i].lastIndexOf(" ");

					parts[i] = insertarSeparadorConTrim(parts[i], idc);
					String reg[] = parts[i].split(";");

					saldoInicial = String2Double(reg[reg.length - 1], SEP_MILES, SEP_DEC);

					parts[i] = "";
				} else {

					int idxFinFecha = getIndiceFecha(parts[i]);
					if (idxFinFecha != -1) {
						// fecha
						parts[i] = insertarSeparadorConTrim(parts[i], idxFinFecha);

						v1.inicializar(parts[i], -1);

						v2.inicializar(parts[i], v1.getPosFin());

						if (v2.isValido()) { // Hay un segundo valor, el anterior valor
							parts[i] = parts[i].substring(0, v1.getPosIni()).trim() + ";" + v1.getValor() + ";" + v2.getValor();
						} else {
							parts[i] = parts[i].substring(0, v1.getPosIni()).trim() + ";" + v1.getValor() + ";";
						}
					} else {
						parts[i] = "";
					}
				}
			}
			return parts;
		}

		return null;
	}

	private int getIndiceFecha(String registro) {
		Matcher matcher = patternFecha.matcher(registro);
		if (matcher.find()) {
			return matcher.end();
		}

		return -1;
	}

	@Override
	protected String armarRegistro(String registro, Double saldoInicial) throws Exception {
		String reg[] = registro.split(";");
		String strSaldo = "";

		Double valor = String2Double(reg[IDX_VALOR], SEP_MILES, SEP_DEC);

		Double subTotal = SALDO_TOTAL_NO_VALIDO;
		if ((reg.length == IDX_SUBTOTAL + 1) && !reg[IDX_SUBTOTAL].equals("")) {
			subTotal = String2Double(reg[IDX_SUBTOTAL], SEP_MILES, SEP_DEC);
			strSaldo = CommonUtils.double2String(subTotal, SEP_MILES, SEP_DEC);
		}

		String debito = CommonUtils.double2String(valor, SEP_MILES, SEP_DEC);
		String credito = "";

		if (valor > 0) {
			credito = debito;
			debito = "";
		}

		if (saldoInicial != SALDO_TOTAL_NO_VALIDO) {

			if (subTotal != SALDO_TOTAL_NO_VALIDO) {
				if (Math.abs(subTotal - (valor + saldoInicial)) >= 1.0) {
					throw new ExceptionSubTotal();
				}
			}

			strSaldo = CommonUtils.double2String(valor + saldoInicial, SEP_MILES, SEP_DEC);
		}
		return String.format("%s;%s;%s;%s;%s", reg[IDX_FECHA].trim(), trimInterno(reg[IDX_DESC]), debito, credito, strSaldo);
	}

	protected Double darSaldoSubTotal(String registro) throws Exception {
		String reg[] = registro.split(";");

		if ((reg.length == IDX_TOTAL_REGISTRO + 1)) { // no hay agregado msg de error
			return String2Double(reg[IDX_TOTAL_REGISTRO], SEP_MILES, SEP_DEC);
		}

		return SALDO_TOTAL_NO_VALIDO;
	}

	@Override
	protected Double darSaldoSubTotalFromErrror(String registro) {
		String reg[] = registro.split(";");

		if ((reg.length == IDX_SUBTOTAL + 1)) {
			try {
				return String2Double(reg[IDX_SUBTOTAL], SEP_MILES, SEP_DEC);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return SALDO_TOTAL_NO_VALIDO;
	}
}
