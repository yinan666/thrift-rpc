package com.wangyn.framework.thrift.config.spring.schema.def;

/**
 * 
 * @author wangyn
 * 用于设置thrift所依赖的zk相关的信息
 *
 */
public class ThriftRegistryDef {
	
	/**
	 * zk的地址
	 */
	private String address;
	
	/**
	 * 会话超时时间
	 */
	private int timeout;

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}


	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	
	
	
}
