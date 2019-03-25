package ar.com.tnba.utils.besumenesbancarios.workers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import ar.com.tnba.utils.besumenesbancarios.business.BancosBusiness.Bancos;
import ar.com.tnba.utils.besumenesbancarios.business.CommonResumenBancario;
import ar.com.tnba.utils.besumenesbancarios.business.LogManager;
import ar.com.tnba.utils.besumenesbancarios.business.bancos.AppOcrBBVA;
import ar.com.tnba.utils.besumenesbancarios.dto.ArchivoProcesar;
import net.sourceforge.lept4j.util.LoadLibs;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract1;

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
				if (archivoProcesar.getBanco().getId().equals(Bancos.BancoFrances.getId())) {
					datosCSV = AppOcrBBVA.procesarArchivo(getInstanceFrances(), archivoOCR);
				} else {
					if (archivoProcesar.getBanco().getId().equals(Bancos.BancoNacion.getId())) {
						// datosCSV = AppOcrBNA.procesarArchivo(getInstanceNacion(), archivoOCR);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				try {
					LogManager.getLogManager().logError(e);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				try {
					FileWriter write = new FileWriter(
							directorioDestino.getPath() + File.separator + archivoProcesar.getNombreArchivo() + CommonResumenBancario.subFijo(nroHoja) + ".ERROR", true);
					PrintWriter pw = new PrintWriter(write);
					pw.println(e.getStackTrace().toString());
					pw.close();
				} catch (IOException e1) {
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

	private ITesseract getInstanceNacion() {
		ITesseract instanceNacion = new Tesseract1(); // JNA Direct Mapping
		instanceNacion.setTessVariable("preserve_interword_spaces", "1");
		instanceNacion.setDatapath("C:\\temp\\tessdata"); // path to tessdata directory
		File tessDataFolder = LoadLibs.extractNativeResources("tessdata");
		instanceNacion.setDatapath(tessDataFolder.getAbsolutePath());
		return instanceNacion;
	}

	private ITesseract getInstanceFrances() {
		Tesseract1 instanceFrances = new Tesseract1(); // JNA Direct Mapping
		// instance.setTessVariable("preserve_interword_spaces", "1");
		// instance.setTessVariable("psm", "6");
		instanceFrances.setDatapath(directorioDestino.getPath() + File.separator + "temp\\tessdata"); // path to tessdata directory
		File tessDataFolder = LoadLibs.extractNativeResources("tessdata");
		instanceFrances.setDatapath(tessDataFolder.getAbsolutePath());
		return instanceFrances;
	}
}
