package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.io.File;
import java.util.StringJoiner;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.tnba.utils.besumenesbancarios.business.CommonResumenBancario;
import net.sourceforge.lept4j.util.LoadLibs;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract1;

public class AppOcrICBC implements BancosInterface {

	private static final String SEP_MILES_ICBC = ".";
	private static final String SEP_DEC_ICBC = ",";
	private static final int NUMERO_ALTO = 999999;

	@Override
	public String procesarArchivo(String strOcr, File archivo, Integer pagina) throws Exception {
		System.out.println("Procesando ICBC: " + archivo.getName() + " Pagina " + pagina);
		String strOcrFormateado = "";
		try {
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
			;

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
				strOcrFormateado = strOcrFormateado.replaceAll("[0123456789]{4,9}[ ]+[0123456789]{4,10}", ";;XX;;");

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

				// result1 = result1.replaceFirst(" ", ";");

				String[] parts = strOcrFormateado.split("\n");

				for (int i = 0; i < parts.length; i++) {
					parts[i] = parts[i].replaceFirst(" ", ";");
				}
				// System.out.println(strOcrFormateado);

				String[] parts2 = new String[parts.length - 1];
				Double saldoInicial = getSaldoInicial(parts[0]);

				// parts tiene el formato fecha;desc;numero;valor;saldo
				for (int i = 1; i < parts.length; i++) {
					parts2[i - 1] = armarRegistro(parts[i], saldoInicial);
					saldoInicial += darvalorOperacion(parts[i], saldoInicial);
				}
				StringJoiner sj = new StringJoiner("\n");
				for (String s : parts2) {
					sj.add(s);
				}
				strOcrFormateado = sj.toString();
			}
			return strOcrFormateado;

		} catch (Exception e) {
			e.printStackTrace();
			throw (e);
		}
	}

	private Double darvalorOperacion(String registro, Double saldoInicial) throws Exception {
		String reg[] = registro.split(";");

		Double valor = AppOcrICBC.String2Double(reg[reg.length - 1], SEP_MILES_ICBC, SEP_DEC_ICBC);
		if (isDouble(reg[reg.length - 2])) {
			valor = AppOcrICBC.String2Double(reg[reg.length - 2], SEP_MILES_ICBC, SEP_DEC_ICBC);
		}

		return valor;
	}

	private String armarRegistro(String registro, Double saldoInicial) throws Exception {
		String reg[] = registro.split(";");

		Double valor = AppOcrICBC.String2Double(reg[reg.length - 1], SEP_MILES_ICBC, SEP_DEC_ICBC);
		Double subTotal = 0.0;
		if (isDouble(reg[reg.length - 2])) {
			valor = AppOcrICBC.String2Double(reg[reg.length - 2], SEP_MILES_ICBC, SEP_DEC_ICBC);
			subTotal = AppOcrICBC.String2Double(reg[reg.length - 1], SEP_MILES_ICBC, SEP_DEC_ICBC);
		}

		String debito = CommonUtils.double2String(valor, SEP_MILES_ICBC, SEP_DEC_ICBC);
		String credito = "";

		if (valor > 0) {
			credito = debito;
			debito = "";
		}

		String strSaldo = CommonUtils.double2String(valor + saldoInicial, SEP_MILES_ICBC, SEP_DEC_ICBC);

		if (subTotal != 0) {
			// TODO ANALIZAR POSIBLE ERROR DE TOTALES
		}

		return String.format("%s;%s;%s;%s;%s;%s", reg[0], reg[1], reg[2], debito, credito, strSaldo);
	}

	private static Double String2Double(String valor, String sepMiles, String sepDec) throws Exception {
		if (valor.substring(valor.length() - 1).equals("-")) {
			valor = "-" + valor.substring(0, valor.length() - 1);
		}
		return CommonResumenBancario.String2Double(valor, sepMiles, sepDec);
	}

	private boolean isDouble(String valor) {
		valor = valor.trim();
		valor = valor.replace(".", "");
		valor = valor.replace(",", ".");

		return valor.matches("\\d*\\.\\d+[-+]?");
	}

	private Double getSaldoInicial(String registro) throws Exception {
		String reg[] = registro.split(";");
		return AppOcrICBC.String2Double(reg[reg.length - 1], SEP_MILES_ICBC, SEP_DEC_ICBC);
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
