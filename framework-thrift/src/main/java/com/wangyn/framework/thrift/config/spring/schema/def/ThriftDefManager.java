package com.wangyn.framework.thrift.config.spring.schema.def;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.access.BootstrapException;
import org.springframework.context.ApplicationContext;

import com.wangyn.framework.thrift.common.FrameCommonUtils;

/**
 * 用于管理thrift标签相关的定义
 * @author wangyn
 *
 */
public class ThriftDefManager {
	
	private static Logger log = LoggerFactory.getLogger(ThriftDefManager.class);
	
	//用于存放对外提供服务信息
	private static Set<ThriftServiceDef> thriftServices = new HashSet<ThriftServiceDef>();
	//用于存放当前应用所依赖的服务
	private static Set<ThriftReferenceDef> thriftReferences = new HashSet<ThriftReferenceDef>();
	//用于存放注册服务器定义信息
	private static ThriftRegistryDef thriftRegistryDef;
	//用于存放应用信息
	private static ThriftApplicationDef thriftApplicationDef;
	//用于存放应用依赖的应用信息,key是其name属性
	private static Map<String,ThriftRefApplicationDef> thriftRefApplicationDef = new HashMap<String,ThriftRefApplicationDef>();
	
	
	/**
	 * 初始化获得thrift服务定义信息 
	 */
	public static void initThriftDefs(){
		ApplicationContext applicationContext = FrameCommonUtils.getApplicationContext();
		log.info("init thrift service Defs............ ");
		Map<String,ThriftServiceDef> serviceMap = applicationContext.getBeansOfType(ThriftServiceDef.class);
		if(serviceMap!=null&&serviceMap.size()>0){
			thriftServices.addAll(serviceMap.values());
			log.info("init thriftServiceDefs over,there are "+serviceMap.size()+" thrift service ");
		}
		//注册服务器的信息
		thriftRegistryDef = applicationContext.getBean(ThriftRegistryDef.class);
		//应用信息
		thriftApplicationDef = applicationContext.getBean(ThriftApplicationDef.class);
		//依赖信息
		Map<String,ThriftReferenceDef> referencesMap  =  applicationContext.getBeansOfType(ThriftReferenceDef.class);
		if(referencesMap!=null&&referencesMap.size()>0){
			thriftReferences.addAll(referencesMap.values());
			log.info("init thriftReferenceDefs over,there are "+referencesMap.size()+" thrift references ");
		}
		
		//依赖应用的应用信息
		Map<String,ThriftRefApplicationDef> refappsMap  =  applicationContext.getBeansOfType(ThriftRefApplicationDef.class);
		if(refappsMap!=null&&refappsMap.size()>0){
			Collection<ThriftRefApplicationDef> c = refappsMap.values();
			for(ThriftRefApplicationDef refapp:c){
				thriftRefApplicationDef.put(refapp.getName(), refapp);
			}
		}
		
		/**
		 * 进行必要的判断
		 */
		if(ThriftDefManager.thriftRegistryDef==null){
			throw new BootstrapException(" thrift:registry does not config !!! ");
		}
		if(ThriftDefManager.thriftApplicationDef==null){
			throw new BootstrapException(" thrift:application does not config !!! ");
		}
	}


	public static Set<ThriftServiceDef> getThriftServices() {
		return thriftServices;
	}


	public static Set<ThriftReferenceDef> getThriftReferences() {
		return thriftReferences;
	}


	public static ThriftRegistryDef getThriftRegistryDef() {
		return thriftRegistryDef;
	}


	public static ThriftApplicationDef getThriftApplicationDef() {
		return thriftApplicationDef;
	}


	public static Map<String, ThriftRefApplicationDef> getThriftRefApplicationDef() {
		return thriftRefApplicationDef;
	}
	
	public static ThriftRefApplicationDef getRefApplicationDef(String application){
		return thriftRefApplicationDef.get(application);
	}

}
