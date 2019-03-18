package ar.com.tool.rb.workers;

import java.util.List;

import ar.com.smsv.csc.cliente.ui.BarraDeProgreso;

public class WorkerBarraDeProgresoProcesar extends WorkerBarraDeProgresoBase {

	private Boolean huboErroresDeCajaCerrada = false;
	private List<ArchivoProcesar> listaArchivoProcesar;

	public WorkerBarraDeProgresoProcesar(BarraDeProgreso pantalla, List<ArchivoProcesar> listaArchivoProcesar) throws Exception {
		super(pantalla, "Procesando...");
		this.listaArchivoProcesar = listaArchivoProcesar;
		setMax(listaArchivoProcesar.size());
	}

	@Override
	protected void ejecutarWorker() throws Exception {
		publish(0); // Inncremento la barra
		int i = 0;
		while ((i < listaArchivoProcesar.size()) && !cancelar) {
			ArchivoProcesar archivoProcesar = listaArchivoProcesar.get(i);
			pantalla.setTitle(archivoProcesar.getBanco().getNombre()+ " - " + archivoProcesar.getNombreArchivo());

			procesarArchivo(archivoProcesar);

			publish(1);
			i++;
		}
		pantalla.setTitle("Finalizado");
	}

	private void procesarArchivo(ArchivoProcesar archivoProcesar) {
		// TODO Auto-generated method stub

	}

	public Boolean getHuboErroresDeCajaCerrada() {
		return huboErroresDeCajaCerrada;
	}

}
