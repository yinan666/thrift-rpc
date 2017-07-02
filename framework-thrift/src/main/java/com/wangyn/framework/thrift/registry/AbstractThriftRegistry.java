package com.wangyn.framework.thrift.registry;

import com.wangyn.framework.thrift.config.spring.schema.def.ThriftApplicationDef;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftRegistryDef;

/**
 * 用于服务注册
 * @author wangyn
 *
 */
public abstract class AbstractThriftRegistry {
	
	//注册服务的定义，如地址等信息
	protected ThriftRegistryDef thriftRegistryDef;
	//主机ip
	protected String ip;
	//服务端口号
	protected int port;
	//当前应用的信息
	protected ThriftApplicationDef thriftApplicationDef;
	
	//必须要有服务的定义信息
	public AbstractThriftRegistry(String ip,int port,ThriftRegistryDef thriftRegistryDef,ThriftApplicationDef thriftApplicationDef){
		this.ip = ip;
		this.port = port;
		this.thriftRegistryDef = thriftRegistryDef;
		this.thriftApplicationDef = thriftApplicationDef;
	}
	
	/**
	 * 注册抽象方法
	 */
	public abstract void registe();

}
