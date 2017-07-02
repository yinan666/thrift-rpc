package com.wangyn.framework.thrift.common;

import org.springframework.context.ApplicationContext;

public class FrameCommonUtils {
	
	//thrift服务实现的接口的类的后缀，如UserService$Iface，其中$Iface为后缀
	public final static String THRIFT_IFACE_SUFFIX = "$Iface";
	//thrift服务接口中提供的Processor的类的后缀
	public final static String THRIFT_PROCESSOR_SUFFIX = "$Processor";
	
	//spring的applicationContext,该对象是在加载ThriftApplicationDef时设置的
	private static ApplicationContext applicationContext;
	
	
	/**
	 * 获得服务的名称，比如com.test.UserRpcService$Iface，得到的是UserRpcService
	 * @param clazzName
	 * @return
	 */
	public static String getServiceClazzSimpleName(Class serviceImplClazz){
		Class<?>[] ifaces  = serviceImplClazz.getInterfaces();
		if(ifaces==null) return null; 
		//找到以$Iface结尾的接口
		for(Class<?> iface : ifaces){
			String $IfaceName = iface.getName();
			//如其接口以$Iface结尾com.wangyn.pay.core.service.TestService$Iface，
			//则表明该接口为thrift生成的接口
			if($IfaceName.endsWith(THRIFT_IFACE_SUFFIX)){
				//截取其中的类名称，作为服务名称，如TestService
				return getServiceClazzSimpleName($IfaceName);
			}
		}
		return null;
	}
	
	/**
	 * 获得服务的名称，比如com.test.UserRpcService$Iface，得到的是UserRpcService
	 * @param $IfaceName
	 * @return
	 */
	public static String getServiceClazzSimpleName(String $IfaceName){
		//截取其中的类名称，作为服务名称，如TestService
		return $IfaceName.substring($IfaceName.lastIndexOf(".")+1,$IfaceName.lastIndexOf("$"));
	}

	
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	 * spring的applicationContext,该对象是在加载ThriftApplicationDef时设置的
	 * @return
	 */
	public static void setApplicationContext(ApplicationContext applicationContext) {
		FrameCommonUtils.applicationContext = applicationContext;
	}

}
