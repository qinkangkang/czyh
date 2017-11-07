package com.czyh.czyhweb.exception;

/**
 * 
 * @author <a href="mailto:ketayao@gmail.com">ketayao</a> Version 1.1.0
 * @since 2012-8-13 上午10:54:15
 */

public class NotExistedException extends ServiceException {

	private static final long serialVersionUID = 1L;

	public NotExistedException() {
		super();
	}

	public NotExistedException(String message) {
		super(message);
	}

	public NotExistedException(Throwable cause) {
		super(cause);
	}

	public NotExistedException(String message, Throwable cause) {
		super(message, cause);
	}
}