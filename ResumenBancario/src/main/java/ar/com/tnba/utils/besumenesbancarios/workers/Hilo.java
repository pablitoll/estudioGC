package ar.com.tnba.utils.besumenesbancarios.workers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import ar.com.rp.rpcutils.ExceptionUtils;
import ar.com.tnba.utils.besumenesbancarios.business.CommonResumenBancario;
import ar.com.tnba.utils.besumenesbancarios.business.LogManager;
import ar.com.tnba.utils.besumenesbancarios.business.bancos.BancosInterface;
import ar.com.tnba.utils.besumenesbancarios.dto.ArchivoProcesar;

public class Hilo extends Thread {

	private File archivoOCR;
	private int nroHoja;
	private ArchivoProcesar archivoProcesar;
	private CallBackWorker callBack;
	private Integer chunk;
	private File directorioDestino;

	public Hilo(File archivoOCR, int nroHoja, ArchivoProcesar archivoProcesar, CallBackWorker callBack, Integer chunk, File directorioDestino) {
		super();
		this.archivoOCR = archivoOCR;
		this.nroHoja = nroHoja;
		this.archivoProcesar = archivoProcesar;
		this.callBack = callBack;
		this.chunk = chunk;
		this.directorioDestino = directorioDestino;
	}

	public File getArchivoOCR() {
		return archivoOCR;
	}

	public int getNroHoja() {
		return nroHoja;
	}

	public ArchivoProcesar getArchivoProcesar() {
		return archivoProcesar;
	}

	@Override
	public void run() {
		String datosCSV = "";
		try {
			try {
				// Obtengo el banco businnes
				BancosInterface bi = archivoProcesar.getBanco().getBancoClass().newInstance();
				// Obtengo OCR
				String strOCR = bi.getOCR(archivoOCR);
				// gravo ocr
				txt2File(strOCR, directorioDestino.getPath() + File.separator + archivoProcesar.getNombreArchivo() + CommonResumenBancario.subFijo(nroHoja) + ".ocr");
				// obtengo archivo parceado
				datosCSV = bi.procesarArchivo(strOCR, archivoOCR, nroHoja);

			} catch (Exception e) {
				e.printStackTrace();

				try {
					LogManager.getLogManager().logError(e);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				try {
					txt2File(ExceptionUtils.exception2String(e),
							directorioDestino.getPath() + File.separator + archivoProcesar.getNombreArchivo() + CommonResumenBancario.subFijo(nroHoja) + ".ERROR");
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}

			if (!datosCSV.equals("")) {
				try {
					Files.write(Paths.get(directorioDestino.getPath() + File.separator + archivoProcesar.getNombreArchivo() + CommonResumenBancario.subFijo(nroHoja) + ".csv"),
							datosCSV.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
				} catch (IOException e) {
					e.printStackTrace();
					try {
						LogManager.getLogManager().logError(e);
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		} finally {
			callBack.terminoHilo(chunk);
		}
	}

	private void txt2File(String txt, String file) throws Exception {
		FileWriter write = new FileWriter(file, true);
		PrintWriter pw = new PrintWriter(write);
		pw.println(txt);
		pw.close();
	}
}
