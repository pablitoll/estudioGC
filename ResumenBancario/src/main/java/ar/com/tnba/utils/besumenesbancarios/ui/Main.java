package ar.com.tnba.utils.besumenesbancarios.ui;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.FileReader;

import javax.swing.JOptionPane;

import ar.com.rp.ui.main.MainFramework;
import ar.com.tnba.utils.besumenesbancarios.business.ArchivoDePropiedadesBusiness;
import ar.com.tnba.utils.besumenesbancarios.business.LogBusiness;

public class Main extends MainFramework {
	/**
	 * Launch the application.
	 */

	private static final int PORT = 12395; // random large port number

	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				Inicio();
			}
		});
	}

	public static void Inicio() {
		try {

			// Inicializo el font
			inicializarFont();
			ArchivoDePropiedadesBusiness.setPathToConfig(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());

			// Cargo un log
			splashMsg("Cargando archivo de Log");
			LogBusiness.inicializarLogManager();

			if (!isValidVersion()) {
				Dialog.showMessageDialog("No esta habilitada esta maquina para correr la aplicacion", "Version no habilitada", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}

			if (isRunning(PORT)) {
				String[] option = { "Si", "No" };
				Object confirm = Dialog.showConfirmDialogObject("<html>Ya hay una instancia de la aplicacion ejecutandose <br>Desea Abrir otra instancia?</html>",
						"Nueva Instancia", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, option, option[1]);
				if (confirm == option[1]) {
					System.exit(1);
				}
			}

			// Cargo pantalla pricipal
			splashMsg("Cargando Pantalla Principal");

			PantallaPrincipal pantallaPrincipal = new PantallaPrincipal();
			pantallaPrincipal.iniciar();

		} catch (Exception e) {
			ManejoDeError.showError(e, "Error al iniciar");
			System.exit(-1);
		}
	}

	private static boolean isValidVersion() {
		try {
			FileReader fr = new FileReader("c:\\Users\\Public\\USER.RB");
			BufferedReader br = new BufferedReader(fr);

			String codigo = br.readLine();

			br.close();
			fr.close();

			return codigo.equals("1277");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}
}
