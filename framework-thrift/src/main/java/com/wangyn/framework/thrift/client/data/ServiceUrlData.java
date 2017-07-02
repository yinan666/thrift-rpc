package com.wangyn.framework.thrift.client.data;
/**
 * 客户端所依赖服务地址的url
 * @author wangyn
 *
 */
public class ServiceUrlData implements Comparable<ServiceUrlData> {
	//服务的ip,host
	private String ip;
	//服务的端口号
	private int port;
	//服务对外暴露的名称
	private String serviceName;
	//服务的权重,默认为1
	private int weight = 1;
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	@Override
	public boolean equals(Object obj) {
		if(obj==null) return false;
		if(obj instanceof ServiceUrlData){
			ServiceUrlData tdata = (ServiceUrlData)obj;
			if(tdata.getIp().equals(this.getIp()) && tdata.getPort()==this.port && tdata.getServiceName().equals(this.serviceName)){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return ip.hashCode()+15*port+serviceName.hashCode();
	}
	
	@Override
	public int compareTo(ServiceUrlData obj) {
		if(obj==null) return 1;
		ServiceUrlData tdata = (ServiceUrlData)obj;
		if(this.weight>tdata.getWeight())
			return 1;
		else if(this.weight<tdata.getWeight())
			return -1;
		else
			return 0;
	}
	
	
	
}
