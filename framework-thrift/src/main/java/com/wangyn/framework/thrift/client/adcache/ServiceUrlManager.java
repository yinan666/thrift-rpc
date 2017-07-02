package com.wangyn.framework.thrift.client.adcache;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.wangyn.framework.thrift.client.data.ServiceUrlData;
import com.wangyn.framework.thrift.common.JsonUtils;
/**
 * 存放客户端所依赖服务的地址列表
 * @author wangyn
 *
 */
public class ServiceUrlManager {
	
	/**
	 * 服务地址列表，key是   applcation+服务全类名+version,value是每个服务的真正要请求的hot port这些信息
	 * 后面的list，在添加到map中时，就已经进行了排序
	 */
	private static final ConcurrentHashMap<String,List<ServiceUrlData>> serviceUrlsCache = new ConcurrentHashMap<String,List<ServiceUrlData>>();
	
	/**
	 * 用于存放application 以及 application对应的host:port
	 */
	private static final ConcurrentHashMap<String,Set<String>> applcationHostsMap = new ConcurrentHashMap<String,Set<String>>();
	
	/**
	 * 获得对应服务的服务地址，默认使用的是随机加权算法，并剔除传递的url信息
	 * @param application
	 * @param interfaceClass
	 * @param version
	 * @param excluteObj
	 */
	public static ServiceUrlData getOneServiceUrlByRandomWeightExclute(String application, String serviceClass, String version,
			ServiceUrlData excluteObj) {
		List<ServiceUrlData> list = serviceUrlsCache.get(buildkey(application, serviceClass, version));
		//剔除重试仍然失败对象
		list.remove(excluteObj);
		return getByRandomWeight(list); 
	}
	
	/**
	 * 获得对应服务的服务地址，默认使用的是随机加权算法
	 * @param String application
	 * @param serviceClass
	 * @param version
	 * @return
	 */
	public static ServiceUrlData getOneServiceUrlByRandomWeight(String application,String serviceClass,String version){
		List<ServiceUrlData> list = serviceUrlsCache.get(buildkey(application, serviceClass, version));
		return getByRandomWeight(list);
	}
	
	/**
	 * 加权随机算法
	 * @param list
	 * @return
	 */
	private static ServiceUrlData getByRandomWeight(List<ServiceUrlData> list){
		if(list!=null&&list.size()>0){
			//加权随机算法
			//先判断所有的项，权重是否一样,只要有一对不一样，就算整个都不一样
			boolean isWeightSame = true;
			for(int i =0;i<list.size()-1;i++){
				ServiceUrlData sd1 = list.get(i);
				ServiceUrlData sd2 = list.get(i+1);
				if(sd1.getWeight() != sd2.getWeight()){
					isWeightSame = false;
				}
			}
			//如果每个项的权重一样，则随机选择一个
			if(isWeightSame){
				int item = new Random().nextInt(list.size());
				return list.get(item);
				
			//如果每个项目的权重不一样，则需按权重取值
			}else{
				//算出中总权重数
				int weightsum = 0;
				for(ServiceUrlData sd : list){
					weightsum +=sd.getWeight();
				}
				//选出一个随机数字
				int randomWight = new Random().nextInt(weightsum)+1;
				//顺序加weight的值，如果有大于等于随机weight的，则就去当前对象
				int weightadd = 0;
				for(ServiceUrlData sd : list){
					weightadd += sd.getWeight();
					if(weightadd>=randomWight){
						return sd;
					}
				}
				return null;
			}
		}else{
			return null;
		}
	}
	
	
	/**
	 * 将参数组装成key,便于解析使用
	 * @param application
	 * @param serviceClass
	 * @param version
	 * @return
	 */
	private static String buildkey(String application,String serviceClass,String version){
		return application+":"+serviceClass+":"+version;
	}
	
	/**
	 * 重新设置service对应的服务rul
	 * @param application
	 * @param serviceClass
	 * @param version
	 * @param servicePathList
	 * @throws Exception
	 */
	public static void resetServiceUrlsCache(String application,String serviceClass,String version,List<String> servicePathList)throws Exception{
		String key = buildkey(application, serviceClass, version);
		resetServiceUrlsCache(key,servicePathList,application);
	}
	
	/**
	 * 重新设置service对应的服务rul
	 * @param key   applcation:服务全类名:version
	 * @param servicePath
	 */
	private static void resetServiceUrlsCache(String key,List<String> servicePathList,String application)throws Exception{
		//如果path列表为空，则清除cache中的值
		if(servicePathList==null||servicePathList.size()<1){
			serviceUrlsCache.remove(key);
		//如果不为空，则重新设置服务地址列表
		}else{
			//用来统计application和其对应的节点信息
			Set<String> apset = applcationHostsMap.get(application);
			if(apset==null){
				apset = new HashSet<String>();
				applcationHostsMap.put(application, apset);
			}
			List<ServiceUrlData> datalist = new ArrayList<ServiceUrlData>();
			for(String json:servicePathList){
				ServiceUrlData urldata = (ServiceUrlData) JsonUtils.getObjFromJson(json, ServiceUrlData.class);
				datalist.add(urldata);
				//用来统计application和其对应的节点信息
				apset.add(urldata.getIp()+":"+urldata.getPort());
			}
			//由于默认用的加权随机软负载算法，所以需要排个序
			Collections.sort(datalist);
			serviceUrlsCache.put(key, datalist);
		}
	}
	
	/**
	 * 获得该应用总共部署了多少个节点
	 * @param application
	 * @return
	 */
	public static int getNodesCountOfApplication(String application){
		Set<String> st = applcationHostsMap.get(application);
		if(st==null){
			return 0;
		}else{
			return st.size();
		}
	}
	
	
}
