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
		NACION("00011", "BancoNacion", "BANCO DE LA NACION ARGENTINA", 11,	AppOcrBNA.class),
		FRANCES("00017", "BancoFrances", "BBVA BANCO FRANCES S.A.", 17, AppOcrBBVA.class),
		FRANCESAnt("00017", "BancoFrancesAnt2020", "BBVA BANCO FRANCES S.A. Anterior a 2020", 1799, AppOcrBBVAAnt2020.class),
		CREDICCOP("00191", "BancoCrediccop", "BANCO CREDICOOP COOPERATIVO LIMITADO", 191, AppOcrCredicoop.class),
		ICBC("00015", "BancoICBC", "INDUSTRIAL AND COMMERCIAL BANK OF CHINA (ICBC)", 15, AppOcrICBC.class), 
		PATAGONIA("00034", "BancoPatagonia", "BANCO PATAGONIA S.A.", 64, AppOcrPatagonia.class),
		RIO("00072", "BancoRio", "BANCO SANTANDER RIO S.A.", 72, AppOcrRio.class),
		HIPOTECARIO("00044", "BancoHipotecario", "BANCO HIPOTECARIO S.A.", 44, AppOcrHipotecario.class),
		CIUDAD("00029", "BancoCiudad", "BANCO DE LA CIUDAD DE BUENOS AIRES", 29, AppOcrCiudad.class),
		GALICIA("00007", "BancoGalicia", "BANCO DE GALICIA Y BUENOS AIRES S.A.U.", 7, AppOcrGalicia.class),
		PROVINCIA("00014", "BancoProvincia", "BANCO DE LA PROVINCIA DE BUENOS AIRES", 14, AppOcrProvincia.class),
		ITAU("00259", "BancoItau", "BANCO ITAU ARGENTINA S.A.", 259, AppOcrITAU.class);
		
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
