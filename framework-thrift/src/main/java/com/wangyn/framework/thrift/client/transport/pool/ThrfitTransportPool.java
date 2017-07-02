package com.wangyn.framework.thrift.client.transport.pool;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.commons.pool.impl.GenericKeyedObjectPool.Config;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wangyn.framework.thrift.client.adcache.ServiceUrlManager;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftDefManager;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftRefApplicationDef;

public class ThrfitTransportPool {
	
	private static final Logger log = LoggerFactory.getLogger(ThrfitTransportPool.class);
	
	/**
	 * 带key的线程池，key参考buildTransportKey方法
	 */
	private  GenericKeyedObjectPool<String,TTransport> keyPool = new GenericKeyedObjectPool<String,TTransport>(new ThriftTransportObjectFactory());
	
	/**
	 * 每个服务上，最大连接数以及maxActive/maxidle等参数是不一样的
	 * 应该根据application来设置连接池的这些参数
	 * transportPoolMap的key是application，连接池中的key是application+ip+port
	 * **/
	private static final Map<String,ThrfitTransportPool> transportPoolMap = new HashMap<String,ThrfitTransportPool>();
	
	private ThrfitTransportPool(String application){
		
		ThriftRefApplicationDef appdef = ThriftDefManager.getRefApplicationDef(application);
		//应用对应的节点数
		int nodecout = ServiceUrlManager.getNodesCountOfApplication(application);
		
		/***下面的值是每个节点上对应的参数值***/
		Config config = new Config();
		//通过计算得出其每个节点上最大active数
		int maxActive = 0;
		if(appdef.getMaxconn()==null){
			//如果没有设置，默认就是1，也就是总的连接数等于节点数
			maxActive = nodecout;
		}else{
			maxActive = appdef.getMaxconn();
		}
		//计算每个节点上的最大数active数
		maxActive = maxActive%nodecout==0?maxActive/nodecout:(maxActive/nodecout+1);
		config.maxActive = maxActive;
		
		//每个节点上最大空闲数
		config.maxIdle = 4>maxActive?maxActive:4;
		
		//最小idle数，等于节点数
		config.minIdle = 0;
		//lifo=true，表示新创建的对象放在空闲队列的的最前面，反之放在后面
		config.lifo = false;
		
		//表示每次借出之前，做一次检查
		config.testOnBorrow = true;
		//当没有空闲时，最大等待时间,单位是毫秒
		config.maxWait = 30000;
		
		keyPool.setConfig(config);// 相关参数设置
	}
	
	/**
	 * 每个服务上，最大连接数以及maxActive/maxidle等参数是不一样的
	 * 应该根据application来设置连接池的这些参数
	 * @return
	 */
	public static ThrfitTransportPool getPool(String application){
			ThrfitTransportPool tpool = transportPoolMap.get(application);
			if(tpool!=null){
				return tpool;
			}else{
				synchronized (ThrfitTransportPool.class) {
					tpool = transportPoolMap.get(application);
					if(tpool != null){
						return tpool;
					}else{
						tpool = new ThrfitTransportPool(application);
						transportPoolMap.put(application, tpool);
						return tpool;
					}
				}
			}
	}
	
	/**
	 * 从池中取得一个transport
	 * @param application
	 * @param host
	 * @param port
	 * @return
	 * @throws Exception
	 */
	public TTransport getTransport(String application,String host,int port) throws Exception{
		return keyPool.borrowObject(buildTransportKey(application, host, port));
	}
	
	/**
	 * 将transport关闭，即归还到池中
	 * @param host
	 * @param port
	 * @param transport
	 * @throws Exception
	 */
	public void closeTransport(String application,String host,int port,TTransport transport)throws Exception{
		keyPool.returnObject(buildTransportKey(application, host, port), transport);
	}
	
	/**
	 * 构建一个key线程池的key
	 * @param application
	 * @param host
	 * @param port
	 * @return
	 */
	private String buildTransportKey(String application,String host,int port){
		return application+":"+host+":"+port;
	}
	
	/**
	 * 清除其中的key
	 * @param application
	 * @param host
	 * @param port
	 */
	public void clearTransport(String application,String host,int port){
		keyPool.clear(buildTransportKey(application, host, port));
	}
	
	/**
	 * 关闭连接池
	 * @param application
	 */
	public static void closePool(String application){
		ThrfitTransportPool pool = transportPoolMap.get(application);
		if(pool!=null){
			try {
				pool.keyPool.close();
				//清除掉连接池对象
				transportPoolMap.remove(application);
			} catch (Exception e) {
				log.error("closePool,applicaton:"+application,e);
			}
		}
	}
}
