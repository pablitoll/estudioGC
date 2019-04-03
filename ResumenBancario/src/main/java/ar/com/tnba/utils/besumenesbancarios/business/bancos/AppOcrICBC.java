package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.io.File;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.tnba.utils.besumenesbancarios.business.CommonResumenBancario;
import ar.com.tnba.utils.besumenesbancarios.business.ConstantesTool;
import ar.com.tnba.utils.besumenesbancarios.business.LogManager;
import net.sourceforge.lept4j.util.LoadLibs;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract1;

public class AppOcrICBC implements BancosInterface {

	private static final String REG_EXP_VISA = "[0123456789]{4,9}[ ]+[0123456789]{4,10}";
	private Pattern pattern = Pattern.compile(REG_EXP_VISA);
	
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

			vecMin[0] = idxSaldoUltimo_Inicio == -1 ? ConstantesTool.NUMERO_ALTO : idxSaldoUltimo_Inicio;
			vecMin[1] = idxSaldoHoja_Inicio == -1 ? ConstantesTool.NUMERO_ALTO : idxSaldoHoja_Inicio;
			vecMin[2] = idxSaldoPagina_Inicio == -1 ? ConstantesTool.NUMERO_ALTO : idxSaldoPagina_Inicio;

			Integer idxInicio = CommonUtils.minimo(vecMin);

			// fin
			int idxContinuaDorso_Fin = strOcr.lastIndexOf("CONTINUA AL DORSO");
			int idxTotal_Fin = strOcr.lastIndexOf("TOTAL IMP.LEY");
			int idxContinuaHoja_Fin = strOcr.lastIndexOf("CONTINUA EN LA HOJA SIGUIENTE");

			vecMin[0] = idxContinuaDorso_Fin == -1 ? ConstantesTool.NUMERO_ALTO : idxContinuaDorso_Fin;
			vecMin[1] = idxTotal_Fin == -1 ? ConstantesTool.NUMERO_ALTO : idxTotal_Fin;
			vecMin[2] = idxContinuaHoja_Fin == -1 ? ConstantesTool.NUMERO_ALTO : idxContinuaHoja_Fin;

			Integer idxFinal = CommonUtils.minimo(vecMin);

			if ((idxInicio != ConstantesTool.NUMERO_ALTO) && (idxFinal != ConstantesTool.NUMERO_ALTO) && (idxInicio < idxFinal)) {

				strOcrFormateado = strOcr.substring(idxInicio, idxFinal);

				// ALTA NEGRADA
				strOcrFormateado = strOcrFormateado.replaceAll(", ", ",");
				strOcrFormateado = strOcrFormateado.replaceAll(" ,", ",");
				strOcrFormateado = strOcrFormateado.replaceAll(" \\.", ".");
				strOcrFormateado = strOcrFormateado.replaceAll("~", "");

				// CASO
				// 03-01 VISA 022019347 0022402739 0221 9.200,43
				// 999999999+ n espacios + 9999999999
			//	strOcrFormateado = strOcrFormateado.replaceAll(REG_EXP_VISA, ";;XX;;");
				Matcher matcher = pattern.matcher(strOcrFormateado);
				
				while(matcher.find()) {
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

				for (int i = 0; i < parts.length; i++) {
					parts[i] = parts[i].replaceFirst(" ", ";");
				}
				// System.out.println(strOcrFormateado);

				String[] parts2 = new String[parts.length - 1];
				Double saldoInicial = getSaldoInicial(parts[0]);

				// parts tiene el formato fecha;desc;numero;valor;saldo
				for (int i = 1; i < parts.length; i++) {
					try {
						parts2[i - 1] = armarRegistro(parts[i], saldoInicial);
						saldoInicial += darvalorOperacion(parts[i], saldoInicial);
					} catch (Exception e) {
						e.printStackTrace();
						parts2[i - 1] = String.format(ConstantesTool.LEYENDA_FALLO, i + 1);
						try {
							LogManager.getLogManager().logError(e);
						} catch (Exception e2) {
							e.printStackTrace();
						}
					}
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

		Double valor = AppOcrICBC.String2Double(reg[reg.length - 1], ConstantesTool.SEP_MILES, ConstantesTool.SEP_DEC);
		if (isDouble(reg[reg.length - 2])) {
			valor = AppOcrICBC.String2Double(reg[reg.length - 2], ConstantesTool.SEP_MILES, ConstantesTool.SEP_DEC);
		}

		return valor;
	}

	private String armarRegistro(String registro, Double saldoInicial) throws Exception {
		String reg[] = registro.split(";");

		Double valor = AppOcrICBC.String2Double(reg[reg.length - 1], ConstantesTool.SEP_MILES, ConstantesTool.SEP_DEC);
		Double subTotal = 0.0;
		if (isDouble(reg[reg.length - 2])) {
			valor = AppOcrICBC.String2Double(reg[reg.length - 2], ConstantesTool.SEP_MILES, ConstantesTool.SEP_DEC);
			subTotal = AppOcrICBC.String2Double(reg[reg.length - 1], ConstantesTool.SEP_MILES, ConstantesTool.SEP_DEC);
		}

		String debito = CommonUtils.double2String(valor, ConstantesTool.SEP_MILES, ConstantesTool.SEP_DEC);
		String credito = "";

		if (valor > 0) {
			credito = debito;
			debito = "";
		}

		String strSaldo = CommonUtils.double2String(valor + saldoInicial, ConstantesTool.SEP_MILES, ConstantesTool.SEP_DEC);

		if (subTotal != 0) {
			if (subTotal != (valor + saldoInicial)) {
				throw new Exception("Error de subTotales");
			}
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
		return AppOcrICBC.String2Double(reg[reg.length - 1], ConstantesTool.SEP_MILES, ConstantesTool.SEP_DEC);
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
