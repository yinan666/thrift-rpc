package com.wangyn.framework.thrift.registry.zk;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wangyn.framework.thrift.common.FrameCommonUtils;
import com.wangyn.framework.thrift.common.JsonUtils;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftApplicationDef;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftRegistryDef;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftServiceDef;
import com.wangyn.framework.thrift.registry.AbstractThriftRegistry;

/**
 * 使用zk做服务注册
 * @author wangyn
 *
 */
public class ThriftServiceRegistryWithZk extends AbstractThriftRegistry {
	
	//需要持久化的path,其中带$表示需要替换的部分
	//$projectName表示项目名称，比如pay
	//$serviceName表示服务名称
	//$version表示服务版本号
	//注意该字符串只能被重新赋值一次（在创建ThriftServiceRegistryWithZk对象的时候）
	private String persistentPath = "/thrift/$projectName/$serviceName/$version/providers/";
	
	//服务定义信息
	protected Set<ThriftServiceDef> thriftServices;
	
	//服务地址列表
	private static List<String> servicePathList = new ArrayList<String>();
	
	private Logger log = LoggerFactory.getLogger(ThriftServiceRegistryWithZk.class);
	
	
	/**
	 * 
	 * @param thriftServices 服务定义
	 * @param ip 服务的ip
	 * @param port 服务端口号
	 */
	public ThriftServiceRegistryWithZk(Set<ThriftServiceDef> thriftServices,String ip,int port,ThriftRegistryDef thriftRegistryDef,ThriftApplicationDef thriftApplicationDef) {
		super(ip,port,thriftRegistryDef,thriftApplicationDef);
		this.thriftServices = thriftServices;
		//先把项目名称给换了，下面就不用换了
		persistentPath = persistentPath.replace("$projectName", thriftApplicationDef.getName());
	}
	
	

	@Override
	public void registe() {
		if(thriftServices!=null&&thriftServices.size()>0){
			log.info("begin to registe thrift services...........");
			for(ThriftServiceDef def : thriftServices){
				//替换相关字段,最后形成的path : /thrift/pay/UserService/1.0/providers/{"port":9501,"serviceName":"UserRpcService2.0","ip":"127.0.0.1"}
				String serviceSimpleName = FrameCommonUtils.getServiceClazzSimpleName(def.getServiceImplClass());
				String path = persistentPath.replace("$serviceName", serviceSimpleName)
											.replace("$version", def.getVersion()) 
											+ getServiceJsonstr(ip, port, serviceSimpleName+def.getVersion());
				servicePathList.add(path);
			}
			final ZkUtil zkUtil = ZkUtil.getUtil(thriftRegistryDef.getAddress(), thriftRegistryDef.getTimeout());
			//当项目第一次启动时，需要执行的任务
			zkUtil.addExecuteWhenFirstStartList(new ExecuteWhenWatchedEvent() {
				@Override
				public void execute() {
					zkUtil.createTmpNodes4Start(servicePathList);
				}
			});
			//当重新建立连接时，重新注册服务
			zkUtil.addExecuteWhenWatchedReconnected(new ExecuteWhenWatchedEvent() {
				@Override
				public void execute() {
					log.info("begin to reRegister thrift services..........");
					zkUtil.createTmpNodes4ReConnected(servicePathList);
				}
			});
		}else{
			log.error("registerServices error,thriftServices is empty");
		}
	}
	
	private String getServiceJsonstr(String ip,int port,String serviceName){
		Map<String,Object> map = new LinkedHashMap<String,Object>();
		map.put("ip", ip);
		map.put("port", port);
		map.put("serviceName", serviceName);
		String val = null;
		try {
			val = JsonUtils.getJsonFromObj(map);
		} catch (Exception e) {
			log.error("ThriftServiceRegistryWithZk.getServiceJsonstr",e);
		}
		return val;
	}

}
