package ar.com.tnba.utils.besumenesbancarios.business.bancos;

public class BancosBusiness {

	public enum Bancos {
		BancoNacion("00011", "BancoNacion", "BANCO DE LA NACION ARGENTINA", 11, "AppOcrBNA"),
		BancoFrances("00017", "BancoFrances", "BBVA BANCO FRANCES S.A.", 17, "AppOcrBBVA"),
		ICBC("00017", "BancoICBC", "ICBC", 18, "AppOcrICBC");

		private String codigo;
		private String directorio;
		private String nombre;
		private Integer id;
		private String bancoBusiness;

		private Bancos(String codigo, String directorio, String nombre, Integer id, String bancoBusiness) {
			this.codigo = codigo;
			this.directorio = directorio;
			this.nombre = nombre;
			this.id = id;
			this.bancoBusiness = bancoBusiness;
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

		@SuppressWarnings("unchecked")
		public Class<BancosInterface> getBancoClass() throws ClassNotFoundException {
			return (Class<BancosInterface>) Class.forName("ar.com.tnba.utils.besumenesbancarios.business.bancos." + bancoBusiness);
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
