package ar.com.tnba.utils.besumenesbancarios.business.bancos;

public class BancosBusiness {
	// BancoNacion("00011", "BancoNacion", "BANCO DE LA NACION ARGENTINA", 11,
	// "AppOcrBNA"),
	// BancoFrances("00017", "BancoFrances", "BBVA BANCO FRANCES S.A.",
	// 17,"AppOcrBBVA"),
	// ICBC("00015", "BancoICBC", "INDUSTRIAL AND COMMERCIAL BANK OF CHINA (ICBC)",
	// 15, "AppOcrICBC"),
	// CREDICOOP("00191", "BancoCrediccop", "BANCO CREDICOOP COOPERATIVO LIMITADO",
	// 191, "AppOcrCredicoop"),
	// PATAGONIA("00034", "BancoPatagonia", "BANCO PATAGONIA S.A.", 64,
	// "AppOcrPatagonia");
	//
	public enum Bancos {
		PATAGONIA("00034", "BancoPatagonia", "BANCO PATAGONIA S.A.", 64, AppOcrPatagonia.class);

		private String codigo;
		private String directorio;
		private String nombre;
		private Integer id;
		private Class<BaseBancos> bancoBusiness;

		@SuppressWarnings("unchecked")
		private Bancos(String codigo, String directorio, String nombre, Integer id, Class<?> bancoBusiness) {
			this.codigo = codigo;
			this.directorio = directorio;
			this.nombre = nombre;
			this.id = id;
			this.bancoBusiness = (Class<BaseBancos>) bancoBusiness;
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

		public Class<BaseBancos> getBancoClass() throws ClassNotFoundException {
			return bancoBusiness;
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
