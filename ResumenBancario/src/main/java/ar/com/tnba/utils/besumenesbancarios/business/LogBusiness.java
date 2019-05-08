package ar.com.tnba.utils.besumenesbancarios.business;

import java.util.UUID;

public class LogBusiness {

	private static LogManager _LogManager = null;
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
		_LogManager = new LogManager(path, "ResumenBancario");
	}

	public static String getCsc_session_id() {
		return csc_session_id;
	}

	public static void setCsc_session_id(String csc_session_id) {
		LogBusiness.csc_session_id = csc_session_id;
	}

	
}
