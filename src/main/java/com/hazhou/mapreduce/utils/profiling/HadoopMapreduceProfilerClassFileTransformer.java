package com.demdex.platform.utils.profiling;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * a java class transformer that will instrument target method with the
 * profiler
 * 
 * @author hazhou
 *
 */
public class HadoopMapreduceProfilerClassFileTransformer implements ClassFileTransformer {
	
	private static Logger logger = LoggerFactory.getLogger(HadoopMapreduceProfilerClassFileTransformer.class);

	private Set<String> methodSet;
	
	/**
	 * create a new transformer instance
	 * 
	 */
	public HadoopMapreduceProfilerClassFileTransformer(){
		this(null);
	}
	
	/**
	 * create a new transformer instance with a list of target method
	 * @param methodList - a list of target method separated with ","
	 *        e.g. com.test.TestClass.method1,com.test.TestClass2.method2
	 */
	public HadoopMapreduceProfilerClassFileTransformer(String methodList){
		if(methodList == null || methodList.isEmpty()){
			methodSet = new HashSet<String>();
		}else{
			methodSet = new HashSet<String>(Arrays.asList(methodList.split(",")));
		}
	}
	
    @SuppressWarnings("rawtypes")
	public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined,
    		ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
 
        byte[] byteCode = classfileBuffer;
        try{
                ClassPool cp = ClassPool.getDefault();
                CtClass cc = cp.get(className.replace("/", "."));
                CtMethod[] methods = cc.getDeclaredMethods();
                boolean needTransfer = false;
            		for(CtMethod cm: methods){
            			String fullName = cc.getName() + "." + cm.getName();
            			logger.debug("Checking method:{}", fullName);

            			Profile anno = (Profile)cm.getAnnotation(Profile.class);
                		if(anno != null){
                			 needTransfer = true;
                			 logger.info("Instrumenting annotated method:{}",cm.getName());
                			 String group = anno.group();
                			 if(group.isEmpty()){
                				 group = "hadoop-profiler";
                			 }
                			 String name = anno.name();
                			 if(name.isEmpty()) {
                				 if(anno.fullname()){
                					 name = fullName;
                				 }else{
                     				name = cc.getName() + "." + cm.getName();
                				 }
                			 }
                			 logger.info("Group name:{}, method name:{}", group, name);
                          cm.insertBefore("{com.demdex.platform.utils.profiling.MethodProfiler.methodStart(\"" + group + "\",\"" + cm.getName() + "\",System.currentTimeMillis());}");
                          cm.insertAfter("{com.demdex.platform.utils.profiling.MethodProfiler.methodEnd(\"" + cm.getName() + "\",System.currentTimeMillis());}");
                		}else if(methodSet.contains(fullName)){
                			needTransfer = true;
                			logger.info("Instrumenting specified method:{}", fullName);
                			String group = "hadoop-profiler";
                         cm.insertBefore("{com.demdex.platform.utils.profiling.MethodProfiler.methodStart(\"" + group + "\",\"" + fullName + "\",System.currentTimeMillis());}");
                         cm.insertAfter("{com.demdex.platform.utils.profiling.MethodProfiler.methodEnd(\"" + fullName + "\",System.currentTimeMillis());}");
                		}else if(isMapReduceTaskCleanup(cm)){
                			needTransfer = true;
                			logger.info("Instrumenting map/reduce tasks cleanup method:{}",fullName);
                         cm.insertAfter("{com.demdex.platform.utils.profiling.MethodProfiler.updateMapReduceTaskCounter(context);}", false);
                		}
            		}
            	if(needTransfer){
                    byteCode = cc.toBytecode();
                    cc.detach();
            	}
        }catch(ClassNotFoundException | NotFoundException cnfe){
        		logger.info("Can't find class:" + className);
        }catch(Throwable e){
        		logger.error("Failed to transform class:" + className + ", with error:", e);
        }
        return byteCode;
    }
    
  /**
   * check if given method is Mapper.cleanup() or Reducer.cleanup()
   * note that we only instrument the cleaup method of the abstract Mapper or Reducer class
   * because the subc
   * @param method
   * @return
   */
  public boolean isMapReduceTaskCleanup(CtMethod method){
	  if(method == null){
		  return false;
	  }
	  if(!method.getName().equalsIgnoreCase("cleanup")){
		  return false;
	  }
	  try {
		CtClass[] paramTypes = method.getParameterTypes();
		if(paramTypes.length != 1){
			return false;
		}
		String classSimpleName = method.getDeclaringClass().getSimpleName();
		if(classSimpleName.equalsIgnoreCase("Mapper")
				|| classSimpleName.equalsIgnoreCase("Reducer")){
			return false;
		}
		String paramName = paramTypes[0].getName();
		return paramName.equalsIgnoreCase("org.apache.hadoop.mapreduce.Mapper$Context")
				|| paramName.equalsIgnoreCase("org.apache.hadoop.mapreduce.Reducer$Context");
	} catch (NotFoundException e) {
		return false;
	}
  }
 
}
