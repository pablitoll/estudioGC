package ar.com.tnba.utils.besumenesbancarios.workers;

import java.io.File;

import ar.com.tnba.utils.besumenesbancarios.business.BancosBusiness.Bancos;

public class ArchivoProcesar {

	private Bancos banco;
	private String nombreArchivo;
	private File archivo;

	public ArchivoProcesar(Bancos banco, String nombreArchivo, File archivo) {
		super();
		this.banco = banco;
		this.nombreArchivo = nombreArchivo;
		this.archivo = archivo;
	}

	public File getArchivo() {
		return archivo;
	}

	public Bancos getBanco() {
		return banco;
	}

	public String getNombreArchivo() {
		return nombreArchivo;
	}

	@Override
	public String toString() {
		return nombreArchivo;
	}
}
