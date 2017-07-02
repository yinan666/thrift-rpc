package com.wangyn.framework.thrift.config.spring.schema.def;
/**
 * thrift服务依赖相关定义
 * @author wangyn
 *
 */
public class ThriftReferenceDef {
	/**
	 * bean的id
	 */
	private String id;
	/**
	 * 依赖服务接口的全类名
	 */
	private String interfaceClass;
	/**
	 * 服务版本号
	 */
	private String version;
	/**
	 * 服务所在应用的应用名称
	 */
	private String application;
	public String getInterfaceClass() {
		return interfaceClass;
	}
	public void setInterfaceClass(String interfaceClass) {
		this.interfaceClass = interfaceClass;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
}
