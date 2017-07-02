package com.wangyn.framework.thrift.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wangyn.framework.thrift.common.JsonUtils;
import com.wangyn.framework.thrift.data.RequestData;
/**
 * 一些拦击器操作
 * @author wangyn
 *
 */
public class CoreFrameInterceptor {
	
	private static Logger log = LoggerFactory.getLogger(CoreFrameInterceptor.class);
	
	
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		LogData logdata = new LogData();
		//获得被调用的方法名称：类名#方法名称
		String method = pjp.getTarget().getClass().getName()+"#"+pjp.getSignature().getName();
		logdata.setMethod(method);
		//获得入参
		Object[] requestData =  pjp.getArgs();
		logdata.setRequestData(requestData);
		if(requestData!=null||requestData.length>0){
			RequestData redata = (RequestData)requestData[0];
			//将callid放到
			CoreCommonUtils.setCallId(redata.getCallId());
		}
		//获得调用方法所花费的时间
        long time = System.currentTimeMillis();  
        Object returnVal = pjp.proceed();
        //获得方法返回结果
        logdata.setResponseData(returnVal);
        time = System.currentTimeMillis() - time;
        //记录方法耗时
        logdata.setCostTime(time+"ms");
        //记录日志
        log.info(JsonUtils.getJsonFromObj(logdata));
        //删除线程中的对象
        CoreCommonUtils.remove();
        return returnVal;  
    }  
	
	//当抛出异常时
	public void doThrowing(JoinPoint jp, Throwable ex) {
		Object requestobj = jp.getArgs();
		String method = jp.getTarget().getClass().getName()+"#"+jp.getSignature().getName();
		try {
			log.error(method+" call error,reqeustData = "+JsonUtils.getJsonFromObj(requestobj),ex);
		} catch (Exception e) {
			log.error("doThrowing log.error",e);
		}
	}
	
}
