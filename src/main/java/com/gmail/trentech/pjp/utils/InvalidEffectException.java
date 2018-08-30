package com.gmail.trentech.pjp.utils;

public class InvalidEffectException extends Exception {

	private static final long serialVersionUID = 4275292748234069293L;

	public InvalidEffectException() {}

	public InvalidEffectException(String message) {
		super(message);
	}

    public InvalidEffectException(Throwable cause) {
        super (cause);
    }

    public InvalidEffectException(String message, Throwable cause) {
        super (message, cause);
    }
}
