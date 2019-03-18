package ar.com.tool.rb.ui;

import java.awt.Image;

import javax.swing.Icon;

import ar.com.rp.rpcutils.CommonUtils;
import ar.com.rp.ui.pantalla.DialogMessage;
import ar.com.tool.rb.business.ConstantesTool;

public class Dialog {

	private static Image iconoAplicacion = null;

	public static void showMessageDialog(String message) {
		try {
			DialogMessage.showMessageDialog(message, getIconoAplicacion(), null, null);
		} catch (Exception e) {
			ManejoDeError.showError(e, "Error al mostrar dialog");
		}
	}

	public static void showMessageDialog(Object message, String title, int messageType) {
		try {
			DialogMessage.showMessageDialog(message, title, messageType, getIconoAplicacion(), null, null);
		} catch (Exception e) {
			ManejoDeError.showError(e, "Error al mostrar dialog");
		}
	}

	public static void showMessageDialog(Object dummy, Object message, String title, int messageType) {
		try {
			DialogMessage.showMessageDialog(message, title, messageType, getIconoAplicacion(), null, null);
		} catch (Exception e) {
			ManejoDeError.showError(e, "Error al mostrar dialog");
		}
	}

	public static Integer showConfirmDialog(Object message, String title, int optionType, int messageType, Icon icon, Object[] options, Object initialValue) {
		try {
			return DialogMessage.showConfirmDialog(message, title, optionType, messageType, icon, options, initialValue, getIconoAplicacion(), null, null);
		} catch (Exception e) {
			ManejoDeError.showError(e, "Error al mostrar dialog");
			return 0;
		}
	}

	public static Object showConfirmDialogObject(Object message, String title, int optionType, int messageType, Icon icon, Object[] options, Object initialValue) {
		try {
			return DialogMessage.showConfirmDialogObject(message, title, optionType, messageType, icon, options, initialValue, getIconoAplicacion(), null, null);
		} catch (Exception e) {
			ManejoDeError.showError(e, "Error al mostrar dialog");
			return 0;
		}
	}

	private static Image getIconoAplicacion() {
		if (iconoAplicacion == null) {
			iconoAplicacion = CommonUtils.loadImage(ConstantesTool.IMG_EXCEL, 80, 80);
		}

		return iconoAplicacion;
	}
}
