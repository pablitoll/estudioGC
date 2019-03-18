package ar.com.smsv.csc.cliente.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.rp.ui.common.Common;
import ar.com.rp.ui.componentes.JButtonRP;
import ar.com.tool.rb.ui.PantallaPrincipal;
import ar.com.tool.rb.workers.WorkerBarraDeProgresoBase;

public class BarraDeProgreso extends JDialog {

	public enum estados {
		OK, ERROR, CANCEL
	};

	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();
	private JButtonRP btnCancelar = new JButtonRP("Cancelar");
	private JButtonRP btnOK = new JButtonRP("OK");
	private JProgressBar progressBar = new JProgressBar();
	private JLabel lblLeyenda = new JLabel("Inicializando..");
	private List<WorkerBarraDeProgresoBase> listaWorker = null;
	private boolean mostrarBotonOK = true;
	private estados estadoInterno = null;

	private PantallaPrincipal pantallaPrincipal;

	/**
	 * Launch the application.
	 */

	/**
	 * Create the dialog.
	 */
	public BarraDeProgreso(String titulo, PantallaPrincipal pantallaPrincipal) {
		setBounds(getX(), getY(), 511, 152);

		this.pantallaPrincipal = pantallaPrincipal;
		setResizable(false);
		setModal(true);
		setTitle(titulo);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		progressBar.setPreferredSize(new Dimension(146, 40));
		progressBar.setMaximumSize(new Dimension(32767, 40));
		progressBar.setMinimumSize(new Dimension(10, 40));
		progressBar.setMaximum(0);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			progressBar.setStringPainted(true);
			contentPanel.add(progressBar, BorderLayout.NORTH);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				btnOK.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						if (isWorkersDone() || isCancelo()) {
							setVisible(false);
							dispose();
						}
					}
				});
				{
					btnCancelar.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							if (listaWorker != null) {
								// Cancelo todos los workers
								cancelarWorkers();
							}
						}
					});
					buttonPane.add(btnCancelar);
				}
				buttonPane.add(btnOK);
				getRootPane().setDefaultButton(btnOK);
			}
		}
		btnCancelar.setFont(Common.getStandarFont());
		btnOK.setFont(Common.getStandarFont());
		lblLeyenda.setFont(Common.getStandarFont());
		progressBar.setFont(Common.getStandarFont());
		{
			contentPanel.add(lblLeyenda, BorderLayout.CENTER);
		}

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				if (btnCancelar.isVisible()) {
					btnCancelar.doClick();
				} else {
					btnOK.doClick();
				}
			}
		});
	}

	public void setWorker(WorkerBarraDeProgresoBase worker) {
		List<WorkerBarraDeProgresoBase> listAux = new ArrayList<WorkerBarraDeProgresoBase>();
		listAux.add(worker);
		this.listaWorker = listAux;
	}

	public void setWorker(List<WorkerBarraDeProgresoBase> listaWorker) {
		this.listaWorker = listaWorker;
	}

	public void inicializar() {
		this.setLocationRelativeTo(pantallaPrincipal);
		setBotones(true);
		if (listaWorker != null) {
			executeWorkers();
		}
		this.setVisible(true);
	}

	public boolean terminoBien() {
		return !isWorkersCancelar() && !isWorkersError();
	}

	private void setEstadoInterno(estados estado) {
		estadoInterno = estado;
		// primero muestro los carteles
		if (estado == estados.CANCEL) {
			lblLeyenda.setText("Se cancelo el proceso. Se preocesaron " + progressBar.getValue() + " registros.");
		}

		if (estado == estados.OK) {
			lblLeyenda.setText("Termino el procesamiento. Total " + progressBar.getMaximum() + " registros.");
		}

		if (estado == estados.ERROR) {
			lblLeyenda.setText("Termino con error. Se procesaron " + progressBar.getValue() + " registros.");
		}

		// hago el seteo
		setBotones(false);

		if (estado == estados.ERROR) {

			List<String> listaError = new ArrayList<String>();
			listaError.add(lblLeyenda.getText());
			int i = 1;
			for (Exception er : getError()) {
				listaError.add(String.format("Error %s: %s", i, er.getMessage()));
				i++;
			}

			String[] lineas = new String[listaError.size()];
			listaError.toArray(lineas);
			this.setBounds(getX(), getY(), getWidth(), getHeight() + (30 * (i - 1)));
			lblLeyenda.setText(CommonUtils.SetHTML(lineas));
		}
	}

	private void setBotones(Boolean mostarCancelar) {
		btnCancelar.setVisible(mostarCancelar);
		btnOK.setVisible(!mostarCancelar);
		if (btnOK.isVisible() && !mostrarBotonOK && !isWorkersError()) {
			btnOK.doClick(); // en vez de mostrarlo lo ejecuto y por ende cierro la pantalla
		}
	}

	public boolean isMostrarBotonOK() {
		return mostrarBotonOK;
	}

	public void setMostrarBotonOK(boolean mostrarBotonOK) {
		this.mostrarBotonOK = mostrarBotonOK;
	}

	public List<Exception> getError() {
		return getWorkersError();
	}

	public void setMax(Integer max) {
		progressBar.setMaximum(max + progressBar.getMaximum());
		lblLeyenda.setText("Procesando... (" + 0 + " de " + progressBar.getMaximum() + ")");
	}

	public void procesado(Integer procesados) {
		int nuevoProcesado = procesados + progressBar.getValue();
		progressBar.setValue(nuevoProcesado);
		lblLeyenda.setText("Procesando... (" + nuevoProcesado + " de " + progressBar.getMaximum() + ")");
	}

	public void setMensaje(String msg) {
		lblLeyenda.setText(msg);
	}

	// Todos estos metodos se podrian pasar a una calse que maneje lista de
	// workers...
	public void setEstado() {
		// Solo seteo el estado final si totos los worker terminaron
		if (isWorkersDone()) {

			if (isWorkersError()) { // si alguno fallo muestro el error
				setEstadoInterno(estados.ERROR);
			} else {
				if (isWorkersCancelar()) { // si alguno fue cancelado
					setEstadoInterno(estados.CANCEL);
				} else { // el estado final es OK
					setEstadoInterno(estados.OK);
				}
			}

		}
	}

	private List<Exception> getWorkersError() {
		List<Exception> retorno = new ArrayList<Exception>();
		for (WorkerBarraDeProgresoBase worker : listaWorker) {
			if (worker.isError()) { // Si uno no termino ya es false
				retorno.add(worker.getError());
			}
		}

		return retorno;
	}

	private boolean isWorkersDone() {
		for (WorkerBarraDeProgresoBase worker : listaWorker) {
			if (!worker.isDone()) { // Si uno no termino ya es false
				return false;
			}
		}
		return true;
	}

	private boolean isWorkersError() {
		for (WorkerBarraDeProgresoBase worker : listaWorker) {
			if (worker.isError()) { //
				return true;
			}
		}
		return false;
	}

	private void cancelarWorkers() {
		for (WorkerBarraDeProgresoBase worker : listaWorker) {
			worker.cancelar();
		}
		setEstadoInterno(estados.CANCEL);
	}

	private void executeWorkers() {
		for (WorkerBarraDeProgresoBase worker : listaWorker) {
			worker.execute();
		}
	}

	private boolean isWorkersCancelar() {
		for (WorkerBarraDeProgresoBase worker : listaWorker) {
			if (!worker.getCancelar()) {
				return false;
			}
		}
		return true;
	}

	public boolean isCancelo() {
		return (estadoInterno != null) && (estadoInterno == estados.CANCEL);
	}
}
