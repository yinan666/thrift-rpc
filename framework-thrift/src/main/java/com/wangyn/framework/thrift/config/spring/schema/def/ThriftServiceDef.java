package com.wangyn.framework.thrift.config.spring.schema.def;

/**
 * 
 * @author wangyn
 * 用于设置thrift所暴露服务的相关参数定义
 *
 */
public class ThriftServiceDef {
	
	/**
	 * 服务实现的类实例，
	 */
	private Class<?> serviceImplClass;
	/**
	 * 服务版本号
	 */
	private String version;
	
	public Class<?> getServiceImplClass() {
		return serviceImplClass;
	}
	public void setServiceImplClass(Class<?> serviceImplClass) {
		this.serviceImplClass = serviceImplClass;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
}
