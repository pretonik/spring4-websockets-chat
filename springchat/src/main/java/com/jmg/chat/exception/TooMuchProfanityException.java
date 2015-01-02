package com.jmg.chat.exception;

/**
 * 
 * @author Julio Muñoz
 */
public class TooMuchProfanityException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -363619815802313137L;

	public TooMuchProfanityException(String message) {
		super(message);
	}
}
