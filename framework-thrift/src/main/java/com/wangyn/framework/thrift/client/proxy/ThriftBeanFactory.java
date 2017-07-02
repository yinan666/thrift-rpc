package com.wangyn.framework.thrift.client.proxy;

import org.apache.thrift.protocol.TProtocol;
import org.springframework.beans.factory.FactoryBean;

import net.sf.cglib.proxy.Enhancer;

/**
 * 用于创建特殊性的thrift客户端bean
 * @author wangyn
 *
 */
public class ThriftBeanFactory<T> implements FactoryBean<T>{

	/**
	 * thrift服务端定义类，如com.wangyn.user.rpc.thrift.service.UserRpcService
	 */
	private String interfaceClass;
	//服务版本号
	private String version;
	//服务所在应用
	private String application;
	
	public String getInterfaceClass() {
		return interfaceClass;
	}

	public void setInterfaceClass(String interfaceClass) {
		this.interfaceClass = interfaceClass;
	}

	@Override
	public T getObject() throws Exception {
		 Enhancer enhancer = new Enhancer();
		 enhancer.setCallback(new ThriftCglibMethodInterceptor(version,interfaceClass,application));
		 //由于是cglib，所以要继承类？ 测试第二句
		 enhancer.setSuperclass(Class.forName(interfaceClass+"$Client"));
		 //interfaceClass+"$Client" 没有空的构造函数，捡了其中一个有参数的
		 Object obj = enhancer.create(new Class[]{TProtocol.class},new Object[]{null});
		 return (T)obj;
	}

	@Override
	public Class<?> getObjectType() {
		try {
			return Class.forName(interfaceClass+"$Iface");
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	@Override
	public boolean isSingleton() {
		return false;
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
	

}
