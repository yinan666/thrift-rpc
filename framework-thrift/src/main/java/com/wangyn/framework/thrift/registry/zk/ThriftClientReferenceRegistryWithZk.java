package com.wangyn.framework.thrift.registry.zk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wangyn.framework.thrift.client.adcache.ServiceUrlManager;
import com.wangyn.framework.thrift.common.JsonUtils;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftApplicationDef;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftReferenceDef;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftRegistryDef;
import com.wangyn.framework.thrift.exception.ThriftFramworkException;
import com.wangyn.framework.thrift.registry.AbstractThriftRegistry;
import com.wangyn.framework.thrift.server.OSUtil;

/**
 * 客户端依赖关系注册
 * @author wangyn
 *
 */
public class ThriftClientReferenceRegistryWithZk extends AbstractThriftRegistry {
	
	private Logger log = LoggerFactory.getLogger(ThriftClientReferenceRegistryWithZk.class);
	
	//客户端注册地址
	private String clientPersistentPath = "/thrift/$projectName/$serviceName/$version/consumers/";
	
	//服务注册地址
	private String servicePersistentPath = "/thrift/$projectName/$serviceName/$version/providers";
	
	/**
	 * 客户端依赖信息
	 */
	private Set<ThriftReferenceDef> thriftReferences;
	
	public ThriftClientReferenceRegistryWithZk(Set<ThriftReferenceDef> thriftReferences,String ip, int port, ThriftRegistryDef thriftRegistryDef,ThriftApplicationDef thriftApplicationDef) {
		super(ip, port, thriftRegistryDef,thriftApplicationDef);
		this.thriftReferences = thriftReferences;
	}
	
	/**
	 * 获得客户端注册信息
	 * @return
	 * @throws Exception
	 */
	private String getClientRegisteMsg()throws Exception{
		Map<String,String> clientRegisterMsg = new HashMap<String,String>();
		String locationIp = OSUtil.getLocalIP();
		clientRegisterMsg.put("ip", locationIp);
		clientRegisterMsg.put("application", thriftApplicationDef.getName());
		return JsonUtils.getJsonFromObj(clientRegisterMsg);
	}

	@Override
	public void registe() {
		//获得需要在zk上注册的客户端信息json串
		String clientRegisterMsg;
		try {
			clientRegisterMsg = getClientRegisteMsg();
		} catch (Exception e1) {
			throw new ThriftFramworkException(e1);
		}
		
		if(thriftReferences!=null&&thriftReferences.size()>0){
			
			log.info("begin to registe thrift references.........");
			
			final ZkUtil zkUtil = ZkUtil.getUtil(thriftRegistryDef.getAddress(), thriftRegistryDef.getTimeout());
			
			//客户端需要注册到zk上的path路径
			final List<String> clientTmpPathList = new ArrayList<String>();
			
			for(final ThriftReferenceDef reference:thriftReferences){
				/***
				 * 1、添加监听,监听当前应用所依赖的每个服务提供者节点的监听
				 */
				//建立到/thrift/$projectName/$serviceName/$version/providers/的监听，如果其中有修改，就重新获取服务
				String interfaceClass = reference.getInterfaceClass();
				String serviceName = reference.getInterfaceClass().substring(interfaceClass.lastIndexOf(".")+1);
				final String path = servicePersistentPath.replace("$projectName", reference.getApplication())
						.replace("$serviceName", serviceName)
						.replace("$version",reference.getVersion());
				
				try {
					
					//重新获得子节点数据，如果为空，则删除本地缓存中的数据，如果不为空则更新本地缓存数据
					List<String> childpathList = zkUtil.getChildrenPaths(path);
					//重新设置service对应的服务rul
					ServiceUrlManager.resetServiceUrlsCache(reference.getApplication(),reference.getInterfaceClass(),reference.getVersion(), childpathList);
					
					/***还要添加对子节点的监听，如果有新的服务节点加入、删除、修改等操作，需要重新获取监听的服务**/
					zkUtil.addWatcherOnChildrenCache(path, new PathChildrenCacheListener() {
						@Override
						public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
							if(event.getType()==PathChildrenCacheEvent.Type.CHILD_ADDED
									||event.getType()==PathChildrenCacheEvent.Type.CHILD_REMOVED
									||event.getType()==PathChildrenCacheEvent.Type.CHILD_UPDATED
									||event.getType()==PathChildrenCacheEvent.Type.CONNECTION_RECONNECTED  //因为怕超时的时间段内（虽然很短），其他服务端的节点有修改
									){
								//重新获得子节点数据，如果为空，则删除本地缓存中的数据，如果不为空则更新本地缓存数据
								List<String> childpathList = client.getChildren().forPath(path);
								//重新设置service对应的服务rul
								ServiceUrlManager.resetServiceUrlsCache(reference.getApplication(),reference.getInterfaceClass(),reference.getVersion(), childpathList);
							}
						}
					});
				} catch (Exception e) {
					throw new ThriftFramworkException(e);
				}
				
				/***
				 * 2、将客户端注册到zk上,当建立连接，或重新建立连接时，将当前应用信息注册到zk上。
				 */
				final String clientTmpPath = clientPersistentPath.replace("$projectName", reference.getApplication())
									.replace("$serviceName",serviceName)
									.replace("$version", reference.getVersion())+clientRegisterMsg;
				clientTmpPathList.add(clientTmpPath);
			}
			
			//将客户端注册到zk上,当建立连接，或重新建立连接时，将当前应用信息注册到zk上。
			if(clientTmpPathList.size()>0){
				
				log.info("begin to regist client msg on zk..............");
				
				//当第一次启动项目时，需要执行的任务
				zkUtil.addExecuteWhenFirstStartList(new ExecuteWhenWatchedEvent() {
					@Override
					public void execute() {
						zkUtil.createTmpNodes4Start(clientTmpPathList);
					}
				});
				//当ReConnection时，需重新注册客户端端信息,重新注册时，如果节点已经存在，则不用删除重建
				zkUtil.addExecuteWhenWatchedReconnected(new ExecuteWhenWatchedEvent() {
					@Override
					public void execute() {
						zkUtil.createTmpNodes4ReConnected(clientTmpPathList);
					}
				});
			}
		}
	}

	

}
