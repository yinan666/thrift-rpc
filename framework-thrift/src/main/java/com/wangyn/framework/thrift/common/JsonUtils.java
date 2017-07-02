package com.wangyn.framework.thrift.common;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.wangyn.framework.thrift.exception.ThriftFramworkException;

/**
 * json处理工具
 * @author wangyn
 *
 */
public class JsonUtils {


	private final static ObjectMapper mapper = new ObjectMapper(); 
	/**
	 * 将json转为对象
	 * @param json
	 * @param clazz
	 * @return
	 * @throws Exception
	 */
	public static Object getObjFromJson(String json,Class clazz){
		try {
			return mapper.readValue(json, clazz);
		} catch (Exception e) {
			throw new ThriftFramworkException(e);
		}
	}
	
	/**
	 * 将对象转为json串
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	public static String getJsonFromObj(Object obj){
		try {
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw new ThriftFramworkException(e);
		}
	}
	
	/**
	 * 将对象转json串
	 * @param paramName 名字
	 * @param obj 值
	 * @return
	 * @throws Exception
	 */
	public static String getJsonFromObj(String paramName, Object obj){
		Map<String, Object> m = new HashMap<String, Object>();
		m.put(paramName, obj);
		try {
			return mapper.writeValueAsString(m);
		} catch (Exception e) {
			throw new ThriftFramworkException(e);
		}
	}
	
	/**
	 * 将json转为list列表
	 * @param json
	 * @param collectionClass
	 * @param elementClasses
	 * @return
	 */
	public static Object getListFromJosn(String json,Class<?> collectionClass,Class<?> elementClasses)throws Exception{
		CollectionLikeType type = mapper.getTypeFactory().constructCollectionLikeType(collectionClass, elementClasses);
		return mapper.readValue(json, type);
	}

}
