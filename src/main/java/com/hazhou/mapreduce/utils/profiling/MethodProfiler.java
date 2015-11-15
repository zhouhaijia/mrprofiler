package com.hazhou.mapreduce.utils.profiling;

import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple java method profiler
 * @author hazhou
 *
 */
public class MethodProfiler {
	
	private static Logger logger = LoggerFactory.getLogger(MethodProfiler.class
			.getSimpleName());

	private static Map<String,Long> methodStartTimeMap = new HashMap<String, Long>();

	private static Map<String,Long> methodRuntimeMap = new HashMap<String, Long>();
	private static Map<String,Long> methodInvocationNumberMap = new HashMap<String, Long>();
	private static Map<String,String> methodToGroupmap = new HashMap<String,String>();
	
	
	/**
	 * intercepter for the target method, will be executed when the target method starts
	 * @param group hadoop counter group name
	 * @param method method name
	 * @param time method execution time in milliseconds
	 */
	public static void methodStart(String group, String method, long time){
		logger.debug("Target method(group:{},name:{}) starts", group, method);
		methodToGroupmap.put(method, group);
		methodStartTimeMap.put(method, time);
		Long count = methodInvocationNumberMap.get(method);
		if(count == null){
			count = 0l;
		}
		methodInvocationNumberMap.put(method, count + 1);

	}
	
	/**
	 * intercepter for the target method, will be executed at the end of the target method
	 * (before the target method returns)
	 * @param method - method name
	 * @param time - method ending time in milliseconds
	 */
	public static void methodEnd(String method, long time){
		logger.debug("Target method(group:{},name:{}) ends", method);
		if(methodStartTimeMap.containsKey(method)){
			long duration = time - methodStartTimeMap.get(method);
			long accumulatedRuntime = methodRuntimeMap.containsKey(method)?methodRuntimeMap.get(method):0;
			methodRuntimeMap.put(method, accumulatedRuntime + duration); 
		}
	}
	
	/**
	 * 
	 * @param method
	 * @return
	 */
	public static Long getMethodExecutionTime(String method){
		return methodRuntimeMap.get(method);
	}
	
	/**
	 * return method invocation number
	 * @param method name
	 * @return invocation number
	 */
	public static Long getMethodInvocationNumber(String method){
		if(methodInvocationNumberMap.containsKey(method)){
			return methodInvocationNumberMap.get(method);
		}else{
			return 0l;
		}
	}
	
	/**
	 * intercepter for hadoop map/reduce task
	 * will be called at the end of Mapper.cleanup() or Reducer.cleanup() method invocation
	 * and will add target methods metrics to hadoop counters.
	 * @param context
	 */
	@SuppressWarnings("rawtypes")
	public static void updateMapReduceTaskCounter(TaskInputOutputContext context){
		logger.info("Updating mapreduce task counter...");
		for(String method: methodToGroupmap.keySet()){
			String group = methodToGroupmap.get(method);
			long runtime = methodRuntimeMap.get(method);
			long invocationCount = methodInvocationNumberMap.get(method);
			Counter counter = context.getCounter(group, method + ".runtime");
			counter.increment(runtime);
			Counter counter2 = context.getCounter(group, method + ".invocationCount");
			counter2.increment(invocationCount);
		}
		logger.info("Mapreduce tasks counters are updated successfully");
	}
	
}
