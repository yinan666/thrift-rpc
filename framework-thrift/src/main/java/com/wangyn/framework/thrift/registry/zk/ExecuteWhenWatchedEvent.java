package com.wangyn.framework.thrift.registry.zk;

/**
 * 当监听触发时，进行的操作
 * @author wangyn
 *
 */
public interface ExecuteWhenWatchedEvent {
	public void execute();
}
