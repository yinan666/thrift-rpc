package com.wangyn.framework.thrift.config.spring.schema;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import com.wangyn.framework.thrift.config.spring.schema.def.ThriftApplicationDef;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftRefApplicationDef;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftReferenceDef;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftRegistryDef;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftServiceDef;

/**
 * 
 * @author wangyn
 * 由sping框架的调用入口。是自定义xml解析的入口
 *
 */
public class ThriftNamespaceHandlerSupport extends NamespaceHandlerSupport {

	@Override
	public void init() {
		//第一参数指定从哪个元素开始解析，这里指定service，也就是 thrift:service
		registerBeanDefinitionParser("service",new ThriftBeanDefinitionParser(ThriftServiceDef.class));
		//注册中心
		registerBeanDefinitionParser("registry",new ThriftBeanDefinitionParser(ThriftRegistryDef.class));
		//项目应用
		registerBeanDefinitionParser("application",new ThriftBeanDefinitionParser(ThriftApplicationDef.class));
		//依赖的应用
		registerBeanDefinitionParser("refapp",new ThriftBeanDefinitionParser(ThriftRefApplicationDef.class));
		//客户端依赖的服务定义
		registerBeanDefinitionParser("reference",new ThriftBeanDefinitionParser(ThriftReferenceDef.class));
	}
	
}
