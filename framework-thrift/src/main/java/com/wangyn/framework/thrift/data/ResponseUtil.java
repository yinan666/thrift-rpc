package com.wangyn.framework.thrift.data;

import com.wangyn.framework.thrift.common.JsonUtils;
import com.wangyn.framework.thrift.exception.CheckParamsException;
import com.wangyn.framework.thrift.exception.CheckServcieException;

/**
 * 响应工具类
 * @author wangyn
 *
 */
public class ResponseUtil {
	
	/**
	 * 成功的结果
	 * @param callId
	 * @param serviceName
	 * @param resultData
	 */
	public static ResponseData success(RequestData request,Object resultData){
		ResponseData data = new ResponseData();
		data.setCallId(request.getCallId());
		data.setServiceName(request.getServiceName());
		data.setResultData(serializeToString(resultData));
		data.setSuccess(true);
		data.setResultCode(ResultCodeEnum.SUCCESS);
		return data;
	}
	
	/**
	 * 将对象转为string字符串，如果是引用类型，则转为json
	 * @param resultData
	 * @return
	 */
	private static String serializeToString(Object resultData){
		if(resultData==null)return null;
		if(resultData instanceof String){
			return (String)resultData; 
		}else{
			return JsonUtils.getJsonFromObj(resultData);
		}
	}
	
	
	/**
	 * 默认处理
	 * @param request
	 * @param error
	 * @return
	 */
	public static ResponseData commonError(RequestData request,Exception e){
		//如果是业务异常，则返回业务异常错误代码
		if(e instanceof CheckServcieException){
			return getErrorResult(request, e.getMessage(), ResultCodeEnum.SERVICEERROR);
		//参数校验异常
		}else if(e instanceof CheckParamsException){
			return getErrorResult(request, e.getMessage(), ResultCodeEnum.PARAMSERROR);
		}else{
			return getErrorResult(request, e.getMessage(), ResultCodeEnum.SYSTEMERROR);
		}
	}
	
	
	/**
	 * 参数错误
	 * @param reqeust
	 * @param errormsg
	 * @return
	 */
	public static ResponseData paramsError(RequestData request,String errormsg){
		return getErrorResult(request,"your params is :"+request.getData()+" , errormsg:"+errormsg, ResultCodeEnum.PARAMSERROR);
	}
	
	/**
	 * 业务错误
	 * @param reqeust
	 * @param errormsg
	 * @return
	 */
	public static ResponseData serviceError(RequestData request,String errormsg){
		return getErrorResult(request, errormsg, ResultCodeEnum.SERVICEERROR);
	}
	
	
	private static ResponseData getErrorResult(RequestData reqeust,String errormsg,ResultCodeEnum errorCode){
		ResponseData data = new ResponseData();
		data.setCallId(reqeust.getCallId());
		data.setServiceName(reqeust.getServiceName());
		data.setSuccess(false);
		data.setResultMsg(errormsg);
		data.setResultCode(errorCode);
		return data;
	}
}
