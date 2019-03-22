package ar.com.tnba.utils.besumenesbancarios.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import com.alee.laf.scroll.WebScrollPane;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.rp.ui.common.Common;
import ar.com.rp.ui.componentes.JButtonRP;
import ar.com.tnba.utils.besumenesbancarios.business.ConstantesTool;

public class PantallaPrincipal extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField txtDescArticulo;
	private JButtonRP btnProcesar;
	private JButtonRP btnCerrar;
	private JButtonRP btnSeleccionarDirectorio;
	private JTree tree;
	private JButtonRP btnRecargar;
	private JPanel pnlCenter;
	private JPanel pnlRigth;

	public PantallaPrincipal() {
		super();
		setBounds(100, 100, 900, 600);
		setMinimumSize(new Dimension(870, 400));
		JPanel pnlBotones = new JPanel();
		setIconImage(CommonUtils.loadImage(ConstantesTool.IMG_SEARCH, 20, 20));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		FlowLayout fl_pnlBotones = (FlowLayout) pnlBotones.getLayout();
		fl_pnlBotones.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(pnlBotones, BorderLayout.SOUTH);
		setTitle("Convertidor Resumenes Bancarios a Excel");
		setLocationRelativeTo(null);

		btnProcesar = new JButtonRP("Procesar");
		btnProcesar.setIcon(Common.loadIconMenu(ConstantesTool.IMG_EXCEL));
		btnProcesar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				procesar();
			}
		});
		btnProcesar.setFont(Common.getStandarFont());
		pnlBotones.add(btnProcesar);

		btnCerrar = new JButtonRP("Cerrar");
		btnCerrar.setIcon(Common.loadIconMenu("com/alee/laf/filechooser/icons/remove.png"));
		btnCerrar.setFont(Common.getStandarFont());
		btnCerrar.setMnemonic(KeyEvent.VK_ESCAPE);
		btnCerrar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cerrar();
			}
		});
		pnlBotones.add(btnCerrar);

		JPanel pnlTop = new JPanel();
		getContentPane().add(pnlTop, BorderLayout.NORTH);
		pnlTop.setLayout(new BorderLayout(0, 0));

		pnlCenter = new JPanel();
		FlowLayout flowLayout = (FlowLayout) pnlCenter.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		pnlTop.add(pnlCenter, BorderLayout.CENTER);

		JLabel lblNombreDelArticulo = new JLabel("Directorio de Trabajo");
		pnlCenter.add(lblNombreDelArticulo);
		lblNombreDelArticulo.setFont(Common.getStandarFont());

		txtDescArticulo = new JTextField();
		pnlCenter.add(txtDescArticulo);
		txtDescArticulo.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				perdioFoco();
			}
		});
		txtDescArticulo.setFont(Common.getStandarFont());
		txtDescArticulo.setColumns(25);

		btnSeleccionarDirectorio = new JButtonRP("Seleccionar Directorio");
		pnlCenter.add(btnSeleccionarDirectorio);
		btnSeleccionarDirectorio.setIcon(Common.loadIconMenu(ConstantesTool.IMG_SEARCH));
		btnSeleccionarDirectorio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				seleccionar();
			}
		});

		btnSeleccionarDirectorio.setFont(Common.getStandarFont());

		btnRecargar = new JButtonRP("Recargar");
		btnRecargar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				recargar();
			}
		});
		pnlCenter.add(btnRecargar);
		btnRecargar.setIcon(Common.loadIconMenu(ConstantesTool.IMG_RECARGAR));

		pnlRigth = new JPanel();
		pnlTop.add(pnlRigth, BorderLayout.EAST);
		String[] texto = { "Crear", "Directorios" };
		JButtonRP btnCrearDirectorios = new JButtonRP(CommonUtils.SetHTML(texto, null, null, "center", true));
		btnCrearDirectorios.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				crearEstructura();
			}
		});
		pnlRigth.add(btnCrearDirectorios);
		btnCrearDirectorios.setFont(Common.getStandarFont(10));

		tree = new JTree(new DefaultMutableTreeNode("Archivos a Procesar"));

		WebScrollPane scrollPane = new WebScrollPane(tree);
		getContentPane().add(scrollPane, BorderLayout.CENTER);

	}

	protected void crearEstructura() {
		PantallaPrincipalControlador.crearEstructura(txtDescArticulo, tree);
	}

	protected void recargar() {
		PantallaPrincipalControlador.cargarDirecorio(txtDescArticulo, tree);
	}

	private void perdioFoco() {
		PantallaPrincipalControlador.cargarDirecorio(txtDescArticulo, tree);
	}

	private void seleccionar() {
		PantallaPrincipalControlador.seleccionarDirectorio(txtDescArticulo, tree, this);
	}

	private void procesar() {
		PantallaPrincipalControlador.procesar(tree, this);
	}

	public void iniciar() {
		PantallaPrincipalControlador.cargaPantalla(txtDescArticulo, tree, this);
		setVisible(true);
	}

	// public boolean presionoTecla(KeyEvent ke) {
	// boolean retorno = super.presionoTecla(ke);
	// if (!retorno) {
	// if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
	// if (txtDescArticulo.hasFocus()) {
	// retorno = true;
	// btnSeleccionarDirectorio.doClick();
	// }
	// }
	// }
	// return retorno;
	// }

	public void cerrar() {
		System.exit(0);
	}
}
