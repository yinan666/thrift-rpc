package com.wangyn.framework.thrift.server;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.access.BootstrapException;

/**
 * TThreadedSelectorServer工作模式的thrif服务
 * @author wangyn
 *
 */
public class ThreadedSelectorThriftServer extends AbstractThriftServer {
	
	private Logger log = LoggerFactory.getLogger(ThreadedSelectorThriftServer.class);
	    
	//NIO中buffer的大小,单位为M，默认10M
	private int maxReadBuffer = 10;
	//selectorThread线程池中线程数，默认5个
	private int selectorThreads = 5;
	//处理业务线程数，默认10个
	private int workerThreads = 10;
	//selector线程池，在创建线程池时，指定的阻塞队列的队列大小
	private int acceptQueueSizePerThread = 4;
	
	/** 
     * 启动服务 
     *  1、获取TProcessor 通过反射找到服务对应的Processor 
     *  2、使用TThreadedSelectorServer创建服务 
     */
	@Override
    protected void startServerInternal() {  
    	log.info("正在启动加载thrift服务............");
        try {  
            TProcessor process = getProcessor();
            //获得传输协议
            TProtocolFactory proFactory = getTprotocolFactory();  
            TNonblockingServerTransport trans = new TNonblockingServerSocket(this.getPort());  
            TThreadedSelectorServer.Args args = new TThreadedSelectorServer.Args(trans);
            //由于传入的按M为单位的，所以这里要转为字节
            args.maxReadBufferBytes = maxReadBuffer*1024*1024;  
            args.transportFactory(new TFramedTransport.Factory());  
            args.protocolFactory(proFactory);  
            args.processor(process);
            //selector线程数，用于监听读写事件，监听到读写操作后，将这个操作交给后端的业务线程处理
            args.selectorThreads(selectorThreads);
            //selector线程池，在创建线程池时，指定的阻塞队列的队列大小
            args.acceptQueueSizePerThread(acceptQueueSizePerThread);
            //处理业务的线程数
            args.workerThreads(workerThreads);
            log.info("thrift服务参数已设置完成，port={},transportProtocol={},maxReadBuffer={},selectorThreads={},acceptQueueSizePerThread={},workerThreads={}"
            			,getPort(),getTransportProtocol(),maxReadBuffer,selectorThreads,acceptQueueSizePerThread,workerThreads);
            TServer server = new TThreadedSelectorServer(args);  
            server.serve();
        } catch (TTransportException e) {
        	log.error("startServerInternal",e);
        	throw new BootstrapException("startServerInternal", e);
        }
    }

	public int getMaxReadBuffer() {
		return maxReadBuffer;
	}

	public void setMaxReadBuffer(int maxReadBuffer) {
		this.maxReadBuffer = maxReadBuffer;
	}

	public int getSelectorThreads() {
		return selectorThreads;
	}

	public void setSelectorThreads(int selectorThreads) {
		this.selectorThreads = selectorThreads;
	}

	public int getWorkerThreads() {
		return workerThreads;
	}

	public void setWorkerThreads(int workerThreads) {
		this.workerThreads = workerThreads;
	}

	public int getAcceptQueueSizePerThread() {
		return acceptQueueSizePerThread;
	}

	public void setAcceptQueueSizePerThread(int acceptQueueSizePerThread) {
		this.acceptQueueSizePerThread = acceptQueueSizePerThread;
	}
}
