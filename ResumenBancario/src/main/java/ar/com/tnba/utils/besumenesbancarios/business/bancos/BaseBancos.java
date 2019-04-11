package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.io.File;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.tnba.utils.besumenesbancarios.business.LogManager;
import ar.com.tnba.utils.besumenesbancarios.business.bancos.BancosBusiness.Bancos;
import net.sourceforge.lept4j.util.LoadLibs;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract1;

public abstract class BaseBancos {

	protected static final String LEYENDA_FALLO = "FALLO EL ANALISIS DE ESTE REGISTRO.";
	protected static final int NUMERO_ALTO = 999999;
	protected static final Double SALDO_TOTAL_NO_VALIDO = 9999998.11;
	protected static final String SEP_DEC = ",";
	protected static final String SEP_MILES = ".";

	protected abstract String armarRegistro(String registro, Double saldoOperacionAnterior) throws Exception;

	protected abstract Double darSaldoSubTotal(String registro) throws Exception;

	protected abstract String[] getRegistrosFromOCR(String strOcr, File archivo, Integer nroPapagina) throws Exception;

	private Bancos banco;
	protected ITesseract tesseract = null;
	private String espacios = "";
	protected Double saldoInicial = SALDO_TOTAL_NO_VALIDO;

	public BaseBancos(Bancos banco) {
		this.banco = banco;
	}

	protected ITesseract getInstanceTesseract(File archivoOCR) {
		if (tesseract == null) {
			tesseract = new Tesseract1(); // JNA Direct Mapping
			tesseract.setTessVariable("preserve_interword_spaces", "1");
			tesseract.setDatapath(archivoOCR.getParent() + File.separator + "temp\\tessdata"); // path to tessdata directory
			File tessDataFolder = LoadLibs.extractNativeResources("tessdata");
			tesseract.setDatapath(tessDataFolder.getAbsolutePath());
		}
		return tesseract;
	}

	public String getOCR(File archivoOCR) throws Exception {
		System.out.println("Procesando OCR: " + archivoOCR.getName());
		return getInstanceTesseract(archivoOCR).doOCR(archivoOCR);
	}

	public String procesarArchivo(String strOcr, File archivo, Integer nroPagina) throws Exception {
		System.out.println(String.format("Procesando %s: %s - Pagina %s ", banco.getNombre(), archivo.getName(), nroPagina));

		String[] registros = getRegistrosFromOCR(strOcr, archivo, nroPagina);
		if (registros != null) {
			StringBuilder retorno = new StringBuilder();
			Double saldo = saldoInicial;
			for (String reg : registros) {

				if (!reg.equals("")) {
					try {
						String registro = armarRegistro(reg, saldo);
						retorno.append(registro + "\n");
						saldo = darSaldoSubTotal(registro);
					} catch (Exception e) {
						e.printStackTrace();
						String errorSubtotal = "";
						if (e instanceof ExceptionSubTotal) {
							System.out.println(reg);
							errorSubtotal = "Error en el Caclulo del Subtotal - ";
						}
						retorno.append(armarRegistroTrim(reg) + errorSubtotal + LEYENDA_FALLO + "\n");
						saldo = darSaldoSubTotalFromErrror(reg);
						try {
							LogManager.getLogManager().logError(e);
						} catch (Exception e2) {
							e.printStackTrace();
						}
					}
				}
			}

			return retorno.toString();
		}
		return "";
	}

	protected Double darSaldoSubTotalFromErrror(String reg) {
		return SALDO_TOTAL_NO_VALIDO; // Si falla no puedo garantizar el contador
	}

	private String armarRegistroTrim(String registro) {
		String reg[] = registro.split(";");
		String resultado = "";
		for (String aux : reg) {
			resultado += aux.trim() + ";";
		}
		return resultado;
	}

	protected String getEmptyRegistro() {
		if (espacios.equals("")) {
			for (int i = 0; i < 500; i++) {
				espacios += " ";
			}
		}
		return espacios;
	}

	protected String insertarSeparadorConTrim(String valor, int pos) {
		return valor.substring(0, pos).trim() + ";" + valor.substring(pos, valor.length()).trim();
	}

	protected String insertarSeparador(String valor, int pos, String cadena) {
		return valor.substring(0, pos) + cadena + valor.substring(pos, valor.length());
	}

	protected String insertarSeparador(String valor, int pos) {
		return insertarSeparador(valor, pos, ";");
	}

	protected Double String2Double(String valor, String sepMiles, String sepDec) throws Exception {

		if (valor.substring(valor.length() - 1).equals("-")) {
			valor = "-" + valor.substring(0, valor.length() - 1);
		}

		valor = valor.replaceAll(" ", "").replaceAll(" .", ".").replaceAll(" ,", ",");
		if (valor.indexOf(sepDec) != -1) {
			valor = valor + "0000000";
			valor = valor.substring(0, valor.indexOf(sepDec) + 3);
		}

		valor = valor.replaceAll("\\.", "").replaceAll("\\,", "");
		valor = valor.substring(0, valor.length() - 2) + sepDec + CommonUtils.strRigth(valor, 2);

		return CommonUtils.String2Double(valor, sepMiles, sepDec);
	}
}
