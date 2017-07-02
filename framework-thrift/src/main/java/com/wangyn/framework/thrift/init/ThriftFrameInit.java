package com.wangyn.framework.thrift.init;

import com.wangyn.framework.thrift.config.spring.schema.def.ThriftDefManager;
import com.wangyn.framework.thrift.registry.ThriftRegistry;

/**
 * 初始化thrift服务框架
 * @author wangyn
 *
 */
public class ThriftFrameInit {
	
	public static void init(){
		//初始化thrift标签定义信息
		ThriftDefManager.initThriftDefs();
		//将service服务或者reference依赖注册到zk上
		ThriftRegistry.registerServicesAndReference();
	}
}
