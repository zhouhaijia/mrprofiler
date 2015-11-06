package com.demdex.platform.utils.profiling;

public class TestService {
	
	public TestService(){
		
	}
	
	@Profile(group="test")
	public void testMethod(){
		try {
			System.out.println("Test method - sleep 2 seconds");
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Profile(group="test")
	private void privateMethod(){
		try {
			System.out.println("Private method - sleep 2 seconds");
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testPrivateMethod(){
		privateMethod();
	}
	
	public void nonAnnotatedMethod(){
		System.out.println("This is non annotated method");
	}

}
