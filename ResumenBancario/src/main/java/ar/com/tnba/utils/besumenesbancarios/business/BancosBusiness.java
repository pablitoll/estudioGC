package ar.com.tnba.utils.besumenesbancarios.business;

public class BancosBusiness {

	public enum Bancos {
		BancoNacion("00011", "BancoNacion", "BANCO DE LA NACION ARGENTINA"), BancoFrances("00017", "BancoFrances", "BBVA BANCO FRANCES S.A.");

		private String codigo;
		private String directorio;
		private String nombre;

		private Bancos(String codigo, String directorio, String nombre) {
			this.codigo = codigo;
			this.directorio = directorio;
			this.nombre = nombre;
		}

		public String getCodigo() {
			return codigo;
		}

		public String getDirectorio() {
			return directorio;
		}

		public String getNombre() {
			return nombre;
		}
	}

	public static Bancos darBancoByDirectorio(String nombreDirectorio) {
		for (Bancos banco : Bancos.values()) {
			if (banco.getDirectorio().equalsIgnoreCase(nombreDirectorio)) {
				return banco;
			}
		}
		return null;
	}

	public static Bancos darBancoByNombre(String nombreBanco) {
		for (Bancos banco : Bancos.values()) {
			if (banco.getNombre().equalsIgnoreCase(nombreBanco)) {
				return banco;
			}
		}
		return null;
	}
}
