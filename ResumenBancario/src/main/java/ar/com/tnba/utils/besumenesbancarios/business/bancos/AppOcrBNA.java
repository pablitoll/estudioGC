package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.io.File;
import java.util.StringJoiner;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.tnba.utils.besumenesbancarios.business.CommonResumenBancario;
import ar.com.tnba.utils.besumenesbancarios.business.ConstantesTool;
import ar.com.tnba.utils.besumenesbancarios.business.LogManager;
import net.sourceforge.lept4j.util.LoadLibs;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract1;

public class AppOcrBNA implements BancosInterface {

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

		Double valor = String2Double(reg[3], SEP_MILES, SEP_DEC);

		if (isCredito(registro, saldoInicial)) {
			return valor;
		}

		return -1 * valor;
	}

	private String armarRegistro(String registro, Double saldoInicial) throws Exception {
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

	private Double getSaldoInicial(String registro) throws Exception {
		String reg[] = registro.split(";");
		return String2Double(reg[reg.length - 1], SEP_MILES, SEP_DEC);
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
