package ar.com.tnba.utils.besumenesbancarios.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.event.ActionListener;

import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.Timer;

import com.alee.extended.image.DisplayType;
import com.alee.extended.image.WebImage;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.tnba.utils.besumenesbancarios.business.ArchivoDePropiedadesBusiness;
import ar.com.tnba.utils.besumenesbancarios.business.ConstantesTool;

public class SplashScreen extends JWindow {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static boolean isRegistered;
	private static JProgressBar progressBar = new JProgressBar();
	private static SplashScreen execute;
	private static int count;
	private static Timer timer1;

	public SplashScreen() {

		Container container = getContentPane();
		getContentPane().setLayout(new BorderLayout(0, 0));
		progressBar.setPreferredSize(new Dimension(146, 30));

		progressBar.setMaximum(50);
		container.add(progressBar, BorderLayout.SOUTH);
		WebImage webImage3 = new WebImage(CommonUtils.loadImage(ConstantesTool.IMG_SPLASH));
		webImage3.setDisplayType(DisplayType.fitComponent);
		getContentPane().add(webImage3);
		setSize(430, 400);
		setLocationRelativeTo(null);
		loadProgressBar();
		setVisible(true);
	}

	private void loadProgressBar() {
		ActionListener al = new ActionListener() {

			public void actionPerformed(java.awt.event.ActionEvent evt) {
				count++;

				progressBar.setValue(count);

				if (count == 20) {
					// inicializo el jasper

				}

				if (count == 50) {
					try {

						ArchivoDePropiedadesBusiness.setPathToConfig(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (count == 100) {
					createFrame();
					execute.setVisible(false);// swapped this around with timer1.stop()
					timer1.stop();
				}
			}

			private void createFrame() throws HeadlessException {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						Main.Inicio();
					}
				});
			}
		};
		timer1 = new Timer(20, al);
		timer1.start();
	}

	public static void main(String[] args) {
		execute = new SplashScreen();
	}
};