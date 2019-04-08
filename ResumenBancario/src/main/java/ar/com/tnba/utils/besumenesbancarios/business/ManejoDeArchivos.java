package ar.com.tnba.utils.besumenesbancarios.business;

public class ManejoDeArchivos {

	private static final String ARCHIVO_OCR = "%s.Hoja %s.ocr";
	private static final String ARCHIVO_CSV = "%s.Hoja %s.csv";
	private static final String ARCHIVO_CSV_ERROR = "%s.Hoja %s.ERROR.csv";
	private static final String ARCHIVO_TIF = "%s.Hoja %s.tif";
	private static final String ARCHIVO_DUMP_ERROR = "%s.Hoja %s.ERROR";

	private static final String ARCHIVO_CSV_COMPLETO = "%s.Completo.csv";
	private static final String ARCHIVO_CSV_COMPLETO_ERROR = "%s.Completo.ERROR.csv";
	private static final String ARCHIVO_COMLETO_DUMP_ERROR = "%s.Completo.ERROR";

	public static String getNombreArchivoOCR(String nombreArchivo, int nroHoja) {
		return String.format(ARCHIVO_OCR, nombreArchivo, CommonResumenBancario.getNroHoja(nroHoja));
	}

	public static String getNombreArchivoCSV(String nombreArchivo, int nroHoja, boolean conError) {
		if (!conError) {
			return String.format(ARCHIVO_CSV, nombreArchivo, CommonResumenBancario.getNroHoja(nroHoja));
		} else {
			return String.format(ARCHIVO_CSV_ERROR, nombreArchivo, CommonResumenBancario.getNroHoja(nroHoja));
		}
	}

	public static String getNombreArchivoTIF(String nombreArchivo, int nroHoja) {
		return String.format(ARCHIVO_TIF, nombreArchivo, CommonResumenBancario.getNroHoja(nroHoja));
	}

	public static String getNombreArchivoCSVCompleto(String nombreArchivo, boolean conError) {
		if (!conError) {
			return String.format(ARCHIVO_CSV_COMPLETO, nombreArchivo);
		} else {
			return String.format(ARCHIVO_CSV_COMPLETO_ERROR, nombreArchivo);
		}
	}

	public static String getNombreArchivoDumpError(String nombreArchivo, int nroHoja) {
		return String.format(ARCHIVO_DUMP_ERROR, nombreArchivo, CommonResumenBancario.getNroHoja(nroHoja));
	}

	public static String getNombreArchivoCompletoDumpError(String nombreArchivo) {
		return String.format(ARCHIVO_COMLETO_DUMP_ERROR, nombreArchivo);
	}
}
