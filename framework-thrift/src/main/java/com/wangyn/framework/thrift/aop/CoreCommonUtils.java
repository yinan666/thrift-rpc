package com.wangyn.framework.thrift.aop;

/**
 * 调用过程的参数
 * @author wangyn
 *
 */
public class CoreCommonUtils {
	/**
	 * 用于存放调用过程的参数
	 */
	private final static ThreadLocal<CoreThreadData> tlocal = new ThreadLocal<CoreThreadData>();
	
	/**
	 * 获得callid
	 * @return
	 */
	public static String getCallId(){
		CoreThreadData td =  tlocal.get();
		if(td!=null){
			return td.getCallId();
		}else{
			return null;
		}
	}
	
	/**
	 * 设置callId
	 * @param callId
	 */
	public static void setCallId(String callId){
		CoreThreadData td =  tlocal.get();
		if(td==null){
			td = new CoreThreadData();
			tlocal.set(td);
		}
		td.setCallId(callId);
	}
	
	/**
	 * 删除线程中的对象
	 */
	public static void remove(){
		tlocal.remove();
	}

}
