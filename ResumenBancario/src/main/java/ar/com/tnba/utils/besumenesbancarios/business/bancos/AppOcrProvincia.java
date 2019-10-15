package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.io.File;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.tnba.utils.besumenesbancarios.business.bancos.BancosBusiness.Bancos;

public class AppOcrProvincia extends BaseBancos {

	private static final int FIN_DESCRIP = 80;
	private static final int FIN_FECHA = 10;

	public AppOcrProvincia() {
		super(Bancos.PROVINCIA);
	}

	private static final String SALDO_ANTERIOR = "SALDO ANTERIOR";

	@Override
	protected String[] getRegistrosFromOCR(String strOcr, File archivo, Integer nroPapagina) throws Exception {

		String strOcrFormateado = "";
		Integer vecMin[] = new Integer[2];

		vecMin[0] = strOcr.indexOf("Cantidad de titulares");
		vecMin[1] = strOcr.indexOf(SALDO_ANTERIOR);
		Integer idxIni = CommonUtils.maximo(vecMin);

		int idxGarantia = strOcr.indexOf("Condiciones de Garantia");
		Integer idxFin = idxGarantia > 0 ? idxGarantia : strOcr.length();

		strOcrFormateado = strOcr.substring(idxIni, idxFin).replaceAll("-g", "-8");

		String[] parts = strOcrFormateado.split("\n");

		// String auxSaldoInicial = parts[0].replace(TRANSPORTE_SALDO,
		// "").replaceAll(SALDO_ANTERIOR, "");
		// saldoInicial = String2Double(auxSaldoInicial, SEP_MILES, SEP_DEC);
		// parts[0] = "";

		// retiro las lineas SIN_MOVIMIENTOS
		for (int i = 0; i < parts.length; i++) {
			if (parts[i].contains(SALDO_ANTERIOR)) {
				String aux[] = parts[i].split(SALDO_ANTERIOR);
				saldoInicial = String2Double(aux[1], SEP_MILES, SEP_DEC);
				parts[i] = "";
			} else {
				if ((parts[i].length() < 2) || !CommonUtils.isNumeric(parts[i].substring(0, 2))) {
					parts[i] = "";
				}
			}
		}

		return parts;
	}

	@Override
	protected String armarRegistro(String registro, Double saldoInicial) throws Exception {
		String fecha = "";
		String descrip = "";
		Double monto = 0.0;
		String fecha2 = "";
		Double saldoRenglon = 0.0;

		String aux = registro.replaceAll("\\|", " ").replaceAll(", ", ",");
		aux = insertarSeparador(aux, FIN_FECHA, "@");
		aux = insertarSeparador(aux, FIN_DESCRIP, "@");
		String splitAux[] = aux.split("@");
		if (splitAux.length == 3) {
			fecha = splitAux[0].trim();
			descrip = splitAux[1].trim();

			String auxMonto = splitAux[2].trim();

			// monto
			int idx = auxMonto.indexOf(" ");
			monto = String2Double(auxMonto.substring(0, idx), SEP_MILES, SEP_DEC);

			// fecha
			auxMonto = auxMonto.substring(idx).trim();
			idx = auxMonto.indexOf(" ");
			fecha2 = auxMonto.substring(0, idx).trim();

			// saldo
			saldoRenglon = String2Double(auxMonto.substring(idx), SEP_MILES, SEP_DEC);
		}

		if (!fecha.equals("")) {
			Double debito = 0.0;
			Double credito = 0.0;

			if (monto >= 0) {
				credito = monto;
			} else {
				debito = -monto;
			}

			return String.format("%s;%s;%s;%s;%s;%s", fecha, descrip, fecha2, CommonUtils.double2String(debito, SEP_MILES, SEP_DEC),
					CommonUtils.double2String(credito, SEP_MILES, SEP_DEC), CommonUtils.double2String(saldoRenglon, SEP_MILES, SEP_DEC));
		} else {
			return registro;
		}
	}

	@Override
	protected Double darSaldoSubTotal(String registro) throws Exception {
		String reg[] = registro.split(";");
		return String2Double(reg[reg.length - 1].trim(), SEP_MILES, SEP_DEC);
	}
}
