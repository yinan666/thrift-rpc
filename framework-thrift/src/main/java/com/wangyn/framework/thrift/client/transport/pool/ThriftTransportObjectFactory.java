package com.wangyn.framework.thrift.client.transport.pool;

import org.apache.commons.pool.BaseKeyedPoolableObjectFactory;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.wangyn.framework.thrift.config.spring.schema.def.ThriftDefManager;
import com.wangyn.framework.thrift.config.spring.schema.def.ThriftRefApplicationDef;
/**
 * Thrift transport 对象创建工厂类
 * @author wangyn
 *
 */
public class ThriftTransportObjectFactory extends BaseKeyedPoolableObjectFactory<String,TTransport> {

	private static final int DEFAULT_TIMEOUT = 5000;
	
	public ThriftTransportObjectFactory() {
	}

	/**
	 * 当借出对象时，如果池中没有可用对象，并且active对象数未超过maxActive，则会创建一个对象
	 */
	@Override
	public TTransport makeObject(String key) throws Exception {
		//key是有applicaiton:host:端口组成的字符串，如user:127.0.0.1:22
		String[] ary = key.split(":");
		String application = ary[0];
		String host = ary[1];
		int port = Integer.valueOf(ary[2]);
		//连接超时时间
		int timeout = DEFAULT_TIMEOUT;
		ThriftRefApplicationDef appref = ThriftDefManager.getRefApplicationDef(application);
		if(appref!=null){
			timeout = appref.getTimeout()==null?DEFAULT_TIMEOUT:appref.getTimeout();
		}
		
		//创建一个Tsocket
		TSocket tsocket = new TSocket(host, port,timeout);
		TTransport transport = new TFramedTransport(tsocket);
		return transport;
	}

	/**
	 * 当pool.setTestOnBorrow(true);时，每次借出之前，都会先调用激活方法，然后调用validateObject（）方法，如果得到true
	 * 则可以正常借出，如果未false，则销毁该对象，并重新创建一个新的对象（新的对象也会被验证）
		pool.setTestOnReturn(true);时，每次归还到池中之前，都会调用validateObject（）方法，如果得到true
	 * 则可以正常归还，如果未false，则销毁该对象
	 */
	@Override
	public boolean validateObject(String key, TTransport transport) {
		if(transport==null) return false;
		//如果transport处于未打开状态，则返回false
		return transport.isOpen();
	}

	
	/**
	 * 当销毁对象时，会调用该方法
	 */
	@Override
	public void destroyObject(String key, TTransport transport) throws Exception {
		//销毁时，关闭连接
		if(transport!=null&&transport.isOpen()){
			transport.close();
		}
		super.destroyObject(key, transport);
	}
	
	/**
	 * 激活对象
	 * 每次借出之前都会调用此方法
	 */
	@Override
	public void activateObject(String key, TTransport transport) throws Exception {
		if(transport!=null&&!transport.isOpen()){
			transport.open();
		}
		super.activateObject(key, transport);
	}

	
	/**
	 * 当归还对象时，会调用此方法将对象挂起
	 */
	@Override
	public void passivateObject(String key, TTransport transport) throws Exception {
		// TODO Auto-generated method stub
		super.passivateObject(key, transport);
	}
	
	

}
