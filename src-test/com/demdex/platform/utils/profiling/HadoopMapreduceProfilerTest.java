package com.demdex.platform.utils.profiling;

import static org.junit.Assert.*;

import org.junit.Test;

public class HadoopMapreduceProfilerTest {

	@Test
	public void testProfilingTestInvocationNumber(){
		TestService service = new TestService();
		service.testMethod();
		assertEquals(1l, MethodProfiler.getMethodInvocationNumber("testMethod").longValue());
	}
	
	@Test
	public void testProfilingPrivateMethod(){
		TestService service = new TestService();
		service.testPrivateMethod();
		assertEquals(1l, MethodProfiler.getMethodInvocationNumber("privateMethod").longValue());
	}
	
	@Test
	public void testBinaryMethodProfiling(){
		TestService service = new TestService();
		service.nonAnnotatedMethod();
		assertEquals(1l, MethodProfiler.getMethodInvocationNumber("com.demdex.platform.utils.profiling").longValue());

	}
}
