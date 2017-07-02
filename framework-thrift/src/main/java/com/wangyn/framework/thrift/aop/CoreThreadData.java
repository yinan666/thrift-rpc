package com.wangyn.framework.thrift.aop;

/**
 * 调用过程的公共参数,放在Threadlocal中
 * @author wangyn
 */
public class CoreThreadData {
	private String callId;

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}
	
}
