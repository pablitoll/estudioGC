package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.com.rp.rpcutils.CommonUtils;

public class ValorICBC {
	private int posIni = -1;
	private int posFin = -1;
	private int cantDeb = 0;
	private int totalDev = 0;

	private static final String REG_EXP_VALOR = "[\\d,.]*\\d+[,.]\\d+[-]{0,1}";
	// {0,1}";
	private Pattern patternValor = Pattern.compile(REG_EXP_VALOR);

	private static final String REG_EXP_ORIGEN = "[ ]\\d{4}[ ]";
	private Pattern patternOrigen = Pattern.compile(REG_EXP_ORIGEN);

	private String valor;
	private boolean debito = false;

	public boolean isDebito() {
		return debito;
	}

	public void inicializar(String registro, int desde, boolean esSubTotal) {
		debito = false;
		posIni = -1;
		posFin = -1;

		Matcher matcherOrigen = patternOrigen.matcher(registro); // Normalizo la entrada
		matcherOrigen.find();
		registro = registro.substring(matcherOrigen.start());

		Matcher matcher = patternValor.matcher(registro);
		while (matcher.find() && (posIni == -1)) {
			if (matcher.start() > desde) {
				posIni = matcher.start();
				posFin = matcher.end();
			}
		}

		if (posIni > -1) { // Encontro algo
			valor = registro.substring(posIni, posFin).trim();

			// El signo lo manejo de acuerdo a la posivion del valor dentro del texto,
			// el caso de los sub totales estan todos alineados en la misma colmna con lo
			// cual no se que signo es
			if (!esSubTotal) {
				String signo = valor.substring(valor.length() - 1, valor.length());
				if (determinarDebito(signo)) {
					addPromedio();
					debito = true;
				}

				// Saco el "-"
				if (signo.equals("-")) {
					valor = valor.substring(0, valor.length() - 1);
				}

			}
		}
	}

	private void addPromedio() {
		totalDev += posFin;
		cantDeb++;

	}

	private boolean determinarDebito(String signo) {
		if (cantDeb >= 2) { // tomo un priomedio superior a 2 para darle certeza al nuevo indicador
			return posFin <= ((totalDev / cantDeb) + 5);
		}

		return signo.equals("-");
	}

	public int getPosFin() {
		return posFin;
	}

	public Boolean isValido() {
		return posIni > -1;
	}

	public int getPosIni() {
		return posIni;
	}

	public String getValor() throws Exception {
		if (isValido()) {
			return formatear(valor);
		}
		return "";
	}
	private String formatear(String valor) throws Exception {
		if (valor.length() > 3) {

			String puntoDecimal = valor.substring(valor.length() - 3, valor.length() - 2);

			if (puntoDecimal.equals(".") || puntoDecimal.equals(",")) {
				valor = valor.replace(".", "").replace(",", "");
				valor = valor.substring(0, valor.length() - 2) + "," + valor.substring(valor.length() - 2);
			} else {
				valor = valor.replace(".", "").replace(",", "");
			}

			return CommonUtils.double2String(CommonUtils.String2Double(valor, ".", ","), ".", ",");
		}
		return valor;
	}

}
