package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValorICBC {

	private int posIni = -1;
	private int posFin = -1;
	private int cantDeb = 0;
	private int totalDev = 0;

	private static final String REG_EXP_VALOR = "[\\d.]*\\d+[,]\\d+[-]{0,1}";
	private Pattern patternValor = Pattern.compile(REG_EXP_VALOR);
	
	private static final String REG_EXP_ORIGEN = "[ ]\\d{4}[ ]";
	private Pattern patternOrigen = Pattern.compile(REG_EXP_ORIGEN);
	
	private String valor;

	public void inicializar(String registro, int desde) {
		posIni = -1;
		posFin = -1;
		
		Matcher matcherOrigen = patternOrigen.matcher(registro); //Normalizo la entrada
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
			String signo = valor.substring(valor.length() - 1, valor.length());
			if (esDebito(signo)) {
				if (!signo.equals("-")) {
					valor += "-";
				}
				addPromedio();
			} else {
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

	private boolean esDebito(String signo) {
		if (cantDeb >= 2) { // tomo un priomedio superior a 5 para darle certeza al nuevo indicador
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

	public String getValor() {
		if (isValido()) {
			return valor;
		}
		return "";
	}

}
