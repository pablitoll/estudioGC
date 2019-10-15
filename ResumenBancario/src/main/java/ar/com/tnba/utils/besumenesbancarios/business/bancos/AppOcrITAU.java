package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.tnba.utils.besumenesbancarios.business.bancos.BancosBusiness.Bancos;
import ar.com.tnba.utils.besumenesbancarios.business.procesarImagen.SoloNegro;

public class AppOcrITAU extends BaseBancos {

	private static final String OPERACION = "Operac.";
	private static final String PAGINA = "Pagina";
	private static final String DETALLE_MOVIMIENTOS = "DETALLE DE MOVIMIENTOS AL";
	private static final String SALDO = "SALDO AL";
	private static final int POS_FIN_DES = 70;
	private static final int POS_FIN_FECHA = 8;

	private ValorITAU v1 = new ValorITAU();
	private ValorITAU vSubtotal = new ValorITAU();

	public AppOcrITAU() {
		super(Bancos.ITAU);
	}

	@Override
	public String getOCR(File archivoOCR) throws Exception {

		System.out.println("Procesando OCR (ITAU): " + archivoOCR.getName());
		BufferedImage bi = SoloNegro.procesar(archivoOCR);

		try {
			System.out.println();
			String nombreCopia = archivoOCR.getAbsolutePath().substring(0, archivoOCR.getAbsolutePath().length() - 4) + ".imagenProcesada"
					+ archivoOCR.getAbsolutePath().substring(archivoOCR.getAbsolutePath().length() - 4);
			File fileImagenProcesda = new File(nombreCopia);
			ImageIO.write(bi, "jpg", fileImagenProcesda);
		} catch (Exception e) {
			System.out.println(e);
		}

		return getInstanceTesseract(archivoOCR).doOCR(bi);
	}

	public String[] getRegistrosFromOCR(String strOcr, File archivo, Integer pagina) throws Exception {
		String strOcrFormateado = "";
		String[] parts = null;
		int idxInicio = strOcr.indexOf(DETALLE_MOVIMIENTOS);

		// fin
		int idxSaldoPri = strOcr.indexOf(SALDO);
		int idxSaldoSeg = strOcr.lastIndexOf(SALDO);

		int idxFinal = idxSaldoSeg; // Si son != uso este

		if (idxSaldoPri == idxSaldoSeg) {
			if (idxSaldoPri - idxInicio < 200) {
				idxFinal = strOcr.lastIndexOf(PAGINA);
			}
		}

		if ((idxInicio > -1) && (idxFinal > -1)) {
			strOcrFormateado = strOcr.substring(idxInicio, idxFinal);

			strOcrFormateado = strOcrFormateado.replaceAll(", ", ",");
			strOcrFormateado = strOcrFormateado.replaceAll(" ,", ",");
			strOcrFormateado = strOcrFormateado.replaceAll(" \\.", ".");
			strOcrFormateado = strOcrFormateado.replaceAll("\\.,", ".");
			strOcrFormateado = strOcrFormateado.replaceAll("~", "");

			parts = strOcrFormateado.split("\n");
			for (int i = 0; i < parts.length; i++) {
				if (parts[i].contains(DETALLE_MOVIMIENTOS) || parts[i].contains(OPERACION)) {
					parts[i] = "";
				} else {
					if (parts[i].contains(SALDO)) {
						String reg[] = parts[i].split(SALDO);
						String saldoAl = reg[reg.length - 1].substring(50); // Tomo una posicion que se que no hay nada
						saldoInicial = String2Double(saldoAl, SEP_MILES, SEP_DEC);
						parts[i] = "";
					} else {
						if (!parts[i].trim().equals("")) {
							String aux = parts[i] + getEmptyRegistro(); // no esta bien pero bueno,anda
							aux = insertarSeparador(aux, POS_FIN_FECHA);
							aux = insertarSeparador(aux, POS_FIN_DES + 1);

							v1.inicializar(aux, -1, null);
							vSubtotal.inicializar(aux, v1.getPosFin() - 1, v1);

							aux = aux.substring(0, POS_FIN_DES + 2);

							if (!v1.isValido() || !vSubtotal.isValido()) {
								System.out.println("no valido");
								aux = parts[i] + " - ERROR al detectar valores";
							} else {

								if (v1.getIsCredito()) {
									aux += ";" + v1.getValor();
								} else {
									aux += v1.getValor() + ";";
								}

								aux += ";" + vSubtotal.getValor();
							}
							parts[i] = aux;
						}
					}
				}
			}
		}
		return parts;

	}

	@Override
	protected Double darSaldoSubTotal(String registro) throws Exception {
		String reg[] = registro.split(";");
		return String2Double(reg[reg.length - 1].trim(), SEP_MILES, SEP_DEC);
	}

	@Override
	protected String armarRegistro(String registro, Double saldoInicial) throws Exception {
		String reg[] = registro.split(";");

		String debito = "";
		String credito = "";
		Double valor = 0.0;

		// todo sacar
		if (reg.length < 4) {
			throw new Exception("Error en longitud del registro");
		}

		Double valorSubTotal = String2Double(reg[4].trim(), SEP_MILES, SEP_DEC);

		if (!reg[2].trim().equals("")) {
			// es debito
			valor = String2Double(reg[2].trim(), SEP_MILES, SEP_DEC);
			debito = reg[2].trim();

			valor = valor * -1;
		}

		if (!reg[3].trim().equals("")) {
			// es credito
			valor = String2Double(reg[3].trim(), SEP_MILES, SEP_DEC);
			credito = reg[3].trim();
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
					throw new ExceptionSubTotal("No coincide el subtotal");
				}
			}

			strSaldo = CommonUtils.double2String(CommonUtils.redondear(nuevoTotal, 2), SEP_MILES, SEP_DEC);
		}

		return String.format("%s;%s;%s;%s;%s", reg[0].trim(), reg[1].trim(), debito, credito, strSaldo);
	}

}
