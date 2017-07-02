package com.wangyn.framework.thrift.registry.zk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.DeleteBuilder;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * zookeeper操作相关的工具类
 * @author wangyn
 *
 */
public class ZkUtil {
	private  static Logger log = LoggerFactory.getLogger(ZkUtil.class);
	private static CuratorFramework client;
	//标记是否退出会话，在项目关闭的时候，需要将其设置为true
	private boolean isQuit = false;
	//连接监听是否已经开启
	private AtomicBoolean connListenerHasStart = new AtomicBoolean(false);
	
	//当监听到重新建立连接时
	private List<ExecuteWhenWatchedEvent> executeWhenWatchedReconnectedList = new ArrayList<ExecuteWhenWatchedEvent>();
	
	//注册完成后，启动项目时，第一次需要执行的任务
	private List<ExecuteWhenWatchedEvent> executeWhenFirstStartList = new ArrayList<ExecuteWhenWatchedEvent>();
	
	private static ZkUtil zkUtil;
	
	/**
	 * 创建工具
	 * @param zkAddress zk的地址列表 
	 * @param sessionTimeOut 会话超时时间
	 */
	private ZkUtil(String zkAddress,int sessionTimeOut){
		//连接创建超时时间1分钟
		//连接重试总次数5次，每隔5秒重试一次
		client = CuratorFrameworkFactory.newClient(zkAddress, sessionTimeOut, 60000, new RetryNTimes(5,10000));
    	client.start();
	}
	
	/**
	 * 获得zkUtil
	 * @param zkAddress
	 * @param sessionTimeOut
	 * @return
	 */
	public static ZkUtil getUtil(String zkAddress,int sessionTimeOut){
		if(zkUtil==null){
			synchronized (ZkUtil.class) {
				if(zkUtil==null){
					zkUtil = new ZkUtil(zkAddress, sessionTimeOut);
				}
			}
		}
		return zkUtil;
	}
	
	/**
	 * 用于获得当前节点的所有子节点信息
	 * @param path
	 * @return
	 */
	public List<String> getChildrenPaths(String path)throws Exception{
		return client.getChildren().forPath(path);
	}
	
	
	/**
	 * 创建一个临时节点
	 * @param nodePath
	 * @param reCreateWhenExists 如果为true，则表示当节点已经存在时，则重新创建
	 */
	public void createTmpNode(String nodePath,boolean reCreateWhenExists)throws Exception{
		CreateBuilder cb = client.create();
		try {
			cb.creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(nodePath);
		} catch (KeeperException.NodeExistsException e) {
			if(reCreateWhenExists){
				//如果节点已经创建，为了将节点纳入当前客户端会话中,则先删除节点，然后重新创建
				try {
					log.debug("delete path [{}],because the path is exists",nodePath);
					//先删除节点
					deletePath(nodePath);
					//重新创建节点
					cb.forPath(nodePath);
				} catch (Exception e1) {
					log.error("createTmpNode {} error",nodePath,e1);
				}
			}else{
				log.warn("path {} has been exists",nodePath);
			}
		}
	}
	
