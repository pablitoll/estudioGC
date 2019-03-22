package ar.com.tnba.utils.besumenesbancarios.business;

public class BancosBusiness {

	public enum Bancos {
		BancoNacion("00011", "BancoNacion", "BANCO DE LA NACION ARGENTINA", 11), BancoFrances("00017", "BancoFrances", "BBVA BANCO FRANCES S.A.", 17);

		private String codigo;
		private String directorio;
		private String nombre;
		private Integer id;

		private Bancos(String codigo, String directorio, String nombre, Integer id) {
			this.codigo = codigo;
			this.directorio = directorio;
			this.nombre = nombre;
			this.id = id;
		}

		public Integer getId() {
			return id;
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
