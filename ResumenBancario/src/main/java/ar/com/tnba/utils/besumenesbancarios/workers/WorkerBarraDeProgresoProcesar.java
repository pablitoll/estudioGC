package ar.com.tnba.utils.besumenesbancarios.workers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ar.com.tnba.utils.besumenesbancarios.business.ConvertPDFtoTIFF;
import ar.com.tnba.utils.besumenesbancarios.dto.ArchivoProcesar;
import ar.com.tnba.utils.besumenesbancarios.ui.BarraDeProgreso;

public class WorkerBarraDeProgresoProcesar extends WorkerBarraDeProgresoBase implements CallBackWorker {

	private static final int CANT_HILO_MAX = 3;
	private static final int SIZE_SLOT_CHUNK = 100000;
	private Boolean huboErroresDeCajaCerrada = false;
	private List<ArchivoProcesar> listaArchivoProcesar;
	private List<Hilo> listaHilos = new ArrayList<Hilo>();

	public WorkerBarraDeProgresoProcesar(BarraDeProgreso pantalla, List<ArchivoProcesar> listaArchivoProcesar) throws Exception {
		super(pantalla, "Procesando...");
		this.listaArchivoProcesar = listaArchivoProcesar;
		setMax(listaArchivoProcesar.size() * SIZE_SLOT_CHUNK);
	}

	@Override
	protected synchronized void ejecutarWorker() throws Exception {
		publish(0); // Inncremento la barra
		int i = 0;
		while ((i < listaArchivoProcesar.size()) && !cancelar) {
			ArchivoProcesar archivoProcesar = listaArchivoProcesar.get(i);

			pantalla.setTitle(archivoProcesar.getBanco().getNombre() + " - " + archivoProcesar.getNombreArchivo() + " - OCR");

			List<File> listaARchivoOCR = ConvertPDFtoTIFF.convert(archivoProcesar.getArchivo());
			Integer nroHoja = 1;
			Integer chunk = SIZE_SLOT_CHUNK / (listaARchivoOCR.size() + 1);

			avanzarbBarra(chunk);
			while (nroHoja <= listaARchivoOCR.size() && !cancelar) {

				if (hayHiloLibre()) {
					File archivoOCR = listaARchivoOCR.get(nroHoja - 1);
					pantalla.setTitle(String.format("%s -  %s - Hoja Terminada %s / %s", archivoProcesar.getBanco().getNombre(), archivoProcesar.getNombreArchivo(), nroHoja - 1,
							listaARchivoOCR.size()));

					Hilo hiloLibre = new Hilo(archivoOCR, nroHoja, archivoProcesar, this, chunk);

					listaHilos.add(hiloLibre);
					hiloLibre.start();
					wait(500);

					nroHoja++;
				} else {
					wait(4000);
				}
			}

			i++;
		}

		while (!terminaronWS()) {
			wait(4000);
		}

		pantalla.setTitle("Finalizado");
	}

	private boolean terminaronWS() {
		int cant = 0;
		for (Hilo hilo : listaHilos) {
			if (hilo.isAlive()) {
				cant++;
			}
		}
		return cant == 0;
	}

	private void avanzarbBarra(Integer chunk) {
		publish(chunk);
	}

	private boolean hayHiloLibre() {
		int cant = 0;
		for (Hilo hilo : listaHilos) {
			if (hilo.isAlive()) {
				cant++;
			}
		}
		return cant < CANT_HILO_MAX;
	}

	public Boolean getHuboErroresDeCajaCerrada() {
		return huboErroresDeCajaCerrada;
	}

	@Override
	public void terminoHilo(Integer chunk) {
		avanzarbBarra(chunk);
	}

}
