package ar.com.tool.rb.ui;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ar.com.rp.ui.error.ErrorManager;
import ar.com.tool.rb.business.LogBusiness;

public class ManejoDeError {


	public static void showError(Exception error, String titulo) {
		showError(error, titulo, "");
	}

	public static void showError(Exception error, String titulo, String mensaje) {
		error.printStackTrace();

		boolean mostrarError = true;

		
		if (mostrarError && SwingUtilities.isEventDispatchThread()) {
			try {
				ErrorManager.showError(null, titulo, mensaje, error);
			} catch (Exception e) {
				e.printStackTrace();
				Dialog.showMessageDialog("Hubo un error al visualizar el msg de error.\nError original : " + error.getMessage(), "Error al visualizar mensage de Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}

		// guardo en el log el error.
		try {
			LogBusiness.logearError(error);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			LogBusiness.forzarEscrituraLogs();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


}
