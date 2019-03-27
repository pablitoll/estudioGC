package ar.com.tnba.utils.besumenesbancarios.business;

import java.io.FileWriter;
import java.io.PrintWriter;

import ar.com.rp.rpcutils.CommonUtils;

public class CommonResumenBancario {
	public static String getNroHoja(int nroHoja) {
		return CommonUtils.strRigth("00000" + String.valueOf(nroHoja).trim(), 3);
	}

	public static String subFijo(int nroHoja) {
		return ".Hoja " + getNroHoja(nroHoja);
	}

	public static void txt2File(String txt, String file) throws Exception {
		FileWriter write = new FileWriter(file, true);
		PrintWriter pw = new PrintWriter(write);
		pw.println(txt);
		pw.flush();
		pw.close();
	}

	public static Double String2Double(String valor, String sepMiles, String sepDec) throws Exception {
		valor = valor.replaceAll(" ", "").replaceAll(" .", ".").replaceAll(" ,", ",").replaceAll("\\.", "").replaceAll("\\,", "");
		valor = valor.substring(0, valor.length() - 2) + sepDec + CommonUtils.strRigth(valor, 2);
		return CommonUtils.String2Double(valor, sepMiles, sepDec);
	}
}
