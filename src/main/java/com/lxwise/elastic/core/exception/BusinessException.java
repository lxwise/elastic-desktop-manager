package com.lxwise.elastic.core.exception;

/**
 * @author lstar
 * @create 2025-02
 * @description: 自定义业务异常
 */
public class BusinessException extends RuntimeException{

	public BusinessException() {
	}

	public BusinessException(String message) {
		super(message);
	}

	public BusinessException(String message, Throwable cause) {
		super(message, cause);
	}

	public BusinessException(Throwable cause) {
		super(cause);
	}

	public BusinessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
