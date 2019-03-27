package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.io.File;
import java.util.StringJoiner;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.tnba.utils.besumenesbancarios.business.CommonResumenBancario;
import net.sourceforge.lept4j.util.LoadLibs;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract1;

public class AppOcrBNA implements BancosInterface {

	private static final String SEP_MILES_BNA = ".";
	private static final String SEP_DEC_BNA = ",";

	@Override
	public String procesarArchivo(String strOcr, File archivo, Integer pagina) throws Exception {
		System.out.println("Procesando Nacion: " + archivo.getName() + " Pagina " + pagina);
		String strOcrFormateado = "";
		try {
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
				// result1 = result1.replaceFirst(" ", ";");

				String[] parts = strOcrFormateado.split("\n");
				for (int i = 0; i < parts.length; i++) {
					parts[i] = parts[i].replaceFirst(" ", ";");
				}
				System.out.println(strOcrFormateado);

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

		Double valor = CommonResumenBancario.String2Double(reg[3], SEP_MILES_BNA, SEP_DEC_BNA);

		if (isCredito(registro, saldoInicial)) {
			return valor;
		}

		return -1 * valor;
	}

	private String armarRegistro(String registro, Double saldoInicial) throws Exception {
		String reg[] = registro.split(";");
		Double saldo = CommonResumenBancario.String2Double(reg[4], SEP_MILES_BNA, SEP_DEC_BNA);
		String strSaldo = CommonUtils.double2String(saldo, SEP_MILES_BNA, SEP_DEC_BNA);

		Double valorReg = CommonResumenBancario.String2Double(reg[3], SEP_MILES_BNA, SEP_DEC_BNA);
		String strvalorReg = CommonUtils.double2String(valorReg, SEP_MILES_BNA, SEP_DEC_BNA);

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

		Double saldo = CommonResumenBancario.String2Double(reg[4], SEP_MILES_BNA, SEP_DEC_BNA);

		return (saldo > saldoInicial);
	}

	private Double getSaldoInicial(String registro) throws Exception {
		String reg[] = registro.split(";");
		return CommonResumenBancario.String2Double(reg[reg.length - 1], SEP_MILES_BNA, SEP_DEC_BNA);
	}

	@Override
	public String getOCR(File archivoOCR) throws Exception {
		System.out.println("Procesando OCR: " + archivoOCR.getName());
		return getInstanceNacion(archivoOCR).doOCR(archivoOCR);
	}

	private ITesseract getInstanceNacion(File archivoOCR) {
		ITesseract instanceNacion = new Tesseract1(); // JNA Direct Mapping
		instanceNacion.setTessVariable("preserve_interword_spaces", "1");
		instanceNacion.setDatapath(archivoOCR.getParent() + File.separator + "temp\\tessdata"); // path to tessdata directory
		File tessDataFolder = LoadLibs.extractNativeResources("tessdata");
		instanceNacion.setDatapath(tessDataFolder.getAbsolutePath());
		return instanceNacion;
	}
}
