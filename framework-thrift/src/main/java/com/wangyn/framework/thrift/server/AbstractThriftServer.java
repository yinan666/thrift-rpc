package com.wangyn.framework.thrift.server;

import java.lang.reflect.Constructor;
import java.util.Set;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.access.BootstrapException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.wangyn.framework.thrift.common.FrameCommonUtils;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftDefManager;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftServiceDef;

/**
 * 服务端抽象类
 * @author wangyn
 *
 */
public abstract class AbstractThriftServer implements ApplicationContextAware,ApplicationListener<ContextRefreshedEvent> {
	
	private Logger log = LoggerFactory.getLogger(AbstractThriftServer.class);
	
	//实现了ApplicationContextAware接口的类，在spring为其创建bean时，会注入ApplicationContext对象
	private  ApplicationContext applicationContext;
	//服务器端口号，需注入
	private static int port;
	//传输协议，可注入
	private String transportProtocol;
	
    public void setTransportProtocol(String transportProtocol) {
		this.transportProtocol = transportProtocol;
	}
    
    protected String getTransportProtocol(){
    	return this.transportProtocol;
    }

	public  void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
     * 数据传输协议
     * @return
     */
    protected TProtocolFactory getTprotocolFactory(){
    	switch (transportProtocol) {
	    	//json格式
			case "json":
				return  new TJSONProtocol.Factory();
			//压缩
			case "compact":
				return  new TCompactProtocol.Factory();
			//提供JSON只写协议, 生成的文件很容易通过脚本语言解析
			case "sjson":
				return  new TSimpleJSONProtocol.Factory();
			//默认是二进制格式
			case "binary":
				return new TBinaryProtocol.Factory();
			default:
				throw new BootStrapException("transportProtocol ["+transportProtocol+"] does not exists ! please check your config");
		}
    }

	public void setPort(int port) {
		this.port = port;
	}
	
	public static int getPort(){
		return port;
	}
	
	
	/**
	 * 多服务处理类
	 * @return
	 */
	TMultiplexedProcessor getProcessor(){
		TMultiplexedProcessor tmprocessor = new TMultiplexedProcessor();
		Set<ThriftServiceDef> thriftServices = ThriftDefManager.getThriftServices();
		if(thriftServices!=null&&thriftServices.size()>0){
			//遍历服务定义信息，设置暴露服务的数据
			for(ThriftServiceDef serviceData : thriftServices){
				String clazzSimpleName = null;
				Class<?> processorClazz = null;
				//服务实现类的类实例
				Class<?> serviceImplClazz = serviceData.getServiceImplClass();
				try {
					Object[] objary = getServiceNameAndProcessorClazz(serviceImplClazz);
					clazzSimpleName = (String) objary[0];
					processorClazz = (Class) objary[1];
				} catch (ClassNotFoundException e) {
					log.error("class ["+serviceImplClazz.getName()+"] regist error !",e);
					throw new BootStrapException("getProcessor",e);
				}
				//获得构造函数
				Constructor<TProcessor> constructor = (Constructor<TProcessor>) processorClazz.getConstructors()[0];
				//从spring容器中获得类对应的bean
				Object serviceImplBean = applicationContext.getBean(serviceImplClazz);
				//通过反射找到服务对应的Processor
				TProcessor processor = BeanUtils.instantiateClass(constructor, serviceImplBean);
				//设置服务暴露的名称 =  服务类名称+version
				String serviceName = clazzSimpleName + serviceData.getVersion();
				tmprocessor.registerProcessor(serviceName, processor);
				log.info(" Thrift服务{}发布成功,version = {}，class = {}",clazzSimpleName,serviceData.getVersion(),serviceImplClazz.getName());
			}
			return tmprocessor;
		}else{
			log.error("serviceImplClasses is empty,please check your config");
			throw new BootStrapException("serviceImplClasses is empty");
		}
	}
	
	/**
	 * 由于作为service的类可能会实现多个接口，这里要找到实现thrift生成的那个接口的全类名，该接口名会以$Iface结尾
	 * @param serviceImplClazz
	 * @return
	 * @throws ClassNotFoundException 
	 */
	private Object[] getServiceNameAndProcessorClazz(Class<?> serviceImplClazz) throws ClassNotFoundException{
		//获得服务实现类所实现的接口
		Class<?>[] ifaces  = serviceImplClazz.getInterfaces();
		if(ifaces==null) return null; 
		//找到以$Iface结尾的接口
		for(Class<?> iface : ifaces){
			String $IfaceName = iface.getName();
			//如其接口以$Iface结尾com.wangyn.pay.core.service.TestService$Iface，
			//则表明该接口为thrift生成的接口
			if($IfaceName.endsWith(FrameCommonUtils.THRIFT_IFACE_SUFFIX)){
				Object[] objary = new Object[2];
				//截取其中的类名称，作为服务名称，如TestService
				objary[0] = FrameCommonUtils.getServiceClazzSimpleName($IfaceName);
                /** 
                 * 换为 com.wangyn.pay.core.service.TestService$Processor **/
				//Processor全类名
				String processorName = $IfaceName.replace(FrameCommonUtils.THRIFT_IFACE_SUFFIX,FrameCommonUtils.THRIFT_PROCESSOR_SUFFIX);
				//根据全类名获得其类实例
				objary[1] =  Class.forName(processorName);
				return objary;
			}
		}
		//如果最后找不到，则抛出一个异常
		throw new BootstrapException(" serviceImplClass does not implements Thrift Service interface");
	}
	
	
	
	/**
	 * 需要子类来实现，用于启动服务
	 */
	protected abstract void startServerInternal();
	
	
	/**
	 * spring容器启动时执行该方法
	 */
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		//防止重复执行。
		if(event.getApplicationContext().getParent() == null){
			start();
		}
	}

	/**
     * 另外开启一个线程的原因：防止spring线程卡死 
     */
    public void start() {  
        new Thread() {  
            public void run() {
            	//启动thrift服务
                startServerInternal();  
            }
        }.start();  
    }
    
    /**
     * 
     * 怎么关闭服务呢？启动时，记录一下进程的pid，写个shell脚本，然后去调用stop时，直接kill -9 这个进程号
     * 
     * 服务关闭时，处理一些后续操作操作，比如断开与zk的会话
     */
    public void destory(){
    	System.out.println("destory--------------------------------------");
    }
    

}