	/**
	 * 第一次连接成功时，一次创建多个临时节点，如果节点存在，则删除原来的节点，再重新创建
	 * @param nodePathList
	 */
	public void createTmpNodes4Start(List<String> nodePathList){
		CreateBuilder cb = client.create();
		cb.creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL);
		for(String path : nodePathList){
			try {
				log.info("thrift服务注册: {}",path);
				cb.forPath(path);
			} catch (KeeperException.NodeExistsException e) {
				//如果节点已经创建，为了将节点纳入当前客户端会话中,则先删除节点，然后重新创建
				try {
					log.debug("delete path [{}],because the path is exists",path);
					//先删除节点
					deletePath(path);
					//重新创建节点
					cb.forPath(path);
				} catch (Exception e1) {
					log.error("delete and create the path {} error",path,e1);
				}
				
			} catch(Exception e){
				log.error("create path {} error",path,e);
			}
		}
	}
	
	/**
	 * 当重新连接成时，一次创建多个临时节点，如果节点存在，则不再创建
	 * 因为重新连接时，要么本次会话还没有超时（当第一个心跳ping的时候，如果没有得到应答，则进入suppend状态），要么会话已经超时又重新连接的，此时临时节点已经被删除了
	 * @param nodePathList
	 */
	public void createTmpNodes4ReConnected(List<String> nodePathList){
		CreateBuilder cb = client.create();
		cb.creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL);
		for(String path : nodePathList){
			try {
				cb.forPath(path);
				log.info("thrift服务注册: {}",path);
			} catch (KeeperException.NodeExistsException e) {
				log.warn("path {} has been exists",path);
				
			} catch(Exception e){
				log.error("create path {} error",path,e);
			}
		}
	}
	
	/**
	 * 删除节点
	 * @param path
	 * @throws Exception
	 */
	public void deletePath(String path)throws Exception{
		DeleteBuilder builder =	client.delete();
		try {
			builder.guaranteed().withVersion(-1).forPath(path);
		} catch (org.apache.zookeeper.KeeperException.NoNodeException e) {
			log.info("path is not exists when delete path:"+path);
		}
	}
	
	/**
	 * 创建一个持久节点
	 * @param nodePath
	 * @throws Exception
	 */
	public void createPresistentNode(String nodePath)throws Exception{
		CreateBuilder cb = client.create();
		cb.creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(nodePath);
	}
	
	/**
	 * 添加监听，并根据传入的ExecuteWhenWatchedEvent类型，来判断当触发xx监听时执行对应的操作
	 * 比如ExecuteWhenWatchedReconnected 类型，当重连时，执行指定操作
	 * @param ewe
	 */
	private void addListeners(){
		//如果监听以及开启，则不需继续执行
		if(connListenerHasStart.get()) return;
		 //对连接状态进行监听
		 client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
	            @Override
	            public void stateChanged(CuratorFramework client, ConnectionState newState) {
	              log.info("Listenning the ConnectionState :"+newState+",client state:"+client.getState());
	               //当重新建立连接时
	               if(newState == ConnectionState.RECONNECTED){
	            	   if(executeWhenWatchedReconnectedList!=null&&executeWhenWatchedReconnectedList.size()>0){
	            		   for(ExecuteWhenWatchedEvent exed : executeWhenWatchedReconnectedList ){
	            			   exed.execute();
	            		   }
	            	   }
	               }
	               
	            }
	     });
		 
		 //对未捕获的异常进行监听
		 client.getUnhandledErrorListenable().addListener(new UnhandledErrorListener() {
            @Override
            public void unhandledError(String message, Throwable e) {
                log.error("CuratorFramework unhandled Error: "+message,e);
            }
         });
		 //如果已经启动对连接的监听
		 connListenerHasStart.set(true);
	}
	
	/**
	 * 监听节点子节点
	 * @param path
	 * @param listener
	 * @throws Exception
	 */
	public void addWatcherOnChildrenCache(String path,PathChildrenCacheListener listener)throws Exception{
		PathChildrenCache pcc = new PathChildrenCache(client, path, false);
		pcc.start();
		//添加监听处理
		pcc.getListenable().addListener(listener);
	}
	
	//启动监听
	public void startListeners(){
		addListeners();
	}
	
	
	public void addExecuteWhenWatchedReconnected(ExecuteWhenWatchedEvent executeWhenWatchedReconnected) {
		this.executeWhenWatchedReconnectedList.add(executeWhenWatchedReconnected);
	}


	public void addExecuteWhenFirstStartList(ExecuteWhenWatchedEvent executeWhenWatchedConnected) {
		this.executeWhenFirstStartList.add(executeWhenWatchedConnected);
	}

	/**
	 * 手动触发一些需第一次项目启动时进行的操作
	 */
	public void initConnectedExecutes(){
		 if(executeWhenFirstStartList!=null&&executeWhenFirstStartList.size()>0){
  		   for(ExecuteWhenWatchedEvent exed : executeWhenFirstStartList ){
  			   exed.execute();
  		   }
  	   }
	}

	/**
	 * 退出客户端会话
	 */
	public void quitSession(){
		//关闭会话
		isQuit = true;
		client.close();
		log.info("zookeeper session closed......... ");
	}
}
