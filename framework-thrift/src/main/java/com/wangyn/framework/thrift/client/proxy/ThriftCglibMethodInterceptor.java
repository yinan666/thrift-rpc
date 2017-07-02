package com.wangyn.framework.thrift.client.proxy;

import java.lang.reflect.Method;

import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wangyn.framework.thrift.client.adcache.ServiceUrlManager;
import com.wangyn.framework.thrift.client.data.ServiceUrlData;
import com.wangyn.framework.thrift.client.transport.pool.ThrfitTransportPool;
import com.wangyn.framework.thrift.exception.ThriftFramworkException;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * 拦截方法调用
 * @author wangy
 *
 */
public class ThriftCglibMethodInterceptor implements MethodInterceptor {
	
	//log
	private Logger log = LoggerFactory.getLogger(ThriftCglibMethodInterceptor.class);
	
	//服务版本号
	private String version;
	//服务类名
	private String interfaceClass;
	//服务所在应用
	private String application;
	//调用服务总的尝试次数
	private final int SERVICERETRYLIMITTIMES = 5;
	//每个服务单独尝试的次数
	private final int CURRENTSERVICETRYLIMITTIMES = 2;
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getInterfaceClass() {
		return interfaceClass;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}


	public ThriftCglibMethodInterceptor(String version, String interfaceClass,String application) {
		this.version = version;
		this.interfaceClass = interfaceClass;
		this.application = application;
	}

	public void setInterfaceClass(String interfaceClass) {
		this.interfaceClass = interfaceClass;
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		//第一次调用时，totalTryTime、currentObjTimes都为0，serviceUrlData为null
		return tryCallServiceMethod(method, args, application,0,0,null);
	}
	
	/**
	 * 尝试发出请求
	 * @param method
	 * @param args
	 * @param application
	 * @param totalTryTime 已经尝试的总次数
	 * @param currentObjTimes 当前对象已经调用的次数
	 * @param serviceUrlData 当前对象
	 * @return
	 */
	public Object tryCallServiceMethod(Method method,Object[] args,String application,int totalTryTime,int currentObjTimes,ServiceUrlData serviceUrlData){
		//如果总的调用次数已经超过了最大值，则直接抛出异常
		if(totalTryTime>=SERVICERETRYLIMITTIMES){
			throw new ThriftFramworkException("application:"+application+",servieName:"+serviceUrlData.getServiceName()+",has been trid "+SERVICERETRYLIMITTIMES+" times, it's still error");
		}
		//每尝试一次，总调用次数都要加1
		totalTryTime++;
		//判断当前对象是否已经超过了重试次数
		if(currentObjTimes>=CURRENTSERVICETRYLIMITTIMES){
			//如果是重新获取的，则当前对象重试次数为第1次尝试
			serviceUrlData = ServiceUrlManager.getOneServiceUrlByRandomWeightExclute(application, interfaceClass, version, serviceUrlData);
			//如果重新获取无法获得对象，则抛出异常
			if(serviceUrlData==null){
				throw new ThriftFramworkException("application:"+application+",servieClass:"+interfaceClass+",has been trid "+CURRENTSERVICETRYLIMITTIMES+" times, there has no more serviceUrlData to use !!!");
			}
			currentObjTimes = 1;
		}else{
			currentObjTimes++;
			//如果是第1次调用，则获取一个新的，如果是第二次尝试，则不需要获取新的对象
			if(serviceUrlData==null){
				serviceUrlData = ServiceUrlManager.getOneServiceUrlByRandomWeight(application, interfaceClass, version);
			}
		}
		try {
			log.info("thriftInterceptCall {threadId:{},application:{},serviceName:{},version:{}host:{},port:{}}",Thread.currentThread().getId(),application,serviceUrlData.getServiceName(),version,serviceUrlData.getIp(),serviceUrlData.getPort());
			return callServiceMethod(method, args,serviceUrlData,application);
		//根据不同的异常触发不同的操作，如果是网络问题，则重新获得连接，重新发起请求
		} catch (Exception e) {
			log.error("thriftInterceptCallResult {result:'fail',msg:{}}",e);
			return tryCallServiceMethod(method, args, application, totalTryTime, currentObjTimes, serviceUrlData);
		}
	}
	
	
	private Object callServiceMethod(Method method, Object[] args,ServiceUrlData serviceUrlData,String application)throws Exception{
		//请求服务
		if(serviceUrlData==null){
			throw new ThriftFramworkException("the reference service [class="+interfaceClass+",version="+version+"], is not registe on zookeeper");
		}
		
		TTransport transport = null;
		try {
			transport = ThrfitTransportPool.getPool(application).getTransport(application,serviceUrlData.getIp(), serviceUrlData.getPort());
			TProtocol protocol = new TJSONProtocol(transport);
			TProtocol protocol2 = new TMultiplexedProtocol(protocol, serviceUrlData.getServiceName());
			String clientClassPath = interfaceClass+"$Client";
			Class<?> clientClazz =  Class.forName(clientClassPath);
			Object clientObj = clientClazz.getConstructor(TProtocol.class).newInstance(protocol2);
			Method targetMethod = null;
			//如果有参数
			if(args!=null&&args.length>0){
				Class<?>[] argsclassary = new Class[args.length];
				for(int i=0;i<args.length;i++){
					argsclassary[i] = args[i].getClass();
				}
				targetMethod = clientClazz.getMethod(method.getName(), argsclassary);
			}else{
				targetMethod = clientClazz.getMethod(method.getName(), null);
			}
			return targetMethod.invoke(clientObj, args);
		}finally{
			if(transport!=null){
				ThrfitTransportPool.getPool(application).closeTransport(application,serviceUrlData.getIp(),serviceUrlData.getPort(), transport);
			}
		}
	}
}
