package ar.com.tnba.utils.besumenesbancarios.workers;

import java.io.File;
import java.util.List;

import ar.com.tnba.utils.besumenesbancarios.business.ConvertPDFtoTIFF;
import ar.com.tnba.utils.besumenesbancarios.business.bancos.AppOcrBBVA;
import ar.com.tnba.utils.besumenesbancarios.ui.BarraDeProgreso;
import net.sourceforge.lept4j.util.LoadLibs;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract1;

public class WorkerBarraDeProgresoProcesar extends WorkerBarraDeProgresoBase {

	private Boolean huboErroresDeCajaCerrada = false;
	private List<ArchivoProcesar> listaArchivoProcesar;

	public WorkerBarraDeProgresoProcesar(BarraDeProgreso pantalla, List<ArchivoProcesar> listaArchivoProcesar) throws Exception {
		super(pantalla, "Procesando...");
		this.listaArchivoProcesar = listaArchivoProcesar;
		setMax(listaArchivoProcesar.size() + 1);
	}

	@Override
	protected void ejecutarWorker() throws Exception {
		publish(1); // Inncremento la barra
		int i = 0;
		while ((i < listaArchivoProcesar.size()) && !cancelar) {
			ArchivoProcesar archivoProcesar = listaArchivoProcesar.get(i);

			pantalla.setTitle(archivoProcesar.getBanco().getNombre() + " - " + archivoProcesar.getNombreArchivo());

			List<File> listaARchivoOCR = ConvertPDFtoTIFF.convert(archivoProcesar.getArchivo());
			Integer nroHoja = 1;
			//TODO FALTA EL CANCELAR
			for (File archivoOCR : listaARchivoOCR) {
				pantalla.setTitle(
						String.format("%s -  %s - Hoja %s de %s", archivoProcesar.getBanco().getNombre(), archivoProcesar.getNombreArchivo(), nroHoja, listaARchivoOCR.size()));
				nroHoja++;

				// TODO CAMBIAT
				AppOcrBBVA.procesarArchivo(getInstance(), archivoOCR);
			}
			publish(1);
			
			i++;
		}
		pantalla.setTitle("Finalizado");
	}

	private ITesseract getInstance() {
		final ITesseract instance = new Tesseract1(); // JNA Direct Mapping
		// instance.setTessVariable("preserve_interword_spaces", "1");
		// instance.setTessVariable("psm", "6");
		instance.setDatapath("C:\\temp\\tessdata"); // path to tessdata directory
		File tessDataFolder = LoadLibs.extractNativeResources("tessdata");
		instance.setDatapath(tessDataFolder.getAbsolutePath());

		return instance;
	}

	public Boolean getHuboErroresDeCajaCerrada() {
		return huboErroresDeCajaCerrada;
	}

}
