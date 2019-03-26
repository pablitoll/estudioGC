package ar.com.tnba.utils.besumenesbancarios.business;

import java.io.File;
import java.io.PrintWriter;

import ar.com.rp.ui.componentes.ArchivoPropiedades;

public class ArchivoDePropiedadesBusiness {

	private static final String ARCHI_CONF = "configuration.properties";
	private static ArchivoPropiedades<propiedades> pPropiedades = null;
	private static String pathToConfig;

	public static String getPathToConfig() {
		return pathToConfig;
	}

	public static void setPathToConfig(String pathToConfig) {
		ArchivoDePropiedadesBusiness.pathToConfig = pathToConfig;
	}

	public enum propiedades {
		directorioDeTrabajo, cantHilos
	}

	public static String getDirectorioDeTrabajo() throws Exception {
		return getPropiedades().getPropiedad(propiedades.directorioDeTrabajo, "C:\\rg");
	}

	public static int getCantHilos() throws Exception {
		return Integer.valueOf(getPropiedades().getPropiedad(propiedades.cantHilos, "3"));
	}

	public static void setDirectorioDeTrabajo(String path) throws Exception {
		getPropiedades().setProperty(propiedades.directorioDeTrabajo, path);
	}

	private static ArchivoPropiedades<propiedades> getPropiedades() throws Exception {
		if (pPropiedades == null) {
			File archivoConf = new File(new File(pathToConfig).getParent() + File.separator + ARCHI_CONF);
			if (!archivoConf.exists()) {
				PrintWriter writer = new PrintWriter(archivoConf);
				writer.print("");
				writer.close();
			}

			pPropiedades = new ArchivoPropiedades<>(archivoConf.getCanonicalPath());
		}
		return pPropiedades;
	}

	public static void recargar() {
		pPropiedades = null;

	}

}
