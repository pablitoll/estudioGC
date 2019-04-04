package ar.com.tnba.utils.besumenesbancarios.business;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ar.com.rp.rpcutils.ExceptionUtils;

public class LogManager {

	private enum archivoLog {

		Error, Info, Warn, WarnEjecucion, Trace, logKeyLoger;

		private StringBuffer buffer = new StringBuffer();
		private Date lastDownload = null;
		private static Long TIMEOUT = (long) (3 * 60 * 1000); // 3 minutos

		public StringBuffer getBuffer() {
			if (buffer == null) {
				buffer = new StringBuffer();
			}
			return buffer;
		}

		public void resetear() {
			buffer = null;
		}

		public boolean isBufferFull() {
			return getBuffer().toString().split("\r\n").length > 20;
		}

		public void agregarMsg(String msg) {
			getBuffer().append(getFechaMostrar() + " " + msg + "\r\n"); // + " Puesto:" + nroPuesto + " -> "
			System.out.println("[" + getTimeStamp() + "] " + msg + "\r\n");
		}

		public void setLastDownload(Date fecha) {
			this.lastDownload = fecha;
		}

		public boolean isTimeOut() {
			if (lastDownload == null) {
				return false;
			} else {
				return (System.currentTimeMillis() - lastDownload.getTime() > TIMEOUT);
			}
		}
	};

	private static LogManager _LogManager = null;
	private String pathLogFile = null;
	private String nombreAplicativo = "";

	public static LogManager getLogManager() throws Exception {
		if (_LogManager == null) {
			throw new Exception("Log no definido");
		} else {
			return _LogManager;
		}
	}

	public LogManager(String pathLogFile, String nombreAplicativo) {
		super();
		this.pathLogFile = pathLogFile;
		this.nombreAplicativo = nombreAplicativo;

		_LogManager = this;
	}

	public void logInfo(String msg, boolean forzarEscritura) {
		logear(archivoLog.Info, msg, forzarEscritura);
	}

	public void logInfo(String msg) {
		logInfo(msg, false);
	}

	public void logKeyLoger(String msg, boolean forzarEscritura) {
		logear(archivoLog.logKeyLoger, msg, forzarEscritura);
	}

	public void logKeyLoger(String msg) {
		logKeyLoger(msg, false);
	}

	public void logTrance(String msg, boolean forzarEscritura) {
		logear(archivoLog.Trace, msg, forzarEscritura);
	}

	public void logTrance(String msg) {
		logTrance(msg, false);
	}

	public void logError(String msg, Exception error, boolean forzarEscritura) {
		String msgError = "";

		if ((msg != null) && !msg.equals("")) {
			msgError = msg;
		}

		if (error != null) {
			msgError += "\n" + ExceptionUtils.exception2String(error);
		}

		//System.out.println(msgError);
		logear(archivoLog.Error, msgError, forzarEscritura);
	}

	public void logError(String msg) {
		logError(msg, null, true);
	}

	public void logError(Exception error) {
		logError("", error, true);
	}

	public void logError(String msg, Exception error) {
		logError(msg, error, true);
	}

	@Deprecated
	public void logError(String error, boolean forzarEscritura) {
		logError(error, null, forzarEscritura);

	}

	public void logError(Exception error, boolean forzarEscritura) {
		logError("", error, forzarEscritura);
	}

	public void logEjecucion(String msg, boolean forzarEscritura) {
		logear(archivoLog.WarnEjecucion, msg, forzarEscritura);
	}

	public void logEjecucion(String msg) {
		logEjecucion(msg, false);
	}

	private Date getDate() {
		return new Date();
	}

	private static String getTimeStamp() {
		return new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
	}

	private static String getFechaMostrar() {
		return new SimpleDateFormat("HH:mm:ss:SSS").format(Calendar.getInstance().getTime());
	}

	public void fornzarEscrituraLogs() {
		for (int i = 0; i < archivoLog.values().length; i++) {
			forzarEscrituraBuffer(archivoLog.values()[i]);
		}
	}

	private void logear(archivoLog nombreArchivo, String msg, boolean forzarEscritura) {
		if (cambioFecha(nombreArchivo)) {
			forzarEscrituraBuffer(nombreArchivo);
		}

		nombreArchivo.agregarMsg(msg);
		if ((nombreArchivo.isBufferFull()) || forzarEscritura || nombreArchivo.isTimeOut()) {
			forzarEscrituraBuffer(nombreArchivo);
		}
	}

	private boolean cambioFecha(archivoLog nombreArchivo) {
		if (nombreArchivo.lastDownload == null) {
			return false;
		} else {
			Calendar cal = Calendar.getInstance();
			int diaHoy = cal.get(Calendar.DAY_OF_MONTH);

			cal.setTime(nombreArchivo.lastDownload);
			int diaLastDown = cal.get(Calendar.DAY_OF_MONTH);

			return diaHoy != diaLastDown;
		}
	}

	private void forzarEscrituraBuffer(archivoLog nombreArchivo) {
		if (nombreArchivo.getBuffer().length() > 0) {
			BufferedWriter writer = null;
			try {
				Date fechaUsoArchivo = nombreArchivo.lastDownload;
				if (nombreArchivo.lastDownload == null) {
					fechaUsoArchivo = getDate();
				}

				String fechaArchivo = "sinFecha";
				try {
					DateFormat df = new SimpleDateFormat("yyyyMMdd");
					fechaArchivo = df.format(fechaUsoArchivo);
				} catch (Exception e) {
					e.printStackTrace();
				}

				String detalle = "";
				if (!nombreAplicativo.equals("")) {
					detalle = "_" + nombreAplicativo;
				}

				String timeLog = pathLogFile + nombreArchivo.toString() + detalle + "_" + fechaArchivo + ".log";
				File logFile = new File(timeLog);

				// Verifico si existe la carpeta, sino trato de crearla
				if (!Files.exists(logFile.toPath())) {
					logFile.getParentFile().mkdirs();
				}

				writer = new BufferedWriter(new FileWriter(logFile, true));

				writer.write(nombreArchivo.getBuffer().toString());

				nombreArchivo.resetear();

				nombreArchivo.setLastDownload(getDate());

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					// Close the writer regardless of what happens...
					writer.close();
				} catch (Exception e) {
				}
			}
		}
	}

}
