package ar.com.tool.rb.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.Timer;

import com.alee.extended.image.DisplayType;
import com.alee.extended.image.WebImage;

import ar.com.tool.rb.business.ArchivoDePropiedadesBusiness;

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
		container.setLayout(null);
		JPanel panel = new JPanel();
		panel.setBorder(new javax.swing.border.EtchedBorder());
		panel.setBackground(new Color(255, 255, 255));
		panel.setBounds(10, 10, 528, 308);
		panel.setLayout(null);
		container.add(panel);

		WebImage webImage3 = new WebImage(Toolkit.getDefaultToolkit().getImage(SplashScreen.class.getResource("/images/rpLogo.PNG")));
		webImage3.setDisplayType(DisplayType.fitComponent);
		webImage3.setImage(Toolkit.getDefaultToolkit().getImage(SplashScreen.class.getResource("/images/rpLogo.PNG")));
		webImage3.setBounds(0, 0, 525, 312);
		panel.add(webImage3);

		progressBar.setMaximum(50);
		progressBar.setBounds(10, 329, 528, 38);
		container.add(progressBar);
		loadProgressBar();
		setSize(548, 378);
		setLocationRelativeTo(null);
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
		timer1 = new Timer(7, al);
		timer1.start();
	}

	public static void main(String[] args) {
		execute = new SplashScreen();
	}
};