package com.wangyn.framework.thrift.config.spring.schema;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.wangyn.framework.thrift.client.proxy.ThriftBeanFactory;
import com.wangyn.framework.thrift.common.FrameCommonUtils;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftApplicationDef;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftRefApplicationDef;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftReferenceDef;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftRegistryDef;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftServiceDef;
import com.wangyn.framework.thrift.exception.ThriftFramworkException;

/**
 * 
 * @author wangyn
 * 解析thrift服务配置
 *
 */
public class ThriftBeanDefinitionParser implements BeanDefinitionParser {

	private Class<?> beanClass;
	
	
	public ThriftBeanDefinitionParser(Class<?> beanClass){
		this.beanClass = beanClass;
	}
	
	@Override
	public BeanDefinition parse(Element element, ParserContext context) {
		
		RootBeanDefinition def = new RootBeanDefinition();
		//设置bean class
		def.setBeanClass(beanClass);
		
		String id = "";
		
		//如果是thrift:service
		if(beanClass==ThriftServiceDef.class){
		    
		    //设置解析到的version属性
	  		String version = element.getAttribute("version");
	  		def.getPropertyValues().addPropertyValue("version", version);
	  	    
	  	   //设置解析到的class属性
	  		String clazz = element.getAttribute("class");
	  		Class<?> serviceImplClass = null;
	  		try {
	  			serviceImplClass = Class.forName(clazz);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
	  		def.getPropertyValues().addPropertyValue("serviceImplClass", serviceImplClass);
	  		//设置默认的bean id
	  		id = FrameCommonUtils.getServiceClazzSimpleName(serviceImplClass)+version;
	       
	    //如果是thrift:registry
		}else if(beanClass==ThriftRegistryDef.class){
			//设置解析到的address属性
			String address = element.getAttribute("address");
			//设置参数
		  	def.getPropertyValues().addPropertyValue("address", address);
		    
	  		//设置解析到的timeout属性
	  		String timeout = element.getAttribute("timeout");
	  		def.getPropertyValues().addPropertyValue("timeout", Integer.valueOf(timeout));
	  		//设置默认的bean id
	  		id = "thriftregistryDefaultId";
	  	
	  	//如果是application
		}else if(beanClass==ThriftApplicationDef.class){
			if(ThriftApplicationDef.isHasApplicationSet()){
				throw new ThriftFramworkException("<thrift:application> can only be set once！！！");
			}
			//设置解析到的name属性
			String name = element.getAttribute("name");
			//设置参数
		  	def.getPropertyValues().addPropertyValue("name", name);
		  	id="thriftapplicationDefaultId";
		  	ThriftApplicationDef.setHasApplicationSet(true);
		  	
		//如果是refapp
		}else if(beanClass==ThriftRefApplicationDef.class){
			//设置解析到的name属性
			String name = element.getAttribute("name");
			//设置参数
		  	def.getPropertyValues().addPropertyValue("name", name);
		    //设置解析到的maxconn属性
			String maxconn = element.getAttribute("maxconn");
			if(!StringUtils.isEmpty(maxconn)){
				//设置参数
			  	def.getPropertyValues().addPropertyValue("maxconn", Integer.valueOf(maxconn));
			}
		  	 //设置解析到的timeout属性
			String timeout = element.getAttribute("timeout");
			if(!StringUtils.isEmpty(timeout)){
				//设置参数
				def.getPropertyValues().addPropertyValue("timeout", Integer.valueOf(timeout));
			}
		  	id=name+"refappdfaultId";
		  	
		//如果是reference
		}else if(beanClass==ThriftReferenceDef.class){
			/**
			 * 设置解析到的id属性,该id不作为当前bean的id，而是作为依赖的服务的id
			 * 另外，此ThriftReferenceDef会作为客户端注册到zk上的信息依据
			 * **/
			id = element.getAttribute("id");
			//设置参数
		  	def.getPropertyValues().addPropertyValue("id", id);
		  	//设置解析到的interface属性
		  	String interfaceClass = element.getAttribute("interface");
		  	def.getPropertyValues().addPropertyValue("interfaceClass", interfaceClass);
		  	//设置解析到的version
		  	String version = element.getAttribute("version");
		  	def.getPropertyValues().addPropertyValue("version", version);
		  	//设置解析到的application
		  	String application = element.getAttribute("application");
		  	def.getPropertyValues().addPropertyValue("application", application);
		  	
		  	/*******************为thrift依赖创建一个代理对象***********/
			//获得依赖的代理对象
			GenericBeanDefinition bean = new GenericBeanDefinition();
			bean.setBeanClass(ThriftBeanFactory.class);
			//设置参数
			bean.getPropertyValues().add("interfaceClass", interfaceClass);
			bean.getPropertyValues().add("version", version);
			bean.getPropertyValues().add("application", application);
			BeanDefinitionHolder idHolder = new BeanDefinitionHolder(bean, id);
		    BeanDefinitionReaderUtils.registerBeanDefinition(idHolder,context.getRegistry());
		    /******************结束为thrift依赖创建一个代理对象*********************/
		   
		  	id = id + "DefaultReference";
		}
		
		/**
	        *  注册ID属性
	        *  id属性是一个默认的属性，可以不在xsd文件中描述，但是需要注册它，
	        *  否则将无法通过getBean方法获取标签定义的bean，也无法被其他bean引用。
	        *  这里以服务名称+verison作为其id
	        */
	       BeanDefinitionHolder idHolder = new BeanDefinitionHolder(def, id);
	       BeanDefinitionReaderUtils.registerBeanDefinition(idHolder,
	    		   context.getRegistry());
		
		return def;
	}
	
}
