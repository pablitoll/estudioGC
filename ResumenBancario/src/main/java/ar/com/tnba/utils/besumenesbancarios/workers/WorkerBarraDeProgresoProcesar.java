package ar.com.tnba.utils.besumenesbancarios.workers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import ar.com.rp.rpcutils.ExceptionUtils;
import ar.com.tnba.utils.besumenesbancarios.business.ArchivoDePropiedadesBusiness;
import ar.com.tnba.utils.besumenesbancarios.business.CommonResumenBancario;
import ar.com.tnba.utils.besumenesbancarios.business.ConvertPDFtoTIFF;
import ar.com.tnba.utils.besumenesbancarios.business.ManejoDeArchivos;
import ar.com.tnba.utils.besumenesbancarios.dto.ArchivoProcesar;
import ar.com.tnba.utils.besumenesbancarios.ui.BarraDeProgreso;

public class WorkerBarraDeProgresoProcesar extends WorkerBarraDeProgresoBase implements CallBackWorker {

	private final int CANT_HILO_MAX = ArchivoDePropiedadesBusiness.getCantHilos();
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

			pantalla.setTitle(archivoProcesar.getBanco().getNombre() + " - " + archivoProcesar.getNombreArchivo() + " - Generando Imagenes");

			File directorioDestino = new File(archivoProcesar.getArchivo().getPath().substring(0, archivoProcesar.getArchivo().getPath().length() - 4));
			FileUtils.deleteDirectory(directorioDestino);
			directorioDestino.mkdir();

			List<File> listaARchivoOCR = ConvertPDFtoTIFF.convert(archivoProcesar.getArchivo(), directorioDestino);
			Integer nroHoja = 1;
			Integer chunk = SIZE_SLOT_CHUNK / (listaARchivoOCR.size() + 1);

			avanzarbBarra(chunk);
			while (nroHoja <= listaARchivoOCR.size() && !cancelar) {

				if (hayHiloLibre()) {
					// TODO SACAR
					// if (nroHoja == 2) {
					File archivoOCR = listaARchivoOCR.get(nroHoja - 1);
					pantalla.setTitle(String.format("%s - %s - %s Hojas", archivoProcesar.getBanco().getNombre(), archivoProcesar.getNombreArchivo(), listaARchivoOCR.size()));
					Hilo hiloLibre = new Hilo(archivoOCR, nroHoja, listaARchivoOCR.size(), archivoProcesar, this, chunk, directorioDestino);

					listaHilos.add(hiloLibre);
					hiloLibre.start();
					wait(500);
					// }
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
		return cant < getCantHilosMax();
	}

	private int getCantHilosMax() {
		// TODO terminar
		return CANT_HILO_MAX;
	}

	public Boolean getHuboErroresDeCajaCerrada() {
		return huboErroresDeCajaCerrada;
	}

	@Override
	public void terminoHilo(Integer chunk, Hilo hilo) {
		if (isUltimoHiloEnEjecutar(hilo)) {

			try {
				// Si es el utimo, genero el csv con la union de todos.
				File rootFile = new File(hilo.getArchivoOCR().getParentFile().toString());
				File[] listaFile = rootFile.listFiles();
				boolean entro = false;
				boolean hayError = false;

				if ((listaFile != null) && (listaFile.length >= 0)) {
					String nombreArchivoCSV = hilo.getArchivoOCR().getParentFile().toString() + File.separator
							+ ManejoDeArchivos.getNombreArchivoCSVCompleto(hilo.getArchivoProcesar().getNombreArchivo(), false);

					PrintWriter pw = new PrintWriter(nombreArchivoCSV);
					for (File f : listaFile) {
						if (f.isFile()) {
							String nombreArchivo = f.getName();

							if (nombreArchivo.substring(nombreArchivo.lastIndexOf(".")).equalsIgnoreCase(".csv")) {
								entro = true;
								BufferedReader br = new BufferedReader(new FileReader(f));
								String line = br.readLine();
								while (line != null) {
									pw.println(line);
									line = br.readLine();
								}
								br.close();
							}

							if (nombreArchivo.substring(nombreArchivo.lastIndexOf(".")).equalsIgnoreCase(".ERROR")) {
								hayError = true;
							}
						}
					}

					if (hayError) {
						pw.println("Hay errores en algunas Hojas, en este archivo NO ESTAN TODOS los movimientos");
					}

					pw.flush();
					pw.close();

				}

				if (!entro) {
					CommonResumenBancario.txt2File("No hay archivo csv generados", hilo.getArchivoOCR().getParentFile().toString() + File.separator
							+ ManejoDeArchivos.getNombreArchivoCSVCompleto(hilo.getArchivoProcesar().getNombreArchivo(), true));
				}

			} catch (Exception e) {
				e.printStackTrace();
				try {
					CommonResumenBancario.txt2File(ExceptionUtils.exception2String(e), hilo.getArchivoOCR().getParentFile().toString() + File.separator
							+ ManejoDeArchivos.getNombreArchivoCompletoDumpError(hilo.getArchivoProcesar().getNombreArchivo()));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
		avanzarbBarra(chunk);
	}

	private boolean isUltimoHiloEnEjecutar(Hilo hiloAVerficar) {
		int cantidadTermiandos = 1; // EL que llama esta por termianr pero aun no termino.
		for (Hilo hilo : listaHilos) {
			if (hilo.getArchivoProcesar().getNombreArchivo().equals(hiloAVerficar.getArchivoProcesar().getNombreArchivo())) {
				if (!hilo.isAlive()) {
					cantidadTermiandos++;
				}
			}
		}
		return cantidadTermiandos == hiloAVerficar.getTotalDeHojas();
	}

}
