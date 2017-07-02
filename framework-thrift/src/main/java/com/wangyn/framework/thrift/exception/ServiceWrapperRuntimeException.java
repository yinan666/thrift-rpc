package com.wangyn.framework.thrift.exception;

/**
 * 用于封装那些需要进行try catch的异常
 * @author wangyn
 *
 */
public class ServiceWrapperRuntimeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ServiceWrapperRuntimeException() {
		// TODO Auto-generated constructor stub
	}

	public ServiceWrapperRuntimeException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public ServiceWrapperRuntimeException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public ServiceWrapperRuntimeException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public ServiceWrapperRuntimeException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
		// TODO Auto-generated constructor stub
	}

}
