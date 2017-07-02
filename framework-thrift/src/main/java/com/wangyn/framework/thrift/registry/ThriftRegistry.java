package com.wangyn.framework.thrift.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.access.BootstrapException;
import org.springframework.util.StringUtils;

import com.wangyn.framework.thrift.config.spring.schema.def.ThriftApplicationDef;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftDefManager;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftRegistryDef;
import com.wangyn.framework.thrift.registry.zk.ThriftClientReferenceRegistryWithZk;
import com.wangyn.framework.thrift.registry.zk.ThriftServiceRegistryWithZk;
import com.wangyn.framework.thrift.registry.zk.ZkUtil;
import com.wangyn.framework.thrift.server.AbstractThriftServer;
import com.wangyn.framework.thrift.server.OSUtil;

public class ThriftRegistry {
	
	Logger log  = LoggerFactory.getLogger(ThriftRegistry.class);
	
	/**
	 * 将服务和客户端依赖信息注册到zk上
	 */
	public static void registerServicesAndReference(){
		
		
		String localip = null;
		try {
			localip = OSUtil.getLocalIP();
		} catch (Exception e) {
			throw new BootstrapException("get local ip",e);
		}
		if(StringUtils.isEmpty(localip)){
			throw new BootstrapException("cannot get local ip,localip get null");
		}
		
		ThriftRegistryDef registryDef = ThriftDefManager.getThriftRegistryDef();
		ThriftApplicationDef applicatioDef = ThriftDefManager.getThriftApplicationDef();
		
		//注册service服务
		AbstractThriftRegistry servicezk = new ThriftServiceRegistryWithZk(ThriftDefManager.getThriftServices(), localip, AbstractThriftServer.getPort(), registryDef,applicatioDef);
		//注册service服务
		servicezk.registe();
		
		//注册reference依赖
		AbstractThriftRegistry referencezk = new ThriftClientReferenceRegistryWithZk(ThriftDefManager.getThriftReferences(),localip, AbstractThriftServer.getPort(), registryDef,applicatioDef);
		referencezk.registe();
		
		
		//启动连接监听，当重新建立连接时，将当前应用信息或当前应用锁暴露的服务信息注册到zk上。
		ZkUtil  zkUtil = ZkUtil.getUtil(registryDef.getAddress(), registryDef.getTimeout());
		zkUtil.startListeners();
		//项目启动时，需要执行的一些操作
		zkUtil.initConnectedExecutes();
	}

}
