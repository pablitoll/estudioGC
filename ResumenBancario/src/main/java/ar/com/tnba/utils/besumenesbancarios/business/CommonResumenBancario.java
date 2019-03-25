package ar.com.tnba.utils.besumenesbancarios.business;

import ar.com.rp.rpcutils.CommonUtils;

public class CommonResumenBancario {
	public static String getNroHoja(int nroHoja) {
		return CommonUtils.strRigth("00000" + String.valueOf(nroHoja).trim(), 3);
	}

	public static String subFijo(int nroHoja) {
		return ".Hoja " + getNroHoja(nroHoja);
	}
}
