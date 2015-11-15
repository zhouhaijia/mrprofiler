package com.hazhou.mapreduce.utils.profiling;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Profile {
	/**
	 * hadoop counter group name
	 * @return
	 */
	String   group() default "hadoop-profiler";
	
	/**
	 * hadoop counter name
	 * @return
	 */
    String   name() default "";
    
    /**
     * flag to indicate whether a full name (package.class.method) should be used
     * @return
     */
    boolean   fullname() default false;
}
