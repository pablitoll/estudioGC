package ar.com.tool.rb.workers;

import ar.com.tool.rb.business.BancosBusiness.Bancos;

public class ArchivoProcesar {

	private Bancos banco;
	private String nombreArchivo;

	public ArchivoProcesar(Bancos banco, String nombreArchivo) {
		super();
		this.banco = banco;
		this.nombreArchivo = nombreArchivo;
	}

	public Bancos getBanco() {
		return banco;
	}

	public String getNombreArchivo() {
		return nombreArchivo;
	}

}
