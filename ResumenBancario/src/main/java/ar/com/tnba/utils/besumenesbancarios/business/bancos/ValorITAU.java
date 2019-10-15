package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValorITAU {
	private static final int DISTANCIA_PARA_CREDITO = 15;
//	private static final String REG_EXP_VALOR = "[-]*[\\d.,]*\\d+[,.]\\d{2,2}[ ]";
	private static final String REG_EXP_VALOR = "[-]*[\\d.,]+[ ]";
	private Boolean credito = false;
	private Pattern patternValor = Pattern.compile(REG_EXP_VALOR);
	private int posFin = -1;
	private int posIni = -1;
	private String valor;

	public Boolean getIsCredito() {
		return credito;
	}

	public int getPosFin() {
		return posFin;
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

	public void inicializar(String registro, int desde, ValorITAU vOperacion ) {
		registro = registro.substring(70); //Me quedo solo con los montos
		// Como reutilizo los objetos, cada vez que lo inicializo debo resetear sus parametros
		setCredito(false);  
		posIni = -1;
		posFin = -1;

		Matcher matcher = patternValor.matcher(registro);
		while (matcher.find() && (posIni == -1)) {
			if (matcher.start() > desde) {
				posIni = matcher.start();
				posFin = matcher.end();
			}
		}

		if (posIni > -1) { // Encontro algo
			valor = formatearValor(registro.substring(posIni, posFin).trim());

			/*
			 * IMPORTANTE: DETERMINO SI ES DEBITO O CREDITO POR LA DISTANCIA QUE HAY ENTRE
			 * EL PRIMER VALOR (CREDITO O DEBITO) AL ULTIMO NUMERO (SUBTOTAL)
			 */			
			if (vOperacion != null) {
				if((posIni - vOperacion.getPosFin()) <= DISTANCIA_PARA_CREDITO) {
					vOperacion.setCredito(true);
				}
			}
		}
	}

	private String formatearValor(String valor) {
		//Asumo que siempre viene con 2 decimales 
		valor = valor.replace(".", "").replace(",", "");
		valor = agregarSepMiles(valor.substring(0, valor.length() - 2)) + "," + valor.substring(valor.length() - 2) ;
		
		return valor;
	}

	private String agregarSepMiles(String valor) {
		if(valor.length() > 3) {
			return agregarSepMiles(valor.substring(0, valor.length() - 3)) + "." + valor.substring(valor.length() - 3);
		} else {
			return valor;
		}
	}

	public Boolean isValido() {
		return posIni > -1;
	}

	public void setCredito(Boolean credito) {
		this.credito = credito;
	}
}
