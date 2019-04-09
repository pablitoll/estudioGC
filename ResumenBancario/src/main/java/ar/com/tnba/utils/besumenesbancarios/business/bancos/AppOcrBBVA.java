package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.io.File;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.tnba.utils.besumenesbancarios.business.bancos.BancosBusiness.Bancos;
import net.sourceforge.lept4j.util.LoadLibs;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract1;

public class AppOcrBBVA extends BaseBancos {

	public AppOcrBBVA() {
		super(Bancos.FRANCES);
	}

	private static final String TRANSPORTE_SALDO = "TRANSPORTE SALDO";
	private static final String SALDO_AL = "SALDO AL";
	private static final String SALDO_ANTERIOR = "SALDO ANTERIOR";
	private static final String SIN_MOVIMIENTOS = "S/MOVIMIENTOS";
	private static final int POS_FECHA = 5;

	@Override
	protected String[] getRegistrosFromOCR(String strOcr, File archivo, Integer nroPapagina) throws Exception {

		String strOcrFormateado = "";
		Integer vecMin[] = new Integer[2];
		// Calculo pos final
		Integer idxTransporteFin = strOcr.lastIndexOf(TRANSPORTE_SALDO);
		Integer idxSaldoAl = strOcr.indexOf(SALDO_AL);

		vecMin[0] = idxTransporteFin == -1 ? NUMERO_ALTO : idxTransporteFin;
		vecMin[1] = idxSaldoAl == -1 ? NUMERO_ALTO : idxSaldoAl;

		Integer idxFin = CommonUtils.minimo(vecMin);

		// Calculo pos ini
		Integer idxTransporteInicio = strOcr.indexOf(TRANSPORTE_SALDO);
		Integer idxSaldoAnt = strOcr.indexOf(SALDO_ANTERIOR);

		vecMin[0] = idxTransporteInicio == -1 ? NUMERO_ALTO : idxTransporteInicio;
		vecMin[1] = idxSaldoAnt == -1 ? NUMERO_ALTO : idxSaldoAnt;

		Integer idxIni = CommonUtils.minimo(vecMin);

		if ((idxIni != NUMERO_ALTO) && (idxFin != NUMERO_ALTO) && (idxIni < idxFin)) {

			strOcrFormateado = strOcr.substring(idxIni, idxFin);

			if (!strOcrFormateado.contains(SIN_MOVIMIENTOS)) {

				strOcrFormateado = strOcrFormateado.replaceAll(", ", ",");
				strOcrFormateado = strOcrFormateado.replaceAll(" ,", ",");
				strOcrFormateado = strOcrFormateado.replaceAll(" \\.", ".");

				String[] parts = strOcrFormateado.split("\n");

				String auxSaldoInicial = parts[0].replace(TRANSPORTE_SALDO, "").replaceAll(SALDO_ANTERIOR, "");
				saldoInicial = String2Double(auxSaldoInicial, SEP_MILES, SEP_DEC);
				parts[0] = "";

				// retiro las lineas SIN_MOVIMIENTOS
				for (int i = 0; i < parts.length; i++) {
					if (parts[i].contains(SIN_MOVIMIENTOS)) {
						parts[i] = "";
					}
				}

				return parts;
			}
		}
		return null;
	}

	@Override
	protected String armarRegistro(String registro, Double saldoInicial) throws Exception {

		registro = insertarSeparador(registro, POS_FECHA); 
		int finmovi = registro.lastIndexOf(" ");
		registro = insertarSeparador(registro, finmovi);
		int inimovi = registro.lastIndexOf(" ", finmovi);
		registro = insertarSeparador(registro, inimovi);

		String rengistroSplit[] = registro.split(";");
		// ya tengo el registro spliteado en registroSplit, de ahora sigo con ese

		String saldoFinal = rengistroSplit[rengistroSplit.length - 1];

		double dblSaldoRenglon = String2Double(saldoFinal, SEP_MILES, SEP_DEC);
		double dblValorMovimiento = String2Double(rengistroSplit[2], SEP_MILES, SEP_DEC);

		String debito = "";
		String credito = "";
		if (dblSaldoRenglon > saldoInicial) {
			// credito
			credito = CommonUtils.double2String(dblValorMovimiento, SEP_MILES, SEP_DEC);
		} else { // debito
			debito = CommonUtils.double2String(dblValorMovimiento, SEP_MILES, SEP_DEC);
		}

		return String.format("%s;%s;%s;%s;%s", rengistroSplit[0], rengistroSplit[1], debito, credito, CommonUtils.double2String(dblSaldoRenglon, SEP_MILES, SEP_DEC));
	}

	@Override
	protected Double darSaldoSubTotal(String registro) throws Exception {
		String reg[] = registro.split(";");
		return String2Double(reg[reg.length - 1].trim(), SEP_MILES, SEP_DEC);
	}

	@Override
	protected ITesseract getInstanceTesseract(File archivoOCR) {
		Tesseract1 instanceFrances = new Tesseract1(); // JNA Direct Mapping
		instanceFrances.setDatapath(archivoOCR.getParent() + File.separator + "temp\\tessdata"); // path to tessdata directory
		File tessDataFolder = LoadLibs.extractNativeResources("tessdata");
		instanceFrances.setDatapath(tessDataFolder.getAbsolutePath());
		return instanceFrances;
	}

}
