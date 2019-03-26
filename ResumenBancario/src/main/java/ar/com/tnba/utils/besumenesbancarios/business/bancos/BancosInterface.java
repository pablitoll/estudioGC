package ar.com.tnba.utils.besumenesbancarios.business.bancos;

import java.io.File;

public interface BancosInterface {

	public String procesarArchivo(String strOcr, File archivo, Integer pagina) throws Exception;

	public String getOCR(File archivoOCR) throws Exception;
}
