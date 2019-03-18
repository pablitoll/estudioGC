package ar.com.tool.rb.ui;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import ar.com.smsv.csc.cliente.ui.BarraDeProgreso;
import ar.com.tool.rb.business.ArchivoDePropiedadesBusiness;
import ar.com.tool.rb.business.BancosBusiness;
import ar.com.tool.rb.business.BancosBusiness.Bancos;
import ar.com.tool.rb.workers.ArchivoProcesar;
import ar.com.tool.rb.workers.WorkerBarraDeProgresoProcesar;

public class PantallaPrincipalControlador {
	/*
	 * Estructura: Root BancoNacion archivo1 archivo2 BancoRio archivo1
	 */
	public static void seleccionarDirectorio(JTextField txtDescArticulo, JTree tree, Component parent) {
		JFileChooser directoryChosser = new JFileChooser(txtDescArticulo.getText());
		directoryChosser.setDialogTitle("Seleccione Directorio de TRabajo");
		directoryChosser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		directoryChosser.setAcceptAllFileFilterUsed(false);
		if (directoryChosser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			if (directoryChosser.getSelectedFile().isDirectory()) {
				txtDescArticulo.setText(directoryChosser.getSelectedFile().getAbsolutePath());
			} else {
				txtDescArticulo.setText(directoryChosser.getCurrentDirectory().getAbsolutePath());
			}
			cargarDirecorio(txtDescArticulo, tree);
		}
	}

	private static void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
		for (int i = startingIndex; i < rowCount; ++i) {
			tree.expandRow(i);
		}

		if (tree.getRowCount() != rowCount) {
			expandAllNodes(tree, rowCount, tree.getRowCount());
		}
	}

	private static void cargaHijos(DefaultMutableTreeNode padre, String directorioDeTrabajo) {
		File rootFile = new File(directorioDeTrabajo);
		File[] listaFile = rootFile.listFiles();

		if (listaFile == null) {
			return;
		}

		for (File f : listaFile) {
			if (f.isFile()) {
				String nombreArchivo = f.getName();
				if (nombreArchivo.substring(nombreArchivo.lastIndexOf(".")).equalsIgnoreCase(".pdf")) {
					DefaultMutableTreeNode hijo = new DefaultMutableTreeNode(nombreArchivo);
					padre.add(hijo);
				}
			}
		}
	}

	public static void procesar(JTree tree, PantallaPrincipal pantallaPrincipal) {
		try {

			List<ArchivoProcesar> listaArchivoProcesar = new ArrayList<ArchivoProcesar>();

			DefaultTreeModel modelTree = (DefaultTreeModel) tree.getModel();
			DefaultMutableTreeNode rootTree = (DefaultMutableTreeNode) modelTree.getRoot();

			for (int i = 0; i < rootTree.getChildCount(); i++) {
				DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) rootTree.getChildAt(i);
				Bancos banco = BancosBusiness.darBancoByNombre(nodo.toString());
				agregarNodoALista(listaArchivoProcesar, nodo, banco);
			}

			if (listaArchivoProcesar.size() > 0) {
				BarraDeProgreso pantalla = new BarraDeProgreso("Procesando...", pantallaPrincipal);
				WorkerBarraDeProgresoProcesar workerBarraDeProgresoProcesar = new WorkerBarraDeProgresoProcesar(pantalla, listaArchivoProcesar);

				pantalla.setWorker(workerBarraDeProgresoProcesar);
				pantalla.inicializar();
			}
		} catch (Exception e) {
			ManejoDeError.showError(e, "Error al procesar archivos");
		}
	}

	private static void agregarNodoALista(List<ArchivoProcesar> listaArchivoProcesar, DefaultMutableTreeNode nodoPadre, Bancos banco) {
		for (int i = 0; i < nodoPadre.getChildCount(); i++) {
			DefaultMutableTreeNode hijo = (DefaultMutableTreeNode) nodoPadre.getChildAt(i);
			listaArchivoProcesar.add(new ArchivoProcesar(banco, hijo.toString()));
		}

	}

	public static void cargaPantalla(JTextField txtDescArticulo, JTree tree, PantallaPrincipal pantallaPrincipal) {
		try {
			txtDescArticulo.setText(ArchivoDePropiedadesBusiness.getDirectorioDeTrabajo());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cargarDirecorio(txtDescArticulo, tree);

	}

	public static void cargarDirecorio(JTextField txtDescArticulo, JTree tree) {
		DefaultTreeModel modelTree = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode rootTree = (DefaultMutableTreeNode) modelTree.getRoot();
		rootTree.removeAllChildren();

		File rootFile = new File(txtDescArticulo.getText());
		File[] listaFile = rootFile.listFiles();

		if (listaFile != null) {
			for (File f : listaFile) {
				if (f.isDirectory()) {
					Bancos banco = BancosBusiness.darBancoByDirectorio(f.getName());
					if (banco != null) {
						DefaultMutableTreeNode padre = new DefaultMutableTreeNode(banco.getNombre());
						cargaHijos(padre, f.getAbsolutePath());
						rootTree.add(padre);
					}
				}
			}
		}
		recargarModelo(tree);
	}

	private static void recargarModelo(JTree tree) {	
		DefaultTreeModel modelTree = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode rootTree = (DefaultMutableTreeNode) modelTree.getRoot();
		
		modelTree.reload(rootTree);
		expandAllNodes(tree, 0, tree.getRowCount());
	}

	public static void crearEstructura(JTextField txtDescArticulo, JTree tree) {
		if (Dialog.showConfirmDialog("¿Esta Seguro que quiere Crear la estructura de Trabajo?\nSe crearea en " + txtDescArticulo.getText() + File.separator,
				"Creacion de Estructura de Trabajo", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.NO_OPTION) == JOptionPane.YES_OPTION) {

			for (Bancos banco : Bancos.values()) {
				File carpeta = new File(txtDescArticulo.getText() + File.separator + banco.getDirectorio());
				if (!carpeta.exists()) {
					carpeta.mkdirs();
				}
			}
			cargarDirecorio(txtDescArticulo, tree);
		}
	}

}
