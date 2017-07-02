package com.wangyn.framework.thrift.config.spring.schema.def;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.wangyn.framework.thrift.common.FrameCommonUtils;
import com.wangyn.framework.thrift.init.ThriftFrameInit;

/**
 * <thrift:application>在所有项目中都会配置，并且只会被配置一次，
 * 因此可以让ThriftApplicationDef
 * 
 * 
 * 指定当前应用的名称
 * 
 * @author wangyn
 *
 */
public class ThriftApplicationDef implements ApplicationContextAware,ApplicationListener<ContextRefreshedEvent>{
	
	Logger log = LoggerFactory.getLogger(ThriftApplicationDef.class);
	
	/**
	 * 用于判断<thrift:application>是已经配置，并且只能设置一次
	 */
	private static boolean hasApplicationSet = false;
	
	public ThriftApplicationDef() {
	}

	/**
	 * 当前应用的名称
	 */
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		FrameCommonUtils.setApplicationContext(applicationContext);
	}

	public static boolean isHasApplicationSet() {
		return hasApplicationSet;
	}
	
	/**
	 *  设置<thrift:application>是已经配置
	 * @param hasApplicationSet
	 */
	public static void setHasApplicationSet(boolean hasApplicationSet) {
		ThriftApplicationDef.hasApplicationSet = hasApplicationSet;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if(event.getApplicationContext().getParent() == null){
			log.info("thrift frame init ...............");
			ThriftFrameInit.init();
	      }
	}

	
	
//	@Override
//	public void afterPropertiesSet() throws Exception {
//		log.info("thrift frame init ...............");
//		ThriftFrameInit.init();
//	}


}
