package ar.com.tnba.utils.besumenesbancarios.business.bancos;

public class ExceptionSubTotal extends Exception {


	private static final long serialVersionUID = 1L;

	public ExceptionSubTotal() {
		super();
	}

	public ExceptionSubTotal(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ExceptionSubTotal(String message, Throwable cause) {
		super(message, cause);
	}

	public ExceptionSubTotal(String message) {
		super(message);
	}

	public ExceptionSubTotal(Throwable cause) {
		super(cause);
	}

}
