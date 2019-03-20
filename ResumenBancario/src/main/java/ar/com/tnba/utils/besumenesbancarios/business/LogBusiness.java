package ar.com.tnba.utils.besumenesbancarios.business;

import java.util.UUID;

public class LogBusiness {

	private static LogManager _LogManager = null;
	private static long DEFAUL_DURACION_WS = 5000; // por default 5 segundos
	private static String csc_session_id = null;

	private static LogManager getLogManager() throws Exception {

		if (_LogManager == null) {
			inicializarLogManager();
		}

		return _LogManager;
	}

	public static void forzarEscrituraLogs() throws Exception {
		getLogManager().fornzarEscrituraLogs();
	}

	public static void logearError(String msg) throws Exception {
		getLogManager().logError(msg);

	}

	public static void logearError(Exception error) throws Exception {
		getLogManager().logError(error);
	}

	public static void logearError(String msg, Exception error) throws Exception {
		getLogManager().logError(msg, error);
	}

	public static void logearInfo(String msg) throws Exception {
		getLogManager().logInfo(msg);
	}

	public static void logKeyLoger(String msg) throws Exception {
		getLogManager().logKeyLoger(msg);
	}

	public static void inicializarLogManager() throws Exception {

		setCsc_session_id(UUID.randomUUID().toString());

		String path =  System.getProperty("user.dir") + "//logs";
		_LogManager = new LogManager(path, "RollPaper");
	}

	public static String getCsc_session_id() {
		return csc_session_id;
	}

	public static void setCsc_session_id(String csc_session_id) {
		LogBusiness.csc_session_id = csc_session_id;
	}

	public static void logEjecucionWS(String nombreMetodo, String parametros, long miliComienzo, long miliFin) throws Exception {

		if (!parametros.equals("")) {
			parametros = "(" + parametros + ")";
		}
		long duracion = miliFin - miliComienzo;
		String mensaje = "";

		if (miliFin > 0) { // termino bin
			long duracionMinuto = duracion / (1000 * 60);
			long duracionSegundo = (duracion - (duracionMinuto * 60)) / 1000;
			long duracionMilisegundo = duracion - (duracionMinuto * 60 * 1000) - (duracionSegundo * 1000);

			mensaje = String.format("#%s %s in %s:%s.%s", // [msec]
					nombreMetodo, parametros, duracionMinuto, duracionSegundo, duracionMilisegundo);
		} else { // fallo
			mensaje = String.format("#%s FALLO %s!!!!!", // [msec]
					nombreMetodo, parametros);
		}

		LogManager.getLogManager().logInfo(mensaje);

		if (duracion > DEFAUL_DURACION_WS) {
			LogManager.getLogManager().logEjecucion(mensaje);
		}
		;

	}

}
