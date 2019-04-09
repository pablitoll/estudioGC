package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.io.File;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.tnba.utils.besumenesbancarios.business.bancos.BancosBusiness.Bancos;

public class AppOcrBNA extends BaseBancos {

	public AppOcrBNA() {
		super(Bancos.NACION);
	}

	@Override
	public String[] getRegistrosFromOCR(String strOcr, File archivo, Integer pagina) throws Exception {
		String strOcrFormateado = "";
		int idxSaldoAnterior = strOcr.lastIndexOf("SALDO ANTERIOR");
		int idxSaldoFinal = strOcr.lastIndexOf("SALDO FINAL");
		int idxTransporte = strOcr.lastIndexOf("TRANSPORTE");

		int idxInicio = idxSaldoAnterior;
		int idxFinal = idxSaldoFinal;

		if ((idxSaldoAnterior == -1) && (idxTransporte > -1)) {
			idxInicio = idxTransporte;
		}

		if ((idxFinal == -1) && (idxTransporte > -1)) {
			idxFinal = idxTransporte;
		}

		if ((idxInicio > -1) && (idxFinal > -1)) {

			strOcrFormateado = strOcr.substring(idxInicio, idxFinal);

			// ALTA NEGRADA
			strOcrFormateado = strOcrFormateado.replace("       -Suc", " -Suc");
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
			for (int i = 0; i < parts.length; i++) {
				parts[i] = parts[i].replaceFirst(" ", ";");
			}

			String reg[] = parts[0].split(";");
			saldoInicial = String2Double(reg[reg.length - 1], SEP_MILES, SEP_DEC);
			parts[0] = "";

			return parts;
		}
		return null;

	}

	// saldoInicial += darvalorOperacion(parts[i], saldoInicial);
	@Override
	protected Double darSaldoSubTotal(String registro) throws Exception {
		String reg[] = registro.split(";");

		return String2Double(reg[5], SEP_MILES, SEP_DEC);
	}

	@Override
	protected String armarRegistro(String registro, Double saldoInicial) throws Exception {
		String reg[] = registro.split(";");
		Double saldo = String2Double(reg[4], SEP_MILES, SEP_DEC);
		String strSaldo = CommonUtils.double2String(saldo, SEP_MILES, SEP_DEC);

		Double valorReg = String2Double(reg[3], SEP_MILES, SEP_DEC);
		String strvalorReg = CommonUtils.double2String(valorReg, SEP_MILES, SEP_DEC);

		String debito = strvalorReg;
		String credito = "";

		if (isCredito(registro, saldoInicial)) {
			debito = "";
			credito = strvalorReg;
		}

		return String.format("%s;%s;%s;%s;%s;%s", reg[0], reg[1], reg[2], debito, credito, strSaldo);
	}

	private boolean isCredito(String registro, Double saldoInicial) throws Exception {
		String reg[] = registro.split(";");

		Double saldo = String2Double(reg[4], SEP_MILES, SEP_DEC);

		return (saldo > saldoInicial);
	}
}
