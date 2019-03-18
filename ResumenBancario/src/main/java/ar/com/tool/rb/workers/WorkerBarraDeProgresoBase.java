package ar.com.tool.rb.workers;

import java.util.List;

import javax.swing.SwingWorker;

import ar.com.smsv.csc.cliente.ui.BarraDeProgreso;
import ar.com.tool.rb.business.LogBusiness;

public abstract class WorkerBarraDeProgresoBase extends SwingWorker<Void, Integer> {

	protected Boolean cancelar = false;
	protected Integer procesados = 0;
	protected BarraDeProgreso pantalla;
	private Exception error = null;
	private String titulo = "S/D";
	private String nombreHilo = null;

	protected abstract void ejecutarWorker() throws Exception;

	public WorkerBarraDeProgresoBase(BarraDeProgreso pantalla, String titulo) {
		this.pantalla = pantalla;
		this.titulo = titulo;
	}

	@Override
	protected Void doInBackground() throws Exception {
		Thread.currentThread().setName("WORKER -" + this.titulo);
		nombreHilo = Thread.currentThread().getName();
		ejecutarWorker();
		return null;
	}

	@Override
	protected void process(List<Integer> chunks) {
		int subTotal = 0;
		for (int i = 0; i < chunks.size(); i++) {
			int cant = chunks.get(i);
			subTotal += cant;
			procesados += cant;
		}

		pantalla.procesado(subTotal);
		System.out.println("worker: " + titulo + " hilo: " + getNombreHilo() + " PROCESO: " + procesados);
	}

	@Override
	protected void done() {
		if (cancelar) {
			System.out.println("worker: " + titulo + " hilo: " + getNombreHilo() + " - CANCELADO");
			// pantalla.setEstado();
		} else {
			try {
				get();
				System.out.println("worker: " + titulo + " hilo: " + getNombreHilo() + " - TERMINO OK");
				pantalla.setEstado();
			} catch (Exception ex) {
				System.out.println("worker: " + titulo + " hilo: " + getNombreHilo() + " - TERMINO ERROR");

				try {
					LogBusiness.logearError("worker: " + titulo + " hilo: " + getNombreHilo() + " - TERMINO ERROR", ex);
				} catch (Exception e) {
					e.printStackTrace();
				}

				error = ex;
				pantalla.setEstado();
				ex.printStackTrace();
			}
		}
	}

	private String getNombreHilo() {
		if (nombreHilo == null) {
			nombreHilo = Thread.currentThread().getName();
		}
		return nombreHilo;
	}

	public void cancelar() {
		cancelar = true;
	}

	public Boolean isError() {
		return error != null;
	}

	public Exception getError() {
		return error;
	}

	public void setMax(Integer max) {
		pantalla.setMax(max);
	}

	public Boolean getCancelar() {
		return cancelar;
	}

}
