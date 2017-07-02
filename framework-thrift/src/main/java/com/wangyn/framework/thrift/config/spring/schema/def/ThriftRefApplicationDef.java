package com.wangyn.framework.thrift.config.spring.schema.def;
/**
 * 当前应用所依赖的服务应用对象
 * @author wangyn
 *
 */
/***
 * 由于服务应用是以多节点集群部署的
 * 如果这里的maxActive表示的是每个到每个服务节点上的最大连接数，则如果某个应用A依赖服务B，服务B有10个节点，
 * 如果A上配置对B的maxActive为300，在并发量大的时候，则B上每个节点都会有300个连接被A所占用。但是B服务并不只是
 * 被服务A所依赖，有可能会被D或其他服务所依赖，如果一开始就限定死了对每个节点的连接数，即时B后期因为并发量大，而
 * 增加节点也是没有用的，因为每个节点上来就有固定的连接数。
 * 
 * 而如果maxActive表示的是A到B的总的连接数，也就是说B中所有节点上总的连接数加到一起的连接数为maxActive，这样如果随着
 * B服务上节点数的增加，每个节点上分摊的连数据也就会减少。
 * 
 * 
 * 
 * 
 * 
 * @author Administrator
 *
 */
public class ThriftRefApplicationDef {
	
	//依赖的应用的名称
	private String name;
	//连接超时时间
	private Integer timeout;
	//最大连接数，到此服务的所有节点的总连接数
	private Integer maxconn;
	
	//*************只有上面三个值是需要配置的
	
	
	
	
	//此值是计算出来的，maxActive = maxConn / 节点数
	private int maxActive;
	//此值是计算出来的，minActive = 节点数
	private int minActive;
	//此值是计算出来的，maxIdle = 节点数
	private int maxIdle;
	//此值是计算出来的，minIdle = 节点数
	private int minIdle;
	//如果无对象可取时，最大等待时间
	private int maxWait;
	
	public int getMaxActive() {
		return maxActive;
	}
	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}
	public int getMinActive() {
		return minActive;
	}
	public void setMinActive(int minActive) {
		this.minActive = minActive;
	}
	public int getMaxIdle() {
		return maxIdle;
	}
	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}
	public int getMaxWait() {
		return maxWait;
	}
	public void setMaxWait(int maxWait) {
		this.maxWait = maxWait;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getTimeout() {
		return timeout;
	}
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}
	public Integer getMaxconn() {
		return maxconn;
	}
	public void setMaxconn(Integer maxconn) {
		this.maxconn = maxconn;
	}
	public int getMinIdle() {
		return minIdle;
	}
	public void setMinIdle(int minIdle) {
		this.minIdle = minIdle;
	}
	
}
